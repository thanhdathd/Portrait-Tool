package core.history;

import core.image.ImageTransformUtils.TransformType;
import core.state.CanvasState;
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
    private final List<SPoint> oldPoints;
    private final List<SPoint> oldGrids;
    private final TransformType type;

    public TransformCommand(ImageCanvas canvas, CanvasState canvasState, BufferedImage oldImage, BufferedImage newImage, TransformType type) {
        this.canvas = canvas;
        this.canvasState = canvasState;
        this.oldImage = oldImage;
        this.newImage = newImage;
        this.type = type;
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
        transformPoints(canvasState.getStickyPoints(), oldImage.getWidth(), oldImage.getHeight());
        transformPoints(canvasState.getGrids(), oldImage.getWidth(), oldImage.getHeight());
    }

    @Override
    public void undo() {
        canvas.setBackgroundImage(oldImage);
        canvasState.getStickyPoints().clear();
        canvasState.getStickyPoints().addAll(oldPoints);
        canvasState.getGrids().clear();
        canvasState.getGrids().addAll(oldGrids);
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
}
