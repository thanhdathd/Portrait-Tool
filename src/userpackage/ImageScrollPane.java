package userpackage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.ScrollPane;

public class ImageScrollPane extends ScrollPane {
    ScrollPane p;
    Float scale = 0.05F;
    private ImgFrame conten;
    private static final long serialVersionUID = 1L;

    public ImageScrollPane() {
        this.p = new ScrollPane();
    }

    public ImageScrollPane(int scrollbarDisplayPolicy) {
        this.p = new ScrollPane(scrollbarDisplayPolicy);
    }

    public ImageScrollPane(ImgFrame conten) {
        this.p = new ScrollPane();
        this.conten = conten;
    }

    public void paint(Graphics g) {
        this.drawBgr(g);
    }

    public void update(Graphics g) {
        this.paint(g);
    }

    public void drawBgr(Graphics g) {
        g.setColor(new Color(90, 90, 90));
        int w = 40;

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

    public void setContent(ImgFrame imageFrame) {
        this.conten = imageFrame;
    }

    public void layoutContent(Point location) {
        this.setLocation(location);
        this.setSize(this.conten.w + 5, this.conten.h + 5);
        this.doLayout();
        Noitifier.printConsole("layoutContent called - content size:" + this.conten.w + "x" + this.conten.h + "; scr Size:" + this.getHeight() + "x" + this.getHeight());
    }
}
