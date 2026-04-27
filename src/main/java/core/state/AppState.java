package core.state;

import tools.PropertyChangeListener;
import user.Enum.MouseMode;
import core.history.HistoryManager;
import java.awt.Color;
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
        MODIFIED,
        NOT_SAVED
    }

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
    private boolean floating = false;
    private int gridSize = 40; // Default from legacy code
    private final HistoryManager historyManager;
    private final CanvasState canvasState;
    private final Map<String, List<PropertyChangeListener>> watchedKeys = new HashMap<>();

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

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
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
}
