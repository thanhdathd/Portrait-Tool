package tools;

import org.junit.jupiter.api.Test;
import java.awt.Point;
import java.awt.geom.Line2D;
import static org.junit.jupiter.api.Assertions.*;

class LineSelectionTest {
    private boolean lineIntersectsRect(Point a, Point b, double rx, double ry, double rw, double rh) {
        // Check if either point is inside
        if (a.x >= rx && a.x <= rx + rw && a.y >= ry && a.y <= ry + rh) return true;
        if (b.x >= rx && b.x <= rx + rw && b.y >= ry && b.y <= ry + rh) return true;

        // Check if segment AB intersects any of the 4 borders
        Line2D.Double segment = new Line2D.Double(a.x, a.y, b.x, b.y);
        Line2D.Double top = new Line2D.Double(rx, ry, rx + rw, ry);
        Line2D.Double bottom = new Line2D.Double(rx, ry + rh, rx + rw, ry + rh);
        Line2D.Double left = new Line2D.Double(rx, ry, rx, ry + rh);
        Line2D.Double right = new Line2D.Double(rx + rw, ry, rx + rw, ry + rh);

        return segment.intersectsLine(top) || segment.intersectsLine(bottom) || 
               segment.intersectsLine(left) || segment.intersectsLine(right);
    }

    @Test
    void testLineIntersectsRect() {
        Point insideA = new Point(10, 10);
        Point insideB = new Point(20, 20);
        assertTrue(lineIntersectsRect(insideA, insideB, 0, 0, 30, 30));

        Point outsideA = new Point(-10, -10);
        Point outsideB = new Point(40, 40); // Cuts across rect diagonal
        assertTrue(lineIntersectsRect(outsideA, outsideB, 0, 0, 30, 30));

        Point parallelOutsideA = new Point(50, 0);
        Point parallelOutsideB = new Point(50, 30);
        assertFalse(lineIntersectsRect(parallelOutsideA, parallelOutsideB, 0, 0, 30, 30));
    }
}
