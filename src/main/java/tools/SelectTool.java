package tools;

import core.state.AppState;
import ui.canvas.ImageCanvas;
import ui.canvas.RenderUtils;
import userpackage.SPoint;

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

    @Override
    public void onMouseMoved(MouseEvent e, AppState appState, ImageCanvas canvas) {}

    @Override
    public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            dragStart   = e.getPoint();
            dragCurrent = e.getPoint();
            isDragging  = false;
        }
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
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
        appState.getCanvasState().setSelectedPoints(matched);
        canvas.repaint();
    }

    // ------------------------------------------------------------------
    // CLICK RELEASE: single/shift/ctrl click
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

        // ---- Hit-test sticky points ----
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
                // Ctrl+click: bỏ chọn point đã selected (chỉ tác dụng nếu trong selection)
                for (SPoint p : pointCandidates) {
                    if (currentSelected.contains(p)) {
                        appState.getCanvasState().removeFromSelection(p);
                    }
                }
                // Không deselect grid
            } else if (isShift) {
                // Shift+click: thêm point chưa selected vào danh sách
                appState.getCanvasState().setSelectedGrid(null);
                for (SPoint p : pointCandidates) {
                    if (!currentSelected.contains(p)) {
                        appState.getCanvasState().addToSelection(p);
                    }
                }
            } else {
                // Click thường: single-select với cycle logic
                SPoint toSelect;
                if (pointCandidates.size() > 1) {
                    // Nếu có nhiều candidates, ưu tiên point chưa được selected
                    List<SPoint> unselected = new ArrayList<>();
                    for (SPoint p : pointCandidates) {
                        if (!currentSelected.contains(p)) unselected.add(p);
                    }
                    if (!unselected.isEmpty()) {
                        toSelect = unselected.get(random.nextInt(unselected.size()));
                    } else {
                        // Tất cả đã selected: cycle sang point khác
                        SPoint last = appState.getCanvasState().getSelectedPoint();
                        List<SPoint> others = new ArrayList<>(pointCandidates);
                        others.remove(last);
                        toSelect = others.get(random.nextInt(others.size()));
                    }
                } else {
                    toSelect = pointCandidates.get(0);
                }
                appState.getCanvasState().setSelectedGrid(null);
                appState.getCanvasState().setSelectedPoint(toSelect); // single-select (replaces set)
            }
            canvas.repaint();
            return;
        }

        // ---- If no point hit: handle grid or deselect ----
        if (isShift || isCtrl) {
            // Shift/Ctrl on empty area — không deselect
            canvas.repaint();
            return;
        }

        // ---- Hit-test grids ----
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
            appState.getCanvasState().setSelectedGrid(toSelect);
        } else {
            // Click vào vùng trống — deselect tất cả
            appState.getCanvasState().setSelectedGrid(null);
            appState.getCanvasState().clearPointSelection();
        }

        canvas.repaint();
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
        // Vẽ drag rectangle preview (trong screen coords)
        if (isDragging && dragStart != null && dragCurrent != null) {
            // g2d tại đây đã được transform (translate+scale). Cần vẽ ở screen coords.
            // Ta không có quyền truy cập g2d gốc, nên ta dùng g2d hiện tại nhưng cần lưu ý
            // rằng g2d trong onPaint đã được transform theo image coords.
            // Để vẽ trong screen coords, ta restore transform tạm thời.
            // Thực tế onPaint nhận g2d đã được apply zoom+offset của ImageCanvas.
            // Ta vẽ rect dưới dạng image coords bằng cách convert lại.
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

    // Simple 2D rect helper (avoid java.awt.geom import issues)
    private static class Rectangle2D {
        final float x, y, w, h;
        Rectangle2D(float x, float y, float w, float h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        boolean contains(float px, float py) { return px >= x && px <= x + w && py >= y && py <= y + h; }
    }
}
