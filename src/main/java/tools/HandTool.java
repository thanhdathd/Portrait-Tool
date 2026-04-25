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
        dragStartPoint = e.getPoint();
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
                
                int dx = dragStartPoint.x - e.getX();
                int dy = dragStartPoint.y - e.getY();
                
                viewPosition.translate(dx, dy);
                
                // Keep the view bounded
                int maxX = canvas.getWidth() - viewport.getWidth();
                int maxY = canvas.getHeight() - viewport.getHeight();
                
                viewPosition.x = Math.max(0, Math.min(viewPosition.x, maxX));
                viewPosition.y = Math.max(0, Math.min(viewPosition.y, maxY));
                
                viewport.setViewPosition(viewPosition);
                // Note: we don't update dragStartPoint because e.getX() is relative to the canvas,
                // and translating the viewport changes the visible area but not the canvas coordinate system
            }
        }
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
        // Nothing to paint for Hand tool
    }
}
