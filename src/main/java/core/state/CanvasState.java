package core.state;

import userpackage.SPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Holds the current visual state of the canvas, decoupled from the history manager.
 */
public class CanvasState {
    
    private final List<SPoint> stickyPoints;
    private final List<SPoint> grids;
    Consumer<Integer> pointChangeListener;
    
    // Properties to allow free-floating small images on the canvas
    private int imageOffsetX = 0;
    private int imageOffsetY = 0;

    public CanvasState() {
        this.stickyPoints = new ArrayList<>();
        this.grids = new ArrayList<>();
    }

    public void addStickyPointChangeListener(Consumer<Integer> listener) {
        this.pointChangeListener = listener;
    }

    public int getImageOffsetX() {
        return imageOffsetX;
    }

    public void setImageOffsetX(int imageOffsetX) {
        this.imageOffsetX = imageOffsetX;
    }

    public int getImageOffsetY() {
        return imageOffsetY;
    }

    public void setImageOffsetY(int imageOffsetY) {
        this.imageOffsetY = imageOffsetY;
    }

    public List<SPoint> getStickyPoints() {
        return stickyPoints;
    }

    public void addStickyPoint(SPoint p) {
        stickyPoints.add(p);
        if (pointChangeListener != null) {
            pointChangeListener.accept(stickyPoints.size());
        }
    }

    public void removeStickyPoint(SPoint p) {
        stickyPoints.remove(p);
        if (pointChangeListener != null) {
            pointChangeListener.accept(stickyPoints.size());
        }
    }
    
    public void removeLastStickyPoint() {
        if (!stickyPoints.isEmpty()) {
            stickyPoints.remove(stickyPoints.size() - 1);
        }
    }

    public List<SPoint> getGrids() {
        return grids;
    }

    public void addGrid(SPoint grid) {
        grids.add(grid);
    }

    public void removeGrid(SPoint grid) {
        grids.remove(grid);
    }

    public void clearAll() {
        stickyPoints.clear();
        grids.clear();
        pointChangeListener.accept(stickyPoints.size());
    }
}
