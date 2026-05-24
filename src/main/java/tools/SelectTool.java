package tools;

import core.history.LineCommand;
import core.history.StickCommand;
import core.state.AppState;
import ui.canvas.ImageCanvas;
import ui.canvas.RenderUtils;
import core.state.SPoint;
import core.state.SLine;

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

    // Handle drag tracking for batch editing
    private final List<core.history.BatchLineCommand.LineStatePair> draggingPairs = new ArrayList<>();
    private Point dragStartMouse = null;

    // Point dragging state
    private SPoint draggingPoint = null;
    private SPoint originalDraggingPointState = null;

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
        Set<SPoint> selectedPoints = appState.getCanvasState().getSelectedPoints();
        if (selectedPoints.size() == 1) {
            float zoom = appState.getCurrentZoom();
            int ox = appState.getCanvasState().getImageOffsetX();
            int oy = appState.getCanvasState().getImageOffsetY();

            float ix = (e.getX() - ox) / zoom;
            float iy = (e.getY() - oy) / zoom;

            SPoint selectedPoint = selectedPoints.iterator().next();
            float dx = (selectedPoint.X - ix) * zoom;
            float dy = (selectedPoint.Y - iy) * zoom;
            float distScreen = (float) Math.sqrt(dx * dx + dy * dy);

            Font labelFont = new Font("SansSerif", Font.BOLD, 12);
            FontMetrics fm = canvas.getFontMetrics(labelFont);
            Rectangle labelBounds = RenderUtils.getLabelBounds(selectedPoint, fm);

            if (distScreen <= SELECT_TOLERANCE_PX || labelBounds.contains(ix, iy)) {
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }

        Set<SLine> selectedLines = appState.getCanvasState().getSelectedLines();
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

            Set<SPoint> selectedPoints = appState.getCanvasState().getSelectedPoints();
            if (selectedPoints.size() == 1) {
                float ix = (e.getX() - ox) / zoom;
                float iy = (e.getY() - oy) / zoom;
                SPoint selectedPoint = selectedPoints.iterator().next();

                float dx = (selectedPoint.X - ix) * zoom;
                float dy = (selectedPoint.Y - iy) * zoom;
                float distScreen = (float) Math.sqrt(dx * dx + dy * dy);

                Font labelFont = new Font("SansSerif", Font.BOLD, 12);
                FontMetrics fm = canvas.getFontMetrics(labelFont);
                Rectangle labelBounds = RenderUtils.getLabelBounds(selectedPoint, fm);

                if (distScreen <= SELECT_TOLERANCE_PX || labelBounds.contains(ix, iy)) {
                    draggingPoint = selectedPoint;
                    originalDraggingPointState = selectedPoint.copy();
                    canvas.repaint();
                    return;
                }
            }

            Set<SLine> selectedLines = appState.getCanvasState().getSelectedLines();
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

        if (draggingPoint != null) {
            int dragX = Math.round((float)(e.getX() - ox) / zoom);
            int dragY = Math.round((float)(e.getY() - oy) / zoom);

            java.awt.image.BufferedImage img = canvas.getBackgroundImage();
            if (img != null) {
                dragX = Math.max(0, Math.min(img.getWidth() - 1, dragX));
                dragY = Math.max(0, Math.min(img.getHeight() - 1, dragY));
            }

            draggingPoint.X = dragX;
            draggingPoint.Y = dragY;
            appState.getCanvasState().notifyPointSelectionChanged();
            canvas.repaint();
            return;
        }

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
        if (draggingPoint != null && originalDraggingPointState != null) {
            boolean moved = draggingPoint.X != originalDraggingPointState.X ||
                            draggingPoint.Y != originalDraggingPointState.Y;
            if (moved) {
                StickCommand cmd = new StickCommand(
                    appState.getCanvasState(),
                    canvas,
                    draggingPoint,
                    StickCommand.Action.EDIT,
                    originalDraggingPointState,
                    draggingPoint.copy()
                );
                appState.getHistoryManager().push(cmd);
            }
            draggingPoint = null;
            originalDraggingPointState = null;
            canvas.repaint();
            return;
        }

        if (activeHandleIndex == 3 && !draggingPairs.isEmpty()) {
            boolean moved = false;
            List<core.history.BatchLineCommand.LineStatePair> finalPairs = new ArrayList<>();
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
        boolean isShift = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
        boolean isCtrl  = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;

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

        double rx = ix1;
        double ry = iy1;
        double rw = ix2 - ix1;
        double rh = iy2 - iy1;

        Rectangle2D selRect = new Rectangle2D(ix1, iy1, ix2 - ix1, iy2 - iy1);

        // Find all points inside rect
        LinkedHashSet<SPoint> matchedPoints = new LinkedHashSet<>();
        for (SPoint p : appState.getCanvasState().getStickyPoints()) {
            if (selRect.contains(p.X, p.Y)) {
                matchedPoints.add(p);
            }
        }

        // Find all lines intersecting rect
        LinkedHashSet<SLine> matchedLines = new LinkedHashSet<>();
        for (SLine l : appState.getCanvasState().getLines()) {
            if (lineIntersectsRect(l.startPoint, l.endPoint, rx, ry, rw, rh)) {
                matchedLines.add(l);
            }
        }

        appState.getCanvasState().setSelectedGrid(null);

        if (isCtrl) {
            // Toggle selection
            LinkedHashSet<SPoint> newPoints = new LinkedHashSet<>(appState.getCanvasState().getSelectedPoints());
            for (SPoint p : matchedPoints) {
                if (newPoints.contains(p)) newPoints.remove(p);
                else newPoints.add(p);
            }
            appState.getCanvasState().setSelectedPoints(newPoints);

            LinkedHashSet<SLine> newLines = new LinkedHashSet<>(appState.getCanvasState().getSelectedLines());
            for (SLine l : matchedLines) {
                if (newLines.contains(l)) newLines.remove(l);
                else newLines.add(l);
            }
            appState.getCanvasState().setSelectedLines(newLines);
        } else if (isShift) {
            // Add to selection
            LinkedHashSet<SPoint> newPoints = new LinkedHashSet<>(appState.getCanvasState().getSelectedPoints());
            newPoints.addAll(matchedPoints);
            appState.getCanvasState().setSelectedPoints(newPoints);

            LinkedHashSet<SLine> newLines = new LinkedHashSet<>(appState.getCanvasState().getSelectedLines());
            newLines.addAll(matchedLines);
            appState.getCanvasState().setSelectedLines(newLines);
        } else {
            // Replace selection
            if (!matchedPoints.isEmpty()) {
                appState.getCanvasState().clearLineSelection();
                appState.getCanvasState().setSelectedPoints(matchedPoints);
            } else if (!matchedLines.isEmpty()) {
                appState.getCanvasState().clearPointSelection();
                appState.getCanvasState().setSelectedLines(matchedLines);
            } else {
                appState.getCanvasState().clearPointSelection();
                appState.getCanvasState().clearLineSelection();
            }
        }
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
            SLine toSelect = null;
            Set<SLine> currentSelected = appState.getCanvasState().getSelectedLines();
            if (lineCandidates.size() > 1) {
                List<SLine> unselected = new ArrayList<>();
                for (SLine l : lineCandidates) {
                    if (!currentSelected.contains(l)) unselected.add(l);
                }
                if (!unselected.isEmpty()) {
                    toSelect = unselected.get(random.nextInt(unselected.size()));
                } else {
                    SLine last = appState.getCanvasState().getSelectedLine();
                    List<SLine> others = new ArrayList<>(lineCandidates);
                    others.remove(last);
                    if (!others.isEmpty()) {
                        toSelect = others.get(random.nextInt(others.size()));
                    } else {
                        toSelect = last;
                    }
                }
            } else {
                toSelect = lineCandidates.get(0);
            }

            if (isCtrl) {
                LinkedHashSet<SLine> newLines = new LinkedHashSet<>(currentSelected);
                if (newLines.contains(toSelect)) {
                    appState.getCanvasState().removeFromSelection(toSelect);
                } else {
                    appState.getCanvasState().setSelectedGrid(null);
                    appState.getCanvasState().clearPointSelection();
                    appState.getCanvasState().addToSelection(toSelect);
                }
            } else if (isShift) {
                appState.getCanvasState().setSelectedGrid(null);
                appState.getCanvasState().clearPointSelection();
                appState.getCanvasState().addToSelection(toSelect);
            } else {
                appState.getCanvasState().setSelectedGrid(null);
                appState.getCanvasState().clearPointSelection();
                appState.getCanvasState().setSelectedLine(toSelect);
            }
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

    private boolean lineIntersectsRect(Point a, Point b, double rx, double ry, double rw, double rh) {
        if (a.x >= rx && a.x <= rx + rw && a.y >= ry && a.y <= ry + rh) return true;
        if (b.x >= rx && b.x <= rx + rw && b.y >= ry && b.y <= ry + rh) return true;

        java.awt.geom.Line2D.Double segment = new java.awt.geom.Line2D.Double(a.x, a.y, b.x, b.y);
        java.awt.geom.Line2D.Double top = new java.awt.geom.Line2D.Double(rx, ry, rx + rw, ry);
        java.awt.geom.Line2D.Double bottom = new java.awt.geom.Line2D.Double(rx, ry + rh, rx + rw, ry + rh);
        java.awt.geom.Line2D.Double left = new java.awt.geom.Line2D.Double(rx, ry, rx, ry + rh);
        java.awt.geom.Line2D.Double right = new java.awt.geom.Line2D.Double(rx + rw, ry, rx + rw, ry + rh);

        return segment.intersectsLine(top) || segment.intersectsLine(bottom) || 
               segment.intersectsLine(left) || segment.intersectsLine(right);
    }

    // Simple 2D rect helper
    private static class Rectangle2D {
        final float x, y, w, h;
        Rectangle2D(float x, float y, float w, float h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        boolean contains(float px, float py) { return px >= x && px <= x + w && py >= y && py <= y + h; }
    }
}
