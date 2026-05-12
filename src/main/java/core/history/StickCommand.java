package core.history;

import core.state.CanvasState;
import core.state.CommandData;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

public class StickCommand implements Command {

    private final CanvasState canvasState;
    private final ImageCanvas canvas;
    private final SPoint point;
    private final SPoint snapshot;

    public StickCommand(CanvasState canvasState, ImageCanvas canvas, SPoint point) {
        this.canvasState = canvasState;
        this.canvas = canvas;
        this.point = point;
        
        // Take a snapshot of the point's state at issuance time
        this.snapshot = new SPoint();
        this.snapshot.id = point.id;
        this.snapshot.X = point.X;
        this.snapshot.Y = point.Y;
        this.snapshot.dr = point.dr;
        this.snapshot.c = point.c;
        this.snapshot.isCustomPlacement = point.isCustomPlacement;
        this.snapshot.customGap = point.customGap;
        this.snapshot.customAngle = point.customAngle;
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

    @Override
    public CommandData capture() {
        CommandData cmd = new CommandData();
        cmd.type = CommandData.CommandType.ADD_POINT;
        cmd.point = snapshot;
        return cmd;
    }

    @Override
    public long getMemorySize() {
        return 256;
    }

    @Override
    public String toString() {
        return "Stick command";
    }
}
