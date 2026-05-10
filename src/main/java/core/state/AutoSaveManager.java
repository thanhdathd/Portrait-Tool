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
            data.undoStack = captureStack(appState.getHistoryManager().getUndoStack());
            data.redoStack = captureStack(appState.getHistoryManager().getRedoStack());

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

    private List<CommandData> captureStack(Deque<Command> stack) {
        List<CommandData> list = new ArrayList<>();
        for (Command cmd : stack) {
            if (cmd instanceof StickCommand) {
                // We need to use reflection or add a getter to StickCommand to get the point
                // For now, I'll assume we can access it or I'll add a getter.
                try {
                    java.lang.reflect.Field field = StickCommand.class.getDeclaredField("point");
                    field.setAccessible(true);
                    userpackage.SPoint p = (userpackage.SPoint) field.get(cmd);
                    list.add(CommandData.createPointCmd(p));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cmd instanceof GridCommand) {
                try {
                    java.lang.reflect.Field field = GridCommand.class.getDeclaredField("gridPoint");
                    field.setAccessible(true);
                    userpackage.SPoint p = (userpackage.SPoint) field.get(cmd);
                    list.add(CommandData.createGridCmd(p));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
