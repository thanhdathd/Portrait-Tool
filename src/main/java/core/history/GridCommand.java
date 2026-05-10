package core.history;

import core.state.CanvasState;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

public class GridCommand implements Command {

    private final CanvasState canvasState;
    private final ImageCanvas canvas;
    private final SPoint gridData;

    public GridCommand(CanvasState canvasState, ImageCanvas canvas, SPoint gridData) {
        this.canvasState = canvasState;
        this.canvas = canvas;
        this.gridData = gridData;
    }

    @Override
    public void execute() {
        canvasState.addGrid(gridData);
        if (canvas != null) canvas.repaint();
    }

    @Override
    public void undo() {
        canvasState.removeGrid(gridData);
        if (canvas != null) canvas.repaint();
    }

    @Override
    public String toString() {
        return "Grid command";
    }
}
