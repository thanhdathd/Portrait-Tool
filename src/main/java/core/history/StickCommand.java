package core.history;

import core.state.CanvasState;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

public class StickCommand implements Command {

    private final CanvasState canvasState;
    private final ImageCanvas canvas;
    private final SPoint point;

    public StickCommand(CanvasState canvasState, ImageCanvas canvas, SPoint point) {
        this.canvasState = canvasState;
        this.canvas = canvas;
        this.point = point;
    }

    @Override
    public void execute() {
        canvasState.addStickyPoint(point);
        if (canvas != null) canvas.repaint();
    }

    @Override
    public void undo() {
        canvasState.removeStickyPoint(point);
        if (canvas != null) canvas.repaint();
    }
}
