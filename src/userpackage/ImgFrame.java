package userpackage;

import filter.BlueGrayFilter;
import filter.BlueOutGrayFilter;
import filter.FStack;
import filter.FilterProperties;
import filter.GreenGrayFilter;
import filter.GreenOutGrayFilter;
import filter.RGBFilter;
import filter.RGBGrayFilter;
import filter.RedGrayFilter;
import filter.RedOutGrayFilter;
import images.Img;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.imageio.ImageIO;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import transform.TStack;
import user.Enum.BufImageState;
import user.Enum.Direction;
import user.Enum.MouseMode;
import user.Enum.Tranform;
import userpackage.zom.ZoomDialog;

public class ImgFrame extends Canvas implements MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    public final int GRID = 25251325;
    public final int STICK = 66772508;
    public final int FILTER = 1001001;
    public final int TRANFORM = 2002002;
    public final int FLIP_H = 3003001;
    public final int FLIP_V = 3003002;
    String p = null;
    Image i;
    public int w = 800;
    public int h = 600;
    public String noity;
    public Observer imgObserver;
    private State _state;
    private BufferedImage _bufImage;
    private BufferedImage ori;
    private Color _color;
    private Point _clpoint;
    private Point _startPoint;
    private Point _endPoint;
    private Integer _clcount;
    public ZoomDialog zoomdialog;
    private PathState _pathState;
    private BufImageState _bufImageState;
    public MouseMode mouseMode;
    private Direction numLocation;
    public Stack s;
    public Stack rs;
    public Stack action;
    public Stack reaction;
    public Stack tempFileStack;
    private Stack gridAction;
    private Stack gridReAction;
    private TStack tranformAction;
    public TStack tranformReAction;
    public SPoint currentAction;
    private JTable table;
    ExcelExporter exp;
    public int stSize;
    public boolean zoomBoxOpened;
    private final Font font;
    public float currentzoom;
    public int gridAmount;
    public float scale;
    public BufferedImage actionBuffImage;
    public File actionBuffImageFile;
    public boolean cmIndicate;
    private Point previousPoint;
    public boolean round;
    public boolean viLang;
    private String unknow_file_format_img_save;
    private String file_saved;
    private String file_not_found;
    private Cursor p2pCursor;
    private Cursor stkCursor;
    public boolean zoom;
    private Frame parent;
    private Dimension origin;
    private String APP_NAME;
    public PopupMenu pop;
    public MenuItem openWith;
    public MenuItem undo;
    public MenuItem redo;
    public MenuItem CW90;
    public MenuItem CCW90;
    public MenuItem R180;
    public MenuItem FH;
    public MenuItem FV;
    public MenuItem RZ;
    public Menu menuImage;

    public ImgFrame(String p) {
        this._state = ImgFrame.State.RELEASED;
        this._bufImage = null;
        this._color = Color.CYAN;
        this._clcount = 0;
        this._pathState = ImgFrame.PathState.PATH_USED;
        this._bufImageState = BufImageState.UPDATED;
        this.mouseMode = MouseMode.DRAG;
        this.numLocation = Direction.EAST;
        this.s = new Stack(15, "s");
        this.rs = new Stack(115, "rs");
        this.action = new Stack(115, "action");
        this.reaction = new Stack(115, "reaction");
        this.tempFileStack = new Stack(115, 1);
        this.gridAction = new Stack(115, "gridAction");
        this.gridReAction = new Stack(15, "gridReAction");
        this.tranformAction = new TStack();
        this.tranformReAction = new TStack();
        this.currentAction = new SPoint();
        this.stSize = 15;
        this.zoomBoxOpened = false;
        this.font = new Font("SansSerif", 1, 12);
        this.currentzoom = 1.0F;
        this.gridAmount = 40;
        this.cmIndicate = true;
        this.round = false;
        this.viLang = false;
        this.unknow_file_format_img_save = "Unknow file format.";
        this.file_saved = "File saved: ";
        this.file_not_found = "File not found!";
        this.zoom = true;
        this.i = this.getToolkit().createImage(p);
        this.p2pCursor = CustomCursor.creatCursor("P2P_CURSOR");
        this.stkCursor = CustomCursor.creatCursor("STICK_CURSOR");

        try {
            this.actionBuffImage = ImageIO.read(new File(p));
            this.actionBuffImageFile = new File(p);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.p = p;
        this.addMouseListener(this);
        this.setCursor(new Cursor(12));
    }

    public ImgFrame(Image i) {
        this._state = ImgFrame.State.RELEASED;
        this._bufImage = null;
        this._color = Color.CYAN;
        this._clcount = 0;
        this._pathState = ImgFrame.PathState.PATH_USED;
        this._bufImageState = BufImageState.UPDATED;
        this.mouseMode = MouseMode.DRAG;
        this.numLocation = Direction.EAST;
        this.s = new Stack(15, "s");
        this.rs = new Stack(115, "rs");
        this.action = new Stack(115, "action");
        this.reaction = new Stack(115, "reaction");
        this.tempFileStack = new Stack(115, 1);
        this.gridAction = new Stack(115, "gridAction");
        this.gridReAction = new Stack(15, "gridReAction");
        this.tranformAction = new TStack();
        this.tranformReAction = new TStack();
        this.currentAction = new SPoint();
        this.stSize = 15;
        this.zoomBoxOpened = false;
        this.font = new Font("SansSerif", 1, 12);
        this.currentzoom = 1.0F;
        this.gridAmount = 40;
        this.cmIndicate = true;
        this.round = false;
        this.viLang = false;
        this.unknow_file_format_img_save = "Unknow file format.";
        this.file_saved = "File saved: ";
        this.file_not_found = "File not found!";
        this.zoom = true;
        this.p2pCursor = CustomCursor.creatCursor("P2P_CURSOR");
        this.stkCursor = CustomCursor.creatCursor("STICK_CURSOR");
        this.i = i;
        this.w = this.i.getWidth(this.imgObserver);
        this.h = this.i.getHeight(this.imgObserver);
        this.setSize(this.w, this.h);
        this.origin = new Dimension(this.w, this.h);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setCursor(new Cursor(12));
        this.s.setEcho(true);
        this.rs.setEcho(true);
        this.action.setEcho(true);
        this.reaction.setEcho(false);
        this.gridAction.setEcho(false);
        this.gridReAction.setEcho(false);
    }

    private BufferedImage creatBufferedImage(Image i2, int W, int H) {
        int[] pix = new int[W * H];
        PixelGrabber pg = new PixelGrabber(i2, 0, 0, W, H, pix, 0, W);

        try {
            pg.grabPixels();
        } catch (InterruptedException var8) {
            Noitifier.printConsole("creat Buff error!!");

            for(int i = 0; i < pix.length; ++i) {
                pix[i] = -65281;
            }

            BufferedImage buf = new BufferedImage(W, H, 3);
            buf.setRGB(0, 0, W, H, pix, 0, W);
            return buf;
        }

        BufferedImage buf = new BufferedImage(W, H, 3);
        buf.setRGB(0, 0, W, H, pix, 0, W);
        return buf;
    }

    public Dimension getD() {
        this.w = this.i.getWidth(this.imgObserver);
        this.h = this.i.getHeight(this.imgObserver);
        return new Dimension(this.w, this.h);
    }

    public void updateImg(Image i) {
        this.i = i;
        this.w = this.i.getWidth(this.imgObserver);
        this.h = this.i.getHeight(this.imgObserver);
        this.setSize(this.w, this.h);
        this.origin = new Dimension(this.w, this.h);
        this.ori = this.creatBufferedImage(i, this.w, this.h);
        this.repaint();
        Noitifier.printConsole("ImgFrame - updateImg called.");
    }

    public void setPath(String p) {
        this.p = p;
        this._pathState = ImgFrame.PathState.HAD_NEW_PATH;
        this._bufImageState = BufImageState.NOT_UPDATED;
        Noitifier.printConsole("setPath called: " + p);

        try {
            this.actionBuffImage = ImageIO.read(new File(p));
            this.actionBuffImageFile = new File(p);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void resetAll() {
        this._clcount = 0;
        this.s.popAll();
        this.rs.popAll();
        this.action.popAll();
        this.reaction.popAll();
        this.tempFileStack.popAll();
        this.gridAction.popAll();
        this.gridReAction.popAll();
        this.currentzoom = 1.0F;
    }

    public void setBrushColor(Color c) {
        this._color = c;
    }

    public void paint(Graphics g) {
        if (this.p != null && this._pathState == ImgFrame.PathState.HAD_NEW_PATH) {
            try {
                BufferedImage b = ImageIO.read(new File(this.p));
                this.setSize(b.getWidth(), b.getHeight());
                this.w = b.getWidth();
                this.h = b.getHeight();
            } catch (IOException e1) {
                e1.printStackTrace();
                Noitifier.printConsole("buff b failed");
            }
        }

        if (this.w != -1 && this.h != -1) {
            this.setSize(this.w, this.h);
        }

        Graphics2D g2d = (Graphics2D)this.getGraphics();
        if (this._bufImage == null && this._bufImageState != BufImageState.CANT_READ) {
            if (this.p != null) {
                File imfile = new File(this.p);

                try {
                    this._bufImage = ImageIO.read(imfile);
                } catch (IOException e) {
                    e.printStackTrace();
                    this._bufImageState = BufImageState.CANT_READ;
                    this.noity = this.file_not_found;
                }
            } else {
                this._bufImage = (BufferedImage)this.createImage(800, 600);
            }

            Graphics2D gc = this._bufImage.createGraphics();
            gc.setColor(Color.WHITE);
            gc.drawRect(3, 3, this.getWidth() - 6, this.getHeight() - 6);
        }

        if (this.ori == null) {
            this.ori = this.creatBufferedImage(this.i, this.w, this.h);
        }

        g2d.drawImage(this._bufImage, 0, 0, this.w, this.h, this.imgObserver);
        if (this._state == ImgFrame.State.CLICKED && this.mouseMode == MouseMode.STICK) {
            SPoint sp = new SPoint(0, 66772508, 0);
            this.action.push(sp);
            this.currentAction = sp;
        }

        if (this._state == ImgFrame.State.CLICKED) {
            MouseMode var10000 = MouseMode.GRID;
        }

    }

    private void drawGird(Graphics2D g2, Point clickPoint, int gridSize, Color c, String mode) {
        this._bufImageState = BufImageState.MODIFIED;
        if (mode == "draw") {
            if (!this.reaction.isEmpty()) {
                this.reaction.popAll();
                this.gridReAction.popAll();
            }

            SPoint gr = new SPoint(gridSize, clickPoint.x, clickPoint.y, c);
            this.gridAction.push(gr);
        }

        if (mode == "redraw") {
        }

        g2.setColor(c);
        int xR = clickPoint.x;
        int yR = clickPoint.y;
        int i = 0;

        for(int xV1 = 0; xV1 < this.origin.width; ++i) {
            xV1 = xR + i * gridSize;
            this.drawVectical(g2, xV1);
        }

        int j = 1;

        for(int var15 = xR; var15 > 0; ++j) {
            var15 = xR - j * gridSize;
            this.drawVectical(g2, var15);
        }

        int k = 0;

        for(int yH1 = 0; yH1 < this.origin.height; ++k) {
            yH1 = yR + k * gridSize;
            this.drawHorizontal(g2, yH1);
        }

        int l = 1;

        for(int var16 = yR; var16 > 0; ++l) {
            var16 = yR - l * gridSize;
            this.drawHorizontal(g2, var16);
        }

    }

    private void drawHorizontal(Graphics2D g2, int yH1) {
        for(int xd = 0; xd < this.origin.width; xd += 3) {
            g2.fillRect(xd, yH1, 1, 1);
        }

    }

    private void drawVectical(Graphics2D g2, int xV) {
        for(int yd = 0; yd < this.origin.height; yd += 3) {
            g2.fillRect(xV, yd, 1, 1);
        }

    }

    public boolean updateBuffer() {
        File imfile = new File(this.p);

        try {
            this._bufImage = ImageIO.read(imfile);
            this.noity = "File Opened - " + this.p;
            this._pathState = ImgFrame.PathState.PATH_USED;
            this._bufImageState = BufImageState.UPDATED;
            if (this.zoomdialog != null) {
                this.zoomdialog.updateBufer(this._bufImage);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            this._bufImageState = BufImageState.CANT_READ;
            this.noity = this.file_not_found;
            Noitifier.printConsole("File not found!");
            return false;
        }
    }

    private void drawCurrentPoint(Graphics2D g2) {
        g2.setColor(this._color);
        g2.fillRect(this._clpoint.x - 1, this._clpoint.y - 1, 1, 1);
        g2.setFont(this.font);
        if (this.numLocation == Direction.EAST) {
            g2.drawString(this._clcount.toString(), this._clpoint.x + 12, this._clpoint.y + 12);
        }

        if (this.numLocation == Direction.WEST) {
            g2.drawString(this._clcount.toString(), this._clpoint.x - 30, this._clpoint.y + 15);
        }

        if (this.numLocation == Direction.SOUTH) {
            g2.drawString(this._clcount.toString(), this._clpoint.x - 5, this._clpoint.y + 25);
        }

        if (this.numLocation == Direction.NORTH) {
            g2.drawString(this._clcount.toString(), this._clpoint.x - 5, this._clpoint.y - 12);
        }

        this._bufImageState = BufImageState.MODIFIED;
    }

    public void paint(Graphics g, int w, int h) {
        this.setSize(w, h);
        g.drawImage(this.i, 0, 0, w, h, this.imgObserver);
    }

    public int zoom(boolean b) {
        int wi = this.w;
        int he = this.h;
        int result = 0;
        if (b) {
            wi = (int)Math.round((double)this.w * (double)1.5F);
            he = (int)Math.round((double)this.h * (double)1.5F);
            if (wi * he < 20000000) {
                this.w = wi;
                this.h = he;
                this.paint(this.getGraphics(), this.w, this.h);
                this.currentzoom = (float)this.w / (float)this.origin.width;
                result = 1;
            } else {
                System.err.println("No more heap");
                MessageBox noi = new MessageBox(this.parent, "Out of memory", true);
                noi.show();
                result = -1;
            }
        }

        if (!b) {
            wi = (int)Math.round((double)this.w / (double)1.5F);
            he = (int)Math.round((double)this.h / (double)1.5F);
            if (wi * he > 2500) {
                this.w = wi;
                this.h = he;
                this.paint(this.getGraphics(), this.w, this.h);
                this.currentzoom = (float)this.w / (float)this.origin.width;
                result = 0;
            } else {
                MessageBox noi = new MessageBox(this.parent, "Can't smaller", true);
                noi.show();
                this.changeZoomMode(true);
                result = -1;
            }
        }

        this.currentzoom = (float)this.w / (float)this.origin.width;
        Noitifier.printConsole("imageFrame - zoom:" + this.currentzoom);
        return result;
    }

    public void saveImg(String dir, String file) {
        File f = new File(dir, file);

        try {
            if (file.endsWith(".jpg")) {
                ImageIO.write(this._bufImage, "jpg", f);
                this.noity = this.file_saved + dir + file;
                this._bufImageState = BufImageState.SAVED;
            } else if (file.endsWith(".png")) {
                ImageIO.write(this._bufImage, "png", f);
                this.noity = this.file_saved + dir + file;
                this._bufImageState = BufImageState.SAVED;
            } else if (file.endsWith(".gif")) {
                ImageIO.write(this._bufImage, "gif", f);
                this.noity = this.file_saved + dir + file;
                this._bufImageState = BufImageState.SAVED;
            } else if (file.endsWith(".bmp")) {
                ImageIO.write(this._bufImage, "bmp", f);
                this.noity = this.file_saved + dir + file;
                this._bufImageState = BufImageState.SAVED;
            } else {
                this.noity = this.unknow_file_format_img_save;
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.noity = this.unknow_file_format_img_save;
        }

    }

    public void mouseClicked(MouseEvent e) {
        if (this.mouseMode == MouseMode.STICK) {
            if (!this.rs.isEmpty()) {
                this.rs.popAll();
            }

            if (!this.reaction.isEmpty()) {
                this.reaction.popAll();
            }
        }

    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (this.mouseMode == MouseMode.STICK && e.getButton() == 1) {
            if (!this.s.isFull()) {
                this._clcount = this._clcount + 1;
                this._state = ImgFrame.State.CLICKED;
                this._clpoint = e.getPoint();
                if (this.currentzoom != 1.0F) {
                    this._clpoint.x = Math.round((float)this._clpoint.x / this.currentzoom);
                    this._clpoint.y = Math.round((float)this._clpoint.y / this.currentzoom);
                }

                this.repaint(this._clpoint.x - 1, this._clpoint.y - 1, 35, 15);
            } else {
                MessageBox mgs = new MessageBox(new Frame(), "Stack Full !!!", true);
                mgs.show();
            }
        }

        if (this.mouseMode == MouseMode.GRID) {
            this._state = ImgFrame.State.CLICKED;
            this._clpoint = e.getPoint();
            this._clpoint.x = (int)((float)this._clpoint.x / this.currentzoom);
            this._clpoint.y = (int)((float)this._clpoint.y / this.currentzoom);
            this.repaint();
        }

        if (this.mouseMode == MouseMode.P2P) {
            this._state = ImgFrame.State.CLICKED;
            this._startPoint = e.getPoint();
            this.previousPoint = this._startPoint;
        }

        if (e.getButton() == 3) {
            this.pop.show(this, e.getX(), e.getY());
        }

    }

    public void mouseReleased(MouseEvent e) {
        if (this._state == ImgFrame.State.CLICKED && this.mouseMode == MouseMode.STICK) {
            this._state = ImgFrame.State.RELEASED;
            if (!this.s.isFull()) {
                this.drawCurrentPoint(this._bufImage.createGraphics());
                this.repaint(this._clpoint.x - 1, this._clpoint.y - 1, 35, 15);
                SPoint p = new SPoint(this._clcount, this._clpoint.x, this._clpoint.y, this._color, this.numLocation);
                this.s.push(p);
                if (this.zoomdialog != null) {
                    this.zoomdialog.iz.repaint();
                }
            } else {
                Noitifier.printConsole("Stack Full");
                MessageBox mgs = new MessageBox(new Frame(), "Stack Full !!!", true);
                mgs.show();
            }

            if (this.s.isFull()) {
                this.s.display();
            }
        }

        if (this._state == ImgFrame.State.CLICKED && this.mouseMode == MouseMode.GRID) {
            this._state = ImgFrame.State.RELEASED;
            this.drawGird(this._bufImage.createGraphics(), this._clpoint, this.gridAmount, this._color, "draw");
            Graphics g = this.getGraphics();
            this.paint(g);
            SPoint sp = new SPoint(0, 25251325, 0);
            this.action.push(sp);
            this.currentAction = sp;
            Noitifier.printConsole("grid wrote to buff");
        }

        if (this._state == ImgFrame.State.CLICKED && this.mouseMode == MouseMode.P2P) {
            this._state = ImgFrame.State.RELEASED;
            this._endPoint = e.getPoint();
            Noitifier.printConsole("end:" + this._endPoint);
            Rectangle r = this.drawMessureLine(this._bufImage.createGraphics(), this._startPoint, this._endPoint);
            int wR = r.width;
            int hR = r.height;
            this.repaint(this._startPoint.x, this._startPoint.y, wR + 20, hR);
        }

    }

    private Rectangle drawMessureLine(Graphics2D g2, Point startPoint, Point endPoint) {
        int startX = startPoint.x;
        int startY = startPoint.y;
        int endX = endPoint.x;
        int endY = endPoint.y;
        startX = (int)((float)startX / this.currentzoom);
        startY = (int)((float)startY / this.currentzoom);
        endX = (int)((float)endX / this.currentzoom);
        endY = (int)((float)endY / this.currentzoom);
        g2.setColor(this._color);
        g2.drawLine(startX, startY, endX, endY);
        int dX = Math.abs(endX - startX);
        int dY = Math.abs(endY - startY);
        float d = (float)Math.sqrt((double)(dX * dX + dY * dY));
        this.printResult(g2, d, new Point(endX, endY));
        return new Rectangle((int)((float)dX * this.currentzoom), (int)((float)dY * this.currentzoom));
    }

    private void printResult(Graphics2D g2, float d, Point p) {
        if (p.x > this.w - 30) {
            p.translate(-70, 20);
        }

        if (p.y > this.h - 30) {
            p.translate(-60, -20);
        }

        if (this.cmIndicate) {
            d *= this.scale;
            if (this.round) {
                d = Math2.round(d);
            }

            g2.drawString(Float.toString(d) + " cm", p.x + 15, p.y);
            int X = p.x;
            int Y = p.y;
            int var10000 = (int)((float)X * this.currentzoom);
            var10000 = (int)((float)Y * this.currentzoom);
        } else if (this.round) {
            d = Math2.round(d);
        }

        g2.drawString(Float.toString(d), p.x + 15, p.y);
        Noitifier.printConsole("Math2.round(d):" + Math2.round(d));
    }

    private void clearLine() {
        Graphics2D g2d = this._bufImage.createGraphics();
        g2d.drawImage(this.actionBuffImage, 0, 0, this.imgObserver);
        if (!this.s.isEmpty()) {
            for(int i = 0; i < this.s.len; ++i) {
                SPoint p = this.s.getAt(i);
                this.redrawPoint(p);
            }
        }

        if (!this.gridAction.isEmpty()) {
            for(int i = 0; i < this.gridAction.len; ++i) {
                SPoint p = this.gridAction.getAt(i);
                this.drawGird(g2d, new Point(p.X, p.Y), p.id, p.c, "redraw");
            }
        }

        Graphics g = this.getGraphics();
        g.drawImage(this._bufImage, 0, 0, this.w, this.h, this.imgObserver);
    }

    public Color getBrushColor() {
        return this._color;
    }

    public BufferedImage getBufferedImage() {
        return this._bufImage;
    }

    public void setBufferedImage(BufferedImage buf) {
        this._bufImage = buf;
    }

    public void exportData(File f) {
        if (!this.s.isEmpty()) {
            String[][] data = new String[this.s.len][3];
            new SPoint();
            int max = this.s.len;

            for(int i = 0; i < max; ++i) {
                SPoint sp = this.s.getAt(i);
                data[i][0] = Integer.toString(sp.id);
                data[i][1] = Integer.toString(sp.X);
                data[i][2] = Integer.toString(sp.Y);
                Noitifier.printConsole("data[" + i + "][0]:" + data[i][0]);
                Noitifier.printConsole("data[" + i + "][1]:" + data[i][1]);
                Noitifier.printConsole("data[" + i + "][2]:" + data[i][2]);
            }

            String[] headers = new String[]{"id", "Toa do X", "Toa do Y"};
            DefaultTableModel model = new DefaultTableModel(data, headers);
            this.table = new JTable(model);

            try {
                this.exp = new ExcelExporter();
                this.exp.setLanguage(this.viLang);
                this.exp.exportTable(this.table, f);
                this.noity = this.exp.getNoitify();
            } catch (IOException ex) {
                Noitifier.printConsole(ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            Noitifier.printConsole("Khong co du lieu");
        }

    }

    private void clearRect(SPoint p, int w, int h) {
        int x = p.X;
        int y = p.Y;
        Graphics g = this.getGraphics();
        if (p.dr == Direction.EAST) {
            Graphics2D g2 = this._bufImage.createGraphics();
            g2.drawImage(this.actionBuffImage, x - 1, y - 1, x + w, y + h, x - 1, y - 1, x + w, y + h, this);
            g.drawImage(this._bufImage, 0, 0, this.w, this.h, this);
        }

        if (p.dr == Direction.WEST) {
            Graphics2D g2 = this._bufImage.createGraphics();
            g2.drawImage(this.actionBuffImage, x - w - 5, y - 3, x + 3, y + h + 3, x - w - 5, y - 3, x + 3, y + h + 3, this);
            g.drawImage(this._bufImage, 0, 0, this.w, this.h, this);
        }

        if (p.dr == Direction.SOUTH) {
            Graphics2D g2 = this._bufImage.createGraphics();
            g2.drawImage(this.actionBuffImage, x - 7, y - 3, x + w - 15, y + 2 * h - 14, x - 7, y - 3, x + w - 15, y + 2 * h - 14, this);
            g.drawImage(this._bufImage, 0, 0, this.w, this.h, this);
        }

        if (p.dr == Direction.NORTH) {
            Graphics2D g2 = this._bufImage.createGraphics();
            g2.drawImage(this.actionBuffImage, x - 7, y - 2 * h + 17, x + w - 15, y + 3, x - 7, y - 2 * h + 17, x + w - 15, y + 3, this);
            g.drawImage(this._bufImage, 0, 0, this.w, this.h, this);
        }

        if (!this.gridAction.isEmpty()) {
            for(int i = 0; i < this.gridAction.len; ++i) {
                SPoint gr = this.gridAction.getAt(i);
                Graphics2D g2 = this._bufImage.createGraphics();
                this.drawGird(g2, new Point(gr.X, gr.Y), gr.id, gr.c, "redraw");
                Noitifier.printConsole("clear rect - drawgrid " + i);
                Noitifier.printConsole("gr.c" + gr.c);
            }

            g.drawImage(this._bufImage, 0, 0, this.w, this.h, this.imgObserver);
        }

    }

    public void undoGrid() {
        Graphics2D g2 = this._bufImage.createGraphics();
        g2.drawImage(this.actionBuffImage, 0, 0, this.imgObserver);
        if (!this.s.isEmpty()) {
            for(int i = 0; i < this.s.len; ++i) {
                SPoint p = this.s.getAt(i);
                this.redrawPoint(p);
            }
        }

        SPoint gr = this.gridAction.pop();
        this.gridReAction.push(gr);

        for(int i = 0; i < this.gridAction.len; ++i) {
            SPoint gr2 = this.gridAction.getAt(i);
            this.drawGird(g2, new Point(gr2.X, gr2.Y), gr2.id, gr2.c, "redraw");
        }

        Graphics g = this.getGraphics();
        g.drawImage(this._bufImage, 0, 0, this.w, this.h, this.imgObserver);
        Noitifier.printConsole("undo grid called");
    }

    public void redoGrid() {
        Graphics2D g2 = this._bufImage.createGraphics();
        SPoint p = this.gridReAction.pop();
        this.gridAction.push(p);
        Point clickPoint = new Point(p.X, p.Y);
        int gridSize = p.id;
        Color c = p.c;
        this.drawGird(g2, clickPoint, gridSize, c, "redraw");
        Graphics g = this.getGraphics();
        g.drawImage(this._bufImage, 0, 0, this.w, this.h, this.imgObserver);
        Noitifier.printConsole("redoGrid called");
    }

    public void undoStick() {
        if (!this.s.isEmpty()) {
            SPoint sp = this.s.pop();
            this.rs.push(sp);
            Noitifier.printConsole("sp.X:" + sp.X + ";sp.Y:" + sp.Y);
            this.clearRect(sp, 35, 20);
            this._clcount = this._clcount - 1;
        } else {
            Noitifier.printConsole("Stack is Empty");
        }

    }

    public void redoClick() {
        if (!this.rs.isEmpty()) {
            this._clcount = this._clcount + 1;
            SPoint sp = this.rs.pop();
            this.s.push(sp);
            this.redrawPoint(sp);
        } else {
            Noitifier.printConsole("redo Stack is Empty");
        }

    }

    private void redrawPoint(SPoint p) {
        Graphics g = this.getGraphics();
        Graphics2D g2 = this._bufImage.createGraphics();
        g2.setColor(p.c);
        g2.fillRect(p.X - 1, p.Y - 1, 2, 2);
        g2.setFont(this.font);
        if (p.dr == Direction.EAST) {
            g2.drawString(Integer.toString(p.id), p.X + 12, p.Y + 12);
        }

        if (p.dr == Direction.WEST) {
            g2.drawString(Integer.toString(p.id), p.X - 30, p.Y + 15);
        }

        if (p.dr == Direction.SOUTH) {
            g2.drawString(Integer.toString(p.id), p.X - 5, p.Y + 25);
        }

        if (p.dr == Direction.NORTH) {
            g2.drawString(Integer.toString(p.id), p.X - 5, p.Y - 12);
        }

        g.drawImage(this._bufImage, 0, 0, this.w, this.h, this.imgObserver);
    }

    public void mouseModeChanged() {
    }

    public void changeStickMode() {
        if (this.mouseMode != MouseMode.STICK) {
            if (this.mouseMode == MouseMode.P2P) {
                this.clearLine();
            }

            this.mouseMode = MouseMode.STICK;
            this.mouseModeChanged();
            this.setCursor(this.stkCursor);
        }

    }

    public void changeDragMode() {
        if (this.mouseMode != MouseMode.DRAG) {
            if (this.mouseMode == MouseMode.P2P) {
                this.clearLine();
            }

            this.mouseMode = MouseMode.DRAG;
            this.setCursor(new Cursor(12));
        }

    }

    public void changeDragMode(boolean isSwitch) {
        if (this.mouseMode != MouseMode.DRAG) {
            if (this.mouseMode == MouseMode.P2P && !isSwitch) {
                this.clearLine();
            }

            this.mouseMode = MouseMode.DRAG;
            this.setCursor(new Cursor(12));
        }

    }

    public void changeGridMode() {
        if (this.mouseMode != MouseMode.GRID) {
            if (this.mouseMode == MouseMode.P2P) {
                this.clearLine();
            }

            this.mouseMode = MouseMode.GRID;
            this.mouseModeChanged();
            this.setCursor(new Cursor(1));
        }

    }

    public void changeP2PMode() {
        if (this.mouseMode != MouseMode.P2P) {
            this.mouseMode = MouseMode.P2P;
            this.setCursor(this.p2pCursor);
        }

    }

    public void changeP2PMode(boolean isSwitch) {
        if (this.mouseMode != MouseMode.P2P) {
            this.mouseMode = MouseMode.P2P;
            this.setCursor(this.p2pCursor);
        }

    }

    public void changeZoomMode(boolean b) {
        if (b) {
            this.setCursor(CustomCursor.ZOOM_IN);
            this.zoom = true;
        } else {
            this.setCursor(CustomCursor.ZOOM_OUT);
            this.zoom = false;
        }

        if (this.mouseMode == MouseMode.P2P) {
            this.clearLine();
        }

        this.mouseMode = MouseMode.ZOOM;
    }

    public void openZoomBox(Frame f, int zW, int zH, boolean ab_ck, boolean cm_ck) {
        if (!this.zoomBoxOpened) {
            BufferedImage buf = this._bufImage;
            this.zoomdialog = new ZoomDialog(f, "Zoom", false, buf, this.numLocation, this.viLang, ab_ck, cm_ck);
            this.zoomdialog.setParent(this);
            this.zoomdialog.show();
            this.zoomdialog.iz.scale = this.scale;
            this.zoomdialog.scale.setText(Float.toString(this.scale));
            this.zoomdialog.setBackground(Color.CYAN);
            this.zoomdialog.zom.setSize(zW, zH);
            this.zoomBoxOpened = true;
        }

    }

    public void closeZoomBox() {
        if (this.zoomdialog.closed) {
            this.zoomdialog.viLang = this.viLang;
            this.zoomdialog.show();
        } else {
            this.scale = this.zoomdialog.iz.scale;
            this.zoomdialog.hide();
            this.zoomdialog = null;
            this.zoomBoxOpened = false;
        }

    }

    public void changeDirection(int keyCode) {
        if (keyCode == 37) {
            this.numLocation = Direction.WEST;
        }

        if (keyCode == 39) {
            this.numLocation = Direction.EAST;
        }

        if (keyCode == 38) {
            this.numLocation = Direction.NORTH;
        }

        if (keyCode == 40) {
            this.numLocation = Direction.SOUTH;
        }

        if (this.zoomdialog != null) {
            this.zoomdialog.iz.changeDirection(this.numLocation);
        }

    }

    public void savePointMatrix() {
        PointMatrix p = new PointMatrix(new Frame(), this._bufImage, this.actionBuffImage);
        p.savePoint(this.s);
    }

    public void mouseDragged(MouseEvent e) {
        if (this._state == ImgFrame.State.CLICKED && this.mouseMode == MouseMode.P2P) {
            Graphics g = this.getGraphics();
            g.setXORMode(Color.WHITE);
            g.drawLine(this._startPoint.x, this._startPoint.y, this.previousPoint.x, this.previousPoint.y);
            g.drawLine(this._startPoint.x, this._startPoint.y, e.getX(), e.getY());
            this.previousPoint = e.getPoint();
        }

    }

    public void mouseMoved(MouseEvent e) {
    }

    public BufImageState getBufImageState() {
        return this._bufImageState;
    }

    public void setLanguage(boolean viLang) {
        if (viLang) {
            this.unknow_file_format_img_save = "Định dạng không được hỗ trợ";
            this.file_saved = "Tệp đã lưu: ";
            this.file_not_found = "Không tìm thấy tệp";
        } else {
            this.unknow_file_format_img_save = "Unknow file format.";
            this.file_saved = "File saved: ";
            this.file_not_found = "File not found!";
        }

        Noitifier.printConsole("ImageFrame - setLanguage - viLang:" + viLang);
    }

    public void writePixel() {
        int w = this._bufImage.getWidth();
        int h = this._bufImage.getHeight();
        int[] pixs = new int[w * h];
        ImageProducer pro = this._bufImage.getSource();
        PixelGrabber pg = new PixelGrabber(pro, 0, 0, w, h, pixs, 0, w);

        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Noitifier.printConsole("------------------>got pixs!");
        int in = (int)(Math.random() * (double)100000.0F);
        byte[] b = new byte[]{13, 10};
        File f = new File("D:\\SAVE\\Work\\pix-" + in + ".txt");

        try {
            RandomAccessFile rf = new RandomAccessFile(f, "rw");
            rf.writeUTF("//Ảnh -->>" + this.p + " S:" + w + "x" + h);
            rf.write(b);

            for(int i = 0; i < pixs.length; ++i) {
                rf.writeBytes(Integer.toString(pixs[i]));
                rf.writeBytes(",");
                if (i % 5 == 0) {
                    rf.write(b);
                }
            }

            rf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < 10; ++i) {
            System.out.print("pix " + i + ":" + pixs[i] + "-");
        }

        Noitifier.printConsole("<<<<<--------");
        Noitifier.printConsole("-----------------------------------------------------------------");

        for(int i = pixs.length - 10; i < pixs.length; ++i) {
            System.out.print("pix " + i + ":" + pixs[i] + "-");
        }

        Noitifier.printConsole("<<<<----------");
        Noitifier.printConsole("File is: " + f);
    }

    public void showImg() {
        Image i = Img.creatImg(1);
        Image i2 = Img.creatImg(2);
        Image i3 = Img.creatImg(3);
        Image i4 = Img.creatImg(13);
        Image i5 = Img.creatImg(14);
        Image i6 = Img.creatImg(15);
        Graphics g = this.getGraphics();
        g.drawImage(i, 15, 15, this.imgObserver);
        g.drawImage(i2, 50, 15, this.imgObserver);
        g.drawImage(i3, 90, 15, this.imgObserver);
        g.drawImage(i4, 120, 15, this.imgObserver);
        g.drawImage(i5, 250, 15, this.imgObserver);
        g.drawImage(i6, 385, 15, this.imgObserver);
        Noitifier.printConsole("show Img called");
    }

    public void Test(int mode) {
        Noitifier.printConsole("Test called");
        if (mode == 1) {
            RGBGrayFilter filter = new RGBGrayFilter();
            Image grayImg = this.createImage(new FilteredImageSource(this._bufImage.getSource(), filter));
            Graphics g = this.getGraphics();
            g.drawImage(grayImg, 0, 0, this.w, this.h, this.imgObserver);
        }

        if (mode == 2) {
            RedGrayFilter filter = new RedGrayFilter();
            Image grayImg = this.createImage(new FilteredImageSource(this._bufImage.getSource(), filter));
            Graphics g = this.getGraphics();
            g.drawImage(grayImg, 0, 0, this.w, this.h, this.imgObserver);
        }

        if (mode == 3) {
            BlueGrayFilter filter = new BlueGrayFilter();
            Image grayImg = this.createImage(new FilteredImageSource(this._bufImage.getSource(), filter));
            Graphics g = this.getGraphics();
            g.drawImage(grayImg, 0, 0, this.w, this.h, this.imgObserver);
        }

        if (mode == 4) {
            GreenGrayFilter filter = new GreenGrayFilter();
            Image grayImg = this.createImage(new FilteredImageSource(this._bufImage.getSource(), filter));
            Graphics g = this.getGraphics();
            g.drawImage(grayImg, 0, 0, this.w, this.h, this.imgObserver);
        }

        if (mode == 5) {
            GreenOutGrayFilter filter = new GreenOutGrayFilter();
            Image grayImg = this.createImage(new FilteredImageSource(this._bufImage.getSource(), filter));
            Graphics g = this.getGraphics();
            g.drawImage(grayImg, 0, 0, this.w, this.h, this.imgObserver);
        }

        if (mode == 6) {
            RedOutGrayFilter filter = new RedOutGrayFilter();
            Image grayImg = this.createImage(new FilteredImageSource(this._bufImage.getSource(), filter));
            Graphics g = this.getGraphics();
            g.drawImage(grayImg, 0, 0, this.w, this.h, this.imgObserver);
        }

        if (mode == 7) {
            BlueOutGrayFilter filter = new BlueOutGrayFilter();
            Image grayImg = this.createImage(new FilteredImageSource(this._bufImage.getSource(), filter));
            Graphics g = this.getGraphics();
            g.drawImage(grayImg, 0, 0, this.w, this.h, this.imgObserver);
        }

        if (mode == 8) {
            RGBGrayFilter filter = new RGBGrayFilter(0, 100, 100, 100);
            Image grayImg = this.createImage(new FilteredImageSource(this._bufImage.getSource(), filter));
            Graphics g = this.getGraphics();
            g.drawImage(grayImg, 0, 0, this.w, this.h, this.imgObserver);
        }

        if (mode == 9) {
            RGBGrayFilter filter = new RGBGrayFilter(0, 100, 100, 100, 16);
            Image grayImg = this.createImage(new FilteredImageSource(this._bufImage.getSource(), filter));
            Graphics g = this.getGraphics();
            g.drawImage(grayImg, 0, 0, this.w, this.h, this.imgObserver);
        }

    }

    public void FlipImg(int type) {
        if (type == 0) {
            this._bufImage = this.flip(this._bufImage, 0);
            this.actionBuffImage = this.flip(this.actionBuffImage, 0);
            this.ori = this.flip(this.ori, 0);
            this.repaint();
        }

        if (type == 1) {
            this._bufImage = this.flip(this._bufImage, 1);
            this.actionBuffImage = this.flip(this.actionBuffImage, 1);
            this.ori = this.flip(this.ori, 1);
            this.repaint();
        }

    }

    private BufferedImage flip(BufferedImage _buf, int type) {
        int w = _buf.getWidth();
        int h = _buf.getHeight();
        BufferedImage b = new BufferedImage(w, h, 3);
        Graphics g = b.getGraphics();
        if (type == 0) {
            g.drawImage(_buf, 0, 0, w, h, w, 0, 0, h, this.imgObserver);
        }

        if (type == 1) {
            g.drawImage(_buf, 0, 0, w, h, 0, h, w, 0, this.imgObserver);
        }

        return b;
    }

    public void remakeCursor(int curID) {
        if (curID == 14) {
            this.p2pCursor = CustomCursor.P2P_CURSOR_32;
        }

        if (curID == 13) {
            this.stkCursor = CustomCursor.STICK_CURSOR_32;
        }

    }

    public void setFrame(Frame f) {
        this.parent = f;
    }

    public void setLoca(Point cur) {
        this.setLocation(cur);
    }

    public void undoFilter(FStack filteraction) {
        Graphics2D g = (Graphics2D)this._bufImage.getGraphics();
        g.drawImage(this.ori, 0, 0, this.imgObserver);
        Graphics2D gb = (Graphics2D)this.actionBuffImage.getGraphics();
        gb.drawImage(this.ori, 0, 0, this.imgObserver);

        for(int i = 0; i < this.s.len; ++i) {
            SPoint p = this.s.getAt(i);
            this.redrawPoint(p);
        }

        for(int j = 0; j < this.gridAction.len; ++j) {
            SPoint p = this.gridAction.getAt(j);
            Point clp = new Point(p.X, p.Y);
            this.drawGird(g, clp, p.id, p.c, "redraw");
        }

        for(int i = 0; i < filteraction.len; ++i) {
            FilterProperties ft = filteraction.getAt(i);
            int re = ft.red;
            int gr = ft.gre;
            int bl = ft.blu;
            int al = ft.alp;
            int gra = ft.gra;
            RGBImageFilter filter;
            if (ft.getMode() == 0) {
                filter = new RGBFilter(re, gr, bl, al);
            } else {
                filter = new RGBGrayFilter(re, gr, bl, al, gra);
            }

            Image img = this.createImage(new FilteredImageSource(this._bufImage.getSource(), filter));
            this.setFiltered(img);
        }

        this.paint(this.getGraphics());
    }

    public void redoFilter(FilterProperties ft) {
        Image i = this.createImage(this._bufImage.getSource());
        int a = ft.alp;
        int r = ft.red;
        int g = ft.gre;
        int b = ft.blu;
        int gr = ft.gra;
        if (ft.getMode() == 0) {
            i = this.createImage(new FilteredImageSource(this._bufImage.getSource(), new RGBFilter(r, g, b, a)));
        }

        if (ft.getMode() == -1) {
            i = this.createImage(new FilteredImageSource(this._bufImage.getSource(), new RGBGrayFilter(r, g, b, a, gr)));
        }

        this.setFiltered(i);
    }

    public void setFiltered(Image i2) {
        int W = this.origin.width;
        int H = this.origin.height;
        BufferedImage b = new BufferedImage(W, H, 3);
        int[] pix = new int[W * H];

        for(int i = 0; i < pix.length; ++i) {
            pix[i] = 16777215;
        }

        b.setRGB(0, 0, this.origin.width, this.origin.height, pix, 0, this.origin.width);
        PixelGrabber pg = new PixelGrabber(i2, 0, 0, W, H, pix, 0, W);

        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        b.setRGB(0, 0, this.origin.width, this.origin.height, pix, 0, this.origin.width);
        this._bufImage = b;
        this._bufImageState = BufImageState.MODIFIED;
        this.actionBuffImage = new BufferedImage(W, H, 3);
        this.actionBuffImage.setRGB(0, 0, this.origin.width, this.origin.height, pix, 0, this.origin.width);
        this.repaint();
    }

    public void update(Graphics g) {
        this.drawBgr(g);
        this.paint(g);
        System.out.println("imageFrame update called");
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

    public void previewImage(Image newImg) {
        BufferedImage pre = (BufferedImage)this.createImage(this.w, this.h);
        Graphics g = pre.getGraphics();
        this.drawBgr(g);
        g.drawImage(newImg, 0, 0, this.w, this.h, this.imgObserver);
        this.getGraphics().drawImage(pre, 0, 0, this.w, this.h, this.imgObserver);
    }

    public void affineTranform(Tranform type) {
        if (type == Tranform.ROTATE_CW || type == Tranform.ROTATE_CCW) {
            AffineTransform aft = new AffineTransform();
            if (this.origin.width > this.origin.height) {
                aft.translate((double)((float)(-(this.origin.width - this.origin.height)) / 2.0F), (double)((float)(this.origin.width - this.origin.height) / 2.0F));
            } else {
                aft.translate((double)((float)(this.origin.height - this.origin.width) / 2.0F), (double)((float)(-(this.origin.height - this.origin.width)) / 2.0F));
            }

            if (type == Tranform.ROTATE_CW) {
                aft.quadrantRotate(1, (double)((float)this.origin.width / 2.0F), (double)((float)this.origin.height / 2.0F));
            }

            if (type == Tranform.ROTATE_CCW) {
                aft.quadrantRotate(-1, (double)((float)this.origin.width / 2.0F), (double)((float)this.origin.height / 2.0F));
            }

            AffineTransformOp aftOp = new AffineTransformOp(aft, 3);
            BufferedImage des = aftOp.filter(this._bufImage, (BufferedImage)null);
            this.tranformAction.push(aft);
            this.tranformReAction.clear();
            int temp = this.w;
            this.w = this.h;
            this.h = temp;
            this.setTranformed(des);
            this.actionBuffImage = aftOp.filter(this.actionBuffImage, (BufferedImage)null);
            this.ori = aftOp.filter(this.ori, (BufferedImage)null);
            Noitifier.printConsole("Affine rotate 90: des.w:" + des.getWidth() + "; des.h:" + des.getHeight());
            Noitifier.printConsole("w:" + this.w + "; h:" + this.h);
            Noitifier.printConsole("Aft type:" + aft.getType());
            double[] matrix = new double[6];
            aft.getMatrix(matrix);
            Noitifier.printConsole("Aft matrix - m00:" + matrix[0]);
            Noitifier.printConsole("Aft matrix - m10:" + matrix[1]);
            Noitifier.printConsole("Aft matrix - m01:" + matrix[2]);
            Noitifier.printConsole("Aft matrix - m11:" + matrix[3]);
            Noitifier.printConsole("Aft matrix - m02:" + matrix[4]);
            Noitifier.printConsole("Aft matrix - m12:" + matrix[5]);
        }

        if (type == Tranform.ROTATE_180) {
            AffineTransform aft = new AffineTransform();
            aft.quadrantRotate(2, (double)((float)this.origin.width / 2.0F), (double)((float)this.origin.height / 2.0F));
            AffineTransformOp aftOp = new AffineTransformOp(aft, 3);
            BufferedImage des = aftOp.filter(this._bufImage, (BufferedImage)null);
            this.tranformAction.push(aft);
            this.tranformReAction.clear();
            this.setTranformed(des);
            this.actionBuffImage = aftOp.filter(this.actionBuffImage, (BufferedImage)null);
            this.ori = aftOp.filter(this.ori, (BufferedImage)null);
            Noitifier.printConsole("Affine Rotate 180: des.w:" + des.getWidth() + "; des.h:" + des.getHeight());
            Noitifier.printConsole("w:" + this.w + "; h:" + this.h);
            Noitifier.printConsole("Aft type:" + aft.getType());
            double[] matrix = new double[6];
            aft.getMatrix(matrix);
            Noitifier.printConsole("Aft matrix - m00:" + matrix[0]);
            Noitifier.printConsole("Aft matrix - m10:" + matrix[1]);
            Noitifier.printConsole("Aft matrix - m01:" + matrix[2]);
            Noitifier.printConsole("Aft matrix - m11:" + matrix[3]);
            Noitifier.printConsole("Aft matrix - m02:" + matrix[4]);
            Noitifier.printConsole("Aft matrix - m12:" + matrix[5]);
        }

    }

    public void setTranformed(BufferedImage des) {
        this._bufImage = des;
        if (this.zoomdialog != null) {
            this.zoomdialog.updateBufer(this._bufImage);
        }

        this.origin.width = this._bufImage.getWidth();
        this.origin.height = this._bufImage.getHeight();
        this.parent.setTitle(this.APP_NAME + this.p + " - " + this._bufImage.getWidth() + "x" + this._bufImage.getHeight());
        this.repaint();
    }

    public void setAPP_NAME(String appName) {
        this.APP_NAME = appName;
    }

    public Dimension getOriginDimension() {
        return this.origin;
    }

    public void reSizeImg(double xrs, double yrs, int type) {
        AffineTransform aft = new AffineTransform();
        aft.setToScale(xrs, yrs);
        int Afftype = 3;
        if (type == 0) {
            Afftype = 3;
        }

        if (type == 1) {
            Afftype = 2;
        }

        if (type == 2) {
            Afftype = 1;
        }

        AffineTransformOp aftOp = new AffineTransformOp(aft, Afftype);
        BufferedImage des = aftOp.filter(this._bufImage, (BufferedImage)null);
        this.tranformAction.push(aft);
        this.w = (int)Math.round((double)this.w * xrs);
        this.h = (int)Math.round((double)this.h * yrs);
        this.setTranformed(des);
        this.actionBuffImage = aftOp.filter(this.actionBuffImage, (BufferedImage)null);
        this.ori = aftOp.filter(this.ori, (BufferedImage)null);
        Noitifier.printConsole("Affine Resize: des.w:" + des.getWidth() + "; des.h:" + des.getHeight());
        Noitifier.printConsole("w:" + this.w + "; h:" + this.h);
    }

    public void undoTranForm(int type) {
        AffineTransform A = (AffineTransform)this.tranformAction.pop();
        this.tranformReAction.push(A);
        AffineTransform aft = new AffineTransform();

        try {
            aft = A.createInverse();
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }

        int Afftype = 3;
        if (type == 0) {
            Afftype = 3;
        }

        if (type == 1) {
            Afftype = 2;
        }

        if (type == 2) {
            Afftype = 1;
        }

        AffineTransformOp aftOp = new AffineTransformOp(aft, Afftype);
        BufferedImage des = aftOp.filter(this._bufImage, (BufferedImage)null);
        if (aft.getType() == 2 || aft.getType() == 4 || aft.getType() == 6) {
            this.w = (int)((double)this.w * aft.getScaleX());
            this.h = (int)((double)this.h * aft.getScaleY());
        }

        double[] matrix = new double[6];
        aft.getMatrix(matrix);
        if (matrix[1] != (double)0.0F && matrix[2] != (double)0.0F && aft.getType() != 2) {
            int temp = this.w;
            this.w = this.h;
            this.h = temp;
            Noitifier.printConsole("undoTranform - dimension updated");
        }

        Noitifier.printConsole("Aft type:" + aft.getType());
        this.setTranformed(des);
        this.actionBuffImage = aftOp.filter(this.actionBuffImage, (BufferedImage)null);
        this.ori = aftOp.filter(this.ori, (BufferedImage)null);
        Noitifier.printConsole("Undo Tranform: des.w:" + des.getWidth() + "; des.h:" + des.getHeight());
        Noitifier.printConsole("w:" + this.w + "; h:" + this.h);
    }

    public void redoTranform(int type) {
        AffineTransform aft = (AffineTransform)this.tranformReAction.pop();
        this.tranformAction.push(aft);
        if (aft.getType() != 2 && aft.getType() != 4 && aft.getType() != 6) {
            AffineTransformOp aftOp = new AffineTransformOp(aft, 3);
            BufferedImage des = aftOp.filter(this._bufImage, (BufferedImage)null);
            this.setTranformed(des);
            this.actionBuffImage = aftOp.filter(this.actionBuffImage, (BufferedImage)null);
            this.ori = aftOp.filter(this.ori, (BufferedImage)null);
            double[] matrix = new double[6];
            aft.getMatrix(matrix);
            if (matrix[1] != (double)0.0F && matrix[2] != (double)0.0F && aft.getType() != 2) {
                int temp = this.w;
                this.w = this.h;
                this.h = temp;
                Noitifier.printConsole("redoTranform - dimension updated");
            }
        } else {
            double xrs = aft.getScaleX();
            double yrs = aft.getScaleY();
            this.reSizeImg(xrs, yrs, type);
        }

        Noitifier.printConsole("redoTranform - Aft type:" + aft.getType());
    }

    public void setPopupMenu(MenuItem menu1, MenuItem menu2, Menu menu3) {
        this.undo = menu1;
        this.redo = menu2;
        this.menuImage = menu3;
        this.CW90 = new MenuItem("Rotate CW 90");
        this.CCW90 = new MenuItem("Rotate CCW 90");
        this.R180 = new MenuItem("Rotate 180");
        this.FH = new MenuItem("Flip Horizontal");
        this.FV = new MenuItem("Flip Vertical");
        this.RZ = new MenuItem("Resize");
        this.menuImage.add(this.CW90);
        this.menuImage.add(this.CCW90);
        this.menuImage.add(this.R180);
        this.menuImage.add(this.FH);
        this.menuImage.add(this.FV);
        this.menuImage.addSeparator();
        this.menuImage.add(this.RZ);
        this.pop = new PopupMenu("Image");
        this.pop.add(this.undo);
        this.pop.add(this.redo);
        this.pop.add(this.menuImage);
        this.pop.addSeparator();
        this.openWith = new MenuItem("Open in paint");
        this.pop.add(this.openWith);
        this.add(this.pop);
    }

    private static enum State {
        CLICKED,
        RELEASED,
        HAD_NEW_PATH;
    }

    private static enum PathState {
        HAD_NEW_PATH,
        PATH_USED;
    }
}
