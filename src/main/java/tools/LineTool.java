package tools;

import core.history.LineCommand;
import core.state.AppState;
import ui.canvas.ImageCanvas;
import userpackage.SLine;

import java.awt.*;
import java.awt.event.MouseEvent;

public class LineTool implements Tool {

    private Point startPoint = null;
    private Point currentDrag = null;
    private boolean isDrawing = false;

    @Override
    public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            int x = Math.round((float)(e.getX() - appState.getCanvasState().getImageOffsetX()) / appState.getCurrentZoom());
            int y = Math.round((float)(e.getY() - appState.getCanvasState().getImageOffsetY()) / appState.getCurrentZoom());
            
            // Boundary validation
            java.awt.image.BufferedImage img = canvas.getBackgroundImage();
            if (img != null && (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight())) {
                return;
            }

            startPoint = new Point(x, y);
            currentDrag = new Point(x, y);
            isDrawing = true;
        }
    }

    @Override
    public void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (isDrawing && startPoint != null && currentDrag != null) {
            int endX = Math.round((float)(e.getX() - appState.getCanvasState().getImageOffsetX()) / appState.getCurrentZoom());
            int endY = Math.round((float)(e.getY() - appState.getCanvasState().getImageOffsetY()) / appState.getCurrentZoom());

            java.awt.image.BufferedImage img = canvas.getBackgroundImage();
            if (img != null) {
                endX = Math.max(0, Math.min(img.getWidth() - 1, endX));
                endY = Math.max(0, Math.min(img.getHeight() - 1, endY));
            }

            Point endPoint = new Point(endX, endY);
            double distance = startPoint.distance(endPoint);

            if (distance >= 3.0) { // Check minimum drag length to prevent accidental clicks
                int nextId = 1;
                for (SLine l : appState.getCanvasState().getLines()) {
                    if (l.id >= nextId) {
                        nextId = l.id + 1;
                    }
                }

                SLine line = new SLine(nextId, startPoint, endPoint, appState.getActiveLineStrokeWidth(), appState.getBrushColor());
                LineCommand cmd = new LineCommand(appState.getCanvasState(), canvas, line, LineCommand.Action.ADD, null, null);
                appState.getHistoryManager().push(cmd);
                appState.getCanvasState().setSelectedLine(line);
            }
        }
        isDrawing = false;
        startPoint = null;
        currentDrag = null;
        canvas.repaint();
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (isDrawing && currentDrag != null) {
            int dragX = Math.round((float)(e.getX() - appState.getCanvasState().getImageOffsetX()) / appState.getCurrentZoom());
            int dragY = Math.round((float)(e.getY() - appState.getCanvasState().getImageOffsetY()) / appState.getCurrentZoom());

            java.awt.image.BufferedImage img = canvas.getBackgroundImage();
            if (img != null) {
                dragX = Math.max(0, Math.min(img.getWidth() - 1, dragX));
                dragY = Math.max(0, Math.min(img.getHeight() - 1, dragY));
            }

            currentDrag.setLocation(dragX, dragY);
            canvas.repaint();
        }
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
        if (isDrawing && startPoint != null && currentDrag != null) {
            float zoom = appState.getCurrentZoom();
            int ox = appState.getCanvasState().getImageOffsetX();
            int oy = appState.getCanvasState().getImageOffsetY();

            int sx = Math.round(startPoint.x * zoom) + ox;
            int sy = Math.round(startPoint.y * zoom) + oy;
            int dx = Math.round(currentDrag.x * zoom) + ox;
            int dy = Math.round(currentDrag.y * zoom) + oy;

            g2d.setColor(appState.getBrushColor());
            g2d.setStroke(new BasicStroke(appState.getActiveLineStrokeWidth() * zoom, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(sx, sy, dx, dy);
        }
    }
}
