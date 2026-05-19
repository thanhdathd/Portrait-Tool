package core.state;

import userpackage.SPoint;
import java.util.*;
import java.util.function.Consumer;

/**
 * Holds the current visual state of the canvas, decoupled from the history manager.
 */
public class CanvasState {
    
    private final List<SPoint> stickyPoints;
    private final List<SPoint> grids;
    private SPoint selectedGrid;

    // Line support
    private final List<userpackage.SLine> lines = new ArrayList<>();
    private final LinkedHashSet<userpackage.SLine> selectedLines = new LinkedHashSet<>();
    private Consumer<Set<userpackage.SLine>> lineSelectionListener;

    // Multi-selection: ordered set of selected points
    private final LinkedHashSet<SPoint> selectedPoints = new LinkedHashSet<>();
    private Consumer<Integer> pointChangeListener;
    private Consumer<SPoint> gridSelectionListener;
    private Consumer<Set<SPoint>> pointSelectionListener;
    
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

    public void setGridSelectionListener(Consumer<SPoint> listener) {
        this.gridSelectionListener = listener;
    }

    public void setPointSelectionListener(Consumer<Set<SPoint>> listener) {
        this.pointSelectionListener = listener;
    }

    // ---- Grid selection ----

    public SPoint getSelectedGrid() {
        return selectedGrid;
    }

    public void setSelectedGrid(SPoint selectedGrid) {
        this.selectedGrid = selectedGrid;
        if (gridSelectionListener != null) {
            gridSelectionListener.accept(selectedGrid);
        }
    }

    // ---- Point selection ----

    /** Returns an unmodifiable view of the current selected points set. */
    public Set<SPoint> getSelectedPoints() {
        return Collections.unmodifiableSet(selectedPoints);
    }

    /**
     * Backward-compat: returns the last selected point (most recently added),
     * or null if nothing is selected.
     */
    public SPoint getSelectedPoint() {
        if (selectedPoints.isEmpty()) return null;
        SPoint last = null;
        for (SPoint p : selectedPoints) last = p;
        return last;
    }

    /**
     * Single-select: replace current selection with exactly one point.
     * Backward-compat equivalent of the old setSelectedPoint().
     */
    public void setSelectedPoint(SPoint point) {
        selectedPoints.clear();
        if (point != null) selectedPoints.add(point);
        firePointSelectionChanged();
    }

    /** Replace current selection with a set of points. */
    public void setSelectedPoints(Set<SPoint> points) {
        selectedPoints.clear();
        if (points != null) selectedPoints.addAll(points);
        firePointSelectionChanged();
    }

    /** Add a point to the current selection. */
    public void addToSelection(SPoint point) {
        if (point != null) {
            selectedPoints.add(point);
            firePointSelectionChanged();
        }
    }

    /** Remove a point from the current selection. */
    public void removeFromSelection(SPoint point) {
        if (point != null && selectedPoints.remove(point)) {
            firePointSelectionChanged();
        }
    }

    /** Clear the entire point selection. */
    public void clearPointSelection() {
        if (!selectedPoints.isEmpty()) {
            selectedPoints.clear();
            firePointSelectionChanged();
        }
    }

    private void firePointSelectionChanged() {
        if (pointSelectionListener != null) {
            pointSelectionListener.accept(Collections.unmodifiableSet(selectedPoints));
        }
    }

    // ---- Offset ----

    public int getImageOffsetX() { return imageOffsetX; }
    public void setImageOffsetX(int imageOffsetX) { this.imageOffsetX = imageOffsetX; }
    public int getImageOffsetY() { return imageOffsetY; }
    public void setImageOffsetY(int imageOffsetY) { this.imageOffsetY = imageOffsetY; }

    // ---- Sticky points ----

    public List<SPoint> getStickyPoints() { return stickyPoints; }

    public void addStickyPoint(SPoint p) {
        stickyPoints.add(p);
        if (pointChangeListener != null) pointChangeListener.accept(stickyPoints.size());
    }

    public void removeStickyPoint(SPoint p) {
        stickyPoints.remove(p);
        if (pointChangeListener != null) pointChangeListener.accept(stickyPoints.size());
    }
    
    public void removeLastStickyPoint() {
        if (!stickyPoints.isEmpty()) {
            stickyPoints.remove(stickyPoints.size() - 1);
        }
    }

    // ---- Grids ----

    public List<SPoint> getGrids() { return grids; }

    public void addGrid(SPoint grid) { grids.add(grid); }

    public void removeGrid(SPoint grid) { grids.remove(grid); }

    public void clearAll() {
        stickyPoints.clear();
        grids.clear();
        lines.clear();
        setSelectedGrid(null);
        clearLineSelection();
        clearPointSelection();
        if (pointChangeListener != null) pointChangeListener.accept(0);
    }

    // ---- Lines ----

    public List<userpackage.SLine> getLines() {
        return lines;
    }

    public userpackage.SLine getSelectedLine() {
        if (selectedLines.isEmpty()) return null;
        userpackage.SLine last = null;
        for (userpackage.SLine l : selectedLines) last = l;
        return last;
    }

    public Set<userpackage.SLine> getSelectedLines() {
        return Collections.unmodifiableSet(selectedLines);
    }

    public void setSelectedLine(userpackage.SLine line) {
        selectedLines.clear();
        if (line != null) {
            selectedLines.add(line);
        }
        fireLineSelectionChanged();
    }

    public void setSelectedLines(Set<userpackage.SLine> lines) {
        selectedLines.clear();
        if (lines != null) {
            selectedLines.addAll(lines);
        }
        fireLineSelectionChanged();
    }

    public void addToSelection(userpackage.SLine line) {
        if (line != null && selectedLines.add(line)) {
            fireLineSelectionChanged();
        }
    }

    public void removeFromSelection(userpackage.SLine line) {
        if (line != null && selectedLines.remove(line)) {
            fireLineSelectionChanged();
        }
    }

    public void clearLineSelection() {
        if (!selectedLines.isEmpty()) {
            selectedLines.clear();
            fireLineSelectionChanged();
        }
    }

    public void setLineSelectionListener(Consumer<Set<userpackage.SLine>> listener) {
        this.lineSelectionListener = listener;
    }

    private void fireLineSelectionChanged() {
        if (lineSelectionListener != null) {
            lineSelectionListener.accept(Collections.unmodifiableSet(selectedLines));
        }
    }

    // --- Export Live Preview Properties ---
    private boolean exportPreviewActive = false;
    private double exportPreviewScale = 1.0;
    private String exportPreviewTitle = "";
    
    public void setExportPreview(boolean active, double scale, String title) {
        this.exportPreviewActive = active;
        this.exportPreviewScale = scale;
        this.exportPreviewTitle = title;
    }
    
    public boolean isExportPreviewActive() { return exportPreviewActive; }
    public double getExportPreviewScale() { return exportPreviewScale; }
    public String getExportPreviewTitle() { return exportPreviewTitle; }
}
