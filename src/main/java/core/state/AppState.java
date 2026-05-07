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

    public void resizeHistoryStack(int newSize) {
        historyManager.setLength(newSize);
        stackSize = newSize;
    }


    public enum EditState {
        SAVED,
        MODIFIED,
        NOT_SAVED
    }

    private int stackSize = 15;
    private float currentZoom = 1.0f;
    private float scale = 1.0f;
    private MouseMode mouseMode = MouseMode.DRAG;
    private boolean cmUnit = false;
    private boolean viLang = false;
    private boolean round = false;
    private int windowX, windowY, windowWidth, windowHeight;
    private String filePath = "Untitled-00.jpg";
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
    private int customGap = 20;   // Giới hạn 8 - 50
    private int customAngle = 40; // Độ (0 - 359), tăng theo chiều CCW (ngược chiều kim đồng hồ)
    private int checkerSize = 40;

    public AppState() {
        this.historyManager = new HistoryManager(115);
        this.canvasState = new CanvasState();
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

    public boolean isViLang() {
        return viLang;
    }

    public void setViLang(boolean viLang) {
        this.viLang = viLang;
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

    public EditState getEditState() {
        return editState;
    }

//    public void setEditState(EditState editState) {
//        this.editState = editState;
//    }

    public void setEditState(EditState editState) {
        System.out.println("setEditState: "+editState);
        this.editState = editState;
    }

    public Color getBrushColor() {
        return brushColor;
    }

    public void setBrushColor(Color brushColor) {
        this.brushColor = brushColor;
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
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
}
