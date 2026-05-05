package core.history;

import core.state.CanvasState;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CropCommand implements Command {
    private final ImageCanvas canvas;
    private final CanvasState canvasState;
    private final BufferedImage oldImage;
    private final BufferedImage newImage;
    private final Rectangle cropBounds;
    
    private final List<SPoint> oldPoints;
    private final List<SPoint> oldGrids;

    public CropCommand(ImageCanvas canvas, CanvasState canvasState, BufferedImage oldImage, Rectangle cropBounds) {
        this.canvas = canvas;
        this.canvasState = canvasState;
        this.oldImage = oldImage;
        this.cropBounds = cropBounds;
        
        // Deep copy the cropped region
        int type = oldImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : oldImage.getType();
        BufferedImage subImage = oldImage.getSubimage(cropBounds.x, cropBounds.y, cropBounds.width, cropBounds.height);
        this.newImage = new BufferedImage(cropBounds.width, cropBounds.height, type);
        java.awt.Graphics2D g2 = this.newImage.createGraphics();
        g2.drawImage(subImage, 0, 0, null);
        g2.dispose();
        
        this.oldPoints = clonePoints(canvasState.getStickyPoints());
        this.oldGrids = clonePoints(canvasState.getGrids());
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
        processPoints(canvasState.getStickyPoints());
        processPoints(canvasState.getGrids());
    }

    @Override
    public void undo() {
        canvas.setBackgroundImage(oldImage);
        canvasState.getStickyPoints().clear();
        canvasState.getStickyPoints().addAll(oldPoints);
        canvasState.getGrids().clear();
        canvasState.getGrids().addAll(oldGrids);
    }
    
    private void processPoints(List<SPoint> points) {
        Iterator<SPoint> it = points.iterator();
        while (it.hasNext()) {
            SPoint p = it.next();
            p.X -= cropBounds.x;
            p.Y -= cropBounds.y;
            // Remove points that fall completely outside the new bounds
            if (p.X < 0 || p.Y < 0 || p.X >= cropBounds.width || p.Y >= cropBounds.height) {
                it.remove();
            }
        }
    }
}
