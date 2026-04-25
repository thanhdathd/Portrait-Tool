package userpackage;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class MyCellRenderer extends JLabel implements ListCellRenderer<JLabel> {
    private static final long serialVersionUID = 1L;

    public Component getListCellRendererComponent(JList<? extends JLabel> list, JLabel value, int index, boolean isSelected, boolean cellHasFocus) {
        this.setText(value.toString());
        JList.DropLocation dropLocation = list.getDropLocation();
        Color background;
        Color foreground;
        if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {
            background = Color.BLUE;
            foreground = Color.WHITE;
        } else if (isSelected) {
            background = Color.RED;
            foreground = Color.WHITE;
        } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        }

        this.setBackground(background);
        this.setForeground(foreground);
        return this;
    }
}
