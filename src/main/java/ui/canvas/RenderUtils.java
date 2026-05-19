package ui.canvas;

import core.state.AppState;
import user.Enum.Direction;
import userpackage.SPoint;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.Set;

public class RenderUtils {
    public static void drawPointMarker(Graphics2D g2d, SPoint p, float scale) {
        if (scale <= 0) return;

        // 1 mm = 0.1 cm
        float circleRadiusCm = 0.1f;
        float rectWidthCm = 0.15f;
        float rectLengthCm = rectWidthCm * 2.0f;

        int circleRadiusPx = Math.max(1, Math.round(circleRadiusCm / scale));
        int rectWidthPx = Math.max(1, Math.round(rectWidthCm / scale));
        int rectLengthPx = Math.max(1, Math.round(rectLengthCm / scale));

        g2d.setColor(Color.BLACK);

        // Draw circle
        g2d.drawOval(p.X - circleRadiusPx, p.Y - circleRadiusPx, circleRadiusPx * 2, circleRadiusPx * 2);

        // Draw crosshair (4 rectangles)
        // Top
        g2d.fillRect(p.X - rectWidthPx / 2, p.Y - circleRadiusPx - rectLengthPx, rectWidthPx, rectLengthPx);
        // Bottom
        g2d.fillRect(p.X - rectWidthPx / 2, p.Y + circleRadiusPx, rectWidthPx, rectLengthPx);
        // Left
        g2d.fillRect(p.X - circleRadiusPx - rectLengthPx, p.Y - rectWidthPx / 2, rectLengthPx, rectWidthPx);
        // Right
        g2d.fillRect(p.X + circleRadiusPx, p.Y - rectWidthPx / 2, rectLengthPx, rectWidthPx);
    }

    public static void drawPointMap(Graphics2D g2d, List<SPoint> points, float scale, int width, int height) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        for (SPoint p : points) {
            drawPointMarker(g2d, p, scale);
        }
    }

    // -----------------------------------------------------------------------
    // drawStickyPoints overloads
    // -----------------------------------------------------------------------

    /** Overload without selection — backward compatible. */
    public static void drawStickyPoints(Graphics2D g2d, List<SPoint> points, boolean drawLabels) {
        drawStickyPoints(g2d, points, drawLabels, (Set<SPoint>) null);
    }

    /** Overload with single selected point — backward compatible. */
    public static void drawStickyPoints(Graphics2D g2d, List<SPoint> points, boolean drawLabels, SPoint selectedPoint) {
        Set<SPoint> sel = selectedPoint != null ? java.util.Collections.singleton(selectedPoint) : null;
        drawStickyPoints(g2d, points, drawLabels, sel);
    }

    /** Primary implementation — draws selection rings for all points in selectedPoints. */
    public static void drawStickyPoints(Graphics2D g2d, List<SPoint> points, boolean drawLabels, Set<SPoint> selectedPoints) {
        // 1. LẤY VÙNG HIỂN THỊ (VIEWPORT CLIP)
        Rectangle clip = g2d.getClipBounds();
        if (clip != null) {
            clip.grow(50, 50);
        }

        int pCount = (int) points.stream().filter(p -> p.isCustomPlacement).count();
        int[][] leaderLines = new int[pCount][];
        int index = 0;
        Font font = new Font("SansSerif", Font.BOLD, 12);
        g2d.setFont(font);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (SPoint p : points) {
            if (clip != null && !clip.contains(p.X, p.Y)) continue;

            // Vẽ Dot
            g2d.setColor(p.c);
            g2d.fillRect(p.X, p.Y, 1, 1);

            if (drawLabels) {
                AffineTransform dotAt = g2d.getTransform();
                g2d.translate(p.X, p.Y);

                String text = String.valueOf(p.id);
                FontMetrics fm = g2d.getFontMetrics(font);
                int textWidth = fm.stringWidth(text);
                int textAscent = fm.getAscent();

                int gap = 10;
                int x = 0, y = 0;

                if (p.isCustomPlacement) {
                    double radians = Math.toRadians(p.customAngle);
                    int lpX = (int) Math.round(p.customGap * Math.cos(radians));
                    int lpY = (int) Math.round(-p.customGap * Math.sin(radians));
                    int bsAdjustY = -2;
                    int bsAdjustX = -1;

                    x = calculateCustomBaselineX(lpX, p.customAngle, textWidth);
                    y = lpY - 1;
                    if (p.customAngle >= 213 && p.customAngle <= 327) {
                        y = lpY + (int) (textAscent * 0.9) - 1;
                        bsAdjustY = 2;
                    }
                    g2d.drawLine(x + bsAdjustX, lpY - bsAdjustY, x + textWidth + bsAdjustX, lpY - bsAdjustY);
                    double offset = 5.0;
                    int anchorX = x + bsAdjustX;
                    int anchorY = lpY - bsAdjustY;
                    int angle = p.customAngle;
                    if (angle >= 100 && angle <= 260) {
                        anchorX = x + textWidth + bsAdjustX;
                    } else if (angle >= 80 && angle <= 280) {
                        anchorX = x + (textWidth + bsAdjustX) / 2;
                    }
                    Point stop = calculateStopPoint(anchorX, anchorY, 1, 1, offset);
                    leaderLines[index] = new int[]{p.c.getRGB(), p.X, p.Y, anchorX, anchorY, stop.x, stop.y};
                    index++;
                } else {
                    if (p.dr == Direction.EAST) {
                        x = gap + 2; y = textAscent / 2 + 4;
                    } else if (p.dr == Direction.WEST) {
                        x = -textWidth - gap; y = textAscent / 2 + 4;
                    } else if (p.dr == Direction.SOUTH) {
                        x = -textWidth / 2; y = textAscent + gap - 2;
                    } else {
                        x = -textWidth / 2; y = -gap;
                    }
                }
                g2d.drawString(text, x, y);
                g2d.setTransform(dotAt);
            }
        }

        // layer 2: vẽ leader lines
        if (drawLabels && pCount > 0) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(0.5f));
            for (int[] line : leaderLines) {
                if (line == null || line.length == 0) continue;
                g2d.setColor(new Color(line[0]));
                AffineTransform dotAt = g2d.getTransform();
                g2d.translate(line[1], line[2]);
                g2d.draw(new Line2D.Double(line[3], line[4], line[5], line[6]));
                g2d.setTransform(dotAt);
            }
            g2d.setStroke(oldStroke);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        }

        // layer 3: vẽ selection ring quanh tất cả các điểm đang được chọn
        if (selectedPoints != null && !selectedPoints.isEmpty()) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(0.5f));
            int r = 10;
            for (SPoint sp : selectedPoints) {
                if (points.contains(sp)) {
                    g2d.setColor(new Color(0, 120, 215));
                    g2d.drawOval(sp.X - r, sp.Y - r, r * 2, r * 2);
                }
            }
            g2d.setStroke(oldStroke);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

    // -----------------------------------------------------------------------
    // getLabelBounds — for hit-testing label clicks in SelectTool
    // -----------------------------------------------------------------------

    /**
     * Computes the label bounding box for a point in image (local) coordinates.
     */
    public static Rectangle getLabelBounds(SPoint p, FontMetrics fm) {
        String text = String.valueOf(p.id);
        int textWidth = fm.stringWidth(text);
        int textAscent = fm.getAscent();
        int gap = 10;
        int lx, ly, lw, lh;
        if (p.isCustomPlacement) {
            double radians = Math.toRadians(p.customAngle);
            int lpX = (int) Math.round(p.customGap * Math.cos(radians));
            int lpY = (int) Math.round(-p.customGap * Math.sin(radians));
            lx = calculateCustomBaselineX(lpX, p.customAngle, textWidth);
            int y;
            if (p.customAngle >= 213 && p.customAngle <= 327) {
                y = lpY + (int) (textAscent * 0.9) - 1;
            } else {
                y = lpY - 1;
            }
            lx += p.X;
            ly = p.Y + y - textAscent;
            lw = textWidth + 2;
            lh = textAscent + 2;
        } else {
            int x, y;
            if (p.dr == Direction.EAST) {
                x = gap + 2; y = textAscent / 2 + 4;
            } else if (p.dr == Direction.WEST) {
                x = -textWidth - gap; y = textAscent / 2 + 4;
            } else if (p.dr == Direction.SOUTH) {
                x = -textWidth / 2; y = textAscent + gap - 2;
            } else {
                x = -textWidth / 2; y = -gap;
            }
            lx = p.X + x - 1;
            ly = p.Y + y - textAscent;
            lw = textWidth + 2;
            lh = textAscent + 2;
        }
        return new Rectangle(lx, ly, lw, lh);
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    public static int calculateCustomBaselineX(int lpX, int angle, int textWidth) {
        if ((angle >= 80 && angle <= 100) || (angle >= 260 && angle <= 280)) {
            return lpX - textWidth / 2;
        } else if (angle > 100 && angle < 260) {
            return lpX - textWidth;
        } else {
            return lpX;
        }
    }

    private static Point calculateStopPoint(int x1, int y1, int x2, int y2, double offset) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length <= offset) return new Point(x1, y1);
        double ratio = (length - offset) / length;
        int stopX = (int) (x1 + dx * ratio);
        int stopY = (int) (y1 + dy * ratio);
        return new Point(stopX, stopY);
    }

    public static void drawGrids(Graphics2D g2d, float zoom, AppState appState, int imageWidth, int imageHeight) {
        Stroke oldStroke = g2d.getStroke();
        float strokeWidth = 1.0f / zoom;
        float selectedStrokeWidth = 3.0f / zoom;
        float[] dash = new float[]{2.0f / zoom, 4.0f / zoom};

        SPoint selectedGrid = appState.getCanvasState().getSelectedGrid();

        for (SPoint grid : appState.getCanvasState().getGrids()) {
            if (grid == selectedGrid) {
                g2d.setStroke(new BasicStroke(selectedStrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dash, 0));
            } else {
                g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dash, 0));
            }

            g2d.setColor(grid.c);
            int gridSize = grid.id;
            int xR = grid.X;
            int yR = grid.Y;
            if (grid == selectedGrid) {
                Stroke old = g2d.getStroke();
                g2d.setStroke(new BasicStroke(0.5f));
                g2d.drawOval(xR - 10, yR - 10, 20, 20);
                g2d.setStroke(old);
            }

            for (int x = xR; x < imageWidth; x += gridSize) g2d.drawLine(x, 0, x, imageHeight);
            for (int x = xR; x > 0; x -= gridSize) g2d.drawLine(x, 0, x, imageHeight);
            for (int y = yR; y < imageHeight; y += gridSize) g2d.drawLine(0, y, imageWidth, y);
            for (int y = yR; y > 0; y -= gridSize) g2d.drawLine(0, y, imageWidth, y);
        }
        g2d.setStroke(oldStroke);
    }

    public static void drawLines(Graphics2D g2d, List<userpackage.SLine> lines, Set<userpackage.SLine> selectedLines, float zoom) {
        if (lines == null || lines.isEmpty()) return;

        Stroke oldStroke = g2d.getStroke();
        Color oldColor = g2d.getColor();
        Object oldAntialiasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (userpackage.SLine line : lines) {
            // Draw segment line
            g2d.setColor(line.strokeColor);
            g2d.setStroke(new BasicStroke(line.strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(line.startPoint.x, line.startPoint.y, line.endPoint.x, line.endPoint.y);

            // Draw handles if selected
            if (selectedLines != null && selectedLines.contains(line)) {
                // Radius of handle: 5px on screen
                float r = 5.0f / zoom;
                float d = 10.0f / zoom;

                float x1 = line.startPoint.x;
                float y1 = line.startPoint.y;
                float x2 = line.endPoint.x;
                float y2 = line.endPoint.y;
                float xm = (x1 + x2) / 2.0f;
                float ym = (y1 + y2) / 2.0f;

                Color handleColor = Color.decode("#229fff");
                g2d.setStroke(new BasicStroke(1.0f / zoom));

                // Start point handle
                g2d.setColor(handleColor);
                g2d.fill(new java.awt.geom.Ellipse2D.Float(x1 - r, y1 - r, d, d));
                g2d.setColor(Color.WHITE);
                g2d.draw(new java.awt.geom.Ellipse2D.Float(x1 - r, y1 - r, d, d));

                // End point handle
                g2d.setColor(handleColor);
                g2d.fill(new java.awt.geom.Ellipse2D.Float(x2 - r, y2 - r, d, d));
                g2d.setColor(Color.WHITE);
                g2d.draw(new java.awt.geom.Ellipse2D.Float(x2 - r, y2 - r, d, d));

                // Midpoint handle
                g2d.setColor(handleColor);
                g2d.fill(new java.awt.geom.Ellipse2D.Float(xm - r, ym - r, d, d));
                g2d.setColor(Color.WHITE);
                g2d.draw(new java.awt.geom.Ellipse2D.Float(xm - r, ym - r, d, d));
            }
        }

        g2d.setStroke(oldStroke);
        g2d.setColor(oldColor);
        if (oldAntialiasing != null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
        }
    }
}
