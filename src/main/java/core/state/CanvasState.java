package core.state;

import userpackage.SPoint;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the current visual state of the canvas, decoupled from the history manager.
 */
public class CanvasState {
    
    private final List<SPoint> stickyPoints;
    private final List<SPoint> grids;

    public CanvasState() {
        this.stickyPoints = new ArrayList<>();
        this.grids = new ArrayList<>();
    }

    public List<SPoint> getStickyPoints() {
        return stickyPoints;
    }

    public void addStickyPoint(SPoint p) {
        stickyPoints.add(p);
    }

    public void removeStickyPoint(SPoint p) {
        stickyPoints.remove(p);
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
    }
}
