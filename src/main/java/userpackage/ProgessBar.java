package userpackage;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class ProgessBar extends Canvas {
    private static final long serialVersionUID = 1L;
    int max;
    int current;

    public ProgessBar(int max) {
        this.max = max;
        this.current = 0;
        this.setPreferredSize(new Dimension(600, 20));
    }

    public void setCurrent(int cur) {
        this.current = cur;
        this.paint(this.getGraphics());
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void paint(Graphics g) {
        g.setColor(new Color(0, 255, 100));
        Dimension d = this.getSize();
        int h = d.height;
        int w = d.width;
        float pro = (float)this.current / (float)(this.max - 1);
        int W = (int)((float)w * pro);
        g.fillRect(0, 0, W, h);
        Noitifier.printConsole("paint progess called.");
        Noitifier.printConsole("pro:" + pro + "; W:" + W + "cur:" + this.current + "; max" + this.max + "; h:" + h + "; w:" + w);
    }
}
