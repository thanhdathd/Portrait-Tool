package userpackage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;

public class EditPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public void paint(Graphics g) {
        g.setColor(new Color(60, 60, 60));
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(new Color(90, 90, 90));
        int w = this.getWidth() / 20;

        for(int i = 0; i < w; ++i) {
            for(int j = 0; j < w; j += 2) {
                if (i % 2 == 0) {
                    g.fillRect(j * w, i * w, w, w);
                }

                if (i % 2 != 0) {
                    g.fillRect(j * w + w, i * w, w, w);
                }
            }
        }

    }
}
