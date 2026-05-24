package core.state;

import java.awt.Color;
import java.awt.Point;

public class SLine {
    public int id;
    public Point startPoint;
    public Point endPoint;
    public int strokeWidth;
    public Color strokeColor;

    public SLine() {
        this.id = 0;
        this.startPoint = new Point(0, 0);
        this.endPoint = new Point(0, 0);
        this.strokeWidth = 2;
        this.strokeColor = Color.BLACK;
    }

    public SLine(int id, Point startPoint, Point endPoint, int strokeWidth, Color strokeColor) {
        this.id = id;
        this.startPoint = new Point(startPoint);
        this.endPoint = new Point(endPoint);
        this.strokeWidth = strokeWidth;
        this.strokeColor = strokeColor;
    }

    public SLine copy() {
        return new SLine(this.id, this.startPoint, this.endPoint, this.strokeWidth, this.strokeColor);
    }
}
