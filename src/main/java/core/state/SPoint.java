package core.state;

import java.awt.Color;

public class SPoint {
    public int id;
    public int X;
    public int Y;
    public Direction dr;
    public Color c;
    public boolean isCustomPlacement = false;
    public int customGap = 0;
    public int customAngle = 0;

    public SPoint() {
        this.id = 0;
        this.X = 0;
        this.Y = 0;
        this.dr = Direction.EAST;
        this.c = Color.BLACK;
    }

    public SPoint(int id, int x, int y) {
        this.id = id;
        this.X = x;
        this.Y = y;
        this.dr = Direction.EAST;
        this.c = Color.BLACK;
    }

    public SPoint(int id, int x, int y, Color c) {
        this.id = id;
        this.X = x;
        this.Y = y;
        this.dr = Direction.EAST;
        this.c = c;
    }

    public SPoint(int id, int x, int y, Color c, Direction d) {
        this.id = id;
        this.X = x;
        this.Y = y;
        this.dr = d;
        this.c = c;
    }

    public SPoint(int id, int x, int y, Direction d) {
        this.id = id;
        this.X = x;
        this.Y = y;
        this.dr = d;
        this.c = Color.BLACK;
    }

    public SPoint copy() {
        SPoint p = new SPoint();
        p.id = this.id;
        p.X = this.X;
        p.Y = this.Y;
        p.dr = this.dr;
        p.c = this.c;
        p.isCustomPlacement = this.isCustomPlacement;
        p.customGap = this.customGap;
        p.customAngle = this.customAngle;
        return p;
    }
}
