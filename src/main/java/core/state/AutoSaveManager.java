package core.state;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
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
     * Deletes the autosave file and shadow copy, resetting session state.
     */
    public void cleanupSession() {
        // Delete .pdt file
        File f = getAutoSaveFile();
        if (f != null && f.exists()) {
            f.delete();
        }

        // Delete shadow copy
        String shadowPath = appState.getShadowPath();
        if (shadowPath != null) {
            File shadowFile = new File(shadowPath);
            if (shadowFile.exists()) {
                shadowFile.delete();
            }
            appState.setShadowPath(null);
            appState.setOriginalHash(null);
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
                    ui.updateProgress(40, "Reconstructing undo stack...");
                    Deque<Command> undo = reconstructStack(ui, data.undoStack, image, true);
                    
                    ui.updateProgress(70, "Reconstructing redo stack...");
                    java.awt.image.BufferedImage lastUndoImage = ui.getCanvas().getBackgroundImage();
                    Deque<Command> redo = reconstructStack(ui, data.redoStack, lastUndoImage, false);
                    
                    // Convert metadata lists back to Deques for HistoryManager
                    Deque<CommandData> persistentUndo = new java.util.ArrayDeque<>(data.undoStack);
                    Deque<CommandData> persistentRedo = new java.util.ArrayDeque<>(data.redoStack);
                    
                    appState.getHistoryManager().reconstructStacks(undo, redo, persistentUndo, persistentRedo);
                    appState.getHistoryManager().markAsSaved();
                    
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
                case ADD_GRID:
                    cmd = new GridCommand(appState.getCanvasState(), ui.getCanvas(), d.point.copy());
                    break;
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

    /**
     * Custom Adapter to save java.awt.Color as Hex string.
     */
    private static class ColorTypeAdapter extends TypeAdapter<Color> {
        @Override
        public void write(JsonWriter out, Color value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            String hex = String.format("#%02X%02X%02X", value.getRed(), value.getGreen(), value.getBlue());
            out.value(hex);
        }

        @Override
        public Color read(JsonReader in) throws IOException {
            String hex = in.nextString();
            return Color.decode(hex);
        }
    }
}
