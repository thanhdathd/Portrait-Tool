package core.state;

import tools.PropertyChangeListener;
import user.Enum.Direction;
import user.Enum.MouseMode;
import core.history.HistoryManager;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppState {

    public String getLastOpenedDir() {
        return lastOpenedDir;
    }

    public void setLastOpenedDir(String lastOpenedDir) {
        this.lastOpenedDir = lastOpenedDir;
    }



    public enum EditState {
        SAVED,
        PARTLY_SAVED,
        MODIFIED,
        NOT_SAVED
    }

    private float currentZoom = 1.0f;
    private float scale = 1.0f;
    private MouseMode mouseMode = MouseMode.DRAG;
    private boolean cmUnit = false;
    private String languageCode = "en";
    private boolean round = false;
    private int windowX, windowY, windowWidth, windowHeight;
    private String filePath = "Untitled-00.jpg";
    private String shadowPath = null;
    private String originalHash = null;
    private String lastOpenedDir = "~/";
    private EditState editState = EditState.SAVED;
    private Color brushColor = Color.CYAN;
    private Direction labelDirection = Direction.EAST;
    private boolean floating = false;
    private boolean showCropHelp = true;
    private float gridSize = 40.0f; // Default from legacy code (pixels)
    private boolean gridInCm = false;
    private final HistoryManager historyManager;
    private final CanvasState canvasState;
    private final Map<String, List<PropertyChangeListener>> watchedKeys = new HashMap<>();
    private Rectangle zoomWindowBounds = null;
    private boolean customLabelMode = false;
    private static final Color[] XOR_COLORS = {
        Color.WHITE, Color.BLACK,
        Color.YELLOW, Color.CYAN, Color.MAGENTA,
        Color.GREEN, Color.RED
    };
    private static final String[] XOR_COLOR_NAMES = {
        "WHITE", "BLACK",
        "YELLOW", "CYAN", "MAGENTA",
        "GREEN", "RED"
    };
    private int xorColorIndex = 0;
    private int customGap = 20;   // Giới hạn 8 - 50
    private int customAngle = 40; // Độ (0 - 359), tăng theo chiều CCW (ngược chiều kim đồng hồ)
    private int checkerSize = 40;
    private boolean showPointMap = false;
    private boolean autoSaveEnabled = true;
    private int autoSaveInterval = 1; // Minutes
    private core.history.HistoryMemoryLevel historyMemoryLevel = core.history.HistoryMemoryLevel.MEDIUM;

    // Persisted initial project points & grids for autosave recovery
    private List<userpackage.SPoint> initialPoints = null;
    private List<userpackage.SPoint> initialGrids = null;
    private List<userpackage.SLine> initialLines = null;
    private int activeLineStrokeWidth = 2;

    public AppState() {
        this.historyManager = new HistoryManager(historyMemoryLevel.getRamBudgetBytes());
        this.canvasState = new CanvasState();
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public void setAutoSaveEnabled(boolean autoSaveEnabled) {
        this.autoSaveEnabled = autoSaveEnabled;
    }

    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }

    public void setAutoSaveInterval(int autoSaveInterval) {
        int oldInterval = this.autoSaveInterval;
        this.autoSaveInterval = Math.max(1, autoSaveInterval);
        if (oldInterval != this.autoSaveInterval && watchedKeys.containsKey("autoSaveInterval")) {
            watchedKeys.get("autoSaveInterval").forEach(listener ->
                    listener.propertyChange(new PropertyChangeEvent(this, "autoSaveInterval", oldInterval, this.autoSaveInterval)));
        }
    }

    public core.history.HistoryMemoryLevel getHistoryMemoryLevel() {
        return historyMemoryLevel;
    }

    public void setHistoryMemoryLevel(core.history.HistoryMemoryLevel level) {
        core.history.HistoryMemoryLevel oldLevel = this.historyMemoryLevel;
        this.historyMemoryLevel = level;
        if (historyManager != null) {
            historyManager.setMemoryBudget(level.getRamBudgetBytes());
        }
        if (oldLevel != this.historyMemoryLevel && watchedKeys.containsKey("historyMemoryLevel")) {
            watchedKeys.get("historyMemoryLevel").forEach(listener ->
                    listener.propertyChange(new PropertyChangeEvent(this, "historyMemoryLevel", oldLevel, this.historyMemoryLevel)));
        }
    }




    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        watchedKeys.putIfAbsent(propertyName, new ArrayList<>());
        watchedKeys.get(propertyName).add(listener);
    }


    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public CanvasState getCanvasState() {
        return canvasState;
    }

    public List<userpackage.SPoint> getInitialPoints() {
        return initialPoints;
    }

    public void setInitialPoints(List<userpackage.SPoint> initialPoints) {
        this.initialPoints = initialPoints;
    }

    public List<userpackage.SPoint> getInitialGrids() {
        return initialGrids;
    }

    public void setInitialGrids(List<userpackage.SPoint> initialGrids) {
        this.initialGrids = initialGrids;
    }

    public List<userpackage.SLine> getInitialLines() {
        return initialLines;
    }

    public void setInitialLines(List<userpackage.SLine> initialLines) {
        this.initialLines = initialLines;
    }

    public int getActiveLineStrokeWidth() {
        return activeLineStrokeWidth;
    }

    public void setActiveLineStrokeWidth(int w) {
        this.activeLineStrokeWidth = w;
    }

    public boolean isFloating() {
        return floating;
    }

    public void setFloating(boolean floating) {
        this.floating = floating;
        if(watchedKeys.containsKey("floating")){
            PropertyChangeEvent evt = new PropertyChangeEvent(this, "floating", floating, floating);
            watchedKeys.get("floating")
                    .forEach( listener ->
                            listener.propertyChange(evt));
        }
    }

    public float getCurrentZoom() {
        return currentZoom;
    }

    public void setCurrentZoom(float currentZoom) {
        this.currentZoom = currentZoom;
        if(watchedKeys.containsKey("currentZoom")){
            watchedKeys.get("currentZoom")
                    .forEach( listener ->
                    listener.propertyChange(null));
        }
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        if(watchedKeys.containsKey("scale")){
            watchedKeys.get("scale")
                    .forEach( listener ->
                            listener.propertyChange(null));
        }
    }

    public MouseMode getMouseMode() {
        return mouseMode;
    }

    public void setMouseMode(MouseMode mouseMode) {
        this.mouseMode = mouseMode;
    }

    public boolean isCmUnit() {
        return cmUnit;
    }

    public void setCmUnit(boolean cmUnit) {
        this.cmUnit = cmUnit;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public boolean isRound() {
        return round;
    }

    public void setRound(boolean round) {
        this.round = round;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getShadowPath() {
        return shadowPath;
    }

    public void setShadowPath(String shadowPath) {
        this.shadowPath = shadowPath;
    }

    public String getOriginalHash() {
        return originalHash;
    }

    public void setOriginalHash(String originalHash) {
        this.originalHash = originalHash;
    }

    public EditState getEditState() {
        return editState;
    }

//    public void setEditState(EditState editState) {
//        this.editState = editState;
//    }

    public void setEditState(EditState editState) {
        this.editState = editState;
    }

    public Color getBrushColor() {
        return brushColor;
    }

    public void setBrushColor(Color brushColor) {
        this.brushColor = brushColor;
    }


    public float getGridSize() {
        return gridSize;
    }

    public void setGridSize(float gridSize) {
        this.gridSize = gridSize;
    }

    public boolean isGridInCm() {
        return gridInCm;
    }

    public void setGridInCm(boolean gridInCm) {
        this.gridInCm = gridInCm;
    }

    public int getWindowX() {
        return windowX;
    }

    public void setWindowX(int windowX) {
        this.windowX = windowX;
    }

    public int getWindowY() {
        return windowY;
    }

    public void setWindowY(int windowY) {
        this.windowY = windowY;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public Rectangle getZoomWindowBounds() {
        return zoomWindowBounds;
    }

    public String getStringZoomWindowBounds() {
        return zoomWindowBounds.x+";"+zoomWindowBounds.y+";"+zoomWindowBounds.width+";"+zoomWindowBounds.height;
    }

    public void setZoomWindowBounds(Rectangle bounds) {
        this.zoomWindowBounds = bounds;
    }

    public void setStringZoomWindowBounds(String bounds) {
        String[] parts = bounds.split(";");
        if(parts.length!=4){
            System.out.println("Invalid zoom window bounds: "+bounds);
            return;
        }
        this.zoomWindowBounds = new Rectangle(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
        );
    }

    public void setLabelDirection(Direction d) {
        this.labelDirection = d;
    }

    public Direction getLabelDirection() {
        return labelDirection;
    }

    public boolean isCustomLabelMode() { return customLabelMode; }
    public void toggleCustomLabelMode() { this.customLabelMode = !this.customLabelMode; }
    public void setCustomLabelMode(boolean customLabelMode) { this.customLabelMode = customLabelMode; }

    public int getCustomGap() { return customGap; }
    public void setCustomGap(int gap) {
        this.customGap = Math.max(8, Math.min(50, gap));
    }

    public int getCustomAngle() { return customAngle; }
    public void setCustomAngle(int angle) {
        // Đảm bảo góc luôn nằm trong [0, 359]
        this.customAngle = (angle % 360 + 360) % 360;
    }


    public boolean isShowCropHelp() {
        return showCropHelp;
    }

    public void setShowCropHelp(boolean showCropHelp) {
        this.showCropHelp = showCropHelp;
    }

    public int getCheckerSize() {
        return checkerSize;
    }

    public void setCheckerSize(int checkerSize) {
        this.checkerSize = checkerSize;
        if(watchedKeys.containsKey("checkerSize")){
            watchedKeys.get("checkerSize")
                    .forEach( listener ->
                            listener.propertyChange(null));
        }
    }

    public boolean isShowPointMap() {
        return showPointMap;
    }

    public void setShowPointMap(boolean showPointMap) {
        this.showPointMap = showPointMap;
    }

    public Color getXorColor() {
        return XOR_COLORS[xorColorIndex];
    }

    public String getXorColorName() {
        return XOR_COLOR_NAMES[xorColorIndex];
    }

    public void cycleXorColor() {
        xorColorIndex = (xorColorIndex + 1) % XOR_COLORS.length;
    }
}
