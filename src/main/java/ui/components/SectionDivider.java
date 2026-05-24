package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SectionDivider extends JPanel {

    private final JLabel label;

    public SectionDivider(String title, int topMargin, int bottomMargin) {

        setLayout(new BorderLayout());
        setOpaque(false);

        // top, left, bottom, right
        setBorder(new EmptyBorder(topMargin, 0, bottomMargin, 0));
        label = new JLabel(title);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));

        add(label, BorderLayout.CENTER);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        FontMetrics fm = g.getFontMetrics(label.getFont());

        int textWidth = fm.stringWidth(label.getText());

        int y = getHeight() / 2;

        int gap = 8;

        int lineStart = 0;
        int lineEnd = getWidth();

        int textStart = (getWidth() - textWidth) / 2;
        int textEnd = textStart + textWidth;

        g.setColor(Color.LIGHT_GRAY);

        g.drawLine(lineStart, y, textStart - gap, y);
        g.drawLine(textEnd + gap, y, lineEnd, y);
    }
}
