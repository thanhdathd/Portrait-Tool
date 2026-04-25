package userpackage;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;

public class ColorSelectDialog extends Dialog implements ActionListener, MouseListener {
    private static final long serialVersionUID = 1L;
    private Color _color;
    private Circle3D circle;
    private Dialog colorDialog;
    private JButton ok;
    private Scrollbar RedValue;
    private Scrollbar BlueValue;
    private Scrollbar GreenValue;
    private Scrollbar TransValue;
    private TextField RedCode;
    private TextField BlueCode;
    private TextField GreenCode;
    private TextField TransCode;
    private boolean viLang;

    public ColorSelectDialog(Frame f, String title, boolean isModal, Color c, boolean viLang) {
        super(f, title, isModal);
        this._color = Color.CYAN;
        this.viLang = false;
        this.colorDialog = new Dialog(f, title, isModal);
        this.colorDialog.setBounds(600, 200, 300, 300);
        this.colorDialog.setResizable(false);
        this._color = c;
        this.viLang = viLang;
        this.creatGUI();
    }

    public ColorSelectDialog(Dialog d, String title, boolean isModal, Color c) {
        super(d, title, isModal);
        this._color = Color.CYAN;
        this.viLang = false;
        this.colorDialog = new Dialog(d, title, isModal);
        this.colorDialog.setBounds(600, 150, 300, 300);
        this._color = c;
        this.creatGUI();
    }

    public void creatGUI() {
        Panel mainPanel = new Panel();
        mainPanel.setLayout(new GridLayout(2, 1, 3, 3));
        this.RedValue = new Scrollbar(0, this._color.getRed(), 25, 0, 280);
        this.GreenValue = new Scrollbar(0, this._color.getGreen(), 25, 0, 280);
        this.BlueValue = new Scrollbar(0, this._color.getBlue(), 25, 0, 280);
        this.TransValue = new Scrollbar(0, this._color.getAlpha(), 25, 0, 280);
        Label redLabel = new Label("Red:");
        Label GreenLabel = new Label("Green:");
        Label BlueLabel = new Label("Blue:");
        Label tranLabel = new Label("Trans:");
        Button setValue = new Button("Set RGB");
        this.ok = new JButton("Ok");
        this.ok.setPreferredSize(new Dimension(25, 80));
        this.ok.setFont(new Font("Arial", 1, 28));
        Panel colorPanel = new Panel();
        Panel subTextPanel = new Panel();
        Panel TextPanel = new Panel();
        Panel ScrPanel = new Panel();
        Panel RedPanel = new Panel();
        Panel GreenPanel = new Panel();
        Panel BluePanel = new Panel();
        Panel TransPanel = new Panel();
        RedPanel.setBackground(Color.RED);
        GreenPanel.setBackground(Color.GREEN);
        BluePanel.setBackground(new Color(49151));
        ScrPanel.setBackground(new Color(11529966));
        TransPanel.setBackground(Color.LIGHT_GRAY);
        colorPanel.setLayout(new GridLayout(1, 0, 5, 5));
        ScrPanel.setLayout(new GridLayout(4, 1, 5, 15));
        RedPanel.setLayout(new BorderLayout());
        GreenPanel.setLayout(new BorderLayout());
        BluePanel.setLayout(new BorderLayout());
        TransPanel.setLayout(new BorderLayout());
        subTextPanel.setLayout(new GridLayout(4, 2, 5, 5));
        TextPanel.setLayout(new BorderLayout());
        this.RedCode = new TextField(Integer.toString(this.RedValue.getValue()), 3);
        this.GreenCode = new TextField(Integer.toString(this.GreenValue.getValue()), 3);
        this.BlueCode = new TextField(Integer.toString(this.BlueValue.getValue()), 3);
        this.TransCode = new TextField(Integer.toString(this.TransValue.getValue()), 3);
        this.RedCode.setForeground(Color.RED);
        this.GreenCode.setForeground(Color.GREEN);
        this.BlueCode.setForeground(Color.BLUE);
        this.circle = new Circle3D(this._color);
        this.circle.addMouseListener(this);
        this.RedValue.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                int Red = evt.getValue();
                int Green = ColorSelectDialog.this.GreenValue.getValue();
                int Blue = ColorSelectDialog.this.BlueValue.getValue();
                int Trans = ColorSelectDialog.this.TransValue.getValue();
                ColorSelectDialog.this.circle.setColor(new Color(Red, Green, Blue, Trans));
                ColorSelectDialog.this.RedCode.setText(Integer.toString(Red));
            }
        });
        this.GreenValue.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                int Green = evt.getValue();
                int Red = ColorSelectDialog.this.RedValue.getValue();
                int Blue = ColorSelectDialog.this.BlueValue.getValue();
                int Trans = ColorSelectDialog.this.TransValue.getValue();
                ColorSelectDialog.this.circle.setColor(new Color(Red, Green, Blue, Trans));
                ColorSelectDialog.this.GreenCode.setText(Integer.toString(Green));
            }
        });
        this.BlueValue.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                int Blue = evt.getValue();
                int Red = ColorSelectDialog.this.RedValue.getValue();
                int Green = ColorSelectDialog.this.GreenValue.getValue();
                int Trans = ColorSelectDialog.this.TransValue.getValue();
                ColorSelectDialog.this.circle.setColor(new Color(Red, Green, Blue, Trans));
                ColorSelectDialog.this.BlueCode.setText(Integer.toString(Blue));
            }
        });
        this.TransValue.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                int Red = ColorSelectDialog.this.RedValue.getValue();
                int Green = ColorSelectDialog.this.GreenValue.getValue();
                int Blue = ColorSelectDialog.this.BlueValue.getValue();
                int Trans = evt.getValue();
                ColorSelectDialog.this.circle.setColor(new Color(Red, Green, Blue, Trans));
                ColorSelectDialog.this.TransCode.setText(Integer.toString(Trans));
            }
        });
        setValue.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int R = 0;
                int G = 0;
                int B = 0;
                int A = 0;

                try {
                    R = Integer.parseInt(ColorSelectDialog.this.RedCode.getText());
                    G = Integer.parseInt(ColorSelectDialog.this.GreenCode.getText());
                    B = Integer.parseInt(ColorSelectDialog.this.BlueCode.getText());
                    A = Integer.parseInt(ColorSelectDialog.this.TransCode.getText());
                } catch (NumberFormatException var7) {
                    System.out.print("ERORR COLOR VALUE!");
                }

                ColorSelectDialog.this.circle.setColor(new Color(R, G, B, A));
                ColorSelectDialog.this.RedValue.setValue(R);
                ColorSelectDialog.this.GreenValue.setValue(G);
                ColorSelectDialog.this.BlueValue.setValue(B);
                ColorSelectDialog.this.TransValue.setValue(A);
            }
        });
        this.ok.addActionListener(this);
        subTextPanel.add(redLabel);
        subTextPanel.add(this.RedCode);
        subTextPanel.add(GreenLabel);
        subTextPanel.add(this.GreenCode);
        subTextPanel.add(BlueLabel);
        subTextPanel.add(this.BlueCode);
        subTextPanel.add(tranLabel);
        subTextPanel.add(this.TransCode);
        subTextPanel.add(setValue);
        subTextPanel.setBackground(Color.LIGHT_GRAY);
        TextPanel.add(setValue, "North");
        TextPanel.add(subTextPanel, "Center");
        RedPanel.add(redLabel, "West");
        RedPanel.add(this.RedValue, "Center");
        GreenPanel.add(GreenLabel, "West");
        GreenPanel.add(this.GreenValue, "Center");
        BluePanel.add(BlueLabel, "West");
        BluePanel.add(this.BlueValue, "Center");
        TransPanel.add(tranLabel, "West");
        TransPanel.add(this.TransValue, "Center");
        ScrPanel.add(RedPanel);
        ScrPanel.add(GreenPanel);
        ScrPanel.add(BluePanel);
        ScrPanel.add(TransPanel);
        colorPanel.add(TextPanel, "West");
        colorPanel.add(this.circle, "Center");
        colorPanel.add(this.ok, "Last");
        colorPanel.setBackground(Color.LIGHT_GRAY);
        Panel mainSubPanel = new Panel();
        mainSubPanel.setLayout(new BorderLayout(5, 5));
        mainSubPanel.add(colorPanel, "Center");
        mainPanel.add(mainSubPanel);
        mainPanel.add(ScrPanel);
        this.colorDialog.add(mainPanel);
        this.setPreferredSize(new Dimension(400, 400));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.ok) {
            this._color = this.circle.getColor();
            this.colorDialog.setVisible(false);
            Noitifier.printConsole("Ok pressed");
        }

    }

    public Color getColor() {
        return this._color;
    }

    public void show() {
        this.colorDialog.setVisible(true);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent e) {
        if (e.getSource() == this.circle) {
            String title;
            if (this.viLang) {
                title = "Chọn Màu";
            } else {
                title = "Select Color";
            }

            ColorChooser colorChoser = new ColorChooser(this, title, true, this.circle.c, this.viLang);
            colorChoser.d.setVisible(true);
            this.circle.setColor(colorChoser.c);
            this._color = colorChoser.c;
            this.RedValue.setValue(this._color.getRed());
            this.BlueValue.setValue(this._color.getBlue());
            this.GreenValue.setValue(this._color.getGreen());
            this.TransValue.setValue(this._color.getAlpha());
            this.RedCode.setText(Integer.toString(this._color.getRed()));
            this.BlueCode.setText(Integer.toString(this._color.getBlue()));
            this.GreenCode.setText(Integer.toString(this._color.getGreen()));
            this.TransCode.setText(Integer.toString(this._color.getAlpha()));
        }

    }

    public void mouseReleased(MouseEvent arg0) {
    }
}
