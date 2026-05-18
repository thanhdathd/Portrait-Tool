package core.history;

import core.state.CanvasState;
import core.state.CommandData;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CropCommand implements Command {
    private static class LineStateBackup {
        userpackage.SLine line;
        java.awt.Point originalStart;
        java.awt.Point originalEnd;
        
        LineStateBackup(userpackage.SLine line) {
            this.line = line;
            this.originalStart = new java.awt.Point(line.startPoint);
            this.originalEnd = new java.awt.Point(line.endPoint);
        }
    }

    private final ImageCanvas canvas;
    private final CanvasState canvasState;
    private final BufferedImage oldImage;
    private final BufferedImage newImage;
    private final Rectangle cropBounds;
    
    private final List<SPoint> removedPoints = new ArrayList<>();
    private final List<SPoint> removedGrids = new ArrayList<>();
    private final List<userpackage.SLine> removedLines = new ArrayList<>();
    private final List<LineStateBackup> editedLinesBackups = new ArrayList<>();
    
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
        
        // Backup all line states
        for (userpackage.SLine l : canvasState.getLines()) {
            this.editedLinesBackups.add(new LineStateBackup(l));
        }
        
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
        removedLines.clear();
        
        processPointsExecute(canvasState.getStickyPoints(), removedPoints);
        processPointsExecute(canvasState.getGrids(), removedGrids);
        processLinesExecute(canvasState.getLines(), removedLines);

        if (canvasState.getSelectedLine() != null && !canvasState.getLines().contains(canvasState.getSelectedLine())) {
            canvasState.setSelectedLine(null);
        }
        
        applyVisualOffset(targetVisualX, targetVisualY, newImage);
    }

    @Override
    public void undo() {
        canvas.setBackgroundImage(oldImage);
        
        processPointsUndo(canvasState.getStickyPoints(), removedPoints);
        processPointsUndo(canvasState.getGrids(), removedGrids);
        
        // Restore all lines perfectly from backups
        canvasState.getLines().clear();
        for (LineStateBackup backup : editedLinesBackups) {
            backup.line.startPoint.setLocation(backup.originalStart);
            backup.line.endPoint.setLocation(backup.originalEnd);
            canvasState.getLines().add(backup.line);
        }
        removedLines.clear();
        
        applyVisualOffset(oldVisualX, oldVisualY, oldImage);
    }

    @Override
    public CommandData capture() {
        CommandData cmd = new CommandData();
        cmd.type = CommandData.CommandType.CROP;
        cmd.cropX = cropBounds.x;
        cmd.cropY = cropBounds.y;
        cmd.cropW = cropBounds.width;
        cmd.cropH = cropBounds.height;
        cmd.oldVisualX = this.oldVisualX;
        cmd.oldVisualY = this.oldVisualY;
        cmd.zomAtCrop = this.zoomAtCrop;
        return cmd;
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
    public long getMemorySize() {
        long size = 0;
        if (oldImage != null) size += (long) oldImage.getWidth() * oldImage.getHeight() * 4;
        if (newImage != null) size += (long) newImage.getWidth() * newImage.getHeight() * 4;
        return size + 1024;
    }

    @Override
    public String toString() {
        return "Crop command";
    }

    // Bit codes for regions
    private static final int INSIDE = 0; // 0000
    private static final int LEFT = 1;   // 0001
    private static final int RIGHT = 2;  // 0010
    private static final int BOTTOM = 4; // 0100
    private static final int TOP = 8;    // 1000

    private int computeOutCode(double x, double y, double xmax, double ymax) {
        int code = INSIDE;
        if (x < 0) code |= LEFT;
        else if (x >= xmax) code |= RIGHT;
        if (y < 0) code |= TOP;
        else if (y >= ymax) code |= BOTTOM;
        return code;
    }

    private boolean clipLine(java.awt.Point p1, java.awt.Point p2, double xmax, double ymax) {
        double x1 = p1.x;
        double y1 = p1.y;
        double x2 = p2.x;
        double y2 = p2.y;

        int code1 = computeOutCode(x1, y1, xmax, ymax);
        int code2 = computeOutCode(x2, y2, xmax, ymax);
        boolean accept = false;

        while (true) {
            if ((code1 | code2) == 0) {
                // Both points inside
                accept = true;
                break;
            } else if ((code1 & code2) != 0) {
                // Both points share an outside region -> completely outside
                break;
            } else {
                // Some segment is outside, select the point that is outside
                int codeOut = (code1 != 0) ? code1 : code2;
                double x = 0, y = 0;

                // Find intersection point
                if ((codeOut & TOP) != 0) {
                    // Point is above clip rectangle
                    x = x1 + (x2 - x1) * (0 - y1) / (y2 - y1);
                    y = 0;
                } else if ((codeOut & BOTTOM) != 0) {
                    // Point is below clip rectangle
                    x = x1 + (x2 - x1) * (ymax - 1 - y1) / (y2 - y1);
                    y = ymax - 1;
                } else if ((codeOut & RIGHT) != 0) {
                    // Point is to the right of clip rectangle
                    y = y1 + (y2 - y1) * (xmax - 1 - x1) / (x2 - x1);
                    x = xmax - 1;
                } else if ((codeOut & LEFT) != 0) {
                    // Point is to the left of clip rectangle
                    y = y1 + (y2 - y1) * (0 - x1) / (x2 - x1);
                    x = 0;
                }

                // Push intersection point into segment and recalculate outcode
                if (codeOut == code1) {
                    x1 = x;
                    y1 = y;
                    code1 = computeOutCode(x1, y1, xmax, ymax);
                } else {
                    x2 = x;
                    y2 = y;
                    code2 = computeOutCode(x2, y2, xmax, ymax);
                }
            }
        }

        if (accept) {
            p1.setLocation(Math.round((float) x1), Math.round((float) y1));
            p2.setLocation(Math.round((float) x2), Math.round((float) y2));
            return true;
        }
        return false;
    }

    private void processLinesExecute(List<userpackage.SLine> lines, List<userpackage.SLine> removedList) {
        Iterator<userpackage.SLine> it = lines.iterator();
        while (it.hasNext()) {
            userpackage.SLine l = it.next();
            // 1. Shift
            l.startPoint.x -= cropBounds.x;
            l.startPoint.y -= cropBounds.y;
            l.endPoint.x -= cropBounds.x;
            l.endPoint.y -= cropBounds.y;
            
            // 2. Clip
            boolean keeps = clipLine(l.startPoint, l.endPoint, cropBounds.width, cropBounds.height);
            if (keeps) {
                // If it's too short (less than 2px) after clipping, remove it
                if (l.startPoint.distance(l.endPoint) < 2.0) {
                    keeps = false;
                }
            }
            
            if (!keeps) {
                removedList.add(l);
                it.remove();
            }
        }
    }
}
