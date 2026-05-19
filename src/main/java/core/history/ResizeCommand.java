package core.history;

import core.state.CanvasState;
import core.state.CommandData;
import ui.canvas.ImageCanvas;
import ui.dialogs.ResizeDialog;
import userpackage.SPoint;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ResizeCommand implements Command {
    private final ImageCanvas canvas;
    private final CanvasState canvasState;
    private final BufferedImage oldImage;
    private final BufferedImage newImage;
    ResizeDialog.ResizeProps props;
    private final List<PointSnapshot> undoPoints = new ArrayList<>();
    private final List<PointSnapshot> undoGrids = new ArrayList<>();
    private final double scaleX;
    private final double scaleY;

    private static class PointSnapshot {
        final SPoint point;
        final int oldX;
        final int oldY;

        PointSnapshot(SPoint point) {
            this.point = point;
            this.oldX = point.X;
            this.oldY = point.Y;
        }
        
        void restore() {
            point.X = oldX;
            point.Y = oldY;
        }
    }

    private static class LineSnapshot {
        final userpackage.SLine line;
        final int oldStartX;
        final int oldStartY;
        final int oldEndX;
        final int oldEndY;

        LineSnapshot(userpackage.SLine line) {
            this.line = line;
            this.oldStartX = line.startPoint.x;
            this.oldStartY = line.startPoint.y;
            this.oldEndX = line.endPoint.x;
            this.oldEndY = line.endPoint.y;
        }

        void restore() {
            line.startPoint.setLocation(oldStartX, oldStartY);
            line.endPoint.setLocation(oldEndX, oldEndY);
        }
    }

    public ResizeCommand(ImageCanvas canvas, CanvasState canvasState,
                         BufferedImage oldImage, BufferedImage newImage, ResizeDialog.ResizeProps props) {
        this.canvas = canvas;
        this.canvasState = canvasState;
        this.oldImage = oldImage;
        this.newImage = newImage;
        this.props = props;
        this.scaleX = (double) newImage.getWidth() / oldImage.getWidth();
        this.scaleY = (double) newImage.getHeight() / oldImage.getHeight();
    }

    private final List<LineSnapshot> undoLines = new ArrayList<>();

    @Override
    public void execute() {
        canvas.setBackgroundImage(newImage);
        
        undoPoints.clear();
        for (SPoint p : canvasState.getStickyPoints()) {
            undoPoints.add(new PointSnapshot(p));
            p.X = (int) Math.round(p.X * scaleX);
            p.Y = (int) Math.round(p.Y * scaleY);
        }

        undoGrids.clear();
        for (SPoint p : canvasState.getGrids()) {
            undoGrids.add(new PointSnapshot(p));
            p.X = (int) Math.round(p.X * scaleX);
            p.Y = (int) Math.round(p.Y * scaleY);
        }

        undoLines.clear();
        for (userpackage.SLine l : canvasState.getLines()) {
            undoLines.add(new LineSnapshot(l));
            l.startPoint.setLocation((int) Math.round(l.startPoint.x * scaleX), (int) Math.round(l.startPoint.y * scaleY));
            l.endPoint.setLocation((int) Math.round(l.endPoint.x * scaleX), (int) Math.round(l.endPoint.y * scaleY));
        }
    }

    @Override
    public void undo() {
        canvas.setBackgroundImage(oldImage);
        for (PointSnapshot ps : undoPoints) {
            ps.restore();
        }
        for (PointSnapshot ps : undoGrids) {
            ps.restore();
        }
        for (LineSnapshot ls : undoLines) {
            ls.restore();
        }
    }

    @Override
    public CommandData capture() {
        CommandData cmd = new CommandData();
        cmd.type = CommandData.CommandType.RESIZE;
        cmd.resizeProps = props;
        return cmd;
    }

    @Override
    public long getMemorySize() {
        long size = 0;
        if (oldImage != null) size += (long) oldImage.getWidth() * oldImage.getHeight() * 4;
        if (newImage != null) size += (long) newImage.getWidth() * newImage.getHeight() * 4;
        return size + 1024;
    }
}
