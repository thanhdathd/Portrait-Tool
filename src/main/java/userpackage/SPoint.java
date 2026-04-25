package userpackage;

import java.awt.Color;
import user.Enum.Direction;

public class SPoint {
    public int id;
    public int X;
    public int Y;
    public Direction dr;
    public Color c;

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
}
