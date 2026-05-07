package ui.button;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class RoundToggleButton extends JToggleButton {

    private int topLeft = 0;
    private int topRight = 0;
    private int bottomRight = 0;
    private int bottomLeft = 0;

    public RoundToggleButton(String text) {
        super(text);

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        Shape shape = createRoundedShape(
                0, 0,
                getWidth(),
                getHeight(),
                topLeft,
                topRight,
                bottomRight,
                bottomLeft
        );

        if (isSelected()) {
            g2.setColor(new Color(100, 150, 255));
            setForeground(Color.WHITE);
        } else {
            g2.setColor(new Color(220, 220, 220));
            setForeground(Color.BLACK);
        }

        g2.fill(shape);

        g2.dispose();

        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(Color.decode("#f3f3f3"));

        Shape shape = createRoundedShape(
                0, 0,
                getWidth() - 1,
                getHeight(),
                topLeft,
                topRight,
                bottomRight,
                bottomLeft
        );

        g2.draw(shape);
        g2.dispose();
    }

    private Shape createRoundedShape(
            int x,
            int y,
            int width,
            int height,
            int tl,
            int tr,
            int br,
            int bl
    ) {
        Path2D path = new Path2D.Double();

        path.moveTo(x + tl, y);

        path.lineTo(x + width - tr, y);
        if (tr > 0)
            path.quadTo(x + width, y, x + width, y + tr);

        path.lineTo(x + width, y + height - br);
        if (br > 0)
            path.quadTo(x + width, y + height, x + width - br, y + height);

        path.lineTo(x + bl, y + height);
        if (bl > 0)
            path.quadTo(x, y + height, x, y + height - bl);

        path.lineTo(x, y + tl);
        if (tl > 0)
            path.quadTo(x, y, x + tl, y);

        path.closePath();

        return path;
    }

    public void setCornerRadius(
            int topLeft,
            int topRight,
            int bottomRight,
            int bottomLeft
    ) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;

        repaint();
    }
}
