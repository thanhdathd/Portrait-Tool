package tools;

import core.history.LineCommand;
import core.state.AppState;
import ui.canvas.ImageCanvas;
import ui.canvas.RenderUtils;
import userpackage.SPoint;
import userpackage.SLine;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class SelectTool implements Tool {

    private static final int SELECT_TOLERANCE_PX = 10; // Pixels on screen
    private static final int DRAG_THRESHOLD_PX   = 4;  // Min drag distance to trigger rect selection

    private final Random random = new Random();

    // Drag rectangle state (screen coords)
    private Point dragStart   = null;
    private Point dragCurrent = null;
    private boolean isDragging = false;

    // Handle interaction state (image coords)
    private int activeHandleIndex = -1; // -1: none, 0: start, 1: end, 2: center
    private SLine draggingLine = null;
    private SLine originalDraggingLineState = null;
    private Point startPointOffset = null;
    private Point endPointOffset = null;

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

    @Override
    public void onMouseMoved(MouseEvent e, AppState appState, ImageCanvas canvas) {
        // Cursor feedback when hovering active selected line handles
        SLine selectedLine = appState.getCanvasState().getSelectedLine();
        if (selectedLine != null) {
            float zoom = appState.getCurrentZoom();
            int ox = appState.getCanvasState().getImageOffsetX();
            int oy = appState.getCanvasState().getImageOffsetY();

            Point mouseLoc = new Point(
                Math.round((float)(e.getX() - ox) / zoom),
                Math.round((float)(e.getY() - oy) / zoom)
            );

            Point midPoint = new Point(
                (selectedLine.startPoint.x + selectedLine.endPoint.x) / 2,
                (selectedLine.startPoint.y + selectedLine.endPoint.y) / 2
            );

            double screenRadius = 15.0 / zoom; // 30px active zone

            if (mouseLoc.distance(selectedLine.startPoint) <= screenRadius ||
                mouseLoc.distance(selectedLine.endPoint) <= screenRadius ||
                mouseLoc.distance(midPoint) <= screenRadius) {
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
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

            // 1. Check handles on active selected line
            SLine selectedLine = appState.getCanvasState().getSelectedLine();
            if (selectedLine != null) {
                Point pMouse = new Point(
                    Math.round((float)(e.getX() - ox) / zoom),
                    Math.round((float)(e.getY() - oy) / zoom)
                );

                Point midPoint = new Point(
                    (selectedLine.startPoint.x + selectedLine.endPoint.x) / 2,
                    (selectedLine.startPoint.y + selectedLine.endPoint.y) / 2
                );

                double screenRadius = 15.0 / zoom; // 30px grab zone diameter on screen

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
            }

            // Normal drag init
            dragStart   = e.getPoint();
            dragCurrent = e.getPoint();
            isDragging  = false;
        }
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
        float zoom = appState.getCurrentZoom();
        int ox = appState.getCanvasState().getImageOffsetX();
        int oy = appState.getCanvasState().getImageOffsetY();

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

        if (dragStart == null) return;
        dragCurrent = e.getPoint();
        double dist = dragStart.distance(dragCurrent);
        if (dist >= DRAG_THRESHOLD_PX) {
            isDragging = true;
        }
        if (isDragging) canvas.repaint();
    }

    @Override
    public void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas) {
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

        if (e.getButton() != MouseEvent.BUTTON1) return;

        if (isDragging && dragStart != null && dragCurrent != null) {
            handleDragRelease(e, appState, canvas);
        } else {
            handleClickRelease(e, appState, canvas);
        }

        // Reset drag state
        dragStart   = null;
        dragCurrent = null;
        isDragging  = false;
    }

    // ------------------------------------------------------------------
    // DRAG RELEASE: rectangle selection
    // ------------------------------------------------------------------
    private void handleDragRelease(MouseEvent e, AppState appState, ImageCanvas canvas) {
        int offsetX = appState.getCanvasState().getImageOffsetX();
        int offsetY = appState.getCanvasState().getImageOffsetY();
        float zoom  = appState.getCurrentZoom();

        // Build selection rect in image coords
        int sx = Math.min(dragStart.x, dragCurrent.x);
        int sy = Math.min(dragStart.y, dragCurrent.y);
        int sw = Math.abs(dragCurrent.x - dragStart.x);
        int sh = Math.abs(dragCurrent.y - dragStart.y);

        float ix1 = (sx - offsetX) / zoom;
        float iy1 = (sy - offsetY) / zoom;
        float ix2 = ((sx + sw) - offsetX) / zoom;
        float iy2 = ((sy + sh) - offsetY) / zoom;

        Rectangle2D selRect = new Rectangle2D(ix1, iy1, ix2 - ix1, iy2 - iy1);

        // Find all points inside rect
        LinkedHashSet<SPoint> matched = new LinkedHashSet<>();
        for (SPoint p : appState.getCanvasState().getStickyPoints()) {
            if (selRect.contains(p.X, p.Y)) {
                matched.add(p);
            }
        }

        appState.getCanvasState().setSelectedGrid(null);
        appState.getCanvasState().setSelectedLine(null);
        appState.getCanvasState().setSelectedPoints(matched);
        canvas.repaint();
    }

    // ------------------------------------------------------------------
    // CLICK RELEASE: single/shift/ctrl click (Priority: Point > Line > Grid)
    // ------------------------------------------------------------------
    private void handleClickRelease(MouseEvent e, AppState appState, ImageCanvas canvas) {
        boolean isShift = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
        boolean isCtrl  = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK)  != 0;

        int cx = e.getX(), cy = e.getY();
        int offsetX = appState.getCanvasState().getImageOffsetX();
        int offsetY = appState.getCanvasState().getImageOffsetY();
        float zoom  = appState.getCurrentZoom();

        float ix = (cx - offsetX) / zoom;
        float iy = (cy - offsetY) / zoom;

        // ---- 1. Hit-test sticky points (Highest Priority) ----
        Font labelFont = new Font("SansSerif", Font.BOLD, 12);
        FontMetrics fm = canvas.getFontMetrics(labelFont);

        List<SPoint> pointProximity = new ArrayList<>();
        List<SPoint> labelHit       = new ArrayList<>();

        for (SPoint p : appState.getCanvasState().getStickyPoints()) {
            float dx = (p.X - ix) * zoom;
            float dy = (p.Y - iy) * zoom;
            float distScreen = (float) Math.sqrt(dx * dx + dy * dy);
            if (distScreen <= SELECT_TOLERANCE_PX) {
                pointProximity.add(p);
                continue;
            }
            Rectangle labelBounds = RenderUtils.getLabelBounds(p, fm);
            if (labelBounds.contains(ix, iy)) {
                labelHit.add(p);
            }
        }

        List<SPoint> pointCandidates = new ArrayList<>(pointProximity);
        for (SPoint p : labelHit) {
            if (!pointCandidates.contains(p)) pointCandidates.add(p);
        }

        if (!pointCandidates.isEmpty()) {
            Set<SPoint> currentSelected = appState.getCanvasState().getSelectedPoints();

            if (isCtrl) {
                for (SPoint p : pointCandidates) {
                    if (currentSelected.contains(p)) {
                        appState.getCanvasState().removeFromSelection(p);
                    }
                }
            } else if (isShift) {
                appState.getCanvasState().setSelectedGrid(null);
                appState.getCanvasState().setSelectedLine(null);
                for (SPoint p : pointCandidates) {
                    if (!currentSelected.contains(p)) {
                        appState.getCanvasState().addToSelection(p);
                    }
                }
            } else {
                SPoint toSelect;
                if (pointCandidates.size() > 1) {
                    List<SPoint> unselected = new ArrayList<>();
                    for (SPoint p : pointCandidates) {
                        if (!currentSelected.contains(p)) unselected.add(p);
                    }
                    if (!unselected.isEmpty()) {
                        toSelect = unselected.get(random.nextInt(unselected.size()));
                    } else {
                        SPoint last = appState.getCanvasState().getSelectedPoint();
                        List<SPoint> others = new ArrayList<>(pointCandidates);
                        others.remove(last);
                        toSelect = others.get(random.nextInt(others.size()));
                    }
                } else {
                    toSelect = pointCandidates.get(0);
                }
                appState.getCanvasState().setSelectedGrid(null);
                appState.getCanvasState().setSelectedLine(null);
                appState.getCanvasState().setSelectedPoint(toSelect);
            }
            canvas.repaint();
            return;
        }

        // ---- 2. Hit-test lines (Medium Priority) ----
        Point clickLoc = new Point(Math.round(ix), Math.round(iy));
        List<SLine> lineCandidates = new ArrayList<>();
        double selectTolerance = 10.0 / zoom; // 10px screen space tolerance

        for (SLine l : appState.getCanvasState().getLines()) {
            if (getDistanceToSegment(clickLoc, l.startPoint, l.endPoint) <= selectTolerance) {
                lineCandidates.add(l);
            }
        }

        if (!lineCandidates.isEmpty()) {
            SLine currentSelectedLine = appState.getCanvasState().getSelectedLine();
            SLine toSelect;
            if (lineCandidates.size() > 1 && lineCandidates.contains(currentSelectedLine)) {
                List<SLine> others = new ArrayList<>(lineCandidates);
                others.remove(currentSelectedLine);
                toSelect = others.get(random.nextInt(others.size()));
            } else {
                toSelect = lineCandidates.get(random.nextInt(lineCandidates.size()));
            }

            appState.getCanvasState().setSelectedGrid(null);
            appState.getCanvasState().clearPointSelection();
            appState.getCanvasState().setSelectedLine(toSelect);
            canvas.repaint();
            return;
        }

        // Shift/Ctrl on empty area - don't clear selections
        if (isShift || isCtrl) {
            canvas.repaint();
            return;
        }

        // ---- 3. Hit-test grids (Lowest Priority) ----
        List<SPoint> gridCandidates = new ArrayList<>();
        SPoint currentSelectedGrid  = appState.getCanvasState().getSelectedGrid();

        for (SPoint grid : appState.getCanvasState().getGrids()) {
            int S = grid.id;
            if (S <= 0) continue;

            float diffX = Math.abs(ix - grid.X);
            float remX  = diffX % S;
            float distScreenX = Math.min(remX, S - remX) * zoom;

            float diffY = Math.abs(iy - grid.Y);
            float remY  = diffY % S;
            float distScreenY = Math.min(remY, S - remY) * zoom;

            if (distScreenX <= SELECT_TOLERANCE_PX || distScreenY <= SELECT_TOLERANCE_PX) {
                gridCandidates.add(grid);
            }
        }

        if (!gridCandidates.isEmpty()) {
            SPoint toSelect;
            if (gridCandidates.size() > 1 && gridCandidates.contains(currentSelectedGrid)) {
                List<SPoint> others = new ArrayList<>(gridCandidates);
                others.remove(currentSelectedGrid);
                toSelect = others.get(random.nextInt(others.size()));
            } else {
                toSelect = gridCandidates.get(random.nextInt(gridCandidates.size()));
            }
            appState.getCanvasState().clearPointSelection();
            appState.getCanvasState().setSelectedLine(null);
            appState.getCanvasState().setSelectedGrid(toSelect);
        } else {
            // Click on empty canvas - clear everything
            appState.getCanvasState().setSelectedGrid(null);
            appState.getCanvasState().setSelectedLine(null);
            appState.getCanvasState().clearPointSelection();
        }

        canvas.repaint();
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
        // Draw drag rectangle preview
        if (isDragging && dragStart != null && dragCurrent != null) {
            int offsetX = appState.getCanvasState().getImageOffsetX();
            int offsetY = appState.getCanvasState().getImageOffsetY();
            float zoom  = appState.getCurrentZoom();

            float ix1 = (Math.min(dragStart.x, dragCurrent.x) - offsetX) / zoom;
            float iy1 = (Math.min(dragStart.y, dragCurrent.y) - offsetY) / zoom;
            float iw  = Math.abs(dragCurrent.x - dragStart.x) / zoom;
            float ih  = Math.abs(dragCurrent.y - dragStart.y) / zoom;

            Stroke old = g2d.getStroke();
            g2d.setStroke(new BasicStroke(0.5f / zoom, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{3f / zoom, 3f / zoom}, 0));
            g2d.setColor(new Color(0, 120, 215, 180));
            g2d.drawRect((int) ix1, (int) iy1, (int) iw, (int) ih);
            g2d.setColor(new Color(0, 120, 215, 30));
            g2d.fillRect((int) ix1, (int) iy1, (int) iw, (int) ih);
            g2d.setStroke(old);
        }
    }

    // Simple 2D rect helper
    private static class Rectangle2D {
        final float x, y, w, h;
        Rectangle2D(float x, float y, float w, float h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        boolean contains(float px, float py) { return px >= x && px <= x + w && py >= y && py <= y + h; }
    }
}
