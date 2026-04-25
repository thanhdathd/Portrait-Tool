package core.history;

import core.state.CanvasState;
import userpackage.ImgFrame;
import userpackage.SPoint;

public class GridCommand implements Command {

    private final CanvasState canvasState;
    private final ImgFrame imgFrame;
    private final SPoint gridData;

    public GridCommand(CanvasState canvasState, ImgFrame imgFrame, SPoint gridData) {
        this.canvasState = canvasState;
        this.imgFrame = imgFrame;
        this.gridData = gridData;
    }

    @Override
    public void execute() {
        canvasState.addGrid(gridData);
        imgFrame.repaint();
    }

    @Override
    public void undo() {
        canvasState.removeGrid(gridData);
        imgFrame.repaint();
    }
}
