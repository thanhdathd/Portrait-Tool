package tools;

import core.state.AppState;
import ui.canvas.ImageCanvas;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class P2PTool implements Tool {

    private static class Line {
        Point start;
        Point end;
        Line(Point start, Point end) {
            this.start = start;
            this.end = end;
        }
    }

    private final List<Line> completedLines = new ArrayList<>();
    private Point startPoint;
    private Point endPoint;

    @Override
    public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            startPoint = getAdjustedPoint(e.getPoint(), appState);
            endPoint = startPoint;
        }
    }

    private Point getAdjustedPoint(Point p, AppState appState) {
        int x = p.x - appState.getCanvasState().getImageOffsetX();
        int y = p.y - appState.getCanvasState().getImageOffsetY();
        if (appState.getCurrentZoom() != 1.0F) {
            x = Math.round((float) x / appState.getCurrentZoom());
            y = Math.round((float) y / appState.getCurrentZoom());
        }
        return new Point(x, y);
    }

    @Override
    public void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (e.getButton() == MouseEvent.BUTTON1 && startPoint != null) {
            endPoint = getAdjustedPoint(e.getPoint(), appState);
            completedLines.add(new Line(startPoint, endPoint));
            canvas.repaint();
            startPoint = null;
            endPoint = null;
        }
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (startPoint != null) {
            endPoint = getAdjustedPoint(e.getPoint(), appState);
            canvas.scrollRectToVisible(new java.awt.Rectangle(e.getX(), e.getY(), 1, 1));
            canvas.repaint();
        }
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
        g2d.setColor(appState.getBrushColor());
        
        // Draw completed lines
        for (Line line : completedLines) {
            drawLine(g2d, line.start, line.end);
        }
        
        // Draw active drag line
        if (startPoint != null && endPoint != null) {
            drawLine(g2d, startPoint, endPoint);
        }
    }
    
    private void drawLine(Graphics2D g2d, Point p1, Point p2) {
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        g2d.drawString(String.format("%.2f px", distance), p2.x + 10, p2.y);
    }
}
