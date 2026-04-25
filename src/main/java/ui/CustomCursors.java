package ui;

import javax.swing.*;
import java.awt.*;

public class CustomCursors {

    public static final Cursor HAND_CURSOR = createCursor("icons/icon6.png", new Point(16, 16), "Hand");
    public static final Cursor STICK_CURSOR = createCursor("icons/icon16.png", new Point(3, 3), "Stick");
    public static final Cursor P2P_CURSOR = createCursor("icons/icon17.png", new Point(2, 30), "P2P");
    public static final Cursor ZOOM_IN_CURSOR = createCursor("icons/icon18.png", new Point(10, 10), "Zoom In");
    public static final Cursor ZOOM_OUT_CURSOR = createCursor("icons/icon19.png", new Point(10, 10), "Zoom Out");
    public static final Cursor GRID_CURSOR = createGridCursor();
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

    private static Cursor createGridCursor() {
        int size = 31; // Odd size so there's a true center pixel
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        
        // Draw black outline cross
        g2.setColor(Color.BLACK);
        g2.fillRect(14, 0, 3, size); // Vertical
        g2.fillRect(0, 14, size, 3); // Horizontal
        
        // Draw white inner cross
        g2.setColor(Color.WHITE);
        g2.fillRect(15, 1, 1, size - 2);
        g2.fillRect(1, 15, size - 2, 1);
        
        g2.dispose();
        return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(15, 15), "Grid");
    }
}
