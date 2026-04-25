package userpackage;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Panel;

public class ColorPlate extends Panel {
    private static final long serialVersionUID = 1L;
    public Color _color;

    public ColorPlate(Color c) {
        this.setBackground(c);
        this._color = c;
        this.setCursor(new Cursor(12));
    }

    public Color getColor() {
        return this._color;
    }
}
