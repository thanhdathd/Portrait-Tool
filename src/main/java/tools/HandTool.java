package tools;

import core.state.AppState;
import ui.canvas.ImageCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class HandTool implements Tool {

    private Point dragStartPoint;

    @Override
    public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
        dragStartPoint = e.getLocationOnScreen();
    }

    @Override
    public void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas) {
        dragStartPoint = null;
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (dragStartPoint != null) {
            Container parent = SwingUtilities.getUnwrappedParent(canvas);
            if (parent instanceof JViewport) {
                JViewport viewport = (JViewport) parent;
                Point viewPosition = viewport.getViewPosition();
                
                Point currentScreen = e.getLocationOnScreen();
                int dx = dragStartPoint.x - currentScreen.x;
                int dy = dragStartPoint.y - currentScreen.y;
                
                // Keep the view bounded
                int maxX = Math.max(0, canvas.getWidth() - viewport.getWidth());
                int maxY = Math.max(0, canvas.getHeight() - viewport.getHeight());
                
                // If the canvas is smaller than the viewport in a dimension, panning the viewport does nothing.
                // So we pan the image inside the canvas instead.
                core.state.CanvasState cs = appState.getCanvasState();
                
                if (maxX == 0) {
                    cs.setImageOffsetX(cs.getImageOffsetX() - dx);
                } else {
                    viewPosition.x = Math.max(0, Math.min(viewPosition.x + dx, maxX));
                }
                
                if (maxY == 0) {
                    cs.setImageOffsetY(cs.getImageOffsetY() - dy);
                } else {
                    viewPosition.y = Math.max(0, Math.min(viewPosition.y + dy, maxY));
                }
                
                viewport.setViewPosition(viewPosition);
                canvas.repaint();
                
                dragStartPoint = currentScreen; // Update drag start
            }
        }
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
        // Nothing to paint for Hand tool
    }
}
