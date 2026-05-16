package tools;

import core.state.AppState;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SelectTool implements Tool {

    private static final int SELECT_TOLERANCE_PX = 5; // Pixels on screen
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

        // Convert mouse coordinates to image coordinates
        float ix = (cx - offsetX) / zoom;
        float iy = (cy - offsetY) / zoom;

        List<SPoint> candidates = new ArrayList<>();
        SPoint currentSelected = appState.getCanvasState().getSelectedGrid();

        for (SPoint grid : appState.getCanvasState().getGrids()) {
            int S = grid.id; // Grid cell size
            if (S <= 0) continue;
            int xR = grid.X;
            int yR = grid.Y;

            // Calculate distance to nearest vertical grid line
            float diffX = Math.abs(ix - xR);
            float remX = diffX % S;
            float distImageX = Math.min(remX, S - remX);
            float distScreenX = distImageX * zoom;

            // Calculate distance to nearest horizontal grid line
            float diffY = Math.abs(iy - yR);
            float remY = diffY % S;
            float distImageY = Math.min(remY, S - remY);
            float distScreenY = distImageY * zoom;

            // If mouse is near a horizontal OR vertical line of this grid
            if (distScreenX <= SELECT_TOLERANCE_PX || distScreenY <= SELECT_TOLERANCE_PX) {
                candidates.add(grid);
            }
        }

        if (candidates.isEmpty()) {
            // Deselect if clicking empty space
            appState.getCanvasState().setSelectedGrid(null);
        } else {
            // Select one randomly, ensuring we can cycle through overlapping grids
            SPoint toSelect = candidates.get(random.nextInt(candidates.size()));
            
            // If the only candidate is already selected, keep it selected (or deselect?)
            // Usually, clicking it again keeps it selected.
            // If multiple candidates and current is one of them, try to pick a different one
            if (candidates.size() > 1 && candidates.contains(currentSelected)) {
                candidates.remove(currentSelected);
                toSelect = candidates.get(random.nextInt(candidates.size()));
            }
            
            appState.getCanvasState().setSelectedGrid(toSelect);
        }
        
        canvas.repaint();
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
    }
}
