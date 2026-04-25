package userpackage;

import images.Img;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

public class CustomCursor extends Cursor {
    private static final long serialVersionUID = 1L;
    public static final String STICK_CURSOR = "STICK_CURSOR";
    public static final String P2P_CURSOR = "P2P_CURSOR";
    public Cursor c = new Cursor(0);
    private static Toolkit tool = Toolkit.getDefaultToolkit();
    private static final Image I_RECT_CURSOR;
    public static final Cursor RECT_CURSOR;
    private static final Image I_TICK_CURSOR_32;
    public static final Cursor STICK_CURSOR_32;
    private static final Image I_P2P_CURSOR_32;
    public static final Cursor P2P_CURSOR_32;
    private static final Image I_ZOOM_IN;
    public static final Cursor ZOOM_IN;
    private static final Image I_ZOOM_OUT;
    public static final Cursor ZOOM_OUT;

    static {
        I_RECT_CURSOR = tool.createImage("data/tick_cursor.png");
        RECT_CURSOR = tool.createCustomCursor(I_RECT_CURSOR, new Point(0, 0), "Rect");
        I_TICK_CURSOR_32 = Img.creatImg(16);
        STICK_CURSOR_32 = tool.createCustomCursor(I_TICK_CURSOR_32, new Point(3, 3), "Stick");
        I_P2P_CURSOR_32 = Img.creatImg(17);
        P2P_CURSOR_32 = tool.createCustomCursor(I_P2P_CURSOR_32, new Point(2, 30), "P2P");
        I_ZOOM_IN = Img.creatImg(18);
        ZOOM_IN = tool.createCustomCursor(I_ZOOM_IN, new Point(10, 10), "Zoom in");
        I_ZOOM_OUT = Img.creatImg(19);
        ZOOM_OUT = tool.createCustomCursor(I_ZOOM_OUT, new Point(10, 10), "Zoom out");
    }

    public CustomCursor(int type) {
        super(type);
        this.c = new Cursor(type);
    }

    public static Cursor creatCursor(String type) {
        Cursor c = new Cursor(0);
        if (type == "STICK") {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image i = toolkit.createImage("data/tick_cursor.png");
            Point hotSpot = new Point(3, 3);
            c = toolkit.createCustomCursor(i, hotSpot, "Stick");
            return c;
        } else if (type == "P2P") {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image i = toolkit.createImage("data/p2p_cursor.png");
            Point hotSpot = new Point(2, 30);
            c = toolkit.createCustomCursor(i, hotSpot, "P2P");
            return c;
        } else if (type == "ZOOM_IN") {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image i = toolkit.createImage("data/zoom_in2.png");
            Point hotSpot = new Point(10, 10);
            c = toolkit.createCustomCursor(i, hotSpot, "zoom in");
            return c;
        } else if (type == "ZOOM_OUT") {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image i = toolkit.createImage("data/zoom_out2.png");
            Point hotSpot = new Point(10, 10);
            c = toolkit.createCustomCursor(i, hotSpot, "zoom out");
            return c;
        } else if (type == "STICK_CURSOR") {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image i = Img.creatImg(13);
            if (i != null) {
                Point hotSpot = new Point(3, 3);
                c = toolkit.createCustomCursor(i, hotSpot, "Stick");
            } else {
                c = STICK_CURSOR_32;
            }

            return c;
        } else if (type == "P2P_CURSOR") {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image i = Img.creatImg(14);
            if (i != null) {
                Point hotSpot = new Point(2, 30);
                c = toolkit.createCustomCursor(i, hotSpot, "P2P");
            } else {
                c = P2P_CURSOR_32;
            }

            return c;
        } else {
            return c;
        }
    }

    public static Cursor creatCursor(String path, Point hotPot, String name) {
        new Cursor(0);
        Image i = tool.createImage(path);
        Cursor c = tool.createCustomCursor(i, hotPot, name);
        return c;
    }
}
