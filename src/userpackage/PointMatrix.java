package userpackage;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import user.Enum.Direction;

public class PointMatrix extends Dialog implements Runnable {
    private static final long serialVersionUID = 16L;
    BufferedImage buf;
    Stack s;
    private BufferedImage i;
    Thread myThread;
    File file;
    private boolean doneFlag = false;
    private BufferedImage mybuf;
    private File temp;
    private Frame f;
    private Panel imgPanel;
    private ProgessBar progess;

    public PointMatrix(Frame f, BufferedImage buf, BufferedImage i) {
        super(f);
        this.buf = buf;
        this.i = i;
        this.f = f;
        this.progess = new ProgessBar(50);
        this.imgPanel = new Panel();
        this.setLayout(new BorderLayout());
        this.add(this.imgPanel, "Center");
        this.add(this.progess, "Last");
        this.myThread = new Thread(this);
        this.setBounds(0, 0, 800, 600);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
            }
        });
    }

    public void run() {
        this.setVisible(true);
        this.temp = new File("temp00.png");

        try {
            ImageIO.write(this.buf, "png", this.temp);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            this.mybuf = ImageIO.read(this.temp);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        int i = 0;

        while(i < this.s.len) {
            SPoint p = this.s.getAt(i);
            this.clearRect(p, this.mybuf);
            this.progess.setCurrent(i);
            ++i;
            if (i == this.s.len) {
                this.doneFlag = true;
            }

            this.repaint();
            this.save();
            Noitifier.printConsole("i = " + i);

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    protected void savePoint(Stack s) {
        this.s = s;
        this.progess.setMax(s.len);
        FileDialog fd = new FileDialog(this, "Save File", 1);
        fd.setFile("testingPoint.png");
        fd.setBounds(0, 0, 600, 400);
        fd.setVisible(true);
        if (fd.getFile() != null) {
            this.file = new File(fd.getDirectory(), fd.getFile());
            if (fd.getFile().endsWith(".png")) {
                this.myThread.start();
            } else {
                MessageBox msg = new MessageBox(this.f, "Format Not Supported!", true);
                msg.setVisible(true);
                this.dispose();
            }
        }

    }

    public void clearRect(SPoint s, BufferedImage buf) {
        Graphics2D g2 = buf.createGraphics();
        int x = s.X;
        int y = s.Y;
        if (s.dr == Direction.EAST) {
            g2.drawImage(this.i, x + 3, y + 2, x + 35, y + 15, x + 3, y + 2, x + 35, y + 15, this);
        }

        if (s.dr == Direction.WEST) {
            g2.drawImage(this.i, x - 35, y + 2, x + 10, y + 18, x - 35, y + 2, x + 10, y + 18, this);
        }

        if (s.dr == Direction.SOUTH) {
            g2.drawImage(this.i, x - 5, y + 3, x + 20, y + 25, x - 5, y + 3, x + 20, y + 25, this);
        }

        if (s.dr == Direction.NORTH) {
            g2.drawImage(this.i, x - 15, y - 25, x + 20, y - 3, x - 15, y - 25, x + 20, y - 3, this);
        }

        Noitifier.printConsole("Clear " + s.id);
    }

    public void setImg(BufferedImage bu) {
        this.i = bu;
    }

    public void paint(Graphics g) {
        g = this.imgPanel.getGraphics();
        Graphics2D g2 = (Graphics2D)g;
        g2.drawImage(this.mybuf, (BufferedImageOp)null, 0, 0);
    }

    public void save() {
        if (this.doneFlag) {
            try {
                ImageIO.write(this.mybuf, "png", this.file);
                Noitifier.printConsole("Point Saved");
                this.temp.delete();
                MessageBox msg = new MessageBox(this.f, "Point Saved", true);
                msg.setVisible(true);
                this.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
