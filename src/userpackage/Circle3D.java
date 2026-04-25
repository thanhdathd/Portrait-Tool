package userpackage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class Circle3D extends JPanel {
    private static final long serialVersionUID = 1L;
    public Color c;

    public Circle3D() {
        this.c = Color.blue;
    }

    public Circle3D(Color c) {
        this.c = Color.blue;
        this.c = c;
    }

    public void setColor(Color c) {
        this.c = c;
        this.repaint();
    }

    public Color getColor() {
        return this.c;
    }

    public void paint(Graphics g) {
        Dimension size = this.getSize();
        int d = 0;
        if (size.width < size.height) {
            d = size.width;
        } else {
            d = size.height;
        }

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, size.width, size.height);
        g.setColor(Color.WHITE);
        g.fillOval(0, 0, d, d);
        g.setColor(Color.GRAY);
        g.fillOval(3, 3, d - 6, d - 6);
        g.setColor(this.c);
        g.fillOval(3, 3, d - 6, d - 6);
    }
}
