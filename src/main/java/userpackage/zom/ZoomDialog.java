package userpackage.zom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Rectangle;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import user.Enum.Direction;
import userpackage.ImgFrame;
import userpackage.Noitifier;

public class ZoomDialog extends Dialog implements ActionListener, KeyListener, MouseListener {
    private static final long serialVersionUID = 11L;
    public Dialog zom;
    public ImgZoom iz;
    Panel mainPanel;
    public TextField texf;
    public TextField zoomTexf;
    public TextField scale;
    TextField scl;
    public JCheckBox absoluteUnit;
    public JCheckBox cmIndicate;
    public boolean absoluteUnit_check;
    public boolean cmIndicate_check;
    public JButton close;
    public JButton setScale;
    public boolean closed = false;
    public boolean viLang;
    private JLabel scale_input_label;
    private ImgFrame imageFrame;

    public ZoomDialog(Frame f, String title, boolean isModal, BufferedImage buf, Direction numLocation, boolean viLang, boolean ab, boolean cm) {
        super(f);
        this.absoluteUnit_check = ab;
        this.cmIndicate_check = cm;
        this.viLang = viLang;
        this.mainPanel = new Panel();
        this.zom = new Dialog(f, title, isModal);
        this.zom.setLayout(new BorderLayout(0, 5));
        this.zom.setFocusableWindowState(false);
        this.close = new JButton("Close");
        this.setScale = new JButton("Set Scale");
        if (viLang) {
            this.close.setText("Đóng");
            this.setScale.setText("Đặt tỷ lệ");
        }

        this.close.addActionListener(this);
        this.setScale.addActionListener(this);
        this.setScale.setFocusable(false);
        this.close.setFocusable(false);
        this.iz = new ImgZoom(buf);
        this.iz.changeDirection(numLocation);
        this.texf = new TextField(15);
        this.scale = new TextField(15);
        this.absoluteUnit = new JCheckBox("Absolute Unit");
        this.cmIndicate = new JCheckBox("Cm Indicate");
        this.absoluteUnit.setToolTipText("Show absolute distance, skip zoom level");
        this.cmIndicate.setToolTipText("Show distance in centimet following given scale");
        if (this.absoluteUnit_check) {
            this.absoluteUnit.setSelected(true);
        }

        if (this.cmIndicate_check) {
            this.cmIndicate.setSelected(true);
        }

        if (viLang) {
            this.absoluteUnit.setText("Đơn vị tuyệt đối");
            this.cmIndicate.setText("Hiển thị centimet");
            this.absoluteUnit.setToolTipText("Hiển thị số đo thực theo ảnh (bỏ chọn hiển thị số đo theo pixel màn hình)");
            this.cmIndicate.setToolTipText("Hiện thị số đo của 2 thanh đo ngang - dọc theo đơn vị centimet");
        }

        this.texf.setEditable(false);
        this.texf.setFocusable(false);
        this.scale.setFocusable(true);
        this.scale.addMouseListener(this);
        this.zoomTexf = new TextField(15);
        this.zoomTexf.setText("Zoom:200%");
        this.zoomTexf.setEditable(false);
        this.zoomTexf.setFocusable(false);
        Panel textPanel = new Panel();
        Panel buttonPanel = new Panel();
        textPanel.add(this.texf);
        textPanel.add(this.zoomTexf);
        textPanel.add(this.absoluteUnit);
        textPanel.add(this.cmIndicate);
        buttonPanel.add(this.close);
        this.scale_input_label = new JLabel("Scale:");
        if (viLang) {
            this.scale_input_label.setText("Tỷ lệ:");
        }

        buttonPanel.add(this.scale_input_label);
        buttonPanel.add(this.scale);
        buttonPanel.add(this.setScale);
        this.zom.setBounds(0, 0, 550, 500);
        this.zom.setFocusable(true);
        this.mainPanel.setBackground(Color.CYAN);
        this.zom.add(buttonPanel, "First");
        this.zom.add(this.iz, "Center");
        this.zom.add(textPanel, "South");
        this.setVisible(true);
        this.zom.addKeyListener(this);
        this.zom.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                ZoomDialog.this.zom.setVisible(false);
                ZoomDialog.this.zom.dispose();
                ZoomDialog.this.iz.stop();
                ZoomDialog.this.iz.move = false;
                ZoomDialog.this.dispose();
                ZoomDialog.this.imageFrame.zoomdialog = null;
                ZoomDialog.this.imageFrame.scale = ZoomDialog.this.iz.scale;
                ZoomDialog.this.imageFrame.zoomBoxOpened = false;
                ZoomDialog.this.closed = true;
            }
        });
        this.addKeyListener(this);
        this.absoluteUnit.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == 1) {
                    ZoomDialog.this.setAbsoluteUnit(true);
                    ZoomDialog.this.absoluteUnit_check = true;
                } else {
                    ZoomDialog.this.setAbsoluteUnit(false);
                    ZoomDialog.this.absoluteUnit_check = false;
                }

            }
        });
        this.cmIndicate.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                if (ev.getStateChange() == 1) {
                    ZoomDialog.this.setCmIndicate(true);
                    ZoomDialog.this.cmIndicate_check = true;
                } else {
                    ZoomDialog.this.cmIndicate_check = false;
                    ZoomDialog.this.setCmIndicate(false);
                }

            }
        });
        this.setAbsoluteUnit(this.absoluteUnit_check);
        this.setCmIndicate(this.cmIndicate_check);
    }

    private void setAbsoluteUnit(boolean b) {
        this.iz.setAbsoluteUnit(b);
    }

    private void setCmIndicate(boolean b) {
        this.iz.setCmIndicate(b);
    }

    public void show() {
        this.setLanguage();
        this.zom.setVisible(true);
        this.repaint();
        this.closed = false;
    }

    private void setLanguage() {
        if (this.viLang) {
            this.close.setText("Đóng");
            this.setScale.setText("Đặt tỷ lệ");
            this.absoluteUnit.setText("Đơn vị tuyệt đối");
            this.cmIndicate.setText("Hiển thị centimet");
            this.absoluteUnit.setToolTipText("Hiển thị số đo thực theo ảnh (bỏ chọn hiển thị số đo theo pixel màn hình)");
            this.cmIndicate.setToolTipText("Hiện thị số đo của 2 thanh đo ngang - dọc theo đơn vị centimet");
            this.scale_input_label.setText("Tỷ lệ:");
        } else {
            this.close.setText("Close");
            this.setScale.setText("Set Scale");
            this.absoluteUnit.setText("Absolute Unit");
            this.cmIndicate.setText("Cm Indicate");
            this.absoluteUnit.setToolTipText("Show absolute distance, skip zoom level)");
            this.cmIndicate.setToolTipText("Show distance in centimet following given scale");
            this.scale_input_label.setText("Scale:");
        }

    }

    public void hide() {
        this.zom.setVisible(false);
        this.iz.stop();
    }

    public void imgMove(int x, int y) {
        this.iz.imgMove(x, y);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.close) {
            this.zom.setVisible(false);
            this.dispose();
            this.iz.stop();
            this.closed = true;
            Noitifier.printConsole("button preesed");
        }

        if (e.getSource() == this.setScale) {
            float f = 0.0F;

            try {
                f = Float.valueOf(this.scale.getText());
                this.scale.setBackground(new Color(150, 255, 150));
            } catch (NumberFormatException var4) {
                this.scale.setBackground(new Color(255, 80, 80));
                Noitifier.printConsole("Illegal Number Format!!!!");
            }

            if (f != 0.0F) {
                this.iz.setScale(f);
            }
        }

    }

    public void updateBufer(BufferedImage buf) {
        this.iz.updateBuffer(buf);
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 33) {
            this.iz.messureLineUp(5);
        }

        if (e.getKeyCode() == 34) {
            this.iz.messureLineDown(5);
        }

        if (e.getKeyCode() == 35) {
            this.iz.messureLineRight(5);
        }

        if (e.getKeyCode() == 36) {
            this.iz.messureLineLeft(5);
        }

        if (e.getKeyCode() == 37) {
            this.iz.messureLineLeft(1);
        }

        if (e.getKeyCode() == 38) {
            this.iz.messureLineUp(1);
        }

        if (e.getKeyCode() == 39) {
            this.iz.messureLineRight(1);
        }

        if (e.getKeyCode() == 40) {
            this.iz.messureLineDown(1);
        }

    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == 90) {
            this.zom.setVisible(false);
            this.iz.stop();
            this.closed = true;
            Noitifier.printConsole("Z preesed");
        }

        if (e.getKeyCode() == 82) {
            this.iz.changeRulerMode();
        }

        if (e.getKeyCode() == 65) {
            this.iz.messureMode();
        }

        if (e.getKeyCode() == 78) {
            this.iz.showUnit();
        }

        if (e.getKeyCode() == 46) {
            this.iz.increRulerUnit(2);
        }

        if (e.getKeyCode() == 44) {
            this.iz.decreRulerUnit(2);
        }

        if (e.getKeyCode() == 93) {
            this.iz.increRulerUnit(8);
        }

        if (e.getKeyCode() == 91) {
            this.iz.decreRulerUnit(8);
        }

        if (e.getKeyCode() == 47) {
            this.iz.resetRulerUnit();
        }

        if (e.getKeyCode() == 97) {
            this.iz.setCmScale(1);
        }

        if (e.getKeyCode() == 98) {
            this.iz.setCmScale(2);
        }

        if (e.getKeyCode() == 99) {
            this.iz.setCmScale(3);
        }

        if (e.getKeyCode() == 100) {
            this.iz.setCmScale(4);
        }

        if (e.getKeyCode() == 101) {
            this.iz.setCmScale(5);
        }

        if (e.getKeyCode() == 102) {
            this.iz.setCmScale(6);
        }

        if (e.getKeyCode() == 103) {
            this.iz.setCmScale(7);
        }

        if (e.getKeyCode() == 104) {
            this.iz.setCmScale(8);
        }

        if (e.getKeyCode() == 105) {
            this.iz.setCmScale(9);
        }

        if (e.getKeyCode() == 67) {
            this.iz.showCross();
        }

        Noitifier.printConsole("key zoom pressed");
    }

    public void keyTyped(KeyEvent arg0) {
    }

    public void mouseClicked(MouseEvent arg0) {
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent e) {
        if (e.getSource() == this.scale) {
            final Dialog d = new Dialog(this.zom, "Scale Input");
            this.scl = new TextField(15);
            this.scl.setText(this.scale.getText());
            JButton ok = new JButton("Ok");
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    ZoomDialog.this.getVal();
                    d.transferFocusBackward();
                    d.dispose();
                }
            });
            d.setLayout(new FlowLayout());
            d.add(this.scl);
            d.add(ok);
            d.setBounds(new Rectangle(200, 100));
            d.setVisible(true);
        }

    }

    protected void getVal() {
        this.scale.setBackground(Color.white);
        this.scale.setText(this.scl.getText());
    }

    public void mouseReleased(MouseEvent arg0) {
    }

    public void setParent(ImgFrame imgFrame) {
        this.imageFrame = imgFrame;
    }
}
