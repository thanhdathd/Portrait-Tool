package ui.canvas;

import core.state.AppState;
import user.Enum.Direction;
import userpackage.SPoint;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.List;

public class RenderUtils {
    public static void drawStickyPoints(Graphics2D g2d, List<SPoint> points, boolean drawLabels) {
        // 1. LẤY VÙNG HIỂN THỊ (VIEWPORT CLIP)
        // Rectangle này chính là khung hình chữ nhật trên ảnh gốc đang được show ra
        Rectangle clip = g2d.getClipBounds();

        // Mở rộng vùng clip (Padding) một chút (ví dụ: 50 pixel)
        // Việc này đảm bảo các điểm nằm sát mép không bị cắt cụt mất phần Nhãn (Label)
        if (clip != null) {
            clip.grow(50, 50);
        }

        int pCount = (int)points.stream().filter(p -> p.isCustomPlacement).count();
        int[][] leaderLines = new int[pCount][];
        int index = 0;
        Font font = new Font("SansSerif", Font.BOLD, 12);
        g2d.setFont(font);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (SPoint p : points) {
            // ----------------------------------------------------
            // 2. CULLING: KIỂM TRA ĐIỂM CÓ TRONG VIEWPORT KHÔNG?
            // ----------------------------------------------------
            if (clip != null && !clip.contains(p.X, p.Y)) {
                continue; // Bỏ qua hoàn toàn, không thực hiện bất kỳ phép tính nào
            }

            // --- NẾU ĐIỂM NẰM TRONG VIEWPORT THÌ MỚI VẼ ---

            // Vẽ Dot
            g2d.setColor(p.c);
            g2d.fillRect(p.X, p.Y, 1, 1);

            if(drawLabels) {
                java.awt.geom.AffineTransform dotAt = g2d.getTransform();
                g2d.translate(p.X, p.Y);

                String text = String.valueOf(p.id);
                FontMetrics fm = g2d.getFontMetrics(font);
                int textWidth = fm.stringWidth(text);
                int textAscent = fm.getAscent();

                int gap = 10; // from point to text
                int x = 0, y = 0;

                if(p.isCustomPlacement) {
                    // --- CHẾ ĐỘ CUSTOM RADAR ---
                    double radians = Math.toRadians(p.customAngle);
                    int lpX = (int) Math.round(p.customGap * Math.cos(radians));
                    int lpY = (int) Math.round(-p.customGap * Math.sin(radians)); // Y ngược
                    int bsAdjustY = -2;
                    int bsAdjustX = -1;

                    x = calculateCustomBaselineX(lpX, p.customAngle, textWidth);
                    y = lpY - 1; // Chữ đặt ngay trên điểm lp, nang 1px so voi base line
                    if(p.customAngle >= 213 && p.customAngle <= 327) {
                        y = lpY + (int)(textAscent*0.9) - 1;
                        bsAdjustY = 2;
                    }
                    // baseline text
                    g2d.drawLine(x+bsAdjustX, lpY - bsAdjustY,x+textWidth+bsAdjustX, lpY - bsAdjustY);
                    // calculate coordinate for leader line
                    double offset = 5.0;
                    int anchorX = x+bsAdjustX;
                    int anchorY = lpY-bsAdjustY;
                    int angle = p.customAngle;
                    if(angle >= 100 && angle <= 260) {
                        anchorX = x + textWidth + bsAdjustX;
                    } else if (angle >= 80 && angle <= 280) {
                        anchorX = x + (textWidth + bsAdjustX)/2;
                    }
                    Point stop = calculateStopPoint(anchorX,anchorY, 1, 1, offset);

                    // put data into array to draw later with better rendering hint
                    leaderLines[index] = new int[]{p.c.getRGB(),p.X, p.Y, anchorX, anchorY, stop.x, stop.y};
                    index++;
                } else {
                    if (p.dr == Direction.EAST) {
                        x = gap+2;
                        y = textAscent/2 + 4;
                    } else if (p.dr == Direction.WEST) {
                        x = -textWidth - gap;
                        y = textAscent/2 + 4;
                    } else if (p.dr == Direction.SOUTH) {
                        x = -textWidth/2;
                        y = textAscent + gap - 2;
                    } else {
                        x = -textWidth/2;
                        y = -gap;
                    }
                }
                g2d.drawString(text, x, y);
                g2d.setTransform(dotAt);
            }
        }

        // layer 2 vẽ leader line
        if (drawLabels && pCount > 0) {
            // Bật Anti-aliasing và Pure Stroke cho đường dẫn (Line)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            Stroke oldStroke = g2d.getStroke();
            // Vẽ nét mảnh (0.5f) để phân biệt với các chi tiết chính
            g2d.setStroke(new BasicStroke(0.5f));
            for (int[] line : leaderLines) {
                if(line == null || line.length == 0) continue;
                g2d.setColor(new Color(line[0]));
                AffineTransform dotAt = g2d.getTransform();
                g2d.translate(line[1], line[2]);
//                    g2d.drawLine(anchorX, anchorY, stop.x, stop.y);
                g2d.draw(new Line2D.Double(line[3], line[4], line[5], line[6]));
                g2d.setTransform(dotAt);
            }
            g2d.setStroke(oldStroke);
        }
    }

    /**
     * Trả về tọa độ X của điểm bắt đầu Baseline của text dựa trên góc
     */
    public static int calculateCustomBaselineX(int lpX, int angle, int textWidth) {
        // Cung 20 độ trên đỉnh (80-100) và dưới đáy (260-280)
        if ((angle >= 80 && angle <= 100) || (angle >= 260 && angle <= 280)) {
            return lpX - textWidth / 2; // Căn giữa Baseline
        }
        // Nửa bên trái (101 - 259)
        else if (angle > 100 && angle < 260) {
            return lpX - textWidth;     // lp trùng điểm cuối Baseline
        }
        // Nửa bên phải (0-79 và 281-359)
        else {
            return lpX;                 // lp trùng điểm bắt đầu Baseline
        }
    }

    private static Point calculateStopPoint(int x1, int y1, int x2, int y2, double offset) {
        // Tính vector từ A đến B
        double dx = x2 - x1;
        double dy = y2 - y1;

        // Tính độ dài đoạn AB
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length <= offset) {
            // Nếu đoạn AB quá ngắn, không vẽ gì cả
            return new Point(x1, y1);
        }

        // Tính tỷ lệ để điểm dừng cách B một khoảng offset
        double ratio = (length - offset) / length;

        // Tính tọa độ điểm dừng
        int stopX = (int) (x1 + dx * ratio);
        int stopY = (int) (y1 + dy * ratio);

        return new Point(stopX, stopY);
    }

    public static void drawGrids(Graphics2D g2d, float zoom, AppState appState, int imageWidth, int imageHeight) {
        Stroke oldStroke = g2d.getStroke();
        float strokeWidth = 1.0f / zoom;
        float[] dash = new float[]{2.0f / zoom, 4.0f / zoom};
        g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dash, 0));
        for (SPoint grid : appState.getCanvasState().getGrids()) {
            g2d.setColor(grid.c);
            int gridSize = grid.id;
            int xR = grid.X;
            int yR = grid.Y;

            for (int x = xR; x < imageWidth; x += gridSize) {
                g2d.drawLine(x, 0, x, imageHeight);
            }
            for (int x = xR; x > 0; x -= gridSize) {
                g2d.drawLine(x, 0, x, imageHeight);
            }
            for (int y = yR; y < imageHeight; y += gridSize) {
                g2d.drawLine(0, y, imageWidth, y);
            }
            for (int y = yR; y > 0; y -= gridSize) {
                g2d.drawLine(0, y, imageWidth, y);
            }
        }
        g2d.setStroke(oldStroke);
    }
}
