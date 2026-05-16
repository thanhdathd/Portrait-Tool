package tools;

import core.history.GridCommand;
import core.state.AppState;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

import java.awt.*;
import java.awt.event.MouseEvent;

public class GridTool implements Tool {

    // Khoảng cách bắt dính (tính bằng pixel vật lý trên màn hình)
    private static final int SNAP_THRESHOLD_SCREEN_PX = 15;

    // Lưu trữ trạng thái chuột để phục vụ việc vẽ Preview và Snapping
    private Point rawImagePos = null;
    private Point snappedImagePos = null;

    // Helper: Tính toán grid size ra pixel một lần để dùng chung
    private int getGridPixelSize(AppState appState) {
        if (appState.isGridInCm()) {
            float scale = appState.getScale();
            return (scale > 0) ? Math.round(appState.getGridSize() / scale) : Math.round(appState.getGridSize());
        }
        return Math.round(appState.getGridSize());
    }

    @Override
    public void onMouseMoved(MouseEvent e, AppState appState, ImageCanvas canvas) {
        processMouseSnapping(e, appState, canvas);
    }

    @Override
    public void onMouseDragged(MouseEvent e, AppState appState, ImageCanvas canvas) {
        processMouseSnapping(e, appState, canvas);
    }

    // Logic tính toán tọa độ và bắt dính (Snapping)
    private void processMouseSnapping(MouseEvent e, AppState appState, ImageCanvas canvas) {
        int cx = e.getX();
        int cy = e.getY();
        int offsetX = appState.getCanvasState().getImageOffsetX();
        int offsetY = appState.getCanvasState().getImageOffsetY();
        float zoom = appState.getCurrentZoom();

        // 1. Chuyển đổi tọa độ chuột sang tọa độ ảnh gốc
        float ix = (cx - offsetX) / zoom;
        float iy = (cy - offsetY) / zoom;
        rawImagePos = new Point(Math.round(ix), Math.round(iy));

        // 2. Logic Snapping
        int S = getGridPixelSize(appState);
        if (S > 0) {
            // Tìm điểm đặc biệt gần nhất (bội số của Grid Size)
            int targetIX = Math.round(ix / S) * S;
            int targetIY = Math.round(iy / S) * S;

            // Tính khoảng cách từ chuột đến điểm đặc biệt trên MÀN HÌNH
            float screenDistX = Math.abs(ix - targetIX) * zoom;
            float screenDistY = Math.abs(iy - targetIY) * zoom;

            // Nếu nằm trong vùng "từ tính", kích hoạt bắt dính cả 2 trục
            if (screenDistX <= SNAP_THRESHOLD_SCREEN_PX && screenDistY <= SNAP_THRESHOLD_SCREEN_PX) {
                snappedImagePos = new Point(targetIX, targetIY);
            } else {
                snappedImagePos = null; // Thả snap
            }
        } else {
            snappedImagePos = null;
        }

        // Yêu cầu Canvas vẽ lại để hiển thị Guide lines
        canvas.repaint();
    }

    @Override
    public void onMousePressed(MouseEvent e, AppState appState, ImageCanvas canvas) {
        // Grid placement relies on mouse release
    }

    @Override
    public void onMouseReleased(MouseEvent e, AppState appState, ImageCanvas canvas) {
        if (e.getButton() == MouseEvent.BUTTON1 && rawImagePos != null) {
            int pixelSize = getGridPixelSize(appState);

            // Ưu tiên sử dụng điểm Snapped, nếu không có thì lấy tọa độ chuột tự do
            int finalX = (snappedImagePos != null) ? snappedImagePos.x : rawImagePos.x;
            int finalY = (snappedImagePos != null) ? snappedImagePos.y : rawImagePos.y;
            
            // Ngăn chặn việc vẽ đè (duplicate) grid tại cùng một vị trí với cùng thông số
            for (SPoint existing : appState.getCanvasState().getGrids()) {
                if (existing.X == finalX && existing.Y == finalY && existing.id == pixelSize) {
                    return; // Đã có grid tại đây, bỏ qua để tránh click đúp sinh ra nhiều grid chồng lên nhau
                }
            }

            SPoint gridData = new SPoint(pixelSize, finalX, finalY, appState.getBrushColor());
            GridCommand command = new GridCommand(appState.getCanvasState(), canvas, gridData);
            appState.getHistoryManager().push(command);
            canvas.repaint();
        }
    }

    @Override
    public void onPaint(Graphics2D g2d, AppState appState, ImageCanvas canvas) {
        java.awt.image.BufferedImage img = canvas.getBackgroundImage();
        if (img == null) return;

        // Lưu trạng thái
        java.awt.geom.AffineTransform oldAt = g2d.getTransform();
        java.awt.Color oldColor = g2d.getColor();
        java.awt.Font oldFont = g2d.getFont();
        Stroke oldStroke = g2d.getStroke();

        // Khử Transform để vẽ pixel chính xác 1:1 lên Panel
        float zoom = appState.getCurrentZoom();
        int offsetX = appState.getCanvasState().getImageOffsetX();
        int offsetY = appState.getCanvasState().getImageOffsetY();

        g2d.scale(1.0 / zoom, 1.0 / zoom);
        g2d.translate(-offsetX, -offsetY);

        // ==========================================
        // 1. VẼ SNAP GUIDE LINES (CHỮ THẬP VÀ GUIDE PHỤ)
        // ==========================================
        if (snappedImagePos != null) {
            // Lấy kích thước lưới theo pixel ảnh gốc và quy đổi ra pixel màn hình
            int S = getGridPixelSize(appState);
            int screenS = Math.round(S * zoom);

            // Đổi tọa độ điểm snap sang tọa độ màn hình
            int screenSnapX = Math.round(offsetX + snappedImagePos.x * zoom);
            int screenSnapY = Math.round(offsetY + snappedImagePos.y * zoom);

            // Giới hạn của bức ảnh trên màn hình
            int imgLeft = offsetX;
            int imgTop = offsetY;
            int imgRight = Math.round(offsetX + img.getWidth() * zoom);
            int imgBottom = Math.round(offsetY + img.getHeight() * zoom);

            // CẮT VÙNG VẼ (Clipping): Đảm bảo các đường guide không bao giờ tràn ra ngoài mép ảnh
            Shape oldClip = g2d.getClip();
            g2d.clipRect(imgLeft, imgTop, imgRight - imgLeft, imgBottom - imgTop);

            // Setup nét vẽ đứt (Dashed line)
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
            g2d.setStroke(dashed);

            // ----------------------------------------------------
            // A. VẼ ĐƯỜNG GUIDE CHÍNH (XOR Mode - Nổi bật 100%)
            // ----------------------------------------------------
            g2d.setXORMode(Color.BLACK);
            g2d.setColor(Color.CYAN);
            // Trục dọc chính
            g2d.drawLine(screenSnapX, imgTop, screenSnapX, imgBottom);
            // Trục ngang chính
            g2d.drawLine(imgLeft, screenSnapY, imgRight, screenSnapY);

            // ----------------------------------------------------
            // B. VẼ ĐƯỜNG GUIDE THỨ CẤP (Paint Mode - Opacity 0.5)
            // ----------------------------------------------------
            g2d.setPaintMode(); // BẮT BUỘC tắt XOR để vẽ được độ trong suốt (Alpha)

            // Màu Cyan với Alpha = 127 (~50% Opacity)
            Color semiTransparentCyan = new Color(0, 255, 255, 80);
            g2d.setColor(semiTransparentCyan);

            // 2 đường phụ dọc (Trái và Phải)
            g2d.drawLine(screenSnapX - screenS, imgTop, screenSnapX - screenS, imgBottom);
            g2d.drawLine(screenSnapX + screenS, imgTop, screenSnapX + screenS, imgBottom);

            // 2 đường phụ ngang (Trên và Dưới)
            g2d.drawLine(imgLeft, screenSnapY - screenS, imgRight, screenSnapY - screenS);
            g2d.drawLine(imgLeft, screenSnapY + screenS, imgRight, screenSnapY + screenS);

            // Phục hồi lại vùng Clip gốc để không ảnh hưởng các hàm vẽ phía sau (ví dụ vẽ Label)
            g2d.setClip(oldClip);
        }

        // ==========================================
        // 2. VẼ LABEL GRID SIZE VÀO GÓC VIEWPORT
        // ==========================================
        java.awt.Rectangle visibleRect = canvas.getVisibleRect();
        int imgRight = Math.round(offsetX + (img.getWidth() * zoom));
        int imgTop = offsetY;
        int viewRight = visibleRect.x + visibleRect.width;
        int viewTop = visibleRect.y;

        int anchorX = Math.min(imgRight, viewRight);
        int anchorY = Math.max(imgTop, viewTop);

        float gridSizeCm = appState.isGridInCm() ? appState.getGridSize() : (appState.getGridSize() * appState.getScale());
        String text = String.format("Grid size: %.2f cm", gridSizeCm);

        g2d.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textAscent = fm.getAscent();

        int drawX = anchorX - textWidth - 15;
        int drawY = anchorY + textAscent + 15;

        g2d.setXORMode(java.awt.Color.BLACK);
        g2d.setColor(java.awt.Color.YELLOW);
        g2d.drawString(text, drawX, drawY);

        // Phục hồi trạng thái
        g2d.setPaintMode();
        g2d.setStroke(oldStroke);
        g2d.setColor(oldColor);
        g2d.setFont(oldFont);
        g2d.setTransform(oldAt);
    }
}
