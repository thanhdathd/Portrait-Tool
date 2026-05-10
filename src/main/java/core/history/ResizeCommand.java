package core.history;

import core.state.CanvasState;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ResizeCommand implements Command {
    private final ImageCanvas canvas;
    private final CanvasState canvasState;
    private final BufferedImage oldImage;
    private final BufferedImage newImage;
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

    public ResizeCommand(ImageCanvas canvas, CanvasState canvasState, BufferedImage oldImage, BufferedImage newImage) {
        this.canvas = canvas;
        this.canvasState = canvasState;
        this.oldImage = oldImage;
        this.newImage = newImage;
        this.scaleX = (double) newImage.getWidth() / oldImage.getWidth();
        this.scaleY = (double) newImage.getHeight() / oldImage.getHeight();
    }

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
    }
}
