package tools;

import core.state.AppState;
import ui.canvas.ImageCanvas;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;

public class P2PTool implements Tool {

    private Point startPoint;
    private Point endPoint;

    @Override
    public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            startPoint = e.getPoint();
            endPoint = e.getPoint();
        }
    }

    @Override
    public void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (e.getButton() == MouseEvent.BUTTON1 && startPoint != null) {
            endPoint = e.getPoint();
            // TODO: Execute a P2PCommand to finalize the measurement line
            // Currently omitted for simplicity, as legacy code didn't even track it in the undo stack
            canvas.repaint();
            startPoint = null;
        }
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (startPoint != null) {
            endPoint = e.getPoint();
            canvas.repaint();
        }
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
        if (startPoint != null && endPoint != null) {
            g2d.setColor(appState.getBrushColor());
            g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
            
            // Draw measurement text
            int dx = endPoint.x - startPoint.x;
            int dy = endPoint.y - startPoint.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            // Scale and draw logic here (simplified)
            g2d.drawString(String.format("%.2f px", distance), endPoint.x + 10, endPoint.y);
        }
    }
}
