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
    
    private final List<SPoint> removedPoints = new ArrayList<>();
    private final List<SPoint> removedGrids = new ArrayList<>();
    
    private final int targetVisualX;
    private final int targetVisualY;
    private final int oldVisualX;
    private final int oldVisualY;
    private final float zoomAtCrop;

    public CropCommand(ImageCanvas canvas, CanvasState canvasState,
                       BufferedImage oldImage, Rectangle cropBounds,
                       float zoom, int oldVisualX, int oldVisualY) {
        this.canvas = canvas;
        this.canvasState = canvasState;
        this.oldImage = oldImage;
        this.cropBounds = cropBounds;
        
        // Deep copy the cropped region, natively supporting out-of-bounds crops
        int type = oldImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : oldImage.getType();
        this.newImage = new BufferedImage(cropBounds.width, cropBounds.height, type);
        java.awt.Graphics2D g2 = this.newImage.createGraphics();
        
        g2.setColor(java.awt.Color.BLACK);
        g2.fillRect(0, 0, cropBounds.width, cropBounds.height);
        
        // If crop is outside oldImage, this simply translates oldImage so the correct part falls into newImage
        g2.drawImage(oldImage, -cropBounds.x, -cropBounds.y, null);
        g2.dispose();
        
        this.zoomAtCrop = zoom;
        this.oldVisualX = oldVisualX;
        this.oldVisualY = oldVisualY;
        this.targetVisualX = Math.round(oldVisualX + cropBounds.x * zoom);
        this.targetVisualY = Math.round(oldVisualY + cropBounds.y * zoom);
    }

    @Override
    public void execute() {
        canvas.setBackgroundImage(newImage);
        
        removedPoints.clear();
        removedGrids.clear();
        
        processPointsExecute(canvasState.getStickyPoints(), removedPoints);
        processPointsExecute(canvasState.getGrids(), removedGrids);
        
        applyVisualOffset(targetVisualX, targetVisualY, newImage);
    }

    @Override
    public void undo() {
        canvas.setBackgroundImage(oldImage);
        
        processPointsUndo(canvasState.getStickyPoints(), removedPoints);
        processPointsUndo(canvasState.getGrids(), removedGrids);
        
        applyVisualOffset(oldVisualX, oldVisualY, oldImage);
    }
    
    private void processPointsExecute(List<SPoint> points, List<SPoint> removedList) {
        Iterator<SPoint> it = points.iterator();
        while (it.hasNext()) {
            SPoint p = it.next();
            p.X -= cropBounds.x;
            p.Y -= cropBounds.y;
            // Remove points that fall completely outside the new bounds
            if (p.X < 0 || p.Y < 0 || p.X >= cropBounds.width || p.Y >= cropBounds.height) {
                removedList.add(p);
                it.remove();
            }
        }
    }

    private void processPointsUndo(List<SPoint> currentPoints, List<SPoint> removedList) {
        // 1. Revert coordinates of points currently on canvas
        for (SPoint p : currentPoints) {
            p.X += cropBounds.x;
            p.Y += cropBounds.y;
        }
        // 2. Revert coordinates of removed points and put them back
        for (SPoint p : removedList) {
            p.X += cropBounds.x;
            p.Y += cropBounds.y;
            currentPoints.add(p);
        }
        removedList.clear();
    }
    
    private void applyVisualOffset(int vX, int vY, BufferedImage img) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            java.awt.Container parent = javax.swing.SwingUtilities.getUnwrappedParent(canvas);
            if (parent instanceof javax.swing.JViewport) {
                javax.swing.JViewport viewport = (javax.swing.JViewport) parent;
                
                int imgWidth = (int) (img.getWidth() * zoomAtCrop);
                int imgHeight = (int) (img.getHeight() * zoomAtCrop);
                int viewWidth = viewport.getWidth();
                int viewHeight = viewport.getHeight();
                
                int newOx, newOy, newScrollX, newScrollY;
                
                if (imgWidth > viewWidth && imgHeight > viewHeight) {
                    newOx = ui.canvas.ImageCanvas.CANVAS_PADDING;
                    newOy = ui.canvas.ImageCanvas.CANVAS_PADDING;
                    newScrollX = newOx - vX;
                    newScrollY = newOy - vY;
                    
                    int maxScrollX = Math.max(0, imgWidth + 2 * newOx - viewWidth);
                    int maxScrollY = Math.max(0, imgHeight + 2 * newOy - viewHeight);
                    
                    newScrollX = Math.max(0, Math.min(newScrollX, maxScrollX));
                    newScrollY = Math.max(0, Math.min(newScrollY, maxScrollY));
                } else {
                    newScrollX = 0;
                    newScrollY = 0;
                    newOx = vX;
                    newOy = vY;
                }
                
                canvasState.setImageOffsetX(newOx);
                canvasState.setImageOffsetY(newOy);
                viewport.setViewPosition(new java.awt.Point(newScrollX, newScrollY));
                canvas.repaint();
            }
        });
    }

    @Override
    public String toString() {
        return "Crop command";
    }
}
