package transform;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;

public class ResizeBox extends Dialog implements ActionListener, ItemListener, KeyListener {
    private static final long serialVersionUID = 1L;
    public Dialog box;
    private int W;
    private int H;
    private Checkbox percent;
    private Checkbox pixel;
    private Checkbox ratioChk;
    private TextField widthTf;
    private TextField heightTf;
    private JButton ok;
    private JButton cancel;
    private Choice typeCh;
    public String action = "CANCEL";
    public double Xrs;
    public double Yrs;
    public int type;
    private int Winput;
    private int Hinput;
    private transform.ResizeBox.Mode m;
    private boolean ratio;

    public ResizeBox(Frame f, String title, boolean isModal) {
        super(f);
        this.m = transform.ResizeBox.Mode.PERCENT;
        this.ratio = true;
        this.box = new Dialog(f, title, isModal);
        this.creatGUI();
        this.processEvent();
    }

    public void setImgDimension(int w, int h) {
        this.W = w;
        this.H = h;
    }

    private void creatGUI() {
        this.box.setBounds(500, 100, 230, 280);
        this.box.setLayout(new GridLayout());
        Panel mainPn = new Panel();
        mainPn.setLayout(new GridLayout(6, 0, 5, 5));
        this.box.add(mainPn);
        Panel modePn = new Panel();
        modePn.setLayout(new GridLayout(1, 3, 5, 0));
        modePn.add(new Label("By:"));
        CheckboxGroup g = new CheckboxGroup();
        this.percent = new Checkbox("Percentage", true, g);
        this.pixel = new Checkbox("Pixel", false, g);
        modePn.add(this.percent);
        modePn.add(this.pixel);
        Panel wPn = new Panel();
        wPn.setLayout(new GridLayout(1, 0, 5, 0));
        wPn.add(new Label("\t \t \t \t \t \t \t \t Width:"));
        this.widthTf = new TextField(10);
        this.widthTf.setText("100");
        wPn.add(this.widthTf);
        Panel hPn = new Panel();
        hPn.setLayout(new GridLayout(1, 0, 5, 0));
        hPn.add(new Label("\t \t \t \t \t \t \t \t Height:"));
        this.heightTf = new TextField(10);
        this.heightTf.setText("100");
        hPn.add(this.heightTf);
        Panel ratioPn = new Panel();
        this.ratioChk = new Checkbox("Maintain aspect ratio", true);
        ratioPn.add(this.ratioChk);
        Panel btPn = new Panel();
        btPn.setLayout(new GridLayout(1, 0, 5, 0));
        this.ok = new JButton("Ok");
        this.cancel = new JButton("Cancel");
        btPn.add(this.ok);
        btPn.add(this.cancel);
        Panel typePn = new Panel();
        typePn.setLayout(new GridLayout(1, 0, 5, 0));
        typePn.add(new Label("Interpolation Type:"));
        this.typeCh = new Choice();
        this.typeCh.addItem("Bicubid");
        this.typeCh.addItem("Bilinear");
        this.typeCh.addItem("Nearest neighbor");
        typePn.add(this.typeCh);
        mainPn.add(modePn);
        mainPn.add(wPn);
        mainPn.add(hPn);
        mainPn.add(ratioPn);
        mainPn.add(typePn);
        mainPn.add(btPn);
        this.Winput = 100;
        this.Hinput = 100;
    }

    private void processEvent() {
        this.box.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                ResizeBox.this.box.dispose();
            }
        });
        this.ok.addActionListener(this);
        this.cancel.addActionListener(this);
        this.widthTf.addKeyListener(this);
        this.heightTf.addKeyListener(this);
        this.percent.addItemListener(this);
        this.pixel.addItemListener(this);
        this.typeCh.addItemListener(this);
        this.ratioChk.addItemListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.ok) {
            this.action = "OK";
            this.Winput = Integer.parseInt(this.widthTf.getText());
            this.Hinput = Integer.parseInt(this.heightTf.getText());
            if (this.m == ResizeBox.Mode.PERCENT) {
                this.Xrs = (double)((float)this.Winput / 100.0F);
                this.Yrs = (double)((float)this.Hinput / 100.0F);
            }

            if (this.m == ResizeBox.Mode.PIXEL) {
                this.Xrs = (double)this.Winput / (double)this.W;
                this.Yrs = (double)this.Hinput / (double)this.H;
            }
        }

        this.box.dispose();
    }

    public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == 1 && ie.getSource() == this.typeCh) {
            this.type = this.typeCh.getSelectedIndex();
        }

        if (ie.getStateChange() == 1 && ie.getSource() == this.percent) {
            this.m = ResizeBox.Mode.PERCENT;
            this.widthTf.setText("100");
            this.heightTf.setText("100");
        }

        if (ie.getStateChange() == 1 && ie.getSource() == this.pixel) {
            this.m = ResizeBox.Mode.PIXEL;
            this.widthTf.setText(Integer.toString(this.W));
            this.heightTf.setText(Integer.toString(this.H));
        }

        if (ie.getStateChange() == 1 && ie.getSource() == this.ratioChk) {
            this.ratio = true;
            if (this.m == ResizeBox.Mode.PERCENT) {
                this.widthTf.setText(Integer.toString(this.Winput));
                this.heightTf.setText(Integer.toString(this.Winput));
            }

            if (this.m == ResizeBox.Mode.PIXEL) {
                this.widthTf.setText(Integer.toString(this.W));
                this.heightTf.setText(Integer.toString(this.H));
            }
        }

        if (ie.getStateChange() == 2 && ie.getSource() == this.ratioChk) {
            this.ratio = false;
        }

    }

    public void keyPressed(KeyEvent arg0) {
    }

    public void keyReleased(KeyEvent ke) {
        if (this.m == ResizeBox.Mode.PERCENT) {
            this.processPercentInput(ke);
        }

        if (this.m == ResizeBox.Mode.PIXEL) {
            this.processPixelInput(ke);
        }

    }

    private void processPixelInput(KeyEvent ke) {
        double r = (double)this.W / (double)this.H;
        if (this.ratio) {
            if (ke.getSource() == this.widthTf) {
                try {
                    this.widthTf.setBackground(Color.white);
                    this.heightTf.setBackground(Color.white);
                    this.Winput = Integer.parseInt(this.widthTf.getText());
                } catch (NumberFormatException var6) {
                    this.widthTf.setBackground(new Color(255, 150, 150));
                    this.heightTf.setBackground(new Color(255, 150, 150));
                }

                this.Hinput = (int)Math.round((double)this.Winput / r);
                this.heightTf.setText(Integer.toString(this.Hinput));
            }

            if (ke.getSource() == this.heightTf) {
                try {
                    this.widthTf.setBackground(Color.white);
                    this.heightTf.setBackground(Color.white);
                    this.Hinput = Integer.parseInt(this.heightTf.getText());
                } catch (NumberFormatException var5) {
                    this.widthTf.setBackground(new Color(255, 150, 150));
                    this.heightTf.setBackground(new Color(255, 150, 150));
                }

                this.Winput = (int)Math.round((double)this.Hinput * r);
                this.widthTf.setText(Integer.toString(this.Winput));
            }
        } else {
            this.ratioUnChk(ke);
        }

    }

    public void processPercentInput(KeyEvent ke) {
        if (this.ratio) {
            if (ke.getSource() == this.widthTf) {
                try {
                    this.widthTf.setBackground(Color.white);
                    this.heightTf.setBackground(Color.white);
                    this.Winput = Integer.parseInt(this.widthTf.getText());
                } catch (NumberFormatException var4) {
                    this.widthTf.setBackground(new Color(255, 150, 150));
                    this.heightTf.setBackground(new Color(255, 150, 150));
                }

                this.Hinput = this.Winput;
                this.heightTf.setText(Integer.toString(this.Hinput));
            }

            if (ke.getSource() == this.heightTf) {
                try {
                    this.widthTf.setBackground(Color.white);
                    this.heightTf.setBackground(Color.white);
                    this.Hinput = Integer.parseInt(this.heightTf.getText());
                } catch (NumberFormatException var3) {
                    this.widthTf.setBackground(new Color(255, 150, 150));
                    this.heightTf.setBackground(new Color(255, 150, 150));
                }

                this.Winput = this.Hinput;
                this.widthTf.setText(Integer.toString(this.Winput));
            }
        } else {
            this.ratioUnChk(ke);
        }

    }

    public void ratioUnChk(KeyEvent ke) {
        if (ke.getSource() == this.widthTf) {
            try {
                this.widthTf.setBackground(Color.white);
                this.Winput = Integer.parseInt(this.widthTf.getText());
            } catch (NumberFormatException var4) {
                this.widthTf.setBackground(new Color(255, 150, 150));
            }
        }

        if (ke.getSource() == this.heightTf) {
            try {
                this.heightTf.setBackground(Color.white);
                this.Hinput = Integer.parseInt(this.heightTf.getText());
            } catch (NumberFormatException var3) {
                this.heightTf.setBackground(new Color(255, 150, 150));
            }
        }

    }

    public void keyTyped(KeyEvent arg0) {
    }

    private static enum Mode {
        PERCENT,
        PIXEL;
    }
}
