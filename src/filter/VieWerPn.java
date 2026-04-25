package filter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.image.BufferedImage;

public class VieWerPn extends Panel {
    private static final long serialVersionUID = 1L;
    BufferedImage buf;
    BufferedImage Sbuf;
    int imgW;
    int imgH;
    int posX;
    int posY;
    int W;
    int H;
    private Image img = null;

    public VieWerPn(BufferedImage buf) {
        this.buf = buf;
        this.W = buf.getWidth();
        this.H = buf.getHeight();
        this.Sbuf = new BufferedImage(this.W, this.H, 3);
    }

    public void paint(Graphics g) {
        Dimension size = this.getSize();
        this.drawGr(g);
        this.imgW = this.buf.getWidth();
        this.imgH = this.buf.getHeight();
        float x = (float)this.imgH / (float)this.imgW;
        if (this.imgH > size.height) {
            this.imgH = size.height;
            this.imgW = (int)((float)this.imgH / x);
        }

        if (this.imgW > size.width) {
            this.imgW = size.width;
            this.imgH = (int)((float)this.imgW * x);
        }

        int w = this.imgW;
        if (this.imgH == size.height) {
            this.posX = size.width / 2 - w / 2;
        } else {
            this.posX = 0;
        }

        if (this.imgW == size.width) {
            this.posY = size.height / 2 - this.imgH / 2;
        } else {
            this.posY = 0;
        }

        if (this.img == null) {
            g.drawImage(this.buf, this.posX, this.posY, this.imgW, this.imgH, this);
        } else {
            g.drawImage(this.img, this.posX, this.posY, this.imgW, this.imgH, this);
        }

    }

    private void drawGr(Graphics g) {
        g.setColor(new Color(60, 60, 60));
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(new Color(90, 90, 90));
        int w = this.getWidth() / 20;

        for(int i = 0; i < w + 10; ++i) {
            for(int j = 0; j < w + 10; j += 2) {
                if (i % 2 == 0) {
                    g.fillRect(j * w, i * w, w, w);
                }

                if (i % 2 != 0) {
                    g.fillRect(j * w + w, i * w, w, w);
                }
            }
        }

    }

    public void updateImg(Image img) {
        Graphics gs = this.getGraphics();
        Graphics gb = this.Sbuf.createGraphics();
        this.drawGr(gb);
        gb.drawImage(img, 0, 0, this);
        gs.drawImage(this.Sbuf, this.posX, this.posY, this.imgW, this.imgH, this);
        this.img = img;
    }
}
