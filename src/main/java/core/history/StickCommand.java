package core.history;

import core.state.CanvasState;
import userpackage.ImgFrame;
import userpackage.SPoint;

public class StickCommand implements Command {

    private final CanvasState canvasState;
    private final ImgFrame imgFrame;
    private final SPoint point;

    public StickCommand(CanvasState canvasState, ImgFrame imgFrame, SPoint point) {
        this.canvasState = canvasState;
        this.imgFrame = imgFrame;
        this.point = point;
    }

    @Override
    public void execute() {
        canvasState.addStickyPoint(point);
        imgFrame.repaint(); // Trigger a redraw
    }

    @Override
    public void undo() {
        canvasState.removeStickyPoint(point);
        imgFrame.repaint(); // Trigger a redraw
    }
}
