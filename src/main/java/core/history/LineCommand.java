package core.history;

import core.state.CanvasState;
import core.state.CommandData;
import ui.canvas.ImageCanvas;
import core.state.SLine;

public class LineCommand implements Command {

    public enum Action {
        ADD, DELETE, EDIT
    }

    private final CanvasState canvasState;
    private final ImageCanvas canvas;
    private final SLine lineData;
    private final SLine oldState;
    private final SLine newState;
    private final Action action;

    public LineCommand(CanvasState canvasState, ImageCanvas canvas, SLine lineData, Action action, SLine oldState, SLine newState) {
        this.canvasState = canvasState;
        this.canvas = canvas;
        this.lineData = lineData;
        this.action = action;
        this.oldState = oldState != null ? oldState.copy() : (lineData != null ? lineData.copy() : null);
        this.newState = newState != null ? newState.copy() : null;
    }

    @Override
    public void execute() {
        switch (action) {
            case ADD:
                canvasState.getLines().add(lineData);
                break;
            case DELETE:
                canvasState.getLines().remove(lineData);
                if (canvasState.getSelectedLine() == lineData) {
                    canvasState.setSelectedLine(null);
                }
                break;
            case EDIT:
                if (newState != null && lineData != null) {
                    lineData.startPoint.setLocation(newState.startPoint);
                    lineData.endPoint.setLocation(newState.endPoint);
                    lineData.strokeWidth = newState.strokeWidth;
                    lineData.strokeColor = newState.strokeColor;
                    lineData.id = newState.id;
                }
                break;
        }
        if (canvas != null) canvas.repaint();
    }

    @Override
    public void undo() {
        switch (action) {
            case ADD:
                canvasState.getLines().remove(lineData);
                if (canvasState.getSelectedLine() == lineData) {
                    canvasState.setSelectedLine(null);
                }
                break;
            case DELETE:
                canvasState.getLines().add(lineData);
                canvasState.setSelectedLine(lineData);
                break;
            case EDIT:
                if (oldState != null && lineData != null) {
                    lineData.startPoint.setLocation(oldState.startPoint);
                    lineData.endPoint.setLocation(oldState.endPoint);
                    lineData.strokeWidth = oldState.strokeWidth;
                    lineData.strokeColor = oldState.strokeColor;
                    lineData.id = oldState.id;
                }
                break;
        }
        if (canvas != null) canvas.repaint();
    }

    @Override
    public CommandData capture() {
        CommandData cmd = new CommandData();
        switch (action) {
            case ADD:
                cmd.type = CommandData.CommandType.ADD_LINE;
                cmd.line = lineData.copy();
                break;
            case DELETE:
                cmd.type = CommandData.CommandType.DELETE_LINE;
                cmd.line = oldState.copy();
                break;
            case EDIT:
                cmd.type = CommandData.CommandType.EDIT_LINE;
                cmd.line = oldState.copy();
                cmd.newLine = newState.copy();
                break;
        }
        return cmd;
    }

    @Override
    public long getMemorySize() {
        return 256;
    }
}
