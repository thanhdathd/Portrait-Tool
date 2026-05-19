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

    // Handle interaction state (image coords)
    private int activeHandleIndex = -1; // -1: none, 0: start, 1: end, 2: center
    private SLine draggingLine = null;
    private SLine originalDraggingLineState = null;
    private Point startPointOffset = null;
    private Point endPointOffset = null;

    // Handle drag tracking for batch editing
    private final java.util.List<core.history.BatchLineCommand.LineStatePair> draggingPairs = new java.util.ArrayList<>();
    private Point dragStartMouse = null;

    @Override
    public void onMouseMoved(MouseEvent e, AppState appState, ImageCanvas canvas) {
        java.util.Set<SLine> selectedLines = appState.getCanvasState().getSelectedLines();
        if (!selectedLines.isEmpty()) {
            float zoom = appState.getCurrentZoom();
            int ox = appState.getCanvasState().getImageOffsetX();
            int oy = appState.getCanvasState().getImageOffsetY();

            Point mouseLoc = new Point(
                Math.round((float)(e.getX() - ox) / zoom),
                Math.round((float)(e.getY() - oy) / zoom)
            );

            double screenRadius = 15.0 / zoom; // 30px active zone

            if (selectedLines.size() == 1) {
                SLine selectedLine = selectedLines.iterator().next();
                Point midPoint = new Point(
                    (selectedLine.startPoint.x + selectedLine.endPoint.x) / 2,
                    (selectedLine.startPoint.y + selectedLine.endPoint.y) / 2
                );

                if (mouseLoc.distance(selectedLine.startPoint) <= screenRadius ||
                    mouseLoc.distance(selectedLine.endPoint) <= screenRadius ||
                    mouseLoc.distance(midPoint) <= screenRadius) {
                    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }
            } else {
                // Only allow midpoint handle drag in multi-select
                for (SLine selectedLine : selectedLines) {
                    Point midPoint = new Point(
                        (selectedLine.startPoint.x + selectedLine.endPoint.x) / 2,
                        (selectedLine.startPoint.y + selectedLine.endPoint.y) / 2
                    );
                    if (mouseLoc.distance(midPoint) <= screenRadius) {
                        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
            }
        }
        canvas.setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            float zoom = appState.getCurrentZoom();
            int ox = appState.getCanvasState().getImageOffsetX();
            int oy = appState.getCanvasState().getImageOffsetY();

            // 1. Check handles on active selected lines
            java.util.Set<SLine> selectedLines = appState.getCanvasState().getSelectedLines();
            if (!selectedLines.isEmpty()) {
                Point pMouse = new Point(
                    Math.round((float)(e.getX() - ox) / zoom),
                    Math.round((float)(e.getY() - oy) / zoom)
                );
                double screenRadius = 15.0 / zoom;

                if (selectedLines.size() == 1) {
                    SLine selectedLine = selectedLines.iterator().next();
                    Point midPoint = new Point(
                        (selectedLine.startPoint.x + selectedLine.endPoint.x) / 2,
                        (selectedLine.startPoint.y + selectedLine.endPoint.y) / 2
                    );

                    if (pMouse.distance(selectedLine.startPoint) <= screenRadius) {
                        activeHandleIndex = 0;
                        draggingLine = selectedLine;
                        originalDraggingLineState = selectedLine.copy();
                        canvas.repaint();
                        return;
                    } else if (pMouse.distance(selectedLine.endPoint) <= screenRadius) {
                        activeHandleIndex = 1;
                        draggingLine = selectedLine;
                        originalDraggingLineState = selectedLine.copy();
                        canvas.repaint();
                        return;
                    } else if (pMouse.distance(midPoint) <= screenRadius) {
                        activeHandleIndex = 2;
                        draggingLine = selectedLine;
                        originalDraggingLineState = selectedLine.copy();
                        startPointOffset = new Point(selectedLine.startPoint.x - pMouse.x, selectedLine.startPoint.y - pMouse.y);
                        endPointOffset = new Point(selectedLine.endPoint.x - pMouse.x, selectedLine.endPoint.y - pMouse.y);
                        canvas.repaint();
                        return;
                    }
                } else {
                    // Multi-select mode: Check midpoint handles only
                    for (SLine l : selectedLines) {
                        Point midPoint = new Point(
                            (l.startPoint.x + l.endPoint.x) / 2,
                            (l.startPoint.y + l.endPoint.y) / 2
                        );
                        if (pMouse.distance(midPoint) <= screenRadius) {
                            activeHandleIndex = 3; // Batch midpoint drag marker
                            dragStartMouse = new Point(pMouse);
                            draggingPairs.clear();
                            for (SLine active : selectedLines) {
                                draggingPairs.add(new core.history.BatchLineCommand.LineStatePair(active, active.copy(), active.copy()));
                            }
                            canvas.repaint();
                            return;
                        }
                    }
                }
            }

            // 2. Normal drawing init
            int x = Math.round((float)(e.getX() - ox) / zoom);
            int y = Math.round((float)(e.getY() - oy) / zoom);
            
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
        if (activeHandleIndex == 3 && !draggingPairs.isEmpty()) {
            boolean moved = false;
            java.util.List<core.history.BatchLineCommand.LineStatePair> finalPairs = new java.util.ArrayList<>();
            for (core.history.BatchLineCommand.LineStatePair pair : draggingPairs) {
                if (!pair.line.startPoint.equals(pair.oldState.startPoint) ||
                    !pair.line.endPoint.equals(pair.oldState.endPoint)) {
                    moved = true;
                }
                finalPairs.add(new core.history.BatchLineCommand.LineStatePair(pair.line, pair.oldState, pair.line.copy()));
            }
            if (moved) {
                core.history.BatchLineCommand cmd = new core.history.BatchLineCommand(
                        appState.getCanvasState(),
                        canvas,
                        core.history.BatchLineCommand.Action.EDIT,
                        finalPairs
                );
                appState.getHistoryManager().push(cmd);
            }
            draggingPairs.clear();
            dragStartMouse = null;
            activeHandleIndex = -1;
            canvas.repaint();
            isDrawing = false;
            startPoint = null;
            currentDrag = null;
            return;
        }

        if (draggingLine != null && originalDraggingLineState != null) {
            boolean moved = !draggingLine.startPoint.equals(originalDraggingLineState.startPoint) ||
                            !draggingLine.endPoint.equals(originalDraggingLineState.endPoint);
            if (moved) {
                LineCommand cmd = new LineCommand(
                    appState.getCanvasState(),
                    canvas,
                    draggingLine,
                    LineCommand.Action.EDIT,
                    originalDraggingLineState,
                    draggingLine.copy()
                );
                appState.getHistoryManager().push(cmd);
            }
            draggingLine = null;
            originalDraggingLineState = null;
            activeHandleIndex = -1;
            canvas.repaint();
            return;
        }

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
            } else {
                // A simple click:
                // 1. Hit-test lines with a strict 3px screen-space tolerance (3.0 / zoom)
                float zoom = appState.getCurrentZoom();
                double selectTolerance = 3.0 / zoom;
                SLine closestLine = null;
                double minDistance = Double.MAX_VALUE;

                for (SLine l : appState.getCanvasState().getLines()) {
                    double dist = getDistanceToSegment(startPoint, l.startPoint, l.endPoint);
                    if (dist <= selectTolerance && dist < minDistance) {
                        minDistance = dist;
                        closestLine = l;
                    }
                }

                if (closestLine != null) {
                    appState.getCanvasState().setSelectedGrid(null);
                    appState.getCanvasState().clearPointSelection();
                    appState.getCanvasState().setSelectedLine(closestLine);
                } else {
                    appState.getCanvasState().setSelectedLine(null);
                }
            }
        }
        isDrawing = false;
        startPoint = null;
        currentDrag = null;
        canvas.repaint();
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
        float zoom = appState.getCurrentZoom();
        int ox = appState.getCanvasState().getImageOffsetX();
        int oy = appState.getCanvasState().getImageOffsetY();

        if (activeHandleIndex == 3 && dragStartMouse != null && !draggingPairs.isEmpty()) {
            int mouseX = Math.round((float)(e.getX() - ox) / zoom);
            int mouseY = Math.round((float)(e.getY() - oy) / zoom);

            java.awt.image.BufferedImage img = canvas.getBackgroundImage();
            if (img != null) {
                mouseX = Math.max(0, Math.min(img.getWidth() - 1, mouseX));
                mouseY = Math.max(0, Math.min(img.getHeight() - 1, mouseY));
            }

            int dx = mouseX - dragStartMouse.x;
            int dy = mouseY - dragStartMouse.y;

            for (core.history.BatchLineCommand.LineStatePair pair : draggingPairs) {
                pair.line.startPoint.setLocation(pair.oldState.startPoint.x + dx, pair.oldState.startPoint.y + dy);
                pair.line.endPoint.setLocation(pair.oldState.endPoint.x + dx, pair.oldState.endPoint.y + dy);
            }
            canvas.repaint();
            return;
        }

        if (draggingLine != null && activeHandleIndex != -1) {
            int dragX = Math.round((float)(e.getX() - ox) / zoom);
            int dragY = Math.round((float)(e.getY() - oy) / zoom);

            java.awt.image.BufferedImage img = canvas.getBackgroundImage();
            if (img != null) {
                dragX = Math.max(0, Math.min(img.getWidth() - 1, dragX));
                dragY = Math.max(0, Math.min(img.getHeight() - 1, dragY));
            }

            if (activeHandleIndex == 0) {
                draggingLine.startPoint.setLocation(dragX, dragY);
            } else if (activeHandleIndex == 1) {
                draggingLine.endPoint.setLocation(dragX, dragY);
            } else if (activeHandleIndex == 2) {
                int newStartX = dragX + startPointOffset.x;
                int newStartY = dragY + startPointOffset.y;
                int newEndX = dragX + endPointOffset.x;
                int newEndY = dragY + endPointOffset.y;

                if (img != null) {
                    int w = img.getWidth() - 1;
                    int h = img.getHeight() - 1;
                    if (newStartX >= 0 && newStartX <= w &&
                        newStartY >= 0 && newStartY <= h &&
                        newEndX >= 0 && newEndX <= w &&
                        newEndY >= 0 && newEndY <= h) {
                        draggingLine.startPoint.setLocation(newStartX, newStartY);
                        draggingLine.endPoint.setLocation(newEndX, newEndY);
                    }
                } else {
                    draggingLine.startPoint.setLocation(newStartX, newStartY);
                    draggingLine.endPoint.setLocation(newEndX, newEndY);
                }
            }
            canvas.repaint();
            return;
        }

        if (isDrawing && currentDrag != null) {
            int dragX = Math.round((float)(e.getX() - ox) / zoom);
            int dragY = Math.round((float)(e.getY() - oy) / zoom);

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
            Object oldAntialias = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            Object oldStrokeControl = g2d.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            g2d.setColor(appState.getBrushColor());
            g2d.setStroke(new BasicStroke(appState.getActiveLineStrokeWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(startPoint.x, startPoint.y, currentDrag.x, currentDrag.y);

            if (oldAntialias != null) {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialias);
            }
            if (oldStrokeControl != null) {
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, oldStrokeControl);
            }
        }
    }

    // Perpendicular segment distance utility
    private double getDistanceToSegment(Point p, Point a, Point b) {
        double l2 = a.distanceSq(b);
        if (l2 == 0) return p.distance(a);
        double t = ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2;
        t = Math.max(0, Math.min(1, t));
        double projX = a.x + t * (b.x - a.x);
        double projY = a.y + t * (b.y - a.y);
        return p.distance(projX, projY);
    }
}
