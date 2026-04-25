package userpackage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;

public class ColorChooser extends Dialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    public Dialog d;
    public Color c;
    JColorChooser cl;
    private JButton ok;
    private JButton cancel;
    boolean viLang;

    public ColorChooser(Dialog d, String title, boolean isModal, Color c, boolean vilang) {
        super(d, title, isModal);
        this.d = new Dialog(d, title, isModal);
        this.c = c;
        this.d.setBounds(600, 150, 615, 420);
        this.viLang = vilang;
        this.creatGUI();
    }

    private void creatGUI() {
        BorderLayout bo = new BorderLayout(3, 3);
        Panel btp = new Panel();
        btp.setLayout(new GridLayout(1, 0));
        btp.setPreferredSize(new Dimension(300, 50));
        this.ok = new JButton("Ok");
        this.cancel = new JButton("Cancel");
        if (this.viLang) {
            this.cancel.setText("Hủy");
        }

        this.ok.setFont(new Font("Arial", 1, 20));
        this.cancel.setFont(new Font("Arial", 1, 18));
        btp.add(this.ok);
        btp.add(this.cancel);
        this.ok.addActionListener(this);
        this.cancel.addActionListener(this);
        Panel colorPanel = new Panel();
        colorPanel.setLayout(bo);
        this.cl = new JColorChooser(this.c);
        colorPanel.add(this.cl, "Center");
        this.d.setLayout(new BorderLayout());
        this.d.add(btp, "Last");
        this.d.add(colorPanel, "Center");
        this.d.setBackground(Color.YELLOW);
        this.d.setPreferredSize(new Dimension(400, 400));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.ok) {
            this.c = this.cl.getColor();
            this.d.setVisible(false);
            Noitifier.printConsole("color select by JColorChooser:" + this.c);
        }

        if (e.getSource() == this.cancel) {
            this.d.setVisible(false);
        }

    }

    public void show() {
        this.d.setVisible(true);
    }
}
