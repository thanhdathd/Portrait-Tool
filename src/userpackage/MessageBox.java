package userpackage;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class MessageBox extends Dialog implements ActionListener {
    Dialog mgsBox;
    JButton ok;
    private static final long serialVersionUID = 15L;

    public MessageBox(Frame f, String mgs, boolean isModal) {
        super(f, mgs, isModal);
        this.mgsBox = new Dialog(f, "Info", isModal);
        this.mgsBox.setMinimumSize(new Dimension(300, 130));
        this.mgsBox.setBounds(500, 300, 300, 135);
        this.setLayout(new BorderLayout());
        this.creatGUI(mgs);
    }

    public MessageBox(Frame f, String mgs, String title, boolean isModal) {
        super(f, mgs, isModal);
        this.mgsBox = new Dialog(f, title, isModal);
        this.mgsBox.setMinimumSize(new Dimension(300, 130));
        this.mgsBox.setBounds(500, 300, 300, 135);
        this.setLayout(new BorderLayout());
        this.creatGUI(mgs);
    }

    public void creatGUI(String mgs) {
        String str = mgs;
        String sub1 = null;
        String sub2 = null;
        String sub3 = null;
        String sub4 = null;
        String sub5 = null;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i1 = mgs.indexOf(10);

        try {
            if (i1 != -1) {
                sub1 = str.substring(0, i1);
                i2 = str.indexOf(10, i1 + 1);
                if (i2 != -1) {
                    sub2 = str.substring(i1 + 1, i2);
                    i3 = str.indexOf(10, i2 + 1);
                    if (i3 != -1) {
                        sub3 = str.substring(i2 + 1, i3);
                        i4 = str.indexOf(10, i3 + 1);
                        if (i4 != -1) {
                            sub4 = str.substring(i3 + 1, i4);
                            i5 = str.indexOf(10, i4 + 1);
                            if (i5 != -1) {
                                sub5 = str.substring(i4 + 1, i5);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Noitifier.printConsole("i1:" + i1);
            Noitifier.printConsole("i2:" + i2);
            Noitifier.printConsole("i3:" + i3);
            Noitifier.printConsole("i4:" + i4);
            Noitifier.printConsole("i5:" + i5);
        }

        Label l1 = null;
        if (sub1 != null) {
            l1 = new Label(sub1);
        }

        Label l2 = null;
        if (sub2 != null) {
            l2 = new Label(sub2);
        }

        Label l3 = null;
        if (sub3 != null) {
            l3 = new Label(sub3);
        }

        Label l4 = null;
        if (sub4 != null) {
            l4 = new Label(sub4);
        }

        Label l5 = null;
        if (sub5 != null) {
            l5 = new Label(sub5);
        }

        Label l = null;
        if (sub1 == null) {
            l = new Label(mgs);
        }

        int ilast = mgs.lastIndexOf(10);
        String last = null;
        if (ilast != -1) {
            last = mgs.substring(ilast, mgs.length());
        }

        Label lastLabel = null;
        Panel labelPanel = new Panel();
        labelPanel.setLayout(new GridLayout(0, 1, 0, 1));
        Panel mainPanel = new Panel();
        mainPanel.setLayout(new FlowLayout(1, 15, 15));
        mainPanel.add(labelPanel);
        if (l != null) {
            labelPanel.add(l);
        }

        if (l1 != null) {
            labelPanel.add(l1);
        }

        if (l2 != null) {
            labelPanel.add(l2);
        }

        if (l3 != null) {
            labelPanel.add(l3);
        }

        if (l4 != null) {
            labelPanel.add(l4);
        }

        if (l5 != null) {
            labelPanel.add(l5);
        }

        if (last != null) {
            lastLabel = new Label(last);
            labelPanel.add(lastLabel);
        }

        this.mgsBox.add(mainPanel, "North");
        this.ok = new JButton("Ok");
        Panel buttonPanel = new Panel();
        buttonPanel.add(this.ok);
        this.ok.addActionListener(this);
        this.mgsBox.add(buttonPanel, "Center");
        this.mgsBox.add(new Panel(), "South");
    }

    public void show() {
        this.mgsBox.setVisible(true);
    }

    public void setSize(int W, int H) {
        if (W >= 300 && H >= 130) {
            this.mgsBox.setSize(W, H);
        }

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.ok) {
            this.mgsBox.setVisible(false);
        }

    }
}
