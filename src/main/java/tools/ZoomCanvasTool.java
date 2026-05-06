package tools;

import core.state.AppState;
import ui.canvas.ImageCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class ZoomCanvasTool implements Tool {

    private boolean zoomInMode = true;

    public void toggleMode() {
        zoomInMode = !zoomInMode;
    }

    public boolean isZoomInMode() {
        return zoomInMode;
    }

    @Override
    public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if(e.getButton() == MouseEvent.BUTTON3) {
            return;
        }
        float zoom = appState.getCurrentZoom();
        float oldZoom = zoom;
        
        boolean effectiveZoomIn = zoomInMode;
        if (e.isShiftDown()) {
            effectiveZoomIn = !effectiveZoomIn; // Shift temporarily inverts
        }

        if (effectiveZoomIn) {
            zoom *= 1.25f;
        } else {
            zoom /= 1.25f;
        }

        zoom = Math.max(0.05f, Math.min(zoom, 8.0f)); // clamp between 5% and 800%
        
        if (zoom == oldZoom) return;

        // Calculate zoom-to-point logic
        Container parent = SwingUtilities.getUnwrappedParent(canvas);
        if (parent instanceof JViewport) {
            JViewport viewport = (JViewport) parent;
            
            // Current view position
            Point viewPos = viewport.getViewPosition();
            
            // The point clicked relative to the unscaled image coordinates
            float imageX = e.getX() / oldZoom;
            float imageY = e.getY() / oldZoom;
            
            // Update state
            appState.setCurrentZoom(zoom);
            canvas.revalidate(); // Updates preferred size
            canvas.repaint();

            // The new expected coordinate of that image pixel on the scaled canvas
            int newCanvasX = (int) (imageX * zoom);
            int newCanvasY = (int) (imageY * zoom);
            
            // The cursor is at e.getX() relative to the old view bounds.
            // We want newCanvasX to be exactly where the cursor is currently on the screen.
            // Screen cursor relative to viewport = e.getX() - viewPos.x
            // So we want: newCanvasX - newViewPos.x = e.getX() - viewPos.x
            // newViewPos.x = newCanvasX - (e.getX() - viewPos.x)
            
            int newViewX = newCanvasX - (e.getX() - viewPos.x);
            int newViewY = newCanvasY - (e.getY() - viewPos.y);
            
            // We need to defer setting view position until after layout is complete
            SwingUtilities.invokeLater(() -> {
                int maxX = Math.max(0, canvas.getWidth() - viewport.getWidth());
                int maxY = Math.max(0, canvas.getHeight() - viewport.getHeight());
                
                viewport.setViewPosition(new Point(
                        Math.max(0, Math.min(newViewX, maxX)),
                        Math.max(0, Math.min(newViewY, maxY))
                ));
            });
        } else {
            appState.setCurrentZoom(zoom);
            canvas.revalidate();
            canvas.repaint();
        }
    }

    @Override
    public void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas) {
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
    }
}
