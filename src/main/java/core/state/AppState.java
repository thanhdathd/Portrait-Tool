package core.state;

import user.Enum.MouseMode;
import core.history.HistoryManager;
import java.awt.Color;

public class AppState {

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
    private String filePath = "Untitled-00.jpg";
    private EditState editState = EditState.SAVED;
    private Color brushColor = Color.CYAN;
    private boolean floating = false;
    private final HistoryManager historyManager;
    private final CanvasState canvasState;

    public AppState() {
        this.historyManager = new HistoryManager(115);
        this.canvasState = new CanvasState();
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
    }

    public float getCurrentZoom() {
        return currentZoom;
    }

    public void setCurrentZoom(float currentZoom) {
        this.currentZoom = currentZoom;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
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

    public void setEditState(EditState editState) {
        this.editState = editState;
    }

    public Color getBrushColor() {
        return brushColor;
    }

    public void setBrushColor(Color brushColor) {
        this.brushColor = brushColor;
    }
}
