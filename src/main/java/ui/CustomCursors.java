package ui;

import javax.swing.*;
import java.awt.*;

public class CustomCursors {

    public static final Cursor HAND_CURSOR = createCursor("icons/icon6.png", new Point(16, 16), "Hand");
    public static final Cursor STICK_CURSOR = createCursor("icons/icon16.png", new Point(3, 3), "Stick");
    public static final Cursor P2P_CURSOR = createCursor("icons/icon17.png", new Point(2, 30), "P2P");
    public static final Cursor ZOOM_IN_CURSOR = createCursor("icons/icon18.png", new Point(10, 10), "Zoom In");
    public static final Cursor ZOOM_OUT_CURSOR = createCursor("icons/icon19.png", new Point(10, 10), "Zoom Out");
    public static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();

    private static Cursor createCursor(String path, Point hotSpot, String name) {
        try {
            java.net.URL url = CustomCursors.class.getClassLoader().getResource(path);
            if (url != null) {
                Image image = new ImageIcon(url).getImage();
                return Toolkit.getDefaultToolkit().createCustomCursor(image, hotSpot, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Cursor.getDefaultCursor();
    }
}
