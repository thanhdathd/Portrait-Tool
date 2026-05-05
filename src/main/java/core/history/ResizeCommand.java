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
    private final List<SPoint> oldPoints;
    private final List<SPoint> oldGrids;
    private final double scaleX;
    private final double scaleY;

    public ResizeCommand(ImageCanvas canvas, CanvasState canvasState, BufferedImage oldImage, BufferedImage newImage) {
        this.canvas = canvas;
        this.canvasState = canvasState;
        this.oldImage = oldImage;
        this.newImage = newImage;
        this.oldPoints = clonePoints(canvasState.getStickyPoints());
        this.oldGrids = clonePoints(canvasState.getGrids());
        this.scaleX = (double) newImage.getWidth() / oldImage.getWidth();
        this.scaleY = (double) newImage.getHeight() / oldImage.getHeight();
    }

    private List<SPoint> clonePoints(List<SPoint> points) {
        List<SPoint> cloned = new ArrayList<>();
        for (SPoint p : points) {
            SPoint newP = new SPoint(p.id, p.X, p.Y, p.c, p.dr);
            newP.isCustomPlacement = p.isCustomPlacement;
            newP.customGap = p.customGap;
            newP.customAngle = p.customAngle;
            cloned.add(newP);
        }
        return cloned;
    }

    @Override
    public void execute() {
        canvas.setBackgroundImage(newImage);
        scalePoints(canvasState.getStickyPoints());
        scalePoints(canvasState.getGrids());
    }

    @Override
    public void undo() {
        canvas.setBackgroundImage(oldImage);
        canvasState.getStickyPoints().clear();
        canvasState.getStickyPoints().addAll(oldPoints);
        canvasState.getGrids().clear();
        canvasState.getGrids().addAll(oldGrids);
    }
    
    private void scalePoints(List<SPoint> points) {
        for (SPoint p : points) {
            p.X = (int) Math.round(p.X * scaleX);
            p.Y = (int) Math.round(p.Y * scaleY);
        }
    }
}
