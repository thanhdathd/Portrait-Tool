package core.state;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import core.history.Command;
import core.history.GridCommand;
import core.history.StickCommand;
import ui.canvas.ImageCanvas;
import java.util.ArrayList;
import java.util.List;
import java.util.Deque;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages periodic auto-saving of the application state.
 */
public class AutoSaveManager implements core.history.HistoryManager.HistoryListener {
    private static final String AUTOSAVE_DIR = ".myapp/autosave";
    private static final String AUTOSAVE_FILE = "autosave.pdt";
    private static final String SHADOW_DIR = "shadow_cache";
    private static final int DEBOUNCE_TIME = 10; // sec
    private static final int PERIODIC_SAVE_TIME = 1; // minute

    private final ImageCanvas canvas;
    private final AppState appState;
    private final ScheduledExecutorService scheduler;
    private final Gson gson;
    private final File autosaveDir;
    private final File shadowCacheDir;
    private final java.util.concurrent.ExecutorService copyExecutor;
    private SaveStatusListener statusListener;

    public interface SaveStatusListener {
        void onSaveStarted();
        void onSaveFinished();
        void onDirtyStateChanged(boolean isDirty);
    }

    public void setSaveStatusListener(SaveStatusListener listener) {
        this.statusListener = listener;
    }

    private boolean isDirty = false;
    private boolean isRestoring = false;
    private java.util.concurrent.ScheduledFuture<?> debounceTask;
    private java.util.concurrent.ScheduledFuture<?> fallbackTask;

    public AutoSaveManager(ImageCanvas canvas) {
        this.canvas = canvas;
        this.appState = canvas.getAppState();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AutoSaveThread");
            t.setDaemon(true);
            return t;
        });
        
        // Setup Gson with Color adapter
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Color.class, new ColorTypeAdapter())
                .setPrettyPrinting()
                .create();

        String userHome = System.getProperty("user.home");
        this.autosaveDir = new File(userHome, AUTOSAVE_DIR);
        if (!autosaveDir.exists()) {
            autosaveDir.mkdirs();
        }

        this.shadowCacheDir = new File(autosaveDir, SHADOW_DIR);
        if (!shadowCacheDir.exists()) {
            shadowCacheDir.mkdirs();
        }

        this.copyExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ShadowCopyThread");
            t.setDaemon(true);
            return t;
        });
        
        // Listen for AppState changes
        tools.PropertyChangeListener changeListener = evt -> notifyChange();
        appState.addPropertyChangeListener("brushColor", changeListener);
        appState.addPropertyChangeListener("gridSize", changeListener);
        appState.addPropertyChangeListener("gridInCm", changeListener);
        appState.addPropertyChangeListener("scale", changeListener);

        // Listen for History changes
        appState.getHistoryManager().addListener(this);

        // Sweep orphaned shadow files at startup
        sweepOrphanedShadowFiles();
    }

    @Override
    public void onHistoryChanged(boolean canUndo, boolean canRedo, boolean isModified) {
        notifyChange();
    }

    public synchronized void notifyChange() {
        if (isRestoring || !appState.isAutoSaveEnabled()) return;
        if (!isDirty) {
            isDirty = true;
            if (statusListener != null) {
                javax.swing.SwingUtilities.invokeLater(() -> statusListener.onDirtyStateChanged(true));
            }
        }
        startTimers();
    }

    private synchronized void startTimers() {
        // Debounce: reset if already running
        if (debounceTask != null) debounceTask.cancel(false);
        debounceTask = scheduler.schedule(this::performAutoSave, DEBOUNCE_TIME, TimeUnit.SECONDS);

        // Fallback: start only if not already running
        if (fallbackTask == null) {
            fallbackTask = scheduler.schedule(this::performAutoSave, PERIODIC_SAVE_TIME, TimeUnit.MINUTES);
        }
    }

    public synchronized void onManualSave() {
        if (isDirty) {
            isDirty = false;
            if (statusListener != null) {
                javax.swing.SwingUtilities.invokeLater(() -> statusListener.onDirtyStateChanged(false));
            }
        }
        cancelTimers();
    }

    private synchronized void cancelTimers() {
        if (debounceTask != null) {
            debounceTask.cancel(false);
            debounceTask = null;
        }
        if (fallbackTask != null) {
            fallbackTask.cancel(false);
            fallbackTask = null;
        }
    }

    /**
     * Captures current state and writes to the .pdt file.
     */
    public synchronized void performAutoSave() {
        if (!isDirty || !appState.isAutoSaveEnabled() || canvas.getBackgroundImage() == null) {
            return;
        }

        // Cancel tasks as we are saving now
        cancelTimers();
        boolean wasDirty = isDirty;
        isDirty = false;
        if (wasDirty && statusListener != null) {
            javax.swing.SwingUtilities.invokeLater(() -> statusListener.onDirtyStateChanged(false));
        }

        if (statusListener != null) {
            javax.swing.SwingUtilities.invokeLater(() -> statusListener.onSaveStarted());
        }

        try {
            AutoSaveData data = new AutoSaveData();
            data.imagePath = appState.getFilePath();
            data.shadowPath = appState.getShadowPath();
            data.originalHash = appState.getOriginalHash();
            data.scale = appState.getScale();
            data.gridSize = appState.getGridSize();
            data.gridInCm = appState.isGridInCm();
            data.brushColor = appState.getBrushColor();
            
            // Capture History
            // Capture Persistent Metadata History
            data.undoStack = new ArrayList<>(appState.getHistoryManager().getPersistentUndoStack());
            data.redoStack = new ArrayList<>(appState.getHistoryManager().getPersistentRedoStack());

            // Capture Project Initial State (for .pdw files)
            data.initialPoints = appState.getInitialPoints();
            data.initialGrids = appState.getInitialGrids();
            data.initialLines = appState.getInitialLines();

            File finalFile = new File(autosaveDir, AUTOSAVE_FILE);
            File tempFile = new File(autosaveDir, AUTOSAVE_FILE + ".tmp");

            // Atomic write pattern
            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(data, writer);
            }
            
            Files.move(tempFile.toPath(), finalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Auto-saved to: " + finalFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Failed to perform auto-save: " + e.getMessage());
        } finally {
            if (statusListener != null) {
                javax.swing.SwingUtilities.invokeLater(() -> statusListener.onSaveFinished());
            }
        }
    }

    public void stop() {
        scheduler.shutdown();
    }

    public File getAutoSaveFile() {
        File f = new File(autosaveDir, AUTOSAVE_FILE);
        return f.exists() ? f : null;
    }

    public AutoSaveData loadAutoSave(File file) throws IOException {
        try (JsonReader reader = new JsonReader(new java.io.FileReader(file))) {
            return gson.fromJson(reader, AutoSaveData.class);
        }
    }

    /**
     * Sweeps and deletes any orphaned shadow files in the shadow cache directory that
     * do not belong to the active autosave session.
     */
    private void sweepOrphanedShadowFiles() {
        copyExecutor.execute(() -> {
            try {
                String activeShadowPath = null;
                File autosaveFile = getAutoSaveFile();
                if (autosaveFile != null && autosaveFile.exists()) {
                    try {
                        AutoSaveData data = loadAutoSave(autosaveFile);
                        if (data != null) {
                            activeShadowPath = data.shadowPath;
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to read active autosave for sweeping: " + e.getMessage());
                    }
                }

                File[] files = shadowCacheDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            if (activeShadowPath == null || !file.getAbsolutePath().equalsIgnoreCase(activeShadowPath)) {
                                if (file.delete()) {
                                    System.out.println("Swept orphaned shadow file: " + file.getAbsolutePath());
                                } else {
                                    System.err.println("Failed to sweep orphaned shadow file: " + file.getAbsolutePath());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during shadow cache sweeping: " + e.getMessage());
            }
        });
    }

    /**
     * Deletes the autosave file and shadow copy, resetting session state.
     */
    public void cleanupSession() {
        File f = getAutoSaveFile();
        String shadowToDelete = null;

        // 1. Read shadow path from the autosave file before deleting it
        if (f != null && f.exists()) {
            try {
                AutoSaveData data = loadAutoSave(f);
                if (data != null) {
                    shadowToDelete = data.shadowPath;
                }
            } catch (Exception e) {
                System.err.println("Could not read autosave file for shadow path cleanup: " + e.getMessage());
            }
        }

        // 2. Fallback or prioritize AppState's record
        if (appState.getShadowPath() != null) {
            shadowToDelete = appState.getShadowPath();
        }

        // 3. Delete shadow copy
        if (shadowToDelete != null) {
            File shadowFile = new File(shadowToDelete);
            if (shadowFile.exists()) {
                if (shadowFile.delete()) {
                    System.out.println("Cleaned up shadow copy: " + shadowToDelete);
                } else {
                    System.err.println("Failed to delete shadow copy: " + shadowToDelete);
                }
            }
            appState.setShadowPath(null);
            appState.setOriginalHash(null);
        }

        // 4. Delete .pdt file
        if (f != null && f.exists()) {
            if (f.delete()) {
                System.out.println("Cleaned up autosave file: " + f.getAbsolutePath());
            }
        }

        onManualSave();
    }

    public void cleanup() {
        cleanupSession();
    }

    /**
     * Initializes a shadow session by creating a background backup of the original image.
     */
    public void initShadowSession(File originalFile) {
        if (originalFile == null || !originalFile.exists()) return;

        // Clean up previous active autosave session and its shadow copy
        cleanupSession();

        copyExecutor.execute(() -> {
            try {
                // 1. Calculate Hash
                String hash = utils.HashUtils.calculateSHA256(originalFile);
                appState.setOriginalHash(hash);

                // 2. Prepare Shadow Copy
                String shadowName = Integer.toHexString(originalFile.getAbsolutePath().hashCode()) + "_" + originalFile.getName();
                File shadowFile = new File(shadowCacheDir, shadowName);

                // 3. Copy if not already there or different
                Files.copy(originalFile.toPath(), shadowFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                appState.setShadowPath(shadowFile.getAbsolutePath());
                System.out.println("Shadow copy created at: " + shadowFile.getAbsolutePath());
                
            } catch (Exception e) {
                System.err.println("Failed to initialize shadow session: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Entry point for restoring a session.
     * Orchestrates image loading and history reconstruction.
     */
    public void restoreSession(File file, RecoveryUI ui) {
        try {
            isRestoring = true;
            AutoSaveData data = loadAutoSave(file);
            if (data == null || data.imagePath == null) {
                isRestoring = false;
                return;
            }
            
            // 1. Determine which file to load (Shadow > Original)
            File fileToLoad = null;

            if (data.shadowPath != null) {
                File shadowFile = new File(data.shadowPath);
                if (shadowFile.exists() && shadowFile.canRead()) {
                    fileToLoad = shadowFile;
                }
            }

            if (fileToLoad == null) {
                // Fallback to original
                File originalFile = new File(data.imagePath);
                if (originalFile.exists() && originalFile.canRead()) {
                    // INTEGRITY CHECK
                    if (data.originalHash != null) {
                        try {
                            String currentHash = utils.HashUtils.calculateSHA256(originalFile);
                            if (!data.originalHash.equals(currentHash)) {
                                ui.onRecoveryError("Recovery failed: Original image has been modified externally.");
                                isRestoring = false;
                                return;
                            }
                        } catch (Exception hashEx) {
                            ui.onRecoveryError("Integrity check failed: " + hashEx.getMessage());
                            isRestoring = false;
                            return;
                        }
                    }
                    fileToLoad = originalFile;
                }
            }

            if (fileToLoad == null) {
                ui.onRecoveryError("Base image not found. Shadow: " + data.shadowPath + ", Original: " + data.imagePath);
                isRestoring = false;
                return;
            }

            final File finalFileToLoad = fileToLoad;
            final String originalPath = data.imagePath;

            ui.onRecoveryStarted();
            ui.updateProgress(10, "Loading base image...");

            new workers.ImageLoadWorker(finalFileToLoad, image -> {
                try {
                    ui.updateProgress(30, "Restoring application state...");
                    AppState appState = ui.getAppState();
                    appState.setFilePath(originalPath);
                    appState.setShadowPath(data.shadowPath);
                    appState.setOriginalHash(data.originalHash);
                    appState.setScale(data.scale);
                    appState.setGridSize(data.gridSize);
                    appState.setGridInCm(data.gridInCm);
                    if (data.brushColor != null) {
                        appState.setBrushColor(data.brushColor);
                    }

                    // Replay
                    appState.getCanvasState().clearAll();

                    // Restore Initial Project State (Points, Grids, and Lines) if it was a project file
                    if (data.initialPoints != null) {
                        for (userpackage.SPoint p : data.initialPoints) {
                            appState.getCanvasState().addStickyPoint(p);
                        }
                    }
                    if (data.initialGrids != null) {
                        for (userpackage.SPoint g : data.initialGrids) {
                            appState.getCanvasState().addGrid(g);
                        }
                    }
                    if (data.initialLines != null) {
                        for (userpackage.SLine l : data.initialLines) {
                            appState.getCanvasState().getLines().add(l);
                        }
                    }
                    // Keep them in AppState so future auto-saves still have them
                    appState.setInitialPoints(data.initialPoints);
                    appState.setInitialGrids(data.initialGrids);
                    appState.setInitialLines(data.initialLines);

                    ui.updateProgress(40, "Reconstructing undo stack...");
                    Deque<Command> undo = reconstructStack(ui, data.undoStack, image, true);
                    
                    ui.updateProgress(70, "Reconstructing redo stack...");
                    java.awt.image.BufferedImage lastUndoImage = ui.getCanvas().getBackgroundImage();
                    Deque<Command> redo = reconstructStack(ui, data.redoStack, lastUndoImage, false);
                    
                    // Convert metadata lists back to Deques for HistoryManager
                    Deque<CommandData> persistentUndo = new java.util.ArrayDeque<>(data.undoStack);
                    Deque<CommandData> persistentRedo = new java.util.ArrayDeque<>(data.redoStack);
                    
                    appState.getHistoryManager().reconstructStacks(undo, redo, persistentUndo, persistentRedo);
                    appState.getHistoryManager().markAsClean();
                    
                    java.awt.image.BufferedImage restoredImage = ui.getCanvas().getBackgroundImage();
                    String finalTitle = originalPath + " - " + restoredImage.getWidth() + "x" + restoredImage.getHeight() + " (Restored)";
                    ui.onRecoveryFinished(finalTitle);
                } catch (Exception ex) {
                    ui.onRecoveryError("Error during reconstruction: " + ex.getMessage());
                } finally {
                    isRestoring = false;
                    onManualSave(); // Reset dirty state and timers
                }
            }, ex -> {
                isRestoring = false;
                ui.onRecoveryError("Failed to load image for recovery: " + ex.getMessage());
            }).execute();

        } catch (IOException e) {
            isRestoring = false;
            ui.onRecoveryError("Failed to load auto-save data: " + e.getMessage());
        }
    }

    /**
     * Reconstructs a history stack from CommandData.
     * Migrated from MainFrame.
     */
    public Deque<Command> reconstructStack(RecoveryUI ui, List<CommandData> list, java.awt.image.BufferedImage startImage, boolean execute) {
        Deque<Command> stack = new java.util.ArrayDeque<>();
        if (list == null) return stack;
        
        java.awt.image.BufferedImage currentImage = startImage;
        AppState appState = ui.getAppState();
        ui.getCanvas(); // Ensure canvas is accessible
        
        for (CommandData d : list) {
            Command cmd = null;
            java.awt.image.BufferedImage nextImage = currentImage;
            
            switch (d.type) {
                case ADD_POINT:
                    cmd = new StickCommand(appState.getCanvasState(), ui.getCanvas(), d.point.copy());
                    break;
                case DELETE_POINT:
                case EDIT_POINT: {
                    // Find existing point reference matching old state
                    userpackage.SPoint existingPoint = null;
                    for (userpackage.SPoint p : appState.getCanvasState().getStickyPoints()) {
                        if (p.X == d.point.X && p.Y == d.point.Y && p.id == d.point.id) {
                            existingPoint = p;
                            break;
                        }
                    }
                    if (existingPoint != null) {
                        if (d.type == CommandData.CommandType.DELETE_POINT) {
                            cmd = new StickCommand(appState.getCanvasState(), ui.getCanvas(), existingPoint,
                                    StickCommand.Action.DELETE, d.point.copy(), null);
                        } else {
                            cmd = new StickCommand(appState.getCanvasState(), ui.getCanvas(), existingPoint,
                                    StickCommand.Action.EDIT, d.point.copy(),
                                    d.newPoint != null ? d.newPoint.copy() : null);
                        }
                    }
                    break;
                }
                case ADD_GRID:
                    cmd = new GridCommand(appState.getCanvasState(), ui.getCanvas(), d.point.copy(), core.history.GridCommand.Action.ADD, null, null);
                    break;
                case DELETE_GRID:
                case EDIT_GRID:
                    // Find the existing grid reference from CanvasState that matches old properties
                    userpackage.SPoint existingGrid = null;
                    for (userpackage.SPoint g : appState.getCanvasState().getGrids()) {
                        if (g.X == d.point.X && g.Y == d.point.Y && g.id == d.point.id && g.c.equals(d.point.c)) {
                            existingGrid = g;
                            break;
                        }
                    }
                    if (existingGrid != null) {
                        if (d.type == CommandData.CommandType.DELETE_GRID) {
                            cmd = new GridCommand(appState.getCanvasState(), ui.getCanvas(), existingGrid, core.history.GridCommand.Action.DELETE, d.point.copy(), null);
                        } else {
                            cmd = new GridCommand(appState.getCanvasState(), ui.getCanvas(), existingGrid, core.history.GridCommand.Action.EDIT, d.point.copy(), d.newPoint != null ? d.newPoint.copy() : null);
                        }
                    }
                    break;
                case ADD_LINE:
                    cmd = new core.history.LineCommand(appState.getCanvasState(), ui.getCanvas(), d.line.copy(), core.history.LineCommand.Action.ADD, null, null);
                    break;
                case DELETE_LINE:
                case EDIT_LINE: {
                    userpackage.SLine existingLine = null;
                    for (userpackage.SLine l : appState.getCanvasState().getLines()) {
                        if (l.id == d.line.id) {
                            existingLine = l;
                            break;
                        }
                    }
                    if (existingLine != null) {
                        if (d.type == CommandData.CommandType.DELETE_LINE) {
                            cmd = new core.history.LineCommand(appState.getCanvasState(), ui.getCanvas(), existingLine, core.history.LineCommand.Action.DELETE, d.line.copy(), null);
                        } else {
                            cmd = new core.history.LineCommand(appState.getCanvasState(), ui.getCanvas(), existingLine, core.history.LineCommand.Action.EDIT, d.line.copy(), d.newLine != null ? d.newLine.copy() : null);
                        }
                    }
                    break;
                }
                case BATCH_DELETE_LINES: {
                    List<core.history.BatchLineCommand.LineStatePair> pairs = new ArrayList<>();
                    if (d.lines != null) {
                        for (userpackage.SLine snap : d.lines) {
                            userpackage.SLine existing = null;
                            for (userpackage.SLine l : appState.getCanvasState().getLines()) {
                                if (l.id == snap.id) {
                                    existing = l;
                                    break;
                                }
                            }
                            if (existing != null) {
                                pairs.add(new core.history.BatchLineCommand.LineStatePair(existing, snap.copy(), null));
                            }
                        }
                    }
                    if (!pairs.isEmpty()) {
                        cmd = new core.history.BatchLineCommand(appState.getCanvasState(), ui.getCanvas(), core.history.BatchLineCommand.Action.DELETE, pairs);
                    }
                    break;
                }
                case BATCH_EDIT_LINES: {
                    List<core.history.BatchLineCommand.LineStatePair> pairs = new ArrayList<>();
                    if (d.lines != null && d.newLines != null && d.lines.size() == d.newLines.size()) {
                        for (int i = 0; i < d.lines.size(); i++) {
                            userpackage.SLine oldSnap = d.lines.get(i);
                            userpackage.SLine newSnap = d.newLines.get(i);
                            userpackage.SLine existing = null;
                            for (userpackage.SLine l : appState.getCanvasState().getLines()) {
                                if (l.id == oldSnap.id) {
                                    existing = l;
                                    break;
                                }
                            }
                            if (existing != null) {
                                pairs.add(new core.history.BatchLineCommand.LineStatePair(existing, oldSnap.copy(), newSnap.copy()));
                            }
                        }
                    }
                    if (!pairs.isEmpty()) {
                        cmd = new core.history.BatchLineCommand(appState.getCanvasState(), ui.getCanvas(), core.history.BatchLineCommand.Action.EDIT, pairs);
                    }
                    break;
                }
                case CROP:
                    // Recreate cropped image
                    nextImage = new java.awt.image.BufferedImage(d.cropW, d.cropH, currentImage.getType() == 0 ? java.awt.image.BufferedImage.TYPE_INT_ARGB : currentImage.getType());
                    java.awt.Graphics2D g2 = nextImage.createGraphics();
                    g2.setColor(java.awt.Color.BLACK);
                    g2.fillRect(0, 0, d.cropW, d.cropH);
                    g2.drawImage(currentImage, -d.cropX, -d.cropY, null);
                    g2.dispose();
                    
                    cmd = new core.history.CropCommand(ui.getCanvas(), appState.getCanvasState(), currentImage, 
                            new java.awt.Rectangle(d.cropX, d.cropY, d.cropW, d.cropH), 
                            d.zomAtCrop != null ? d.zomAtCrop : 1.0f, 
                            d.oldVisualX != null ? d.oldVisualX : 0, 
                            d.oldVisualY != null ? d.oldVisualY : 0);
                    break;
                case RESIZE:
                    java.awt.RenderingHints.Key hintKey = java.awt.RenderingHints.KEY_INTERPOLATION;
                    Object hintObj = switch (d.resizeProps.hint) {
                        case 0 -> java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                        case 1 -> java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                        default -> java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                    };
                    nextImage = core.image.ImageResizer.resize(currentImage, d.resizeProps.width, d.resizeProps.height, hintObj);
                    cmd = new core.history.ResizeCommand(ui.getCanvas(), appState.getCanvasState(), currentImage, nextImage, d.resizeProps);
                    break;
                case ROTATE:
                case FLIP:
                    nextImage = core.image.ImageTransformUtils.transform(currentImage, d.transformType);
                    cmd = new core.history.TransformCommand(ui.getCanvas(), appState.getCanvasState(), currentImage, nextImage, d.transformType);
                    break;
                case FILTER:
                    nextImage = core.image.ImageProcessor.applyFilter(currentImage, d.filterProps);
                    cmd = new core.history.FilterCommand(ui.getCanvas(), currentImage, nextImage, d.filterProps);
                    break;
                case BATCH_DELETE_POINTS: {
                    // Tìm live refs trong CanvasState khớp với từng point snapshot
                    java.util.List<userpackage.SPoint> targets = new java.util.ArrayList<>();
                    if (d.points != null) {
                        for (userpackage.SPoint snap : d.points) {
                            for (userpackage.SPoint live : appState.getCanvasState().getStickyPoints()) {
                                if (live.X == snap.X && live.Y == snap.Y && live.id == snap.id) {
                                    targets.add(live);
                                    break;
                                }
                            }
                        }
                    }
                    if (!targets.isEmpty()) {
                        cmd = new core.history.BatchStickCommand(
                                appState.getCanvasState(), ui.getCanvas(), targets);
                    }
                    break;
                }
                case BATCH_EDIT_POINTS_COLOR: {
                    // Tìm live refs và tạo BatchStickCommand đổi màu
                    java.util.List<userpackage.SPoint> targets = new java.util.ArrayList<>();
                    if (d.points != null) {
                        for (userpackage.SPoint snap : d.points) {
                            for (userpackage.SPoint live : appState.getCanvasState().getStickyPoints()) {
                                if (live.X == snap.X && live.Y == snap.Y && live.id == snap.id) {
                                    targets.add(live);
                                    break;
                                }
                            }
                        }
                    }
                    if (!targets.isEmpty() && d.batchColor != null) {
                        cmd = new core.history.BatchStickCommand(
                                appState.getCanvasState(), ui.getCanvas(), targets, d.batchColor);
                    }
                    break;
                }
            }
            
            if (cmd != null) {
                if (execute) {
                    cmd.execute();
                }
                stack.addLast(cmd);
                currentImage = nextImage;
            }
        }
        
        if (execute) {
            ui.setCanvasImage(currentImage);
        }
        
        return stack;
    }

    private List<CommandData> captureStack(Deque<Command> stack) {
        List<CommandData> list = new ArrayList<>();
        for (Command cmd : stack) {
            CommandData data = cmd.capture();
            if (data != null) {
                list.add(data);
            }
        }
        return list;
    }
}
