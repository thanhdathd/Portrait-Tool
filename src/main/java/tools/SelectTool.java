package tools;

import core.state.AppState;
import ui.canvas.ImageCanvas;
import ui.canvas.RenderUtils;
import userpackage.SPoint;
import user.Enum.Direction;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SelectTool implements Tool {

    private static final int SELECT_TOLERANCE_PX = 10; // Pixels on screen
    private final Random random = new Random();

    @Override
    public void onMouseMoved(MouseEvent e, AppState appState, ImageCanvas canvas) {
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
    }

    @Override
    public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
    }

    @Override
    public void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (e.getButton() != MouseEvent.BUTTON1) return;

        int cx = e.getX();
        int cy = e.getY();
        int offsetX = appState.getCanvasState().getImageOffsetX();
        int offsetY = appState.getCanvasState().getImageOffsetY();
        float zoom = appState.getCurrentZoom();

        // Convert screen → image coordinates
        float ix = (cx - offsetX) / zoom;
        float iy = (cy - offsetY) / zoom;

        // ======================================================
        // STEP 1: Check sticky points (priority over grids)
        // ======================================================
        List<SPoint> pointProximity = new ArrayList<>(); // within 5px of the dot
        List<SPoint> labelHit       = new ArrayList<>(); // click inside label bbox

        // Need a FontMetrics to compute label bounds — create a dummy one
        // We use a temporary Graphics to get FontMetrics
        Font labelFont = new Font("SansSerif", Font.BOLD, 12);
        FontMetrics fm = canvas.getFontMetrics(labelFont);

        for (SPoint p : appState.getCanvasState().getStickyPoints()) {
            // --- Distance to point dot (screen pixels) ---
            float dx = (p.X - ix) * zoom;
            float dy = (p.Y - iy) * zoom;
            float distScreen = (float) Math.sqrt(dx * dx + dy * dy);
            if (distScreen <= SELECT_TOLERANCE_PX) {
                pointProximity.add(p);
                continue; // Already captured as proximity; skip label check for this point
            }

            // --- Label bounding box hit (image coords, no tolerance) ---
            Rectangle labelBounds = RenderUtils.getLabelBounds(p, fm);
            if (labelBounds.contains(ix, iy)) {
                labelHit.add(p);
            }
        }

        // Merge: proximity takes priority; only add labelHit entries not already in proximity
        List<SPoint> pointCandidates = new ArrayList<>(pointProximity);
        for (SPoint p : labelHit) {
            if (!pointCandidates.contains(p)) {
                pointCandidates.add(p);
            }
        }

        if (!pointCandidates.isEmpty()) {
            // There is at least one point candidate
            SPoint currentSelected = appState.getCanvasState().getSelectedPoint();
            SPoint toSelect;

            if (pointCandidates.size() > 1 && pointCandidates.contains(currentSelected)) {
                // Cycle: remove current, pick another
                List<SPoint> others = new ArrayList<>(pointCandidates);
                others.remove(currentSelected);
                toSelect = others.get(random.nextInt(others.size()));
            } else {
                toSelect = pointCandidates.get(random.nextInt(pointCandidates.size()));
            }

            // Select the point, deselect any grid
            appState.getCanvasState().setSelectedGrid(null);
            appState.getCanvasState().setSelectedPoint(toSelect);
            canvas.repaint();
            return;
        }

        // ======================================================
        // STEP 2: Check grids (only if no point was hit)
        // ======================================================
        List<SPoint> gridCandidates = new ArrayList<>();
        SPoint currentSelectedGrid = appState.getCanvasState().getSelectedGrid();

        for (SPoint grid : appState.getCanvasState().getGrids()) {
            int S = grid.id;
            if (S <= 0) continue;
            int xR = grid.X;
            int yR = grid.Y;

            float diffX = Math.abs(ix - xR);
            float remX = diffX % S;
            float distImageX = Math.min(remX, S - remX);
            float distScreenX = distImageX * zoom;

            float diffY = Math.abs(iy - yR);
            float remY = diffY % S;
            float distImageY = Math.min(remY, S - remY);
            float distScreenY = distImageY * zoom;

            if (distScreenX <= SELECT_TOLERANCE_PX || distScreenY <= SELECT_TOLERANCE_PX) {
                gridCandidates.add(grid);
            }
        }

        if (!gridCandidates.isEmpty()) {
            SPoint toSelect;
            if (gridCandidates.size() > 1 && gridCandidates.contains(currentSelectedGrid)) {
                List<SPoint> others = new ArrayList<>(gridCandidates);
                others.remove(currentSelectedGrid);
                toSelect = others.get(random.nextInt(others.size()));
            } else {
                toSelect = gridCandidates.get(random.nextInt(gridCandidates.size()));
            }

            appState.getCanvasState().setSelectedPoint(null);
            appState.getCanvasState().setSelectedGrid(toSelect);
        } else {
            // Clicking empty space — deselect both
            appState.getCanvasState().setSelectedGrid(null);
            appState.getCanvasState().setSelectedPoint(null);
        }

        canvas.repaint();
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
    }
}
