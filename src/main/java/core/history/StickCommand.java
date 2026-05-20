package core.history;

import core.state.CanvasState;
import core.state.CommandData;
import ui.canvas.ImageCanvas;
import core.state.SPoint;

public class StickCommand implements Command {

    public enum Action {
        ADD, DELETE, EDIT
    }

    private final CanvasState canvasState;
    private final ImageCanvas canvas;
    private final SPoint point;   // Reference to the actual point in CanvasState (for ADD/DELETE/EDIT)
    private final SPoint oldState;
    private final SPoint newState;
    private final Action action;

    /** Backward-compatible constructor for ADD. */
    public StickCommand(CanvasState canvasState, ImageCanvas canvas, SPoint point) {
        this(canvasState, canvas, point, Action.ADD, null, null);
    }

    public StickCommand(CanvasState canvasState, ImageCanvas canvas, SPoint point,
                        Action action, SPoint oldState, SPoint newState) {
        this.canvasState = canvasState;
        this.canvas = canvas;
        this.point = point;
        this.action = action;
        this.oldState = oldState != null ? oldState.copy() : (point != null ? point.copy() : null);
        this.newState = newState != null ? newState.copy() : null;
    }

    @Override
    public void execute() {
        switch (action) {
            case ADD:
                canvasState.addStickyPoint(point);
                break;
            case DELETE:
                canvasState.removeStickyPoint(point);
                if (canvasState.getSelectedPoint() == point) {
                    canvasState.setSelectedPoint(null);
                }
                break;
            case EDIT:
                if (newState != null && point != null) {
                    point.X = newState.X;
                    point.Y = newState.Y;
                    point.c = newState.c;
                    point.dr = newState.dr;
                    point.isCustomPlacement = newState.isCustomPlacement;
                    point.customAngle = newState.customAngle;
                    point.customGap = newState.customGap;
                }
                break;
        }
        canvasState.notifyPointSelectionChanged();
        if (canvas != null) canvas.repaint();
    }

    @Override
    public void undo() {
        switch (action) {
            case ADD:
                canvasState.removeStickyPoint(point);
                if (canvasState.getSelectedPoint() == point) {
                    canvasState.setSelectedPoint(null);
                }
                break;
            case DELETE:
                canvasState.addStickyPoint(point);
                canvasState.setSelectedPoint(point);
                break;
            case EDIT:
                if (oldState != null && point != null) {
                    point.X = oldState.X;
                    point.Y = oldState.Y;
                    point.c = oldState.c;
                    point.dr = oldState.dr;
                    point.isCustomPlacement = oldState.isCustomPlacement;
                    point.customAngle = oldState.customAngle;
                    point.customGap = oldState.customGap;
                }
                break;
        }
        canvasState.notifyPointSelectionChanged();
        if (canvas != null) canvas.repaint();
    }

    @Override
    public CommandData capture() {
        CommandData cmd = new CommandData();
        switch (action) {
            case ADD:
                cmd.type = CommandData.CommandType.ADD_POINT;
                cmd.point = point.copy();
                break;
            case DELETE:
                cmd.type = CommandData.CommandType.DELETE_POINT;
                cmd.point = oldState.copy();
                break;
            case EDIT:
                cmd.type = CommandData.CommandType.EDIT_POINT;
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
        return "Point action: " + action;
    }
}
