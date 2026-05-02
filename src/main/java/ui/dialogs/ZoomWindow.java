package ui.dialogs;

import core.state.AppState;
import ui.canvas.RenderUtils;
import user.Enum.Direction;
import userpackage.SPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class ZoomWindow extends JDialog {

    private final AppState appState;
    private BufferedImage currentImage;
    
    private Point mousePosition = new Point(0, 0);
    private float zoomRate = 2.0f;

    // Measurement caliper state
    private boolean showCross = true;
    private boolean showRuler = false;
    private boolean measuring = false;
    private int xDistance = -35;
    private int yDistance = 35;
    private int rulerUnitPixels = 35; // Previously rA

    private final ZoomPanel zoomPanel; // Panel chuyên trách việc vẽ

    public ZoomWindow(Frame owner, AppState appState) {
        super(owner, false);
        this.appState = appState;

        this.zoomPanel = new ZoomPanel();
        this.add(zoomPanel);

        Rectangle savedBounds = appState.getZoomWindowBounds();
        if (savedBounds != null) {
            this.setBounds(savedBounds);
        } else {
            setSize(400, 400);
            setLocationRelativeTo(owner);
        }
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setFocusableWindowState(false);

        updateTitle();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                appState.setZoomWindowBounds(getBounds());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                appState.setZoomWindowBounds(getBounds());
            }
        });
    }

    private void updateTitle() {
        // Hiển thị phần trăm zoom (ví dụ: "Zoom: 200%")
        setTitle(String.format("Zoom:%.0f%%          X:%d, Y:%d", zoomRate * 100, mousePosition.x, mousePosition.y));
    }
    
    public void updateImage(BufferedImage image) {
        this.currentImage = image;
        updateTitle();
        zoomPanel.repaint();
    }

    public void updateMousePosition(Point mousePosition) {
        this.mousePosition = mousePosition;
        updateTitle();
        zoomPanel.repaint();
    }


    // Inner class quản lý việc vẽ chất lượng cao
    private class ZoomPanel extends JPanel {
        public ZoomPanel() {
            setDoubleBuffered(true); // Bật đệm đúp chống nhấp nháy [cite: 37]
            setBackground(Color.DARK_GRAY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Tự động xóa nền sạch sẽ
            if (currentImage == null || mousePosition == null) return;


            Graphics2D g2 = (Graphics2D) g;
            // đảm bảo pixel accurate với NEAREST_NEIGHBOR
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            int viewW = getWidth();
            int viewH = getHeight();
            int cx = viewW / 2;
            int cy = viewH / 2;

            // TÍNH TOÁN VÙNG NGUỒN (Source): Phần ảnh gốc cần lấy
            int sourceW = (int) (viewW / zoomRate);
            int sourceH = (int) (viewH / zoomRate);
            int sx1 = mousePosition.x - sourceW / 2;
            int sy1 = mousePosition.y - sourceH / 2;
            int sx2 = sx1 + sourceW;
            int sy2 = sy1 + sourceH;

            // VẼ TRỰC TIẾP (Sub-image rendering):
            // Vẽ từ vùng (sx1, sy1, sx2, sy2) của ảnh gốc
            // vào toàn bộ diện tích (0, 0, viewW, viewH) của panel
            g2.drawImage(currentImage,
                    0, 0, viewW, viewH,
                    sx1, sy1, sx2, sy2,
                    null);

            // draw data layer
            // Lưu trạng thái transform cũ
            java.awt.geom.AffineTransform oldAt = g2.getTransform();

            // Dịch chuyển hệ tọa độ về tâm ZoomWindow và phóng đại theo zoomRate
            g2.translate(viewW / 2.0, viewH / 2.0);
            g2.scale(zoomRate, zoomRate);
            // Trừ đi tọa độ chuột để đưa hệ tọa độ về (0,0) của ảnh gốc
            g2.translate(-mousePosition.x, -mousePosition.y);

            // Gọi logic vẽ dùng chung - Lúc này các điểm p.X, p.Y sẽ tự động khớp pixel ảnh
            RenderUtils.drawStickyPoints(g2, appState.getCanvasState().getStickyPoints(), true);

            RenderUtils.drawGrids(g2, zoomRate, appState, currentImage.getWidth(), currentImage.getHeight());
            // draw label placement indicator
            if (appState.isCustomLabelMode()) {
                drawLabelRadarIndicator(mousePosition, g2);
            }
            // Khôi phục transform để vẽ Crosshair hoặc Ruler không bị phóng đại
            g2.setTransform(oldAt);

            // Vẽ các Overlay (Cross, Ruler) lên trên
            if (showCross && !showRuler) drawCross(g2, cx, cy, zoomRate);
//            if (showRuler) drawRuler(g2, cx, cy, viewW, viewH);
        }

        private void drawLabelRadarIndicator(Point mousePosition, Graphics2D g2) {
            int cx = mousePosition.x;
            int cy = mousePosition.y;
            int gap = appState.getCustomGap();
            int angle = appState.getCustomAngle();

            g2.setXORMode(Color.YELLOW); // Dùng màu nổi bật cho Radar
            // Thủ thuật: Giữ nét vẽ luôn mỏng 1 pixel trên màn hình bất chấp độ Zoom
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(1.0f / zoomRate));

            // 1. Vẽ vòng tròn Gap
            Ellipse2D.Double circle = new Ellipse2D.Double(
                    cx - gap, cy - gap, gap * 2, gap * 2);
            g2.draw(circle);

            // 2. Tính toán điểm lp (Lượng giác)
            // Lưu ý: Trục Y của Java đi xuống, nên CCW (Ngược chiều KĐH) thì Y phải trừ đi Sin
            double radians = Math.toRadians(angle);
            double lpX_d = cx + gap * Math.cos(radians);
            double lpY_d = cy - gap * Math.sin(radians);

            int lpX = (int) Math.round(lpX_d);
            int lpY = (int) Math.round(lpY_d);

            // Vẽ một đường Line mỏng từ tâm ra điểm lp (như kim radar) để user dễ nhìn
            g2.draw(new Line2D.Double(cx, cy, lpX, lpY));

            Font font = new Font("SansSerif", Font.BOLD, 12);
            g2.setFont(font);

            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            FontMetrics fm = g2.getFontMetrics(font);

            // Nội suy Text
            List<SPoint> points = appState.getCanvasState().getStickyPoints();
            int predictedId = points.isEmpty() ? 1 : points.get(points.size() - 1).id + 1;
            String text = String.valueOf(predictedId);

            // 4. Đo đạc Text
            int textWidth = fm.stringWidth(text);
            int textAscent = fm.getAscent();

            // 5. Tính tọa độ X của Baseline dựa trên hàm dùng chung
            int baselineStartX = RenderUtils.calculateCustomBaselineX(lpX, angle, textWidth);

            // Tọa độ Y của baseline chính là lpY (vì text ngồi ngay trên lp)
            int baselineY = lpY;

            // 6. Vẽ Indicator
            // Vẽ đường Baseline (một gạch chân ngang bên dưới text)
            g2.draw(new Line2D.Double(baselineStartX, lpY, baselineStartX + textWidth, lpY));

            // Vẽ nội dung Text mẫu (vẽ cao hơn baseline 1-2 pixel để không dính vào vạch)
            if(angle >= 213 && angle <= 327) {
                baselineY = lpY + (int)(textAscent*0.9);
            }
            g2.drawString(text, baselineStartX, baselineY - 1);


            g2.setFont(new  Font("SansSerif", Font.PLAIN, 8));
            g2.drawString(angle+"", baselineStartX + textWidth + 20, lpY);

            // Trả lại trạng thái nét vẽ cũ
            g2.setStroke(oldStroke);
            g2.setPaintMode();
        }
    }

    // --- Các hàm hỗ trợ vẽ giữ nguyên logic của bạn nhưng tối ưu hóa tham số ---
    private void drawCross(Graphics2D g2, int cx, int cy, float zoomRate) {
        g2.setXORMode(Color.WHITE);
        g2.drawOval(cx - 14, cy - 14, 28, 28);
        g2.drawLine(cx, cy - 5, cx, cy - 25);
        g2.drawLine(cx, cy + 5, cx, cy + 25);
        g2.drawLine(cx - 5, cy, cx - 25, cy);
        g2.drawLine(cx + 5, cy, cx + 25, cy);

        // vẽ indicator phụ khi zoomRate đủ lớn
        if(zoomRate > 5.0f) {
            // Tọa độ góc dưới bên phải
            int brX = cx + (int) zoomRate;
            int brY = cy + (int) zoomRate;

            // Tính toán độ dài của nét vẽ phụ (nhỏ hơn cạnh của pixel một chút cho đẹp)
            int lineLength = Math.min(16, (int) (zoomRate / 2.0f));

            // Vẽ đường ngang (từ phải qua trái, hướng vào trong pixel)
            g2.drawLine(brX, brY, brX - lineLength, brY);

            // Vẽ đường dọc (từ dưới lên trên, hướng vào trong pixel)
            g2.drawLine(brX, brY, brX, brY - lineLength);
        } else {
            g2.fillRect(cx, cy, (int)zoomRate, (int)zoomRate);
        }
        // draw direction indicator
        if(!appState.isCustomLabelMode()) {
            switch (appState.getLabelDirection()) {
                case NORTH -> {
                    g2.drawLine(cx + 3, cy - 20, cx + 3, cy - 25);
                }
                case EAST -> {
                    g2.drawLine(cx + 20, cy + 3, cx + 25, cy + 3);
                }
                case SOUTH -> {
                    g2.drawLine(cx + 3, cy + 20, cx + 3, cy + 25);
                }
                case WEST -> {
                    g2.drawLine(cx - 20, cy + 3, cx - 25, cy + 3);
                }
            }
        }
        g2.setPaintMode();
    }
    
    // API to control state from outside
    public void zoomIn() {
        zoomRate *= 1.2f;
        zoomRate = Math.min(330.0f, zoomRate);
        updateTitle();
        repaint();
    }

    public void zoomOut() {
        zoomRate /= 1.2f;
        zoomRate = Math.max(0.08f, zoomRate);
        updateTitle();
        repaint();
    }
}
