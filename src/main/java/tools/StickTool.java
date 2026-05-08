package tools;

import core.history.StickCommand;
import core.state.AppState;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

public class StickTool implements Tool {

    @Override
    public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
        // Sticky point placement relies on mouse release in the original implementation
    }

    @Override
    public void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            int nextId = 1;
            for (SPoint p : appState.getCanvasState().getStickyPoints()) {
                if (p.id >= nextId) {
                    nextId = p.id + 1;
                }
            }
            
            int x = e.getX() - appState.getCanvasState().getImageOffsetX();
            int y = e.getY() - appState.getCanvasState().getImageOffsetY();
            
            // Adjust for zoom
            if (appState.getCurrentZoom() != 1.0F) {
                x = Math.round((float) x / appState.getCurrentZoom());
                y = Math.round((float) y / appState.getCurrentZoom());
            }

            // Boundary check: Only allow placement on the image
            java.awt.image.BufferedImage img = canvas.getBackgroundImage();
            if (img != null) {
                if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight()) {
                    return;
                }
            }

            // Create point and push to history
            SPoint point = new SPoint(nextId, x, y, appState.getBrushColor(), appState.getLabelDirection());
            if(appState.isCustomLabelMode()) {
                point.customAngle = appState.getCustomAngle();
                point.customGap = appState.getCustomGap();
                point.isCustomPlacement = true;
            }
            StickCommand command = new StickCommand(appState.getCanvasState(), null, point); // ImgFrame is null because we decouple it
            appState.getHistoryManager().push(command);
            canvas.repaint();
        }
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
        // No drag behavior for sticky points
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
        // StickTool could draw an indicator at the cursor, but for now we keep it simple
    }
}
