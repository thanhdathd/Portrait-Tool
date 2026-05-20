package core.history;

import core.state.CanvasState;
import core.state.CommandData;
import ui.canvas.ImageCanvas;
import core.state.SPoint;

public class GridCommand implements Command {

    public enum Action {
        ADD, DELETE, EDIT
    }

    private final CanvasState canvasState;
    private final ImageCanvas canvas;
    private final SPoint gridData; // Reference to the actual grid in CanvasState
    private final SPoint oldState;
    private final SPoint newState;
    private final Action action;

    // Backward compatibility constructor for ADD action
    public GridCommand(CanvasState canvasState, ImageCanvas canvas, SPoint gridData) {
        this(canvasState, canvas, gridData, Action.ADD, null, null);
    }

    public GridCommand(CanvasState canvasState, ImageCanvas canvas, SPoint gridData, Action action, SPoint oldState, SPoint newState) {
        this.canvasState = canvasState;
        this.canvas = canvas;
        this.gridData = gridData;
        this.action = action;
        this.oldState = oldState != null ? oldState.copy() : (gridData != null ? gridData.copy() : null);
        this.newState = newState != null ? newState.copy() : null;
    }

    @Override
    public void execute() {
        switch (action) {
            case ADD:
                canvasState.addGrid(gridData);
                break;
            case DELETE:
                canvasState.removeGrid(gridData);
                if (canvasState.getSelectedGrid() == gridData) {
                    canvasState.setSelectedGrid(null);
                }
                break;
            case EDIT:
                if (newState != null && gridData != null) {
                    gridData.X = newState.X;
                    gridData.Y = newState.Y;
                    gridData.id = newState.id;
                    gridData.c = newState.c;
                }
                break;
        }
        if (canvas != null) canvas.repaint();
    }

    @Override
    public void undo() {
        switch (action) {
            case ADD:
                canvasState.removeGrid(gridData);
                if (canvasState.getSelectedGrid() == gridData) {
                    canvasState.setSelectedGrid(null);
                }
                break;
            case DELETE:
                canvasState.addGrid(gridData);
                canvasState.setSelectedGrid(gridData);
                break;
            case EDIT:
                if (oldState != null && gridData != null) {
                    gridData.X = oldState.X;
                    gridData.Y = oldState.Y;
                    gridData.id = oldState.id;
                    gridData.c = oldState.c;
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
                cmd.type = CommandData.CommandType.ADD_GRID;
                cmd.point = gridData.copy();
                break;
            case DELETE:
                cmd.type = CommandData.CommandType.DELETE_GRID;
                cmd.point = oldState.copy();
                break;
            case EDIT:
                cmd.type = CommandData.CommandType.EDIT_GRID;
                cmd.point = oldState.copy();
                cmd.newPoint = newState.copy();
                break;
        }
        return cmd;
    }

    @Override
    public long getMemorySize() {
        return 256;
    }

    @Override
    public String toString() {
        return "Grid action: " + action;
    }
}
