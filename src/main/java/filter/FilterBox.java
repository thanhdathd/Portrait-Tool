package filter;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import userpackage.ImgFrame;
import userpackage.Noitifier;

public class FilterBox extends Dialog implements MouseListener, ItemListener, ActionListener, KeyListener {
    private static final long serialVersionUID = 1L;
    public Dialog box;
    private BufferedImage buf;
    private TextField redTf;
    private TextField bluTf;
    private TextField greTf;
    private TextField alpTf;
    private TextField graTf;
    private JSlider redSld;
    private JSlider greSld;
    private JSlider bluSld;
    private JSlider alpSld;
    private JSlider graSld;
    private JLabel redLb;
    private JLabel greLb;
    private JLabel bluLb;
    private JLabel alpLb;
    private JLabel graLb;
    private JButton ok;
    private JButton cancel;
    private VieWerPn viewer;
    private Image newImg;
    private Choice grChoice;
    private Choice chlChoice;
    private int red;
    private int gre;
    private int blu;
    private int alp;
    private int gra;
    public String action = "CANCEL";
    private boolean valueChange = false;
    private Mode mode;
    private Filter filter;
    private ImgFrame imageFrame;

    public FilterBox(Frame f, String title, boolean isModal, BufferedImage buf) {
        super(f);
        this.mode = FilterBox.Mode.RGB;
        this.filter = FilterBox.Filter.RGB;
        this.box = new Dialog(f, title, isModal);
        this.buf = buf;
        this.creatGUI();
    }

    private void creatGUI() {
        this.box.setBounds(300, 100, 500, 400);
        this.box.setLayout(new BorderLayout());
        this.viewer = new VieWerPn(this.buf);
        Panel ctrPn = new Panel();
        Panel viewPanel = new Panel();
        Panel grayModelPn = new Panel();
        grayModelPn.setLayout(new BorderLayout());
        JLabel grayLabel = new JLabel("Gray filter:");
        Panel outPutChanelPn = new Panel();
        outPutChanelPn.setLayout(new BorderLayout());
        JLabel opchlLabel = new JLabel("Output:");
        this.chlChoice = new Choice();
        this.chlChoice.addItem("Gray");
        this.chlChoice.addItem("RGB");
        this.chlChoice.select(1);
        this.grChoice = new Choice();
        this.grChoice.addItem("Default RGB");
        this.grChoice.addItem("RGB Black & White");
        this.grChoice.addItem("Gray with Blue filter");
        this.grChoice.addItem("Gray with Red filter");
        this.grChoice.addItem("Gray with Green filter");
        this.grChoice.addItem("Gray with Orange filter");
        this.grChoice.addItem("Gray with Yelow filter");
        this.grChoice.addItem("Red off");
        this.grChoice.addItem("Green off");
        this.grChoice.addItem("Blue off");
        this.grChoice.addItem("--- Custom ---");
        grayModelPn.add(this.grChoice, "Center");
        outPutChanelPn.add(opchlLabel, "West");
        outPutChanelPn.add(this.chlChoice, "Center");
        viewPanel.setLayout(new BorderLayout(2, 5));
        Panel btPn = new Panel();
        btPn.setLayout(new GridLayout(10, 1, 3, 3));
        btPn.setPreferredSize(new Dimension(175, 200));
        btPn.add(grayLabel);
        btPn.add(grayModelPn);
        btPn.add(outPutChanelPn);
        btPn.add(new JLabel("wait 2"));
        btPn.add(new JLabel("wait 3"));
        btPn.add(new JLabel("wait 4"));
        btPn.add(new JLabel("wait 5"));
        btPn.add(new JLabel("wait 6"));
        Panel buttonPn = new Panel();
        buttonPn.setLayout(new GridLayout(1, 0, 3, 3));
        btPn.add(buttonPn);
        this.ok = new JButton("Ok");
        this.cancel = new JButton("Cancel");
        buttonPn.add(this.ok);
        buttonPn.add(this.cancel);
        ctrPn.setPreferredSize(new Dimension(300, 150));
        Panel redScrPn = new Panel();
        Panel greScrPn = new Panel();
        Panel bluScrPn = new Panel();
        Panel graScrPn = new Panel();
        Panel alpScrPn = new Panel();
        this.redTf = new TextField(4);
        this.greTf = new TextField(4);
        this.bluTf = new TextField(4);
        this.graTf = new TextField(4);
        this.alpTf = new TextField(4);
        this.redSld = new JSlider(0, 0, 200, 100);
        this.greSld = new JSlider(0, 0, 200, 100);
        this.bluSld = new JSlider(0, 0, 200, 100);
        this.alpSld = new JSlider(0, 0, 100, 100);
        this.graSld = new JSlider(0, 0, 128, 128);
        this.redLb = new JLabel("R:");
        this.greLb = new JLabel("G:");
        this.bluLb = new JLabel("B:");
        this.alpLb = new JLabel("A:");
        this.graLb = new JLabel("Gray:");
        this.redLb.setForeground(Color.RED);
        this.greLb.setForeground(Color.GREEN);
        this.bluLb.setForeground(Color.BLUE);
        this.alpLb.setForeground(Color.GRAY);
        this.redTf.setText(Integer.toString(this.redSld.getValue()) + "%");
        this.greTf.setText(Integer.toString(this.greSld.getValue()) + "%");
        this.bluTf.setText(Integer.toString(this.bluSld.getValue()) + "%");
        this.alpTf.setText(Integer.toString(this.alpSld.getValue()) + "%");
        this.graTf.setText(Integer.toString(this.graSld.getValue()));
        this.redTf.setPreferredSize(new Dimension(70, 30));
        this.greTf.setPreferredSize(new Dimension(70, 30));
        this.bluTf.setPreferredSize(new Dimension(70, 30));
        this.alpTf.setPreferredSize(new Dimension(70, 30));
        this.graTf.setPreferredSize(new Dimension(70, 30));
        redScrPn.setLayout(new BorderLayout());
        greScrPn.setLayout(new BorderLayout());
        bluScrPn.setLayout(new BorderLayout());
        alpScrPn.setLayout(new BorderLayout());
        graScrPn.setLayout(new BorderLayout());
        redScrPn.add(this.redLb, "West");
        greScrPn.add(this.greLb, "West");
        bluScrPn.add(this.bluLb, "West");
        alpScrPn.add(this.alpLb, "West");
        graScrPn.add(this.graLb, "West");
        redScrPn.add(this.redSld, "Center");
        greScrPn.add(this.greSld, "Center");
        bluScrPn.add(this.bluSld, "Center");
        alpScrPn.add(this.alpSld, "Center");
        graScrPn.add(this.graSld, "Center");
        redScrPn.add(this.redTf, "East");
        greScrPn.add(this.greTf, "East");
        bluScrPn.add(this.bluTf, "East");
        alpScrPn.add(this.alpTf, "East");
        graScrPn.add(this.graTf, "East");
        ctrPn.setLayout(new GridLayout(0, 1, 3, 3));
        ctrPn.add(redScrPn);
        ctrPn.add(greScrPn);
        ctrPn.add(bluScrPn);
        ctrPn.add(alpScrPn);
        ctrPn.add(graScrPn);
        viewPanel.add(btPn, "East");
        viewPanel.add(this.viewer, "Center");
        this.box.add(viewPanel, "Center");
        this.box.add(ctrPn, "Last");
        this.box.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                FilterBox.this.box.dispose();
            }
        });
        this.addListener();
    }

    private void addListener() {
        this.redSld.addMouseListener(this);
        this.greSld.addMouseListener(this);
        this.bluSld.addMouseListener(this);
        this.alpSld.addMouseListener(this);
        this.graSld.addMouseListener(this);
        this.grChoice.addItemListener(this);
        this.chlChoice.addItemListener(this);
        this.ok.addActionListener(this);
        this.cancel.addActionListener(this);
        this.redSld.addKeyListener(this);
        this.greSld.addKeyListener(this);
        this.bluSld.addKeyListener(this);
        this.alpSld.addKeyListener(this);
        this.graSld.addKeyListener(this);
        MyChangeListener clsnr = new MyChangeListener(this);
        this.redSld.addChangeListener(clsnr);
        this.greSld.addChangeListener(clsnr);
        this.bluSld.addChangeListener(clsnr);
        this.alpSld.addChangeListener(clsnr);
        this.graSld.addChangeListener(clsnr);
    }

    public void setImg(BufferedImage buf) {
        this.buf = buf;
    }

    public Image getImage() {
        return this.newImg;
    }

    public void mouseClicked(MouseEvent arg0) {
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent arg0) {
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == this.redSld || e.getSource() == this.greSld || e.getSource() == this.bluSld || e.getSource() == this.alpSld || e.getSource() == this.graSld) {
            this.red = this.redSld.getValue();
            this.gre = this.greSld.getValue();
            this.blu = this.bluSld.getValue();
            int a = this.alpSld.getValue();
            this.alp = a;
            if (this.mode == FilterBox.Mode.RGB) {
                RGBFilter filter = new RGBFilter(this.red, this.gre, this.blu, this.alp);
                this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), filter));
            }

            if (this.mode == FilterBox.Mode.BW) {
                this.newImg = this.grayFilter(this.buf.getSource(), this.red, this.gre, this.blu, this.alp);
            }

            this.grChoice.select(10);
            this.itemStateChanged(new ItemEvent(this.grChoice, 701, this.grChoice.getItem(10), 1));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            Noitifier.printConsole("filtered:" + this.alp + " - " + this.red + " - " + this.gre + " - " + this.blu);
        }

    }

    public void itemStateChanged(ItemEvent e) {
        String itemLabel = (String)e.getItem();
        if (itemLabel == "Default RGB") {
            this.newImg = this.createImage(this.buf.getSource());
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.redSld.setValue(100);
            this.greSld.setValue(100);
            this.bluSld.setValue(100);
            this.alpSld.setValue(100);
            this.graSld.setValue(128);
            this.mode = FilterBox.Mode.RGB;
            this.filter = FilterBox.Filter.RGB;
            this.chlChoice.select(1);
        }

        if (itemLabel == "RGB Black & White") {
            this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), new RGBGrayFilter()));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.redSld.setValue(100);
            this.greSld.setValue(100);
            this.bluSld.setValue(100);
            this.alpSld.setValue(100);
            this.graSld.setValue(128);
            this.mode = FilterBox.Mode.BW;
            this.filter = FilterBox.Filter.RGB;
            this.chlChoice.select(0);
        }

        if (itemLabel == "Gray with Blue filter") {
            this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), new BlueOutGrayFilter()));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.redSld.setValue(0);
            this.greSld.setValue(0);
            this.bluSld.setValue(100);
            this.alpSld.setValue(100);
            this.graSld.setValue(128);
            this.mode = FilterBox.Mode.BW;
            this.filter = FilterBox.Filter.RGB;
            this.chlChoice.select(0);
        }

        if (itemLabel == "Gray with Red filter") {
            this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), new RedOutGrayFilter()));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.redSld.setValue(100);
            this.greSld.setValue(0);
            this.bluSld.setValue(0);
            this.alpSld.setValue(100);
            this.graSld.setValue(128);
            this.mode = FilterBox.Mode.BW;
            this.filter = FilterBox.Filter.RGB;
            this.chlChoice.select(0);
        }

        if (itemLabel == "Gray with Green filter") {
            this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), new GreenOutGrayFilter()));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.redSld.setValue(0);
            this.greSld.setValue(100);
            this.bluSld.setValue(0);
            this.alpSld.setValue(100);
            this.graSld.setValue(128);
            this.mode = FilterBox.Mode.BW;
            this.filter = FilterBox.Filter.RGB;
            this.chlChoice.select(0);
        }

        if (itemLabel == "Gray with Orange filter") {
            this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), new OrangeOutGrayFilter()));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.redSld.setValue(50);
            this.greSld.setValue(50);
            this.bluSld.setValue(0);
            this.alpSld.setValue(100);
            this.graSld.setValue(128);
            this.mode = FilterBox.Mode.BW;
            this.filter = FilterBox.Filter.ORANGE;
            this.chlChoice.select(0);
        }

        if (itemLabel == "Gray with Yelow filter") {
            this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), new YelowOutGrayFilter()));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.redSld.setValue(34);
            this.greSld.setValue(66);
            this.bluSld.setValue(0);
            this.alpSld.setValue(100);
            this.graSld.setValue(128);
            this.mode = FilterBox.Mode.BW;
            this.filter = FilterBox.Filter.YELOW;
            this.chlChoice.select(0);
        }

        if (itemLabel == "Red off") {
            this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), new RedGrayFilter()));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.redSld.setValue(0);
            this.greSld.setValue(100);
            this.bluSld.setValue(100);
            this.alpSld.setValue(100);
            this.graSld.setValue(128);
            this.mode = FilterBox.Mode.BW;
            this.filter = FilterBox.Filter.RGB;
            this.chlChoice.select(0);
        }

        if (itemLabel == "Green off") {
            this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), new GreenGrayFilter()));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.redSld.setValue(100);
            this.greSld.setValue(0);
            this.bluSld.setValue(100);
            this.alpSld.setValue(100);
            this.graSld.setValue(128);
            this.mode = FilterBox.Mode.BW;
            this.filter = FilterBox.Filter.RGB;
            this.chlChoice.select(0);
        }

        if (itemLabel == "Blue off") {
            this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), new BlueGrayFilter()));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.redSld.setValue(100);
            this.greSld.setValue(100);
            this.bluSld.setValue(0);
            this.alpSld.setValue(100);
            this.graSld.setValue(128);
            this.mode = FilterBox.Mode.BW;
            this.filter = FilterBox.Filter.RGB;
            this.chlChoice.select(0);
        }

        if (itemLabel == "Gray") {
            this.red = this.redSld.getValue();
            this.gre = this.greSld.getValue();
            this.blu = this.bluSld.getValue();
            this.alp = this.alpSld.getValue();
            this.newImg = this.grayFilter(this.buf.getSource(), this.red, this.gre, this.blu, this.alp);
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.mode = FilterBox.Mode.BW;
            this.filter = FilterBox.Filter.RGB;
        }

        if (itemLabel == "RGB") {
            this.red = this.redSld.getValue();
            this.gre = this.greSld.getValue();
            this.blu = this.bluSld.getValue();
            this.alp = this.alpSld.getValue();
            this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), new RGBFilter(this.red, this.gre, this.blu, this.alp)));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.mode = FilterBox.Mode.RGB;
            this.filter = FilterBox.Filter.RGB;
            this.grChoice.select(0);
        }

    }

    public Image grayFilter(ImageProducer pro, int r, int g, int b, int a) {
        if (this.filter == FilterBox.Filter.ORANGE) {
            r *= 2;
            g *= 2;
            b *= 2;
        }

        if (this.filter == FilterBox.Filter.YELOW) {
            r *= 2;
            g *= 2;
            b *= 2;
        }

        this.gra = this.graSld.getValue();
        Noitifier.printConsole("Gray Filter called - " + r + ";" + g + ";" + b + ";" + a + ";" + this.gra);
        return this.gra < 128 ? this.createImage(new FilteredImageSource(pro, new RGBGrayFilter(r, g, b, a, this.gra))) : this.createImage(new FilteredImageSource(pro, new RGBGrayFilter(r, g, b, a)));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.ok) {
            this.action = "OK";
            this.box.dispose();
        }

        if (e.getSource() == this.cancel) {
            this.action = "CANCEL";
            this.box.dispose();
        }

    }

    public void keyPressed(KeyEvent arg0) {
    }

    public void keyTyped(KeyEvent arg0) {
    }

    public void keyReleased(KeyEvent evt) {
        if (this.valueChange) {
            RGBImageFilter filter;
            if (this.mode == FilterBox.Mode.RGB) {
                filter = new RGBFilter(this.red, this.gre, this.blu, this.alp);
            } else {
                filter = new RGBGrayFilter(this.red, this.gre, this.blu, this.alp, this.gra);
            }

            this.newImg = this.createImage(new FilteredImageSource(this.buf.getSource(), filter));
            this.viewer.updateImg(this.newImg);
            this.imageFrame.previewImage(this.newImg);
            this.valueChange = false;
        }

    }

    public FilterProperties getFilterProperties() {
        this.red = this.redSld.getValue();
        this.gre = this.greSld.getValue();
        this.blu = this.bluSld.getValue();
        this.alp = this.alpSld.getValue();
        this.gra = this.graSld.getValue();
        if (this.filter == FilterBox.Filter.ORANGE || this.filter == FilterBox.Filter.YELOW) {
            this.red *= 2;
            this.blu *= 2;
            this.gre *= 2;
        }

        return this.mode == FilterBox.Mode.RGB ? new FilterProperties(this.red, this.gre, this.blu, this.alp, this.gra, 0) : new FilterProperties(this.red, this.gre, this.blu, this.alp, this.gra, -1);
    }

    public void setConten(ImgFrame imageFrame) {
        this.imageFrame = imageFrame;
    }

    public static enum Mode {
        BW,
        RGB;
    }

    public static enum Filter {
        RGB,
        ORANGE,
        YELOW;
    }

    public class MyChangeListener implements ChangeListener {
        FilterBox fb;

        public MyChangeListener(FilterBox f) {
            this.fb = f;
        }

        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == this.fb.redSld) {
                this.fb.redTf.setText(Integer.toString(this.fb.redSld.getValue()) + " %");
                this.fb.red = this.fb.redSld.getValue();
            }

            if (e.getSource() == this.fb.greSld) {
                this.fb.greTf.setText(Integer.toString(this.fb.greSld.getValue()) + " %");
                this.fb.gre = this.fb.greSld.getValue();
            }

            if (e.getSource() == this.fb.bluSld) {
                this.fb.bluTf.setText(Integer.toString(this.fb.bluSld.getValue()) + " %");
                this.fb.blu = this.fb.bluSld.getValue();
            }

            if (e.getSource() == this.fb.alpSld) {
                this.fb.alpTf.setText(Integer.toString(this.fb.alpSld.getValue()) + " %");
                int a = this.fb.alpSld.getValue();
                this.fb.alp = a;
            }

            if (e.getSource() == this.fb.graSld) {
                this.fb.graTf.setText(Integer.toString(this.fb.graSld.getValue()));
                this.fb.gra = this.fb.graSld.getValue();
            }

            this.fb.valueChange = true;
        }
    }
}
