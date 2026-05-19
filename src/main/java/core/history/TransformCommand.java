package core.history;

import core.image.ImageTransformUtils.TransformType;
import core.state.CanvasState;
import core.state.CommandData;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;
import user.Enum.Direction;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TransformCommand implements Command {
    private final ImageCanvas canvas;
    private final CanvasState canvasState;
    private final BufferedImage oldImage;
    private final BufferedImage newImage;
    private final List<PointSnapshot> undoPoints = new ArrayList<>();
    private final List<PointSnapshot> undoGrids = new ArrayList<>();
    private final TransformType type;

    private static class PointSnapshot {
        final SPoint point;
        final int oldX;
        final int oldY;
        final Direction oldDr;
        final int oldAngle;

        PointSnapshot(SPoint point) {
            this.point = point;
            this.oldX = point.X;
            this.oldY = point.Y;
            this.oldDr = point.dr;
            this.oldAngle = point.customAngle;
        }
        
        void restore() {
            point.X = oldX;
            point.Y = oldY;
            point.dr = oldDr;
            point.customAngle = oldAngle;
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

    public TransformCommand(ImageCanvas canvas, CanvasState canvasState, BufferedImage oldImage, BufferedImage newImage, TransformType type) {
        this.canvas = canvas;
        this.canvasState = canvasState;
        this.oldImage = oldImage;
        this.newImage = newImage;
        this.type = type;
    }

    private final List<LineSnapshot> undoLines = new ArrayList<>();

    @Override
    public void execute() {
        canvas.setBackgroundImage(newImage);
        
        undoPoints.clear();
        for (SPoint p : canvasState.getStickyPoints()) {
            undoPoints.add(new PointSnapshot(p));
        }
        undoGrids.clear();
        for (SPoint p : canvasState.getGrids()) {
            undoGrids.add(new PointSnapshot(p));
        }
        undoLines.clear();
        for (userpackage.SLine l : canvasState.getLines()) {
            undoLines.add(new LineSnapshot(l));
        }
        
        transformPoints(canvasState.getStickyPoints(), oldImage.getWidth(), oldImage.getHeight());
        transformPoints(canvasState.getGrids(), oldImage.getWidth(), oldImage.getHeight());
        transformLines(canvasState.getLines(), oldImage.getWidth(), oldImage.getHeight());
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
        if (type.name().contains("ROTATE")) {
            cmd.type = CommandData.CommandType.ROTATE;
        } else {
            cmd.type = CommandData.CommandType.FLIP;
        }
        cmd.transformType = type;
        return cmd;
    }

    private void transformPoints(List<SPoint> points, int w, int h) {
        for (SPoint p : points) {
            int x = p.X;
            int y = p.Y;
            switch (type) {
                case ROTATE_90_CW:
                    p.X = h - 1 - y;
                    p.Y = x;
                    p.dr = rotateCW(p.dr);
                    p.customAngle = (p.customAngle + 270) % 360;
                    break;
                case ROTATE_90_CCW:
                    p.X = y;
                    p.Y = w - 1 - x;
                    p.dr = rotateCCW(p.dr);
                    p.customAngle = (p.customAngle + 90) % 360;
                    break;
                case ROTATE_180:
                    p.X = w - 1 - x;
                    p.Y = h - 1 - y;
                    p.dr = rotateCW(rotateCW(p.dr));
                    p.customAngle = (p.customAngle + 180) % 360;
                    break;
                case FLIP_H:
                    p.X = w - 1 - x;
                    if (p.dr == Direction.EAST) p.dr = Direction.WEST;
                    else if (p.dr == Direction.WEST) p.dr = Direction.EAST;
                    p.customAngle = (180 - p.customAngle + 360) % 360;
                    break;
                case FLIP_V:
                    p.Y = h - 1 - y;
                    if (p.dr == Direction.NORTH) p.dr = Direction.SOUTH;
                    else if (p.dr == Direction.SOUTH) p.dr = Direction.NORTH;
                    p.customAngle = (360 - p.customAngle) % 360;
            }
        }
    }

    private void transformLines(List<userpackage.SLine> lines, int w, int h) {
        for (userpackage.SLine l : lines) {
            int sx = l.startPoint.x;
            int sy = l.startPoint.y;
            int ex = l.endPoint.x;
            int ey = l.endPoint.y;
            switch (type) {
                case ROTATE_90_CW:
                    l.startPoint.setLocation(h - 1 - sy, sx);
                    l.endPoint.setLocation(h - 1 - ey, ex);
                    break;
                case ROTATE_90_CCW:
                    l.startPoint.setLocation(sy, w - 1 - sx);
                    l.endPoint.setLocation(ey, w - 1 - ex);
                    break;
                case ROTATE_180:
                    l.startPoint.setLocation(w - 1 - sx, h - 1 - sy);
                    l.endPoint.setLocation(w - 1 - ex, h - 1 - ey);
                    break;
                case FLIP_H:
                    l.startPoint.setLocation(w - 1 - sx, sy);
                    l.endPoint.setLocation(w - 1 - ex, ey);
                    break;
                case FLIP_V:
                    l.startPoint.setLocation(sx, h - 1 - sy);
                    l.endPoint.setLocation(ex, h - 1 - ey);
                    break;
            }
        }
    }

    private Direction rotateCW(Direction d) {
        if (d == null) return null;
        if (d == Direction.NORTH) return Direction.EAST;
        if (d == Direction.EAST) return Direction.SOUTH;
        if (d == Direction.SOUTH) return Direction.WEST;
        return Direction.NORTH;
    }

    private Direction rotateCCW(Direction d) {
        if (d == null) return null;
        if (d == Direction.NORTH) return Direction.WEST;
        if (d == Direction.WEST) return Direction.SOUTH;
        if (d == Direction.SOUTH) return Direction.EAST;
        return Direction.NORTH;
    }

    @Override
    public long getMemorySize() {
        long size = 0;
        if (oldImage != null) size += (long) oldImage.getWidth() * oldImage.getHeight() * 4;
        if (newImage != null) size += (long) newImage.getWidth() * newImage.getHeight() * 4;
        return size + 1024;
    }
}
