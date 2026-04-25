package tools;

import core.history.GridCommand;
import core.state.AppState;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

public class GridTool implements Tool {

    private int gridSize;

    public GridTool(int gridSize) {
        this.gridSize = gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    @Override
    public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
        // Grid placement relies on mouse release in legacy implementation
    }

    @Override
    public void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            int x = e.getX() - appState.getCanvasState().getImageOffsetX();
            int y = e.getY() - appState.getCanvasState().getImageOffsetY();
            
            // Adjust for zoom
            if (appState.getCurrentZoom() != 1.0F) {
                x = Math.round((float) x / appState.getCurrentZoom());
                y = Math.round((float) y / appState.getCurrentZoom());
            }

            // Create grid data point and push to history
            // We use the ID to store gridSize to match legacy drawing logic temporarily
            SPoint gridData = new SPoint(gridSize, x, y, appState.getBrushColor()); 
            GridCommand command = new GridCommand(appState.getCanvasState(), canvas, gridData);
            appState.getHistoryManager().push(command);
            canvas.repaint();
        }
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
        // No drag behavior for grids yet
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
        // Optional: Draw a preview of the grid at the current mouse location
    }
}
