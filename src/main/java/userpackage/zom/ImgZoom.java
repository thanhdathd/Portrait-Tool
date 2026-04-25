package userpackage.zom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Point;
import java.awt.image.BufferedImage;
import user.Enum.Direction;
import userpackage.Noitifier;

public class ImgZoom extends Panel implements Runnable {
    private static final long serialVersionUID = 13L;
    BufferedImage buf;
    Point previousPoint = new Point(0, 0);
    Point mousePosition = new Point(0, 0);
    Direction numDirection;
    boolean ruler;
    int rA;
    Thread myThread;
    int xG;
    int yG;
    int zoomLevel;
    float zoomRate;
    public float scale;
    private boolean showUnit;
    private boolean absoluteUnit;
    private int yDistance;
    private boolean messure;
    private int xDistance;
    private boolean cmIndicate;
    private boolean cross;
    public boolean move;
    private static final float[] zoom = new float[]{0.1F, 0.14F, 0.2F, 0.3F, 0.45F, 0.7F, 1.0F, 1.55F, 2.0F, 2.3F, 3.4F, 5.05F, 7.52F, 10.0F, 10.122F, 16.77F, 25.09F, 37.57F, 56.29F, 84.37F, 126.49F, 316.16F, 320.0F, 330.0F};

    public ImgZoom(BufferedImage i) {
        this.numDirection = Direction.EAST;
        this.ruler = false;
        this.rA = 35;
        this.scale = 1.0F;
        this.showUnit = true;
        this.absoluteUnit = false;
        this.yDistance = 35;
        this.messure = false;
        this.xDistance = -35;
        this.cmIndicate = false;
        this.cross = true;
        this.move = true;
        this.buf = i;
        this.myThread = new Thread(this);
        this.xG = 0;
        this.yG = 0;
        this.zoomRate = 2.0F;
        this.zoomLevel = 9;
        this.setBackground(Color.DARK_GRAY);
    }

    public void updateBuffer(BufferedImage buf) {
        this.buf = buf;
        this.repaint();
    }

    public void zoom(int whellRotation) {
        if (whellRotation == -1) {
            if (this.zoomLevel < 23) {
                ++this.zoomLevel;
                this.zoomRate = zoom[this.zoomLevel];
            }

            this.repaint();
        }

        if (whellRotation == 1) {
            if (this.zoomLevel > 0) {
                --this.zoomLevel;
                this.zoomRate = zoom[this.zoomLevel];
            }

            this.repaint();
        }

    }

    public float getZoomrate() {
        return this.zoomRate;
    }

    public void paint(Graphics g) {
        int w = this.buf.getWidth();
        int h = this.buf.getHeight();
        int W = (int)((float)w * this.zoomRate);
        int H = (int)((float)h * this.zoomRate);
        this.xG = this.getParent().getWidth() / 2 - 5;
        this.yG = this.getParent().getHeight() / 2 - 50;
        int xl = (int)((float)this.xG - this.zoomRate * (float)this.mousePosition.x);
        int yl = (int)((float)this.yG - this.zoomRate * (float)this.mousePosition.y);
        g.setPaintMode();
        g.drawImage(this.buf, xl, yl, W, H, this);
        this.drawBoder(g, xl, yl, W, H, this.getBackground());
        if (!this.ruler && this.cross) {
            this.drawCross(g);
        }

        if (this.ruler) {
            this.drawRuler(g);
        }

    }

    public void drawCross(Graphics g) {
        g.setXORMode(Color.WHITE);
        g.drawOval(this.xG - 11, this.yG - 11, 20, 20);
        g.drawLine(this.xG - 1, this.yG - 5, this.xG - 1, this.yG - 25);
        g.drawLine(this.xG - 1, this.yG + 5, this.xG - 1, this.yG + 25);
        g.drawLine(this.xG - 5, this.yG - 1, this.xG - 25, this.yG - 1);
        g.drawLine(this.xG + 5, this.yG - 1, this.xG + 25, this.yG - 1);
        if (this.numDirection == Direction.WEST) {
            g.drawLine(this.xG - 25, this.yG + 3, this.xG - 20, this.yG + 3);
        }

        if (this.numDirection == Direction.NORTH) {
            g.drawLine(this.xG + 3, this.yG - 25, this.xG + 3, this.yG - 20);
        }

        if (this.numDirection == Direction.SOUTH) {
            g.drawLine(this.xG + 3, this.yG + 20, this.xG + 3, this.yG + 25);
        }

        if (this.numDirection == Direction.EAST) {
        }

        g.setPaintMode();
    }

    public void imgMove(int x, int y) {
        Graphics g = this.getGraphics();
        int w = this.buf.getWidth();
        int h = this.buf.getHeight();
        this.xG = this.getParent().getWidth() / 2 - 5;
        this.yG = this.getParent().getHeight() / 2 - 50;
        int xl = (int)((float)this.xG - this.zoomRate * (float)x);
        int yl = (int)((float)this.yG - this.zoomRate * (float)y);
        g.setPaintMode();
        int W = (int)((float)w * this.zoomRate);
        int H = (int)((float)h * this.zoomRate);
        g.drawImage(this.buf, xl, yl, W, H, this);
        this.drawBoder(g, xl, yl, W, H, this.getBackground());
        g.setXORMode(Color.WHITE);
        if (!this.ruler && this.cross) {
            this.drawCross(g);
        }

        if (this.ruler) {
            this.drawRuler(g);
        }

        this.previousPoint = new Point(xl, yl);
        this.mousePosition.move(x, y);
    }

    public void drawRuler(Graphics g) {
        g.setXORMode(Color.WHITE);
        int height = this.getParent().getHeight();
        int width = this.getParent().getWidth();
        g.drawLine(this.xG, 0, this.xG, this.yG - 3);
        g.drawLine(this.xG, this.yG + 3, this.xG, height);
        g.drawLine(0, this.yG, this.xG - 3, this.yG);
        g.drawLine(this.xG + 3, this.yG, width, this.yG);
        int rw = 3;
        int yR = this.yG;
        int xStart = this.xG - rw;
        int xEnd = this.xG + rw;
        int i = 1;
        String st = "";
        float rA1 = 0.0F;
        if (!this.absoluteUnit) {
            st = Float.toString((float)this.rA * this.scale);
            rA1 = (float)this.rA;
            this.drawUnit(g, height, width, rw, st, rA1);
        } else {
            rA1 = (float)this.rA / this.zoomRate;
            st = Float.toString(rA1 * this.scale);
            this.drawUnit(g, height, width, rw, st, rA1);
        }

        if (this.messure) {
            this.drawMessureLine(g, width, height);
        }

        for(; yR > 0; ++i) {
            yR = this.yG - i * this.rA;
            if (i % 2 == 0) {
                g.drawLine(xStart - 4, yR, xEnd + 4, yR);
            } else {
                g.drawLine(xStart, yR, xEnd, yR);
            }
        }

        int j = 1;

        for(int var17 = 0; var17 < height; ++j) {
            var17 = this.yG + j * this.rA;
            if (j % 2 == 0) {
                g.drawLine(xStart - 4, var17, xEnd + 4, var17);
            } else {
                g.drawLine(xStart, var17, xEnd, var17);
            }
        }

        int xR = this.xG;
        int yStart = this.yG - rw;
        int yEnd = this.yG + rw;

        for(int k = 1; xR > 0; ++k) {
            xR = this.xG - k * this.rA;
            if (k % 2 == 0) {
                g.drawLine(xR, yStart - 4, xR, yEnd + 4);
            } else {
                g.drawLine(xR, yStart, xR, yEnd);
            }
        }

        int l = 1;

        for(int var22 = 0; var22 < width; ++l) {
            var22 = this.xG + l * this.rA;
            if (l % 2 == 0) {
                g.drawLine(var22, yStart - 4, var22, yEnd + 4);
            } else {
                g.drawLine(var22, yStart, var22, yEnd);
            }
        }

        g.setPaintMode();
    }

    public void drawUnit(Graphics g, int height, int width, int rw, String st, float rA1) {
        if (this.showUnit) {
            if (this.yG - this.rA + 5 < this.yG - 25) {
                if (this.absoluteUnit) {
                    g.drawString(Float.toString(rA1), this.xG + rw + 5, this.yG - this.rA + 5);
                } else {
                    g.drawString(Integer.toString(this.rA), this.xG + rw + 5, this.yG - this.rA + 5);
                }
            } else if (this.absoluteUnit) {
                g.drawString(Float.toString(rA1), this.xG + rw + 5, this.yG - 25);
            } else {
                g.drawString(Integer.toString(this.rA), this.xG + rw + 5, this.yG - 25);
            }

            if (this.xG + this.rA - 3 > this.xG + 25) {
                g.drawString(st + " cm", this.xG + this.rA - 3, this.yG - rw - 8);
            } else {
                g.drawString(st + " cm", this.xG + 25, this.yG - rw - 8);
            }
        } else {
            g.drawString(Float.toString(rA1), width / 4, height * 3 / 4);
            g.drawString(st + " cm", width * 3 / 4, height * 3 / 4);
        }

    }

    public void drawMessureLine(Graphics g, int width, int height) {
        int y = this.yG - this.yDistance;
        g.drawLine(0, y, width, y);
        float f = (float)this.yDistance / this.zoomRate;
        if (this.cmIndicate) {
            f *= this.scale;
        }

        if (this.absoluteUnit) {
            g.drawString(Float.toString(f), this.xG - 120, y - 15);
        } else if (this.cmIndicate) {
            g.drawString(Float.toString((float)this.yDistance * this.scale), this.xG - 120, y - 15);
        } else {
            g.drawString(Integer.toString(this.yDistance), this.xG - 120, y - 15);
        }

        int x = this.xG + this.xDistance;
        g.drawLine(x, 0, x, height);
        float f2 = (float)this.xDistance / this.zoomRate;
        if (this.cmIndicate) {
            f2 *= this.scale;
        }

        if (this.absoluteUnit) {
            g.drawString(Float.toString(f2), x + 25, 50);
        } else if (this.cmIndicate) {
            g.drawString(Float.toString((float)this.xDistance * this.scale), x + 25, 50);
        } else {
            g.drawString(Integer.toString(this.xDistance), x + 25, 50);
        }

    }

    public void messureLineUp(int up) {
        if (this.messure) {
            this.yDistance += up;
            this.repaint(0, this.yG - this.yDistance - 30, this.getParent().getHeight() + 50, 31 + up);
        }

    }

    public void messureLineDown(int down) {
        if (this.messure) {
            this.yDistance -= down;
            this.repaint(0, this.yG - this.yDistance - 30, this.getParent().getWidth(), 30 + down);
        }

    }

    public void messureLineRight(int r) {
        if (this.messure) {
            this.xDistance += r;
            this.repaint(this.xG + this.xDistance - r, 0, 100, this.getParent().getHeight());
        }

    }

    public void messureLineLeft(int l) {
        if (this.messure) {
            this.xDistance -= l;
            this.repaint(this.xG + this.xDistance, 0, 100, this.getParent().getHeight());
        }

    }

    public void messureMode() {
        if (this.messure) {
            this.messure = false;
        } else {
            this.messure = true;
        }

        this.repaint();
        Noitifier.printConsole("messure:" + this.messure);
    }

    public void fillBackground(Graphics g, int newX, int newY) {
        g.setColor(new Color(60, 60, 60));
        if (this.previousPoint.x > 0 || this.previousPoint.y > 0) {
            if (this.previousPoint.x == 0 && this.previousPoint.y == 0) {
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            } else {
                int r = this.getWidth() - this.previousPoint.x;
                int d = newY - this.previousPoint.y;
                g.fillRect(this.previousPoint.x, this.previousPoint.y, r, d);
                int r2 = newX - this.previousPoint.x;
                int d2 = this.getHeight() - this.previousPoint.y;
                g.fillRect(this.previousPoint.x, this.previousPoint.y, r2, d2);
            }
        }

    }

    public void drawBoder(Graphics g, int x, int y, int W, int H, Color bg) {
        int bw = 30;
        g.setColor(bg);
        g.fillRect(x - bw, y - bw, W + 2 * bw, bw);
        g.fillRect(x - bw, y - bw, bw, H + 2 * bw);
        g.fillRect(x - bw, y + H, W + 2 * bw, bw);
        g.fillRect(x + W, y, bw, H);
    }

    public void drawGround(Graphics g) {
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

    public void stop() {
        this.myThread.stop();
    }

    public void start() {
        this.myThread.start();
    }

    public void run() {
        while(this.isVisible()) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.stop();
    }

    public void changeDirection(Direction d) {
        this.numDirection = d;
        this.repaint();
    }

    public void changeRulerMode() {
        if (!this.ruler) {
            this.ruler = true;
        } else {
            this.ruler = false;
        }

        this.repaint();
    }

    public void showCross() {
        if (!this.cross) {
            this.cross = true;
        } else {
            this.cross = false;
        }

        this.repaint(this.xG - 50, this.yG - 50, 100, 100);
    }

    public void increRulerUnit(int inc) {
        if (this.rA < this.getHeight() / 2) {
            this.rA += inc;
        }

        this.repaint();
        Noitifier.printConsole(">>rA" + this.rA);
    }

    public void decreRulerUnit(int dec) {
        if (this.rA > dec + 2) {
            this.rA -= dec;
        }

        this.repaint();
        Noitifier.printConsole("<<rA" + this.rA);
    }

    public void resetRulerUnit() {
        this.rA = 35;
        this.repaint();
    }

    public void setScale(float f) {
        this.scale = f;
        this.repaint();
    }

    public void setCmScale(int i) {
        this.rA = (int)((float)i / this.scale);
        this.repaint();
    }

    public void showUnit() {
        if (!this.showUnit) {
            this.showUnit = true;
        } else {
            this.showUnit = false;
        }

        this.repaint();
    }

    public void setAbsoluteUnit(boolean b) {
        this.absoluteUnit = b;
        Noitifier.printConsole("iz absolute:" + this.absoluteUnit);
    }

    public void setCmIndicate(boolean b) {
        this.cmIndicate = b;
    }

    public void lock() {
        if (!this.move) {
            this.move = true;
        } else {
            this.move = false;
        }

    }
}
