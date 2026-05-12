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
public class AutoSaveManager {
    private static final String AUTOSAVE_DIR = ".myapp/autosave";
    private static final String AUTOSAVE_FILE = "autosave.pdt";
    
    private final ImageCanvas canvas;
    private final AppState appState;
    private final ScheduledExecutorService scheduler;
    private final Gson gson;
    private final File autosaveDir;

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
        
        appState.addPropertyChangeListener("autoSaveInterval", evt -> restartTimer());
        
        startTimer();
    }

    private java.util.concurrent.ScheduledFuture<?> currentTask;

    private void startTimer() {
        int interval = appState.getAutoSaveInterval();
        currentTask = scheduler.scheduleAtFixedRate(this::performAutoSave, interval, interval, TimeUnit.MINUTES);
    }

    public synchronized void restartTimer() {
        if (currentTask != null) {
            currentTask.cancel(false);
        }
        startTimer();
        System.out.println("AutoSave timer restarted with interval: " + appState.getAutoSaveInterval() + " min");
    }

    /**
     * Captures current state and writes to the .pdt file.
     */
    public synchronized void performAutoSave() {
        if (!appState.isAutoSaveEnabled() || canvas.getBackgroundImage() == null) {
            return;
        }

        try {
            AutoSaveData data = new AutoSaveData();
            data.imagePath = appState.getFilePath();
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

    public void cleanup() {
        File f = getAutoSaveFile();
        if (f != null) {
            f.delete();
        }
    }

    /**
     * Entry point for restoring a session.
     * Orchestrates image loading and history reconstruction.
     */
    public void restoreSession(File file, RecoveryUI ui) {
        try {
            AutoSaveData data = loadAutoSave(file);
            if (data == null || data.imagePath == null) return;
            
            File imageFile = new File(data.imagePath);
            if (!imageFile.exists()) {
                ui.onRecoveryError("Original image not found: " + data.imagePath);
                return;
            }

            ui.onRecoveryStarted();
            ui.updateProgress(10, "Loading base image...");

            new workers.ImageLoadWorker(imageFile, image -> {
                try {
                    ui.updateProgress(30, "Restoring application state...");
                    AppState appState = ui.getAppState();
                    appState.setFilePath(imageFile.getAbsolutePath());
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
                    String finalTitle = imageFile.getAbsolutePath() + " - " + restoredImage.getWidth() + "x" + restoredImage.getHeight() + " (Restored)";
                    ui.onRecoveryFinished(finalTitle);
                } catch (Exception ex) {
                    ui.onRecoveryError("Error during reconstruction: " + ex.getMessage());
                }
            }, ex -> {
                ui.onRecoveryError("Failed to load image for recovery: " + ex.getMessage());
            }).execute();

        } catch (IOException e) {
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
                    cmd = new StickCommand(appState.getCanvasState(), ui.getCanvas(), d.point);
                    break;
                case ADD_GRID:
                    cmd = new GridCommand(appState.getCanvasState(), ui.getCanvas(), d.point);
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
