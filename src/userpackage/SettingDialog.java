package userpackage;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class SettingDialog extends Dialog implements MouseListener {
    private static final long serialVersionUID = 1L;
    private Color _color;
    private int stackSize;
    private int gridSize;
    public int zW;
    public int zH;
    private float intialScale;
    private boolean viLang;
    private boolean cmUnit;
    private boolean cmMeaUnit;
    private boolean round;
    private boolean autoLoad;
    private Dialog stb;
    JTextField gridAmountInput;
    private Circle3D circle;
    private JTextField scaleInput;
    private JCheckBox autoLoadCheckBox;
    private JRadioButton cm;
    private JRadioButton px;
    private String autoloadLabel;
    public JLabel distanceUnit;
    public String roundChkbLabel;
    public JLabel vi;
    public JLabel en;
    public JLabel stSizeLabel;
    public JLabel grSizeLabel;
    public JLabel scaleLabel;
    public JLabel zoomDialogSizeLabel;
    public JLabel languageLabel;
    private Choice unitChoice;
    private Choice stsize;
    private JTextField WtextFeild;
    private JTextField HtextFeild;
    private JComboBox<String> combo;
    private JCheckBox roundChkb;
    private JButton ok;
    private JButton cancel;
    public String action;
    private String ok_button_label;
    private String cancel_button_label;
    private String auto_load_tip;
    private String round_tip;
    private String cm_tip;
    private String px_tip;
    private String circle_tip;
    private String scale_tip;
    private String w_tip;
    private String h_tip;
    private String grid_tip;
    private String p2p_label_tip;

    public SettingDialog(Frame f, String title, boolean isModal, Color c, int stackSize, int grid, float scale) {
        super(f);
        this._color = Color.CYAN;
        this.stackSize = 0;
        this.gridSize = 40;
        this.autoloadLabel = "Auto Load";
        this.distanceUnit = new JLabel("P2P Distance Unit");
        this.roundChkbLabel = "Round";
        this.vi = new JLabel("Vietnamese");
        this.en = new JLabel("English");
        this.stSizeLabel = new JLabel("Stack Size:");
        this.grSizeLabel = new JLabel("Grid Size:");
        this.scaleLabel = new JLabel("Scale:");
        this.zoomDialogSizeLabel = new JLabel("Zoom Dialog Size:");
        this.languageLabel = new JLabel("Ngôn ngữ:");
        this.ok_button_label = "Ok";
        this.cancel_button_label = "Cancel";
        this.auto_load_tip = "Auto load file from previous section";
        this.round_tip = "Rounding point to point measure distance";
        this.cm_tip = "Show distance in centimet";
        this.px_tip = "Show distance in pixel";
        this.circle_tip = "Select color";
        this.scale_tip = "scale centimet - pixel";
        this.w_tip = "Width";
        this.h_tip = "Height";
        this.grid_tip = "Grid size";
        this.p2p_label_tip = "Choose unit show in Point to Point measurement mode";
        this.stb = new Dialog(f, title, isModal);
        this.stb.setBounds(600, 200, 300, 300);
        this.stackSize = stackSize;
        this._color = c;
        this.intialScale = scale;
        this.gridSize = grid;
        this.creatGUI();
        this.processEvent();
        this.setToolTip();
    }

    public void creatGUI() {
        Panel mainPanel = new Panel();
        mainPanel.setLayout(new GridLayout(2, 1, 3, 3));
        this.ok = new JButton(this.ok_button_label);
        this.cancel = new JButton(this.cancel_button_label);
        this.stsize = new Choice();
        this.stsize.addItem("15");
        this.stsize.addItem("30");
        this.stsize.addItem("50");
        this.stsize.addItem("100");
        this.stsize.addItem("200");
        this.stsize.addItem("300");
        this.unitChoice = new Choice();
        this.unitChoice.addItem("Cm");
        this.unitChoice.addItem("px");
        this.combo = new JComboBox();
        this.combo.addItem(this.vi.getText());
        this.combo.addItem(this.en.getText());
        this.vi.setName("VIETNAM");
        this.en.setName("ENGLISH");
        this.gridAmountInput = new JTextField(3);
        this.gridAmountInput.setText(Integer.toString(this.gridSize));
        this.scaleInput = new JTextField();
        this.scaleInput.setDragEnabled(true);
        this.scaleInput.setText(Float.toString(this.intialScale));
        this.WtextFeild = new JTextField(3);
        this.HtextFeild = new JTextField(3);
        this.WtextFeild.setText("600");
        this.HtextFeild.setText("400");
        this.autoLoadCheckBox = new JCheckBox(this.autoloadLabel);
        this.roundChkb = new JCheckBox(this.roundChkbLabel);
        this.autoLoadCheckBox.setSelected(true);
        Panel buttonPanel = new Panel();
        Panel settingPanel = new Panel();
        Panel settingSubPanel1 = new Panel();
        Panel settingSubPanel2 = new Panel();
        Panel stSizePanel = new Panel();
        Panel grSizePanel = new Panel();
        Panel scalePanel = new Panel();
        Panel zoomDialogSizePanel = new Panel();
        Panel languagePanel = new Panel();
        Panel colorPanel = new Panel();
        Panel checkBoxPanel = new Panel();
        Panel gridSizeSubPanel = new Panel();
        Panel zoomSubPanel = new Panel();
        GridLayout grLayout = new GridLayout(2, 1);
        stSizePanel.setLayout(grLayout);
        grSizePanel.setLayout(grLayout);
        scalePanel.setLayout(grLayout);
        zoomDialogSizePanel.setLayout(new BorderLayout(0, 0));
        languagePanel.setLayout(grLayout);
        this.stb.setLayout(new BorderLayout(1, 3));
        settingPanel.setLayout(new GridLayout(1, 2, 3, 3));
        buttonPanel.setLayout(new GridLayout(1, 2, 15, 1));
        buttonPanel.setPreferredSize(new Dimension(300, 30));
        settingSubPanel1.setLayout(new GridLayout(0, 1, 3, 3));
        settingSubPanel2.setLayout(new GridLayout(2, 1, 3, 3));
        colorPanel.setLayout(new BorderLayout());
        checkBoxPanel.setLayout(new GridLayout(0, 1, 3, 3));
        zoomSubPanel.setLayout(new FlowLayout(0, 0, 0));
        gridSizeSubPanel.setLayout(new GridLayout(1, 0));
        this.circle = new Circle3D(this._color);
        this.circle.setCursor(new Cursor(12));
        this.stb.add(settingPanel, "Center");
        this.stb.add(buttonPanel, "South");
        settingPanel.add(settingSubPanel1);
        settingPanel.add(settingSubPanel2);
        settingSubPanel1.add(stSizePanel);
        settingSubPanel1.add(grSizePanel);
        settingSubPanel1.add(scalePanel);
        settingSubPanel1.add(zoomDialogSizePanel);
        settingSubPanel1.add(languagePanel);
        settingSubPanel2.add(colorPanel);
        settingSubPanel2.add(checkBoxPanel);
        stSizePanel.add(this.stSizeLabel);
        stSizePanel.add(this.stsize);
        grSizePanel.add(this.grSizeLabel);
        grSizePanel.add(gridSizeSubPanel);
        gridSizeSubPanel.add(this.gridAmountInput);
        gridSizeSubPanel.add(this.unitChoice);
        scalePanel.add(this.scaleLabel);
        scalePanel.add(this.scaleInput);
        zoomDialogSizePanel.add(this.zoomDialogSizeLabel, "North");
        zoomDialogSizePanel.add(zoomSubPanel, "Center");
        this.zoomDialogSizeLabel.setFont(new Font("SansSerif", 1, 10));
        this.zoomDialogSizeLabel.setPreferredSize(new Dimension(50, 20));
        Label w = new Label("W:");
        Label h = new Label("H:");
        Label p = new Label("px");
        w.setPreferredSize(new Dimension(20, 25));
        h.setPreferredSize(new Dimension(20, 25));
        p.setPreferredSize(new Dimension(15, 25));
        zoomSubPanel.add(w);
        zoomSubPanel.add(this.WtextFeild);
        zoomSubPanel.add(h);
        zoomSubPanel.add(this.HtextFeild);
        zoomSubPanel.add(p);
        languagePanel.add(this.languageLabel);
        languagePanel.add(this.combo);
        colorPanel.add(this.circle, "Center");
        checkBoxPanel.add(this.autoLoadCheckBox);
        checkBoxPanel.add(this.distanceUnit);
        ButtonGroup btgr = new ButtonGroup();
        this.cm = new JRadioButton("Centimet");
        this.px = new JRadioButton("Pixels");
        btgr.add(this.cm);
        btgr.add(this.px);
        checkBoxPanel.add(this.cm);
        checkBoxPanel.add(this.px);
        checkBoxPanel.add(this.roundChkb);
        buttonPanel.add(this.ok);
        buttonPanel.add(this.cancel);
        settingSubPanel2.setBackground(Color.lightGray);
        settingSubPanel1.setBackground(Color.DARK_GRAY);
        this.stb.setBackground(Color.BLACK);
        stSizePanel.setBackground(Color.LIGHT_GRAY);
        grSizePanel.setBackground(Color.LIGHT_GRAY);
        scalePanel.setBackground(Color.LIGHT_GRAY);
        zoomDialogSizePanel.setBackground(Color.LIGHT_GRAY);
        languagePanel.setBackground(Color.LIGHT_GRAY);
        this.setPreferredSize(new Dimension(400, 400));
    }

    public void initialFeilds(boolean vilang, boolean cmUnit, boolean cmMeaunit, boolean round, boolean autoLoad, int zW, int zH) {
        this.viLang = vilang;
        this.cmUnit = cmUnit;
        this.cmMeaUnit = cmMeaunit;
        this.round = round;
        this.autoLoad = autoLoad;
        this.zW = zW;
        this.zH = zH;
        this.showViewFeils();
        this.setLanguage();
    }

    public void showViewFeils() {
        if (this.stackSize == 15) {
            this.stsize.select(0);
        }

        if (this.stackSize == 30) {
            this.stsize.select(1);
        }

        if (this.stackSize == 50) {
            this.stsize.select(2);
        }

        if (this.stackSize == 100) {
            this.stsize.select(3);
        }

        if (this.stackSize == 200) {
            this.stsize.select(4);
        }

        if (this.stackSize == 300) {
            this.stsize.select(5);
        }

        if (this.cmUnit) {
            float f = (float)this.gridSize * this.intialScale;
            f = Math2.round(f);
            this.gridAmountInput.setText(Float.toString(f));
        } else {
            this.gridAmountInput.setText(Integer.toString(this.gridSize));
        }

        if (this.cmUnit) {
            this.unitChoice.select(0);
        } else {
            this.unitChoice.select(1);
        }

        this.WtextFeild.setText(Integer.toString(this.zW));
        this.HtextFeild.setText(Integer.toString(this.zH));
        if (this.viLang) {
            this.combo.setSelectedIndex(0);
        } else {
            this.combo.setSelectedIndex(1);
        }

        this.autoLoadCheckBox.setSelected(this.autoLoad);
        if (this.cmMeaUnit) {
            this.cm.setSelected(true);
        } else {
            this.px.setSelected(true);
        }

        this.roundChkb.setSelected(this.round);
    }

    public void processEvent() {
        ItemListener myChoiceListener = new ChoiceItemListener(this);
        this.stsize.addItemListener(myChoiceListener);
        this.unitChoice.addItemListener(myChoiceListener);
        this.combo.addItemListener(myChoiceListener);
        this.cm.addItemListener(myChoiceListener);
        this.px.addItemListener(myChoiceListener);
        this.circle.addMouseListener(this);
        ActionListener myButtonListener = new ButtonListener(this);
        this.ok.addActionListener(myButtonListener);
        this.cancel.addActionListener(myButtonListener);
        ItemListener myItemListener = new CheckBoxListener(this);
        this.autoLoadCheckBox.addItemListener(myItemListener);
        this.roundChkb.addItemListener(myItemListener);
    }

    private void setToolTip() {
        this.autoLoadCheckBox.setToolTipText(this.auto_load_tip);
        this.roundChkb.setToolTipText(this.round_tip);
        this.cm.setToolTipText(this.cm_tip);
        this.px.setToolTipText(this.px_tip);
        this.circle.setToolTipText(this.circle_tip);
        this.scaleInput.setToolTipText(this.scale_tip);
        this.WtextFeild.setToolTipText(this.w_tip);
        this.HtextFeild.setToolTipText(this.h_tip);
        this.gridAmountInput.setToolTipText(this.grid_tip);
        this.distanceUnit.setToolTipText(this.p2p_label_tip);
    }

    private void setLanguage() {
        if (this.viLang) {
            this.stb.setTitle("Tùy Chỉnh");
            this.cancel_button_label = "Hủy";
            this.cancel.setText("Hủy");
            this.stSizeLabel.setText("Kích thước stack:");
            this.grSizeLabel.setText("Kích thước ô lưới:");
            this.scaleLabel.setText("Tỷ lệ:");
            this.zoomDialogSizeLabel.setText("Kích thước khung zoom:");
            this.languageLabel.setText("Language:");
            this.autoloadLabel = "Nhớ phiên";
            this.distanceUnit.setText("Đơn vị đo hiện thị:");
            this.roundChkbLabel = "Làm tròn";
            this.vi.setText("Tiếng Việt");
            this.en.setText("Tiếng Anh");
            this.combo.setSelectedIndex(0);
            this.roundChkb.setText(this.roundChkbLabel);
            this.autoLoadCheckBox.setText(this.autoloadLabel);
            this.auto_load_tip = "Tự động mở tệp từ phiên trước";
            this.round_tip = "Làm tròn kết quả đo";
            this.p2p_label_tip = "Chọn đơn vị hiện thị kết quả trong chế độ đo điểm";
            this.cm_tip = "Hiển thị kết quả đo ở đơn vị centimet";
            this.px_tip = "Hiển thị kết quả đo ở đơn vị pixel";
            this.circle_tip = "Chọn màu";
            this.scale_tip = "Tỷ lệ quy đổi centimet/pixel";
            this.w_tip = "Rộng";
            this.h_tip = "Cao";
            this.grid_tip = "Kích thước ô lưới";
            this.setToolTip();
        } else {
            this.stb.setTitle("Setting");
            this.cancel_button_label = "Cancel";
            this.cancel.setText("Cancel");
            this.stSizeLabel.setText("Stack Size:");
            this.grSizeLabel.setText("Grid Size:");
            this.scaleLabel.setText("Scale:");
            this.zoomDialogSizeLabel.setText("Zoom Dialog Size:");
            this.languageLabel.setText("Ngôn ngữ:");
            this.autoloadLabel = "Auto Load";
            this.distanceUnit.setText("P2P Distance Unit");
            this.roundChkbLabel = "Round";
            this.vi.setText("Vietnamese");
            this.en.setText("English");
            this.combo.setSelectedIndex(1);
            this.roundChkb.setText(this.roundChkbLabel);
            this.autoLoadCheckBox.setText(this.autoloadLabel);
            this.auto_load_tip = "Auto load file from previous section";
            this.round_tip = "Rounding point to point measure distance";
            this.p2p_label_tip = "Choose unit show in Point to Point measurement mode";
            this.cm_tip = "Show distance in centimet";
            this.px_tip = "Show distance in pixel";
            this.circle_tip = "Select color";
            this.scale_tip = "scale centimet - pixel";
            this.w_tip = "Width";
            this.h_tip = "Height";
            this.grid_tip = "Grid size";
            this.setToolTip();
        }

        Noitifier.printConsole(" setting - setlanguage called - viLang:" + this.viLang);
    }

    public Color getColor() {
        return this._color;
    }

    public void setColor(Color c) {
        this._color = c;
    }

    public int getStackSize() {
        return this.stackSize;
    }

    public int getGridSize() {
        return this.gridSize;
    }

    public void setStackSize(int s) {
        this.stackSize = s;
    }

    public void setVisible(boolean b) {
        this.stb.setVisible(b);
    }

    public float getScale() {
        return this.intialScale;
    }

    public void changeUnit() {
        if (this.cmUnit) {
            float f = (float)this.gridSize * this.intialScale;
            f = Math2.round(f);
            this.gridAmountInput.setText(Float.toString(f));
        } else {
            this.gridAmountInput.setText(Integer.toString(this.gridSize));
        }

    }

    public void mouseClicked(MouseEvent arg0) {
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
        }

    }

    public void mouseReleased(MouseEvent arg0) {
    }

    public boolean getLanguage() {
        return this.viLang;
    }

    public boolean getcmUnit() {
        return this.cmUnit;
    }

    public boolean getCmMeasureUnit() {
        return this.cmMeaUnit;
    }

    public boolean getRound() {
        return this.round;
    }

    public boolean getAutoLoad() {
        return this.autoLoad;
    }

    public class ChoiceItemListener implements ItemListener {
        SettingDialog d;

        public ChoiceItemListener(SettingDialog s) {
            this.d = s;
        }

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == 1 && e.getSource().equals(this.d.stsize)) {
                String itemLabel = (String)e.getItem();
                if (itemLabel == "15") {
                    this.setStackSize(Integer.parseInt(itemLabel));
                }

                if (itemLabel == "30") {
                    this.setStackSize(Integer.parseInt(itemLabel));
                }

                if (itemLabel == "50") {
                    this.setStackSize(Integer.parseInt(itemLabel));
                }

                if (itemLabel == "100") {
                    this.setStackSize(Integer.parseInt(itemLabel));
                }

                if (itemLabel == "200") {
                    this.setStackSize(Integer.parseInt(itemLabel));
                }

                if (itemLabel == "300") {
                    this.setStackSize(Integer.parseInt(itemLabel));
                }

                Noitifier.printConsole("itemLabel" + itemLabel);
            }

            if (e.getSource().equals(this.d.unitChoice)) {
                String itemLabel = (String)e.getItem();
                if (itemLabel == "Cm") {
                    this.d.cmUnit = true;
                }

                if (itemLabel == "px") {
                    this.d.cmUnit = false;
                }

                this.d.changeUnit();
                Noitifier.printConsole("cmUnit:" + this.d.cmUnit);
            }

            if (e.getSource().equals(this.d.combo)) {
                if (e.getID() == 701) {
                    Noitifier.printConsole("intem first:701");
                }

                String item = (String)e.getItem();
                if (item == "Vietnamese") {
                    this.d.viLang = true;
                }

                if (item == "English") {
                    this.d.viLang = false;
                }

                this.d.setLanguage();
                Noitifier.printConsole("viLang:" + SettingDialog.this.viLang);
                Noitifier.printConsole("item:" + item);
                Noitifier.printConsole("d.en.getText:" + this.d.en.getText());
                Noitifier.printConsole("d.vi.getText:" + this.d.vi.getText());
            }

            if (e.getStateChange() == 1 && e.getSource() == this.d.cm) {
                this.d.cmMeaUnit = true;
                Noitifier.printConsole("cmMeasure:" + SettingDialog.this.cmMeaUnit);
            }

            if (e.getStateChange() == 1 && e.getSource() == this.d.px) {
                this.d.cmMeaUnit = false;
                Noitifier.printConsole("cmMeasure:" + SettingDialog.this.cmMeaUnit);
            }

        }

        public void setStackSize(int s) {
            this.d.setStackSize(s);
        }
    }

    public class ButtonListener implements ActionListener {
        SettingDialog s;
        Color c;

        public ButtonListener(SettingDialog s) {
            this.s = s;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand() == this.s.ok_button_label) {
                this.s.action = "OK";
                this.s.setColor(this.s.circle.getColor());

                try {
                    if (SettingDialog.this.cmUnit) {
                        float f = Float.parseFloat(this.s.gridAmountInput.getText());
                        float sc = Float.parseFloat(this.s.scaleInput.getText());
                        f /= sc;
                        this.s.gridSize = (int)f;
                    } else {
                        this.s.gridSize = Integer.parseInt(this.s.gridAmountInput.getText());
                    }

                    this.s.intialScale = Float.parseFloat(this.s.scaleInput.getText());
                    this.s.zW = Integer.parseInt(this.s.WtextFeild.getText());
                    this.s.zH = Integer.parseInt(this.s.HtextFeild.getText());
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }

                Noitifier.printConsole("circle color:" + this.s.circle.getColor());
                Noitifier.printConsole("auto:" + this.s.autoLoad + "; scale:" + this.s.intialScale);
                this.s.setVisible(false);
            }

            if (e.getActionCommand() == this.s.cancel_button_label) {
                this.s.action = "CANCEL";
                Noitifier.printConsole("Setting canceled");
                this.s.setVisible(false);
            }

        }
    }

    public class CheckBoxListener implements ItemListener {
        SettingDialog s;

        public CheckBoxListener(SettingDialog s) {
            this.s = s;
        }

        public void itemStateChanged(ItemEvent ev) {
            if (ev.getSource().equals(this.s.autoLoadCheckBox)) {
                if (ev.getStateChange() == 1) {
                    this.s.autoLoad = true;
                }

                if (ev.getStateChange() == 2) {
                    this.s.autoLoad = false;
                }
            }

            if (ev.getSource().equals(this.s.roundChkb)) {
                if (ev.getStateChange() == 1) {
                    this.s.round = true;
                }

                if (ev.getStateChange() == 2) {
                    this.s.round = false;
                }
            }

            Noitifier.printConsole("autoload:" + this.s.autoLoad);
            Noitifier.printConsole("round:" + this.s.round);
        }
    }
}
