import filter.FStack;
import filter.FilterBox;
import filter.FilterProperties;
import images.Img;
import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import transform.ResizeBox;
import user.Enum.BufImageState;
import user.Enum.Key;
import user.Enum.MouseMode;
import user.Enum.Tranform;
import userpackage.Circle3D;
import userpackage.ColorPlate;
import userpackage.ColorSelectDialog;
import userpackage.ConfirmBox;
import userpackage.EditPanel;
import userpackage.ImageScrollPane;
import userpackage.ImgFrame;
import userpackage.Math2;
import userpackage.MessageBox;
import userpackage.MyWindowListener;
import userpackage.Noitifier;
import userpackage.SPoint;
import userpackage.SettingDialog;
import userpackage.help.HelpBox;
import userpackage.help.KeyAssistBox;

public class StickyPoint extends Frame implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private static final String ver = "1.05";
    private static final String APP_NAME = "Portrait Tool 1.05 - ";
    private static final Color LIGHT_GREEN = new Color(150, 255, 150);
    private static final Color LIGHT_RED = new Color(255, 80, 80);
    private static final Color WHITE;
    private Key Ctrl;
    private Key Alt;
    private Key Shf;
    int clickCount;
    FileDialog openfd;
    FileDialog savefd;
    JFileChooser openfd2;
    JButton openButton;
    JButton saveButton;
    JButton undoButton;
    JButton redoButton;
    JButton settingButton;
    Menu menuFile;
    Menu menuEdit;
    Menu menuHelp;
    Menu menuView;
    Menu menuOption;
    Menu menuLanguage;
    Menu menuImage;
    MenuItem open;
    MenuItem save;
    MenuItem export;
    MenuItem exit;
    MenuItem Undo;
    MenuItem Redo;
    MenuItem resize;
    MenuItem rotateCW;
    MenuItem rotateCCW;
    MenuItem rotate180;
    MenuItem flipH;
    MenuItem flipV;
    MenuItem help;
    MenuItem about;
    MenuItem keyAss;
    MenuItem savepoint;
    MenuItem setting;
    MenuItem viItem;
    MenuItem enItem;
    CheckboxMenuItem stateBarChk;
    CheckboxMenuItem ctrlPnlChk;
    String filePath;
    File curF;
    Image img;
    ImgFrame imageFrame;
    ImageScrollPane imgScrollPane;
    private EditPanel editPanel;
    Panel controlPanel;
    MediaTracker myTracker;
    private LayoutUpdater layoutUpdater;
    private JButton zoomButton;
    private JButton dragButton;
    private JButton stickButton;
    private JButton gridButton;
    private JButton pointButton;
    private JButton exportButton;
    private JButton pMatrixButton;
    private JButton filterButton;
    Panel redPlate;
    Panel bluePlate;
    Panel greenPlate;
    Panel blackPlate;
    Panel whitePlate;
    Panel yellowPlate;
    Panel grayPlate;
    Panel dark_grayPlate;
    Panel cyanPlate;
    private Circle3D currentColor;
    private Panel stateBar;
    private Panel currentColorWarper;
    private TextField state;
    private TextField currentZoomState;
    public EditState editstate;
    private FStack filterAction;
    private FStack filterReAction;
    public Point _start;
    public Point currentScrollPosition;
    private boolean dragMode;
    private float currentZoom;
    private float scale;
    private Point currentMousePosition;
    private MouseMode mouseMode;
    private boolean floating;
    private boolean cmUnit;
    private boolean viLang;
    private boolean cmMeaUnit;
    private boolean round;
    private boolean autoLoad;
    private int zW;
    private int zH;
    private boolean ab_ck;
    private boolean cm_ck;
    private String not_support_format_export_file;
    private String export_cancel;
    private String export_point_dialog_title;
    private String file_opened;
    private String file_not_found;
    private String save_cancel;
    private String unknow_file_format_img_save;
    private String stack_size;
    private String grid_size;
    private String confirm_save_subfix;
    private String confirm_save_prefix;
    private String scale_label;
    private String select_color_title;
    private String open_flie_dialog_title;
    private String save_file_dialog_title;
    private String cancel_openfile;
    private String setting_dialog_title;
    private String filter_01;
    private String filter_02;
    private String filter_03;
    private String filter_04;
    private String filter_05;
    private String filter_06;
    private String filter_07;
    private String filter_08;
    private String filter_09;
    private String filter_00;

    static {
        WHITE = Color.WHITE;
    }

    public StickyPoint() {
        this.Ctrl = Key.NONE_PRESSED;
        this.Alt = Key.NONE_PRESSED;
        this.Shf = Key.NONE_PRESSED;
        this.clickCount = 0;
        this.filePath = "Untitled-00.jpg";
        this.editstate = StickyPoint.EditState.SAVED;
        this.filterAction = new FStack(115, "Filter Action", true);
        this.filterReAction = new FStack(115, "Filter ReAction", true);
        this.dragMode = true;
        this.currentZoom = 1.0F;
        this.scale = 1.0F;
        this.currentMousePosition = new Point(0, 0);
        this.mouseMode = MouseMode.DRAG;
        this.floating = false;
        this.ab_ck = false;
        this.cm_ck = false;
        this.not_support_format_export_file = "File formart not supported";
        this.export_cancel = "Export canceled";
        this.export_point_dialog_title = "Save data ( *.xls type only)";
        this.file_opened = "File opened: ";
        this.file_not_found = "File not found";
        this.save_cancel = "Save cancel";
        this.unknow_file_format_img_save = "Unknow file format.";
        this.stack_size = "Stack Size: ";
        this.grid_size = "Grid Size: ";
        this.confirm_save_subfix = " had been modified.\n Do you want to save it?";
        this.confirm_save_prefix = "File: ";
        this.scale_label = "Scale: ";
        this.select_color_title = "Select Color";
        this.open_flie_dialog_title = "Open File";
        this.save_file_dialog_title = "Save File";
        this.cancel_openfile = "Cancel Open";
        this.setting_dialog_title = "Setting";
        this.filter_01 = "Image in Default RGB Gray Filter - (Preview)";
        this.filter_02 = "Image in Gray Filter with Red Chanel off - (Preview)";
        this.filter_03 = "Image in Gray Filter with Blue Chanel off - (Preview)";
        this.filter_04 = "Image in Gray Filter with Green Chanel off - (Preview)";
        this.filter_05 = "Image in Gray Filter with Green Chanel out - (Preview)";
        this.filter_06 = "Image in Gray Filter with Red Chanel out - (Preview)";
        this.filter_07 = "Image in Gray Filter with Blue Chanel out - (Preview)";
        this.filter_08 = "Image in Gray Filter with Red Chanel off, Gray level 255 - (Preview)";
        this.filter_09 = "Image in Gray Filter with Red Chanel off, Gray level 16 - (Preview)";
        this.filter_00 = "Reset layout!";
        this.cmUnit = false;
        this.viLang = false;
        this.cmMeaUnit = true;
        this.round = false;
        this.autoLoad = true;
        this.zW = 550;
        this.zH = 500;
        this.setLayout(new BorderLayout(0, 0));
        this.setMinimumSize(new Dimension(640, 525));
        this.img = this.getToolkit().getImage(this.filePath);
        this.imageFrame = new ImgFrame(this.img);
        this.imageFrame.setFocusable(true);
        this.imageFrame.scale = this.scale;
        this.imageFrame.setFrame(this);
        this.imageFrame.setAPP_NAME("Portrait Tool 1.05 - ");
        this.imgScrollPane = new ImageScrollPane(this.imageFrame);
        this.imgScrollPane.setBackground(Color.DARK_GRAY);
        this.imgScrollPane.setWheelScrollingEnabled(false);
        this.imgScrollPane.add(this.imageFrame, 0, 0);
        this.imgScrollPane.setContent(this.imageFrame);
        this.myTracker = new MediaTracker(this.imageFrame);
        this._start = new Point(0, 0);
        this.currentScrollPosition = new Point(0, 0);
        this.creatGUI();
        initLayoutUpdater();
        this.processEvent();
    }

    private void initLayoutUpdater() {
        layoutUpdater = new LayoutUpdater();
        layoutUpdater.setLayoutContent(this.imgScrollPane);
        layoutUpdater.setMasterContent(this);
        layoutUpdater.setEditPanel(this.editPanel);
    }


    public void processEvent() {
        this.open.addActionListener(this);
        this.save.addActionListener(this);
        this.Undo.addActionListener(this);
        this.Redo.addActionListener(this);
        this.export.addActionListener(this);
        this.savepoint.addActionListener(this);
        this.exit.addActionListener(this);
        this.about.addActionListener(this);
        this.help.addActionListener(this);
        this.keyAss.addActionListener(this);
        this.setting.addActionListener(this);
        this.viItem.addActionListener(this);
        this.enItem.addActionListener(this);
        this.filterButton.addActionListener(this);
        this.rotateCW.addActionListener(this);
        this.rotateCCW.addActionListener(this);
        this.rotate180.addActionListener(this);
        this.resize.addActionListener(this);
        this.flipH.addActionListener(this);
        this.flipV.addActionListener(this);
        this.imageFrame.openWith.addActionListener(this);
        this.imageFrame.undo.addActionListener(this);
        this.imageFrame.redo.addActionListener(this);
        this.imageFrame.CW90.addActionListener(this);
        this.imageFrame.CCW90.addActionListener(this);
        this.imageFrame.FH.addActionListener(this);
        this.imageFrame.FV.addActionListener(this);
        this.imageFrame.RZ.addActionListener(this);
        this.imageFrame.R180.addActionListener(this);
        this.redPlate.addMouseListener(this);
        this.bluePlate.addMouseListener(this);
        this.greenPlate.addMouseListener(this);
        this.blackPlate.addMouseListener(this);
        this.whitePlate.addMouseListener(this);
        this.yellowPlate.addMouseListener(this);
        this.grayPlate.addMouseListener(this);
        this.dark_grayPlate.addMouseListener(this);
        this.cyanPlate.addMouseListener(this);
        this.currentColor.addMouseListener(this);
        this.imageFrame.addMouseListener(this);
        this.imgScrollPane.addMouseListener(this);
        this.imageFrame.addMouseMotionListener(this);
        this.imageFrame.addKeyListener(this);
        this.addKeyListener(this);
        this.imageFrame.addMouseWheelListener(this);
        this.stateBarChk.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) {
                    StickyPoint.this.stateBar.setVisible(true);
                }

                if (e.getStateChange() == 2) {
                    StickyPoint.this.stateBar.setVisible(false);
                }

                StickyPoint.this.doLayout();
            }
        });
        this.ctrlPnlChk.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) {
                    StickyPoint.this.controlPanel.setVisible(true);
                }

                if (e.getStateChange() == 2) {
                    StickyPoint.this.controlPanel.setVisible(false);
                    Noitifier.printConsole("Control Panel visible:" + StickyPoint.this.controlPanel.isVisible());
                }

                StickyPoint.this.doLayout();
            }
        });
    }

    public void creatGUI() {
        this.openfd = new FileDialog(this, this.open_flie_dialog_title, 0);
        this.savefd = new FileDialog(this, this.save_file_dialog_title, 1);
        String[] list = new String[]{"jpg", "png", "gif", "jpeg", "bmp"};
        ImageFileFilter filter = new ImageFileFilter(list);
        this.openfd.setFilenameFilter(filter);
        this.openfd.setMultipleMode(false);
        this.openfd.setBounds(250, 100, 700, 500);
        ImageIcon zoomIcon = new ImageIcon(Img.creatImg(1));
        ImageIcon dragIcon = new ImageIcon(Img.creatImg(6));
        ImageIcon stickIcon = new ImageIcon(Img.creatImg(11));
        ImageIcon gridIcon = new ImageIcon(Img.creatImg(4));
        ImageIcon p2pIcon = new ImageIcon(Img.creatImg(8));
        ImageIcon openIcon = new ImageIcon(Img.creatImg(3));
        ImageIcon saveIcon = new ImageIcon(Img.creatImg(2));
        ImageIcon undoIcon = new ImageIcon(Img.creatImg(12));
        ImageIcon redoIcon = new ImageIcon(Img.creatImg(5));
        ImageIcon exportIcon = new ImageIcon(Img.creatImg(7));
        ImageIcon pmatrixIcon = new ImageIcon(Img.creatImg(9));
        ImageIcon settingIcon = new ImageIcon(Img.creatImg(10));
        ImageIcon filterIcon = new ImageIcon(Img.creatImg(20));
        this.openButton = new JButton("Open", openIcon);
        this.openButton.addActionListener(this);
        this.openButton.setMnemonic('o');
        this.saveButton = new JButton("Save", saveIcon);
        this.saveButton.addActionListener(this);
        this.saveButton.setMnemonic('s');
        this.zoomButton = new JButton("", zoomIcon);
        this.zoomButton.addActionListener(this);
        this.zoomButton.setToolTipText("Z  Open Zoom Dialog");
        this.zoomButton.setEnabled(false);
        this.dragButton = new JButton("", dragIcon);
        this.dragButton.addActionListener(this);
        this.dragButton.setMnemonic('d');
        this.dragButton.setToolTipText("Alt + D Hand Tool");
        this.undoButton = new JButton("Undo", undoIcon);
        this.undoButton.addActionListener(this);
        this.undoButton.setToolTipText("Ctrl + Z");
        this.redoButton = new JButton("Redo", redoIcon);
        this.redoButton.addActionListener(this);
        this.redoButton.setMnemonic('z');
        this.redoButton.setToolTipText("Alt + Z");
        this.stickButton = new JButton("", stickIcon);
        this.stickButton.addActionListener(this);
        this.stickButton.setMnemonic('t');
        this.stickButton.setToolTipText("Alt + T  Tick point and save location into Stack");
        this.gridButton = new JButton("", gridIcon);
        this.gridButton.addActionListener(this);
        this.gridButton.setMnemonic('g');
        this.gridButton.setToolTipText("Alt + G  Draw grid with preferred gridsize");
        this.pointButton = new JButton("", p2pIcon);
        this.pointButton.addActionListener(this);
        this.pointButton.setMnemonic('p');
        this.pointButton.setToolTipText("Alt + P  Measure distance point to point");
        this.exportButton = new JButton("", exportIcon);
        this.exportButton.addActionListener(this);
        this.exportButton.setMnemonic('x');
        this.exportButton.setToolTipText("Alt + X  Export Ticked point's location to excell file");
        this.pMatrixButton = new JButton("", pmatrixIcon);
        this.pMatrixButton.addActionListener(this);
        this.pMatrixButton.setToolTipText("Save Image with Tick point only");
        this.settingButton = new JButton("Setting", settingIcon);
        this.settingButton.setHorizontalAlignment(2);
        this.settingButton.addActionListener(this);
        this.filterButton = new JButton("Filter", filterIcon);
        this.filterButton.setToolTipText("Open filter box");
        this.undoButton.setEnabled(false);
        this.redoButton.setEnabled(false);
        this.gridButton.setEnabled(false);
        this.stickButton.setEnabled(true);
        this.dragButton.setEnabled(false);
        this.pointButton.setEnabled(false);
        this.exportButton.setEnabled(false);
        this.pMatrixButton.setEnabled(false);
        this.filterButton.setEnabled(false);
        this.zoomButton.setFocusable(false);
        this.dragButton.setFocusable(false);
        this.stickButton.setFocusable(false);
        this.gridButton.setFocusable(false);
        this.undoButton.setFocusable(false);
        this.redoButton.setFocusable(false);
        this.settingButton.setFocusable(false);
        this.pointButton.setFocusable(false);
        this.exportButton.setFocusable(false);
        this.pMatrixButton.setFocusable(false);
        this.filterButton.setFocusable(false);
        Panel buttonPanel = new Panel();
        Panel btpSubPanel1 = new Panel();
        Panel btpSubPanel2 = new Panel();
        Panel textFeildpnl = new Panel();
        Panel btpSubPanel2b = new Panel();
        buttonPanel.setLayout(new GridLayout(2, 0, 2, 3));
        btpSubPanel1.setLayout(new GridLayout(1, 0, 5, 5));
        btpSubPanel2.setLayout(new BorderLayout(5, 1));
        btpSubPanel2b.setLayout(new GridLayout(1, 0, 5, 2));
        textFeildpnl.setLayout(new BorderLayout());
        textFeildpnl.setPreferredSize(new Dimension(90, 25));
        btpSubPanel1.add(this.openButton);
        btpSubPanel1.add(this.saveButton);
        btpSubPanel1.add(this.undoButton);
        btpSubPanel1.add(this.redoButton);
        textFeildpnl.add(this.filterButton, "Center");
        btpSubPanel2b.add(this.zoomButton);
        btpSubPanel2b.add(this.dragButton);
        btpSubPanel2b.add(this.stickButton);
        btpSubPanel2b.add(this.gridButton);
        btpSubPanel2b.add(this.pointButton);
        btpSubPanel2b.add(this.exportButton);
        btpSubPanel2b.add(this.pMatrixButton);
        btpSubPanel2.add(textFeildpnl, "West");
        btpSubPanel2.add(btpSubPanel2b, "Center");
        buttonPanel.add(btpSubPanel1);
        buttonPanel.add(btpSubPanel2);
        Panel selectColorPanel = new Panel();
        Panel colorPanel = new Panel();
        colorPanel.setLayout(new BorderLayout(5, 1));
        this.currentColor = new Circle3D(Color.CYAN);
        this.currentColorWarper = new Panel();
        this.currentColorWarper.setBackground(Color.white);
        this.currentColorWarper.setLayout(new BorderLayout());
        this.currentColor.setPreferredSize(new Dimension(50, 50));
        this.currentColor.setCursor(new Cursor(12));
        selectColorPanel.setBackground(new Color(4140076));
        selectColorPanel.setLayout(new GridLayout(2, 0, 5, 5));
        this.redPlate = new ColorPlate(Color.RED);
        this.bluePlate = new ColorPlate(Color.BLUE);
        this.greenPlate = new ColorPlate(Color.GREEN);
        this.blackPlate = new ColorPlate(Color.BLACK);
        this.whitePlate = new ColorPlate(Color.WHITE);
        this.yellowPlate = new ColorPlate(Color.YELLOW);
        this.grayPlate = new ColorPlate(Color.GRAY);
        this.dark_grayPlate = new ColorPlate(Color.DARK_GRAY);
        this.cyanPlate = new ColorPlate(Color.CYAN);
        selectColorPanel.add(this.redPlate);
        selectColorPanel.add(this.bluePlate);
        selectColorPanel.add(this.greenPlate);
        selectColorPanel.add(this.blackPlate);
        selectColorPanel.add(this.whitePlate);
        selectColorPanel.add(this.yellowPlate);
        selectColorPanel.add(this.grayPlate);
        selectColorPanel.add(this.dark_grayPlate);
        selectColorPanel.add(this.cyanPlate);
        selectColorPanel.add(this.settingButton);
        this.currentColorWarper.add(this.currentColor, "Center");
        colorPanel.add(this.currentColorWarper, "West");
        colorPanel.add(selectColorPanel, "Center");
        this.controlPanel = new Panel();
        this.stateBar = new Panel();
        this.stateBar.setBackground(Color.LIGHT_GRAY);
        this.stateBar.setLayout(new BorderLayout());
        this.state = new TextField("App Started", 120);
        this.state.setBackground(this.getBackground());
        this.state.setEditable(false);
        this.state.setFocusable(false);
        this.state.setCursor(new Cursor(0));
        this.currentZoomState = new TextField("100%", 5);
        this.currentZoomState.setEditable(false);
        this.currentZoomState.setFocusable(false);
        this.currentZoomState.setCursor(new Cursor(0));
        this.stateBar.add(this.currentZoomState, "East");
        this.stateBar.add(this.state, "West");
        this.controlPanel.setLayout(new GridLayout(1, 2, 5, 5));
        this.controlPanel.setPreferredSize(new Dimension(300, 50));
        this.controlPanel.add(buttonPanel);
        this.controlPanel.add(colorPanel);
        this.controlPanel.setBackground(new Color(12357519));
        this.editPanel = new EditPanel();
        this.editPanel.setLayout(new BorderLayout());
        this.editPanel.add(this.imgScrollPane);
        this.editPanel.setBackground(new Color(128, 128, 128, 0));
        this.add(this.controlPanel, "North");
        this.add(this.stateBar, "South");
        this.add(this.editPanel, "Center");
        MenuBar menu = new MenuBar();
        this.menuFile = new Menu("File");
        this.menuEdit = new Menu("Edit");
        this.menuHelp = new Menu("Help");
        this.menuView = new Menu("View");
        this.menuOption = new Menu("Options");
        this.menuLanguage = new Menu("Language");
        this.menuImage = new Menu("Image");
        this.open = new MenuItem("Open");
        this.save = new MenuItem("Save");
        this.export = new MenuItem("Export Data");
        this.savepoint = new MenuItem("Save Point Matrix");
        this.exit = new MenuItem("Exit");
        this.Undo = new MenuItem("Undo");
        this.Redo = new MenuItem("Redo");
        MenuItem select = new MenuItem("Select");
        select.setEnabled(false);
        this.rotateCW = new MenuItem("Rotate CW 90");
        this.rotateCCW = new MenuItem("Rotate CCW 90");
        this.rotate180 = new MenuItem("Rotate 180");
        this.flipH = new MenuItem("Flip Horizontal");
        this.flipV = new MenuItem("Flip Vertical");
        this.resize = new MenuItem("Resize");
        this.help = new MenuItem("Help Content");
        this.keyAss = new MenuItem("Key Assist");
        this.about = new MenuItem("About");
        this.setting = new MenuItem("Setting");
        this.viItem = new MenuItem("Vietnamese");
        this.enItem = new MenuItem("English");
        this.stateBarChk = new CheckboxMenuItem("Statebar", true);
        this.ctrlPnlChk = new CheckboxMenuItem("Control Panel", true);
        this.menuFile.setFont(new Font("Dialog", 1, 12));
        this.menuImage.setEnabled(false);
        this.menuFile.add(this.open);
        this.menuFile.add(this.save);
        this.menuFile.add(this.export);
        this.menuFile.add(this.savepoint);
        this.menuFile.addSeparator();
        this.menuFile.add(this.exit);
        this.menuEdit.add(this.Undo);
        this.menuEdit.add(this.Redo);
        this.menuEdit.addSeparator();
        this.menuEdit.add(this.menuImage);
        this.menuEdit.add(select);
        this.menuImage.add(this.rotateCW);
        this.menuImage.add(this.rotateCCW);
        this.menuImage.add(this.rotate180);
        this.menuImage.add(this.flipH);
        this.menuImage.add(this.flipV);
        this.menuImage.addSeparator();
        this.menuImage.add(this.resize);
        this.menuHelp.add(this.help);
        this.menuHelp.add(this.keyAss);
        this.menuHelp.addSeparator();
        this.menuHelp.add(this.about);
        this.menuView.add(this.ctrlPnlChk);
        this.menuView.add(this.stateBarChk);
        this.menuOption.add(this.setting);
        this.menuOption.addSeparator();
        this.menuOption.add(this.menuLanguage);
        this.menuLanguage.add(this.viItem);
        this.menuLanguage.add(this.enItem);
        menu.add(this.menuFile);
        menu.add(this.menuEdit);
        menu.add(this.menuView);
        menu.add(this.menuOption);
        menu.add(this.menuHelp);
        MenuItem u = new MenuItem("Undo");
        MenuItem r = new MenuItem("Redo");
        Menu i = new Menu("Image");
        i.setEnabled(false);
        this.setMenuBar(menu);
        this.imageFrame.setPopupMenu(u, r, i);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.openButton || e.getSource() == this.open) {
            String[] list = new String[]{"jpg", "png", "gif", "jpeg", "bmp"};
            ImageFileFilter filter = new ImageFileFilter(list);
            this.openfd.setVisible(true);
            this.openfd.doLayout();
            if (this.openfd.getFile() != null) {
                String path = this.openfd.getDirectory() + this.openfd.getFile();
                String dir = this.openfd.getDirectory();
                File d = new File(dir);
                String name = this.openfd.getFile();
                boolean fchk = false;

                for(int i = 0; i < list.length; ++i) {
                    fchk = filter.accept(d, name.toLowerCase());
                }

                if (fchk) {
                    this.openFile(path);
                } else {
                    this.state.setBackground(LIGHT_RED);
                    this.state.setText(this.not_support_format_export_file + " ---------------> " + name);
                }
            } else {
                this.state.setBackground(Color.WHITE);
                this.state.setText(this.cancel_openfile);
                Noitifier.printConsole("Canceled by user");
            }
        }

        if (e.getSource() == this.saveButton || e.getSource() == this.save) {
            this.savefd.setFile("Untitled.png");
            this.savefd.setVisible(true);
            if (this.savefd.getFile() != null) {
                String dir = this.savefd.getDirectory();
                String filename = this.savefd.getFile();
                Noitifier.printConsole("dir:" + dir + "; filename:" + filename);
                this.imageFrame.saveImg(dir, filename);
                Noitifier.printConsole(this.imageFrame.noity);
                if (this.imageFrame.noity == this.unknow_file_format_img_save) {
                    this.state.setBackground(LIGHT_RED);
                } else {
                    this.state.setBackground(LIGHT_GREEN);
                }

                this.state.setText(this.imageFrame.noity);
                if (this.imageFrame.noity != this.unknow_file_format_img_save) {
                    this.editstate = StickyPoint.EditState.SAVED;
                } else {
                    this.editstate = StickyPoint.EditState.NOT_SAVED;
                }
            } else {
                Noitifier.printConsole("Save cancel");
                this.state.setBackground(Color.WHITE);
                this.state.setText(this.save_cancel);
                this.editstate = StickyPoint.EditState.NOT_SAVED;
            }
        }

        if (e.getSource() == this.undoButton || e.getSource() == this.Undo || e.getSource() == this.imageFrame.undo) {
            this.undo();
        }

        if (e.getSource() == this.redoButton || e.getSource() == this.Redo || e.getSource() == this.imageFrame.redo) {
            this.redo();
        }

        if (this.imageFrame.action.isEmpty()) {
            this.editstate = StickyPoint.EditState.SAVED;
        } else {
            this.editstate = StickyPoint.EditState.MODIFIED;
        }

        if (e.getSource() == this.export || e.getSource() == this.exportButton) {
            this.exportStickPoint();
        }

        if (e.getSource() == this.savepoint || e.getSource() == this.pMatrixButton) {
            this.imageFrame.savePointMatrix();
        }

        if (e.getSource() == this.settingButton || e.getSource() == this.setting) {
            this.openSetting();
        }

        if (e.getSource() == this.exit) {
            this.closeApp();
        }

        if (e.getSource() == this.viItem) {
            this.viLang = true;
            this.imageFrame.viLang = true;
            this.setLanguage(this.viLang);
        }

        if (e.getSource() == this.enItem) {
            this.viLang = false;
            this.imageFrame.viLang = false;
            this.setLanguage(this.viLang);
        }

        if (e.getSource() == this.rotateCW || e.getSource() == this.imageFrame.CW90) {
            this.imageFrame.affineTranform(Tranform.ROTATE_CW);
            this.imageFrame.getClass();
            SPoint p = new SPoint(0, 2002002, 0);
            this.imageFrame.action.push(p);
            this.imageFrame.reaction.popAll();
            if (this.imageFrame.w < this.editPanel.getWidth() || this.imageFrame.h < this.editPanel.getHeight()) {
                this.floating = true;
            }

            if (this.floating) {
                this.floattingScrPanel();
            }

            if (this.imageFrame.w > this.editPanel.getWidth() && this.imageFrame.h > this.editPanel.getHeight()) {
                this.fixed();
                this.updateLayout();
            }
        }

        if (e.getSource() == this.rotateCCW || e.getSource() == this.imageFrame.CCW90) {
            this.imageFrame.affineTranform(Tranform.ROTATE_CCW);
            this.imageFrame.getClass();
            SPoint p = new SPoint(0, 2002002, 0);
            this.imageFrame.action.push(p);
            this.imageFrame.reaction.popAll();
            if (this.imageFrame.w < this.editPanel.getWidth() || this.imageFrame.h < this.editPanel.getHeight()) {
                this.floating = true;
            }

            if (this.floating) {
                this.floattingScrPanel();
            }

            if (this.imageFrame.w > this.editPanel.getWidth() && this.imageFrame.h > this.editPanel.getHeight()) {
                this.fixed();
                this.updateLayout();
            }
        }

        if (e.getSource() == this.rotate180 || e.getSource() == this.imageFrame.R180) {
            this.imageFrame.affineTranform(Tranform.ROTATE_180);
            this.imageFrame.reaction.popAll();
        }

        if (e.getSource() == this.resize || e.getSource() == this.imageFrame.RZ) {
            this.openResizeBox();
        }

        if (e.getSource() == this.flipH || e.getSource() == this.imageFrame.FH) {
            this.imageFrame.FlipImg(0);
            this.imageFrame.getClass();
            SPoint act = new SPoint(0, 3003001, 0);
            this.imageFrame.action.push(act);
            this.imageFrame.reaction.popAll();
        }

        if (e.getSource() == this.flipV || e.getSource() == this.imageFrame.FV) {
            this.imageFrame.FlipImg(1);
            this.imageFrame.getClass();
            SPoint act = new SPoint(0, 3003002, 0);
            this.imageFrame.action.push(act);
            this.imageFrame.reaction.popAll();
        }

        if (e.getSource() == this.about) {
            String mgs = "Portrait Tool 1.05 - \n Coded by Thanh Dat \n phithanhdathd@gmail.com \n This is free for all. \n Thanks !";
            MessageBox about = new MessageBox(this, mgs, true);
            about.setSize(300, 250);
            about.setVisible(true);
            Noitifier.printConsole("About pressed");
        }

        if (e.getSource() == this.help) {
            this.openHelp();
        }

        if (e.getSource() == this.imageFrame.openWith) {
            Runtime run = Runtime.getRuntime();

            try {
                run.exec("mspaint.exe " + this.filePath);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        if (e.getSource() == this.keyAss) {
            this.openKeyAss();
        }

        if (e.getSource() == this.zoomButton) {
            if (this.Shf == Key.PRESSED) {
                this.openZoomBox();
                this.Shf = Key.NONE_PRESSED;
            } else {
                if (this.mouseMode != MouseMode.ZOOM) {
                    this.imageFrame.changeZoomMode(true);
                } else if (this.imageFrame.zoom) {
                    this.imageFrame.changeZoomMode(false);
                } else {
                    this.imageFrame.changeZoomMode(true);
                }

                this.dragMode = false;
                this.mouseMode = MouseMode.ZOOM;
                this.buttonStateTracking();
            }
        }

        if (e.getSource() == this.dragButton) {
            this.imageFrame.changeDragMode();
            this.buttonStateTracking();
            this.dragMode = true;
            this.mouseMode = MouseMode.DRAG;
        }

        if (e.getSource() == this.stickButton) {
            this.imageFrame.changeStickMode();
            this.buttonStateTracking();
            this.dragMode = false;
            this.mouseMode = MouseMode.STICK;
        }

        if (e.getSource() == this.gridButton) {
            this.imageFrame.changeGridMode();
            this.buttonStateTracking();
            this.dragMode = false;
            this.mouseMode = MouseMode.GRID;
        }

        if (e.getSource() == this.pointButton) {
            this.imageFrame.changeP2PMode();
            this.buttonStateTracking();
            this.dragMode = false;
            this.mouseMode = MouseMode.P2P;
            this.printScale(this.imageFrame.scale);
            Noitifier.printConsole("P 2 P pressed");
        }

        if (e.getSource() == this.filterButton) {
            this.openFilterBox();
        }

        this.buttonStateTracking();
    }

    private void updateLayout() {
        int imgSize = this.imageFrame.getOriginDimension().width
                * this.imageFrame.getOriginDimension().height;

        layoutUpdater.setIterval(imgSize);

        layoutUpdater.start(); // reuse, không tạo mới

        this.floating = true;
        System.out.println("Update layout called");
    }

    private void openResizeBox() {
        ResizeBox rsb = new ResizeBox(this, "Resize", true);
        rsb.setImgDimension(this.imageFrame.getBufferedImage().getWidth(), this.imageFrame.getBufferedImage().getHeight());
        rsb.box.setVisible(true);
        if (rsb.action == "OK") {
            Noitifier.printConsole("Resize: X" + rsb.Xrs + "; Y:" + rsb.Yrs + "; type:" + rsb.type);
            this.imageFrame.reSizeImg(rsb.Xrs, rsb.Yrs, rsb.type);
            this.imageFrame.getClass();
            SPoint p = new SPoint(0, 2002002, rsb.type);
            this.imageFrame.action.push(p);
            this.imageFrame.reaction.popAll();
            this.imageFrame.tranformReAction.clear();
            this.editstate = StickyPoint.EditState.MODIFIED;
            this.buttonStateTracking();
            this.state.setBackground(WHITE);
            this.state.setText("Resize Image !!");
            this.floattingScrPanel();
            this.updateLayout();
        }

        if (rsb.action == "CANCEL") {
            Noitifier.printConsole("resize cancel");
        }

    }

    private void openKeyAss() {
        KeyAssistBox keyAssBox = new KeyAssistBox(this, "Key Assist", false, this.viLang);
        keyAssBox.box.setVisible(true);
    }

    private void openHelp() {
        HelpBox help = new HelpBox(this, "Help Content", true);
        help.box.setVisible(true);
    }

    private void openFilterBox() {
        FilterBox fb = new FilterBox(this, "Image Filter", true, this.imageFrame.getBufferedImage());
        fb.setConten(this.imageFrame);
        fb.box.setVisible(true);
        if (fb.action == "OK") {
            Image i = fb.getImage();
            FilterProperties fp = fb.getFilterProperties();
            this.filterAction.push(fp);
            this.imageFrame.getClass();
            SPoint act = new SPoint(0, 1001001, 0);
            this.imageFrame.action.push(act);
            this.imageFrame.setFiltered(i);
            this.imageFrame.reaction.popAll();
            this.editstate = StickyPoint.EditState.MODIFIED;
            this.buttonStateTracking();
            this.state.setBackground(WHITE);
            this.state.setText(fp.toString());
        }

        if (fb.action == "CANCEL") {
            this.imageFrame.repaint();
        }

    }

    public void redo() {
        SPoint sp = this.imageFrame.reaction.pop();
        this.imageFrame.action.push(sp);
        int var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 66772508) {
            this.imageFrame.redoClick();
            this.stackAvailableSpaceNoitifier();
            this.redoButton.setFocusable(false);
        }

        var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 25251325) {
            this.imageFrame.redoGrid();
        }

        var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 1001001) {
            FilterProperties fp = this.filterReAction.pop();
            this.filterAction.push(fp);
            this.imageFrame.redoFilter(fp);
        }

        var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 2002002) {
            this.imageFrame.redoTranform(sp.Y);
            this.updateLayout();
        }

        var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 3003001) {
            this.imageFrame.FlipImg(0);
        }

        var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 3003002) {
            this.imageFrame.FlipImg(1);
        }

        this.imageFrame.currentAction = sp;
    }

    public void undo() {
        SPoint sp = this.imageFrame.action.pop();
        this.imageFrame.reaction.push(sp);
        int var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 66772508) {
            this.imageFrame.undoStick();
            this.stackAvailableSpaceNoitifier();
            this.undoButton.setFocusable(false);
        }

        var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 25251325) {
            this.imageFrame.undoGrid();
        }

        var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 1001001) {
            FilterProperties fp = this.filterAction.pop();
            this.imageFrame.undoFilter(this.filterAction);
            this.filterReAction.push(fp);
        }

        var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 2002002) {
            this.imageFrame.undoTranForm(sp.Y);
            this.updateLayout();
        }

        var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 3003001) {
            this.imageFrame.FlipImg(0);
        }

        var10000 = sp.X;
        this.imageFrame.getClass();
        if (var10000 == 3003002) {
            this.imageFrame.FlipImg(1);
        }

    }

    public void exportStickPoint() {
        FileDialog fd = new FileDialog(this, this.export_point_dialog_title, 1);
        fd.setFile("*.xls");
        fd.setVisible(true);
        if (fd.getFile() != null && fd.getFile() != "*.xls") {
            String p = fd.getDirectory() + fd.getFile();
            if (fd.getFile().endsWith(".xls")) {
                File file = new File(p);
                this.imageFrame.exportData(file);
                this.state.setText(this.imageFrame.noity);
                this.state.setBackground(LIGHT_GREEN);
            } else {
                this.state.setText(this.not_support_format_export_file);
                this.state.setBackground(LIGHT_RED);
            }
        } else {
            this.state.setText(this.export_cancel);
            this.state.setBackground(Color.WHITE);
        }

    }

    public void openFile(String file_path) {
        File f = new File(file_path);
        if (f.exists()) {
            Noitifier.printConsole("---------->Flie is okey:" + f.getName());
            this.filePath = file_path;
            this.zoomButton.setEnabled(true);
            this.gridButton.setEnabled(true);
            this.pointButton.setEnabled(true);
            this.filterButton.setEnabled(true);
            this.menuImage.setEnabled(true);
            this.imageFrame.menuImage.setEnabled(true);
            this.createImg(this.filePath);
            this.myTracker.addImage(this.img, 0);

            try {
                this.myTracker.waitForID(0);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                this.state.setText(e1.toString());
            }

            this.imageFrame.updateImg(this.img);
            this.imageFrame.setPath(this.filePath);
            this.imageFrame.resetAll();
            this.currentZoom = 1.0F;
            this.currentZoomState.setText("100%");
            this.stackAvailableSpaceNoitifier();
            if (this.imageFrame.w >= this.getWidth() && this.imageFrame.h >= this.getHeight()) {
                this.fixed();
            } else {
                this.floating = true;
            }

            if (this.floating) {
                this.imgScrollPane.layoutContent(new Point(0, 0));
            }

            this.imgScrollPane.doLayout();
            this.doLayout();
            this.state.setBackground(LIGHT_GREEN);
            this.state.setText(this.file_opened + this.filePath);
            if (this.myTracker.statusID(0, true) == 8) {
                this.imgScrollPane.repaint();
                this.imageFrame.updateBuffer();
                this.imgScrollPane.doLayout();
                this.imgScrollPane.setScrollPosition(0, 0);
                Noitifier.printConsole("imageFrame Size:" + this.imageFrame.getSize());
                Noitifier.printConsole("img size:" + this.img.getWidth(this.getParent()) + "," + this.img.getHeight(this.getParent()));
                Noitifier.printConsole("Openfile - floating:" + this.floating);
                this.openButton.setFocusable(false);
                this.imageFrame.setFocusable(true);
            }

            this.updateWindowTitle();
            Noitifier.printConsole("Path:" + this.filePath);
        } else {
            Noitifier.printConsole("File not exist");
            this.state.setBackground(LIGHT_RED);
            this.state.setText(this.file_not_found);
        }

    }

    public void updateWindowTitle() {
        this.setTitle("Portrait Tool 1.05 - " + this.filePath + "  -  " + this.imageFrame.getBufferedImage().getWidth() + "x" + this.imageFrame.getBufferedImage().getHeight());
    }

    public void buttonStateTracking() {
        if (this.imageFrame.action.isEmpty()) {
            this.undoButton.setEnabled(false);
        } else {
            this.undoButton.setEnabled(true);
        }

        if (this.imageFrame.reaction.isEmpty()) {
            this.redoButton.setEnabled(false);
        } else {
            this.redoButton.setEnabled(true);
        }

        if (this.imageFrame.s.isEmpty()) {
            this.exportButton.setEnabled(false);
            this.pMatrixButton.setEnabled(false);
        } else {
            this.exportButton.setEnabled(true);
            this.pMatrixButton.setEnabled(true);
        }

        if (this.imageFrame.mouseMode == MouseMode.DRAG) {
            this.dragButton.setEnabled(false);
            this.stickButton.setEnabled(true);
            this.gridButton.setEnabled(true);
            this.pointButton.setEnabled(true);
        }

        if (this.imageFrame.mouseMode == MouseMode.STICK) {
            this.dragButton.setEnabled(true);
            this.stickButton.setEnabled(false);
            this.gridButton.setEnabled(true);
            this.pointButton.setEnabled(true);
        }

        if (this.imageFrame.mouseMode == MouseMode.GRID) {
            this.dragButton.setEnabled(true);
            this.stickButton.setEnabled(true);
            this.gridButton.setEnabled(false);
            this.pointButton.setEnabled(true);
        }

        if (this.imageFrame.mouseMode == MouseMode.P2P) {
            this.dragButton.setEnabled(true);
            this.stickButton.setEnabled(true);
            this.gridButton.setEnabled(true);
            this.pointButton.setEnabled(false);
        }

        if (this.imageFrame.mouseMode == MouseMode.ZOOM) {
            this.dragButton.setEnabled(true);
            this.stickButton.setEnabled(true);
            this.gridButton.setEnabled(true);
            this.pointButton.setEnabled(true);
        }

        this.cursorButton(this.dragButton);
        this.cursorButton(this.gridButton);
        this.cursorButton(this.stickButton);
        this.cursorButton(this.zoomButton);
        this.cursorButton(this.pointButton);
        this.cursorButton(this.openButton);
        this.cursorButton(this.saveButton);
        this.cursorButton(this.undoButton);
        this.cursorButton(this.redoButton);
        this.cursorButton(this.exportButton);
        this.cursorButton(this.pMatrixButton);
        this.menuItemStateTracking();
    }

    private void cursorButton(JButton b) {
        if (b.isEnabled()) {
            b.setCursor(new Cursor(12));
        } else {
            b.setCursor(new Cursor(0));
        }

    }

    private void menuItemStateTracking() {
        if (this.imageFrame.action.isEmpty()) {
            this.Undo.setEnabled(false);
            this.imageFrame.undo.setEnabled(false);
        } else {
            this.Undo.setEnabled(true);
            this.imageFrame.undo.setEnabled(true);
        }

        if (this.imageFrame.reaction.isEmpty()) {
            this.Redo.setEnabled(false);
            this.imageFrame.redo.setEnabled(false);
        } else {
            this.Redo.setEnabled(true);
            this.imageFrame.redo.setEnabled(true);
        }

        if (this.imageFrame.s.isEmpty()) {
            this.export.setEnabled(false);
            this.savepoint.setEnabled(false);
            this.menuImage.setEnabled(true);
            this.imageFrame.menuImage.setEnabled(true);
        } else {
            this.export.setEnabled(true);
            this.savepoint.setEnabled(true);
            this.menuImage.setEnabled(false);
            this.imageFrame.menuImage.setEnabled(false);
        }

        if (this.filePath == "Untitled-00.jpg") {
            this.menuImage.setEnabled(false);
            this.imageFrame.menuImage.setEnabled(false);
        }

    }

    public void openZoomBox() {
        if (this.imageFrame.zoomdialog != null) {
            this.scale = this.imageFrame.zoomdialog.iz.scale;
            this.ab_ck = this.imageFrame.zoomdialog.absoluteUnit_check;
            this.cm_ck = this.imageFrame.zoomdialog.cmIndicate_check;
            this.imageFrame.scale = this.scale;
            Noitifier.printConsole("scale:" + this.scale);
        }

        if (!this.imageFrame.zoomBoxOpened) {
            this.imageFrame.openZoomBox(this, this.zW, this.zH, this.ab_ck, this.cm_ck);
        } else {
            this.imageFrame.closeZoomBox();
        }

    }

    public void openSetting() {
        SettingDialog set = new SettingDialog(this, this.setting_dialog_title, true, this.imageFrame.getBrushColor(), this.imageFrame.stSize, this.imageFrame.gridAmount, this.imageFrame.scale);
        set.initialFeilds(this.viLang, this.cmUnit, this.cmMeaUnit, this.round, this.autoLoad, this.zW, this.zH);
        set.setVisible(true);
        if (set.action == "OK") {
            this.imageFrame.setBrushColor(set.getColor());
            Color c = set.getColor();
            Color indicateSelectedColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
            this.currentColor.setColor(indicateSelectedColor);
            this.imageFrame.s.setSize(set.getStackSize());
            this.imageFrame.stSize = set.getStackSize();
            this.imageFrame.gridAmount = set.getGridSize();
            set.getGridSize();
            this.imageFrame.scale = set.getScale();
            this.scale = set.getScale();
            this.viLang = set.getLanguage();
            this.setLanguage(this.viLang);
            this.cmUnit = set.getcmUnit();
            this.cmMeaUnit = set.getCmMeasureUnit();
            this.round = set.getRound();
            this.autoLoad = set.getAutoLoad();
            if (this.zW >= 200 && this.zH >= 200 && this.zH <= 700 && this.zW <= 1200) {
                this.zW = set.zW;
                this.zH = set.zH;
            } else {
                this.zW = 550;
                this.zH = 500;
            }

            this.imageFrame.round = this.round;
            this.imageFrame.cmIndicate = this.cmMeaUnit;
            this.imageFrame.viLang = this.viLang;
            this.saveSetting();
            this.state.setBackground(Color.WHITE);
            if (this.cmUnit) {
                float f = (float)this.imageFrame.gridAmount * this.scale;
                f = Math2.round(f);
                this.state.setText(this.stack_size + this.imageFrame.stSize + "\t " + this.grid_size + f + " cm");
            }

            if (!this.cmUnit) {
                this.state.setText(this.stack_size + this.imageFrame.stSize + "\t " + this.grid_size + this.imageFrame.gridAmount);
            }

            Noitifier.printConsole("setted: Vietnamese:" + this.viLang + "; grid Unit centimet:" + this.cmUnit + "; mes in Cm:" + this.cmMeaUnit + "; round:" + this.round + "; autoload:" + this.autoLoad + "; zW:" + this.zW + "; zH:" + this.zH);
        } else {
            Noitifier.printConsole("StickyPoint - setting cancel ");
        }

    }

    private void createImg(String filePath) {
        this.img = this.getToolkit().createImage(filePath);
    }

    public static void main(String[] args) {
        File log = new File("log.txt");
        log.delete();
        final StickyPoint stp = new StickyPoint();
        stp.setBounds(560, 70, 800, 600);
        stp.setTitle("Portrait Tool 1.05 - " + stp.filePath);
        stp.loadSetting();
        stp.menuItemStateTracking();
        stp.imageFrame.requestFocus();
        stp.imgScrollPane.doLayout();
        if (stp.filePath != "Untitled-00.jpg") {
            stp.curF = new File(stp.filePath);
        }

        if (args.length > 0) {
            try {
                stp.imageFrame.setPath(args[0]);
            } catch (ArrayIndexOutOfBoundsException var5) {
                Noitifier.printConsole("ko co tham so");
            }
        }

        boolean ck = stp.checkData();
        stp.setVisible(true);
        Noitifier.printConsole("App Started.");
        if (!ck) {
            MessageBox noi = new MessageBox(stp, "Warning: Data missing or corrupted!", "Warning", true);
            noi.show();
        }

        stp.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                stp.saveSetting();
                stp.closeApp();
                if (e.getNewState() == 101) {
                    stp.editPanel.doLayout();
                    Noitifier.printConsole("W state change");
                }

                Noitifier.printConsole(e);
            }
        });
        MyWindowListener wl = new MyWindowListener();
        stp.addWindowListener(wl);
        stp.addWindowStateListener(new WindowAdapter() {
            public void windowStateChanged(WindowEvent e) {
                Noitifier.printConsole("State change:" + e);
                if (e.getNewState() == 6) {
                    stp.floattingScrPanel();
                    stp.floating = true;
                }

                if (e.getOldState() == 6 && e.getNewState() == 0) {
                    if (stp.imageFrame.w > stp.editPanel.getWidth() || stp.imageFrame.h > stp.editPanel.getHeight()) {
                        stp.doLayout();
                        stp.fixed();
                        Noitifier.printConsole("main - fixed called");
                    }

                    Noitifier.printConsole("stp.imageFrame.w" + stp.imageFrame.w + " ;stp.editPanel.getWidth()" + stp.editPanel.getWidth());
                    Noitifier.printConsole("stp.imageFrame.h" + stp.imageFrame.h + "; stp.editPanel.getHeight()" + stp.editPanel.getHeight());
                }

            }
        });
    }

    private boolean checkData() {
        int pattern1 = 0;
        int pattern2 = 0;
        int pattern3 = 0;
        int pattern4 = 0;
        int pattern1b = 0;
        int pattern2b = 0;
        int pattern3b = 0;
        int pattern4b = 0;
        boolean r1 = false;
        boolean r2 = false;
        boolean result = false;

        try {
            RandomAccessFile rf = new RandomAccessFile("data\\data-69213.tmb", "r");
            RandomAccessFile rfb = new RandomAccessFile("data\\data-3971.tmb", "r");
            rf.readLine();
            rf.readLine();
            rf.readLine();
            Noitifier.printConsole(" offset begin:" + rf.getFilePointer());
            long begin = rf.getFilePointer();
            rf.seek(17024L + begin);
            pattern1 = rf.readInt();
            rf.seek(21804L + begin);
            pattern2 = rf.readInt();
            rf.seek(21812L + begin);
            pattern3 = rf.readInt();
            rf.seek(42168L + begin);
            pattern4 = rf.readInt();
            rf.close();
            rfb.readLine();
            rfb.readLine();
            rfb.readLine();
            Noitifier.printConsole(" offset begin:" + rfb.getFilePointer());
            begin = rfb.getFilePointer();
            rfb.seek(16056L + begin);
            pattern1b = rfb.readInt();
            rfb.seek(29868L + begin);
            pattern2b = rfb.readInt();
            rfb.seek(38568L + begin);
            pattern3b = rfb.readInt();
            rfb.seek(44220L + begin);
            pattern4b = rfb.readInt();
            rfb.close();
            if (pattern1 == -15421477 && pattern2 == -1122777868 && pattern3 == 34849779 && pattern4 == 1662541168) {
                Noitifier.printConsole("-------------->check data-69213.tmb passed ");
                Noitifier.printConsole("pattern1:" + pattern1);
                Noitifier.printConsole("pattern2:" + pattern2);
                Noitifier.printConsole("pattern3:" + pattern3);
                Noitifier.printConsole("pattern4:" + pattern4);
                r1 = true;
            } else {
                Noitifier.printConsole("--------------->check data-69213.tmb failed ");
                Noitifier.printConsole("pattern1:" + pattern1);
                Noitifier.printConsole("pattern2:" + pattern2);
                Noitifier.printConsole("pattern3:" + pattern3);
                Noitifier.printConsole("pattern4:" + pattern4);
                this.imageFrame.remakeCursor(13);
                r1 = false;
            }

            if (pattern1b == 2067424882 && pattern2b == -1925552526 && pattern3b == -63281550 && pattern4b == 3827314) {
                Noitifier.printConsole("--------------->check data-3971.tmb passed ");
                Noitifier.printConsole("pattern1b:" + pattern1b);
                Noitifier.printConsole("pattern2b:" + pattern2b);
                Noitifier.printConsole("pattern3b:" + pattern3b);
                Noitifier.printConsole("pattern4b:" + pattern4b);
                r2 = true;
            } else {
                Noitifier.printConsole("--------------->check data-3971.tmb failed ");
                Noitifier.printConsole("pattern1:" + pattern1b);
                Noitifier.printConsole("pattern2:" + pattern2b);
                Noitifier.printConsole("pattern3:" + pattern3b);
                Noitifier.printConsole("pattern4:" + pattern4b);
                this.imageFrame.remakeCursor(14);
                r2 = false;
            }

            if (r1 && r2) {
                result = true;
            }

            return result;
        } catch (FileNotFoundException var16) {
            Noitifier.printConsole("Data check failed, file not found!");
            return false;
        } catch (IOException var17) {
            Noitifier.printConsole("Data check failed, can't read file!");
            return false;
        }
    }

    private void setLanguage(boolean viLang2) {
        if (viLang2) {
            JLabel tep = new JLabel("Têp+");
            this.menuFile.setLabel(tep.getText());
            this.menuEdit.setLabel("Thao tác");
            this.menuHelp.setLabel("Tro giúp");
            this.open.setLabel("Mo File");
            this.openButton.setText("Mở");
            this.save.setLabel("Luu File");
            this.saveButton.setText("Lưu");
            this.export.setLabel("Xuát du liêu");
            this.savepoint.setLabel("Luu diêm");
            this.exit.setLabel("Thoát");
            this.Undo.setLabel("Hoàn tác");
            this.undoButton.setText("Hoàn tác");
            this.Redo.setLabel("Làm Lai");
            this.redoButton.setText("Làm lại");
            this.settingButton.setText("Tùy chỉnh");
            this.help.setLabel("Tro giúp");
            this.keyAss.setLabel("Phím tắt");
            this.about.setLabel("Thông tin");
            this.menuView.setLabel("Hiển thị");
            this.menuOption.setLabel("Tùy chọn");
            this.setting.setLabel("Thiết lập");
            this.menuLanguage.setLabel("Ngôn ngữ");
            this.viItem.setLabel("Việt nam");
            this.enItem.setLabel("English");
            this.stateBarChk.setLabel("Thanh trạng thái");
            this.ctrlPnlChk.setLabel("Bảng điều khiển");
            this.zoomButton.setToolTipText("Mở khung thu phóng");
            this.dragButton.setToolTipText("Kéo hình trong cửa sổ");
            this.gridButton.setToolTipText("Kẻ khung lưới ô vuông");
            this.stickButton.setToolTipText("Đánh dấu điểm và lưu tọa độ vào Stack");
            this.pointButton.setToolTipText("Đo khoảng cách điểm tới điểm");
            this.exportButton.setToolTipText("Xuất tọa độ đã đánh dấu ra file excel");
            this.pMatrixButton.setToolTipText("Xóa chỉ số, chỉ lưu điểm");
            this.openfd.setTitle("Mở tệp");
            this.savefd.setTitle("Lưu tệp");
            this.export_cancel = "Hủy xuất";
            this.not_support_format_export_file = "Định dạng không được hỗ trợ";
            this.export_point_dialog_title = "Lưu tọa độ (chỉ hỗ trợ *.xls)";
            this.file_opened = "Tệp đã mở: ";
            this.file_not_found = "Không tìm thấy tệp";
            this.save_cancel = "Hủy lưu";
            this.unknow_file_format_img_save = "Định dạng không được hỗ trợ";
            this.stack_size = "Ngăn xếp: ";
            this.grid_size = "Ô lưới: ";
            this.confirm_save_subfix = " có sự thay đổi .\n Bạn có muốn lưu lại ?";
            this.confirm_save_prefix = "Nội dung tệp: ";
            this.scale_label = "Tỷ lệ: ";
            this.select_color_title = "Chọn màu";
            this.open_flie_dialog_title = "Mở tệp";
            this.save_file_dialog_title = "Lưu tệp";
            this.cancel_openfile = "Hủy thao tác";
            this.filter_01 = "Hình với bộ lọc thang xám mặc định  -  (xem trước)";
            this.filter_02 = "Hình với bộ lọc xám khử màu đỏ (red)  -  (xem trước)";
            this.filter_03 = "Hình với bộ lọc xám khử màu lục (blue)  -  (xem trước)";
            this.filter_04 = "Hình với bộ lọc xám khử màu xanh (green)  -  (xem trước)";
            this.filter_05 = "Hình với bộ lọc xám lấy màu xanh (green)  -  (xem trước)";
            this.filter_06 = "Hình với bộ lọc xám lấy màu đỏ (red)  -  (xem trước)";
            this.filter_07 = "Hình với bộ lọc xám lấy màu lục (blue)  -  (xem trước)";
            this.filter_08 = "Hình với bộ lọc xám khử màu đỏ (red), thang xám 255  -  (xem trước)";
            this.filter_09 = "Hình với bộ lọc xám khử màu đỏ (red), thang xám 16  -  (xem trước)";
            this.filter_00 = "Mặc định!";
            this.imageFrame.setLanguage(viLang2);
            this.setting_dialog_title = "Tùy Chỉnh";
        } else {
            this.menuFile.setLabel("File");
            this.menuEdit.setLabel("Edit");
            this.menuHelp.setLabel("Help");
            this.open.setLabel("Open");
            this.openButton.setText("Open");
            this.save.setLabel("Save");
            this.saveButton.setText("Save");
            this.export.setLabel("Export Data");
            this.savepoint.setLabel("Save Point Matrix");
            this.exit.setLabel("Exit");
            this.Undo.setLabel("Undo");
            this.undoButton.setText("Undo");
            this.Redo.setLabel("Redo");
            this.redoButton.setText("Redo");
            this.settingButton.setText("Setting");
            this.help.setLabel("Help Content");
            this.keyAss.setLabel("Key Assist");
            this.about.setLabel("About");
            this.menuView.setLabel("View");
            this.menuOption.setLabel("Options");
            this.setting.setLabel("Setting");
            this.menuLanguage.setLabel("Language");
            this.viItem.setLabel("Vietnamese");
            this.enItem.setLabel("English");
            this.stateBarChk.setLabel("Statebar");
            this.ctrlPnlChk.setLabel("Control Pane");
            this.zoomButton.setToolTipText("Z  Open Zoom Dialog");
            this.dragButton.setToolTipText("Alt + D  Hand Tool");
            this.gridButton.setToolTipText("Alt + G  Draw grid with preferred gridsize");
            this.stickButton.setToolTipText("Alt + T  Tick point and save location into Stack");
            this.pointButton.setToolTipText("Alt + P  Measure distance point to point");
            this.exportButton.setToolTipText("Alt + X  Export Ticked point's location to excell file");
            this.pMatrixButton.setToolTipText("Save Image with Tick point only");
            this.not_support_format_export_file = "File formart not supported";
            this.export_cancel = "Export canceled";
            this.export_point_dialog_title = "Save data ( *.xls type only)";
            this.file_opened = "File opened: ";
            this.file_not_found = "File not found";
            this.save_cancel = "Save cancel";
            this.unknow_file_format_img_save = "Unknow file format.";
            this.stack_size = "Stack Size: ";
            this.grid_size = "Grid Size: ";
            this.confirm_save_subfix = " had been modified.\n Do you want to save it?";
            this.confirm_save_prefix = "File: ";
            this.scale_label = "Scale: ";
            this.select_color_title = "Select Color";
            this.open_flie_dialog_title = "Open File";
            this.save_file_dialog_title = "Save File";
            this.cancel_openfile = "Cancel Open";
            this.setting_dialog_title = "Setting";
            this.filter_01 = "Image in Default RGB Gray Filter - (Preview)";
            this.filter_02 = "Image in Gray Filter with Red Chanel off - (Preview)";
            this.filter_03 = "Image in Gray Filter with Blue Chanel off - (Preview)";
            this.filter_04 = "Image in Gray Filter with Green Chanel off - (Preview)";
            this.filter_05 = "Image in Gray Filter with Green Chanel out - (Preview)";
            this.filter_06 = "Image in Gray Filter with Red Chanel out - (Preview)";
            this.filter_07 = "Image in Gray Filter with Blue Chanel out - (Preview)";
            this.filter_08 = "Image in Gray Filter with Red Chanel off, Gray level 255 - (Preview)";
            this.filter_09 = "Image in Gray Filter with Red Chanel off, Gray level 16 - (Preview)";
            this.filter_00 = "Reset layout!";
            this.imageFrame.setLanguage(viLang2);
        }

    }

    private void loadSetting() {
        try {
            RandomAccessFile rf = new RandomAccessFile("config.ini", "rw");
            String p = rf.readUTF();
            int stsize = rf.readInt();
            int gridsize = rf.readInt();
            float scale = rf.readFloat();
            int R = 0;
            int G = 0;
            int B = 0;
            int A = 255;
            R = rf.readInt();
            G = rf.readInt();
            B = rf.readInt();
            A = rf.readInt();
            String color = rf.readUTF();
            this.viLang = rf.readBoolean();
            this.cmUnit = rf.readBoolean();
            this.cmMeaUnit = rf.readBoolean();
            this.round = rf.readBoolean();
            this.autoLoad = rf.readBoolean();
            this.zW = rf.readInt();
            this.zH = rf.readInt();
            this.ab_ck = rf.readBoolean();
            this.cm_ck = rf.readBoolean();
            rf.close();
            this.imageFrame.stSize = stsize;
            this.imageFrame.s.setSize(stsize);
            this.imageFrame.gridAmount = gridsize;
            this.imageFrame.scale = scale;
            this.scale = scale;
            this.imageFrame.round = this.round;
            this.imageFrame.cmIndicate = this.cmMeaUnit;
            this.imageFrame.viLang = this.viLang;

            try {
                this.imageFrame.setBrushColor(new Color(R, G, B, A));
                this.currentColor.setColor(new Color(R, G, B, A));
            } catch (Exception e) {
                e.printStackTrace();
                this.state.setText("Color Load Error!");
            }

            this.stackAvailableSpaceNoitifier();
            if (this.viLang) {
                this.setLanguage(this.viLang);
            }

            if (this.autoLoad) {
                this.openFile(p);
            }

            Noitifier.printConsole("loadSetting - path:" + p);
            Noitifier.printConsole("loadSetting - Stack size:" + stsize);
            Noitifier.printConsole("loadSetting - Grid Size:" + gridsize);
            Noitifier.printConsole("loadSetting - Scale:" + scale);
            Noitifier.printConsole("loadSetting - Color:" + color);
            Noitifier.printConsole("loadSetting - RGB.A: " + R + ";" + G + ";" + B + ";" + A);
            Noitifier.printConsole("loadSetting - cmUnit:" + this.cmUnit);
            Noitifier.printConsole("loadSetting - cmMeaUnit:" + this.cmMeaUnit);
            Noitifier.printConsole("loadSetting - viLang:" + this.viLang);
            Noitifier.printConsole("loadSetting - round:" + this.round);
            Noitifier.printConsole("loadSetting - autoLoad:" + this.autoLoad);
            Noitifier.printConsole("loadSetting - zW:" + this.zW);
            Noitifier.printConsole("loadSetting - zH:" + this.zH);
            Noitifier.printConsole("loadSetting - ab_ck:" + this.ab_ck);
            Noitifier.printConsole("loadSetting - cm_ck:" + this.cm_ck);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }

    protected void saveSetting() {
        try {
            RandomAccessFile rf = new RandomAccessFile("config.ini", "rw");
            rf.writeUTF(this.filePath);
            rf.writeInt(this.imageFrame.stSize);
            rf.writeInt(this.imageFrame.gridAmount);
            rf.writeFloat(this.scale);
            rf.writeInt(this.imageFrame.getBrushColor().getRed());
            rf.writeInt(this.imageFrame.getBrushColor().getGreen());
            rf.writeInt(this.imageFrame.getBrushColor().getBlue());
            rf.writeInt(this.imageFrame.getBrushColor().getAlpha());
            rf.writeUTF(this.imageFrame.getBrushColor().toString());
            rf.writeBoolean(this.viLang);
            rf.writeBoolean(this.cmUnit);
            rf.writeBoolean(this.cmMeaUnit);
            rf.writeBoolean(this.round);
            rf.writeBoolean(this.autoLoad);
            rf.writeInt(this.zW);
            rf.writeInt(this.zH);
            rf.writeBoolean(this.ab_ck);
            rf.writeBoolean(this.cm_ck);
            rf.writeBytes("End");
            rf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void closeApp() {
        if (this.editstate != StickyPoint.EditState.MODIFIED && this.editstate != StickyPoint.EditState.NOT_SAVED) {
            this.shutdownAndDispose();
        } else {
            this.curF = new File(this.filePath);
            String fname = this.curF.getName();
            String mgs = this.confirm_save_prefix + fname + this.confirm_save_subfix;
            ConfirmBox con = new ConfirmBox(this, mgs, true, this.viLang);
            con.setVisible(true);
            boolean conFirmSave = true;

            while(conFirmSave) {
                if (con.getState() == 0) {
                    conFirmSave = false;
                    this.shutdownAndDispose();
                }

                if (con.getState() == 3) {
                    conFirmSave = false;
                }

                if (con.getState() == 1) {
                    this.savefd.setFile("Untitled.png");
                    this.savefd.setVisible(true);
                    if (this.savefd.getFile() != null) {
                        String dir = this.savefd.getDirectory();
                        String fn = this.savefd.getFile();
                        this.imageFrame.saveImg(dir, fn);
                        Noitifier.printConsole(this.imageFrame.noity);
                        conFirmSave = false;
                        if (this.imageFrame.noity != this.unknow_file_format_img_save) {
                            this.shutdownAndDispose();
                        } else if (!this.viLang) {
                            this.state.setBackground(LIGHT_RED);
                            this.state.setText("Unknow file format. File not been saved!");
                        } else {
                            this.state.setBackground(LIGHT_RED);
                            this.state.setText("Không thể lưu tệp! Định dạng không được hỗ trợ.");
                        }
                    } else {
                        conFirmSave = true;
                    }
                }
            }
        }

    }

    private void shutdownAndDispose() {
        if (layoutUpdater != null) {
            layoutUpdater.shutdown();
        }
        this.dispose();
    }

    public void update(Graphics g) {
        this.paint(g);
    }

    private void printScale(float scale) {
        Graphics g2 = this.imageFrame.getBufferedImage().getGraphics();
        Graphics g = this.imageFrame.getGraphics();
        g2.setColor(this.currentColor.c);
        g2.drawString(this.scale_label + Float.toString(scale), 50, 50);
        g.setColor(this.currentColor.c);
        g.drawString(this.scale_label + Float.toString(scale), 50, 50);
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == this.redPlate) {
            this.imageFrame.setBrushColor(Color.RED);
            this.currentColor.setColor(Color.RED);
        }

        if (e.getSource() == this.greenPlate) {
            this.imageFrame.setBrushColor(Color.GREEN);
            this.currentColor.setColor(Color.GREEN);
        }

        if (e.getSource() == this.bluePlate) {
            this.imageFrame.setBrushColor(Color.BLUE);
            this.currentColor.setColor(Color.BLUE);
        }

        if (e.getSource() == this.blackPlate) {
            this.imageFrame.setBrushColor(Color.BLACK);
            this.currentColor.setColor(Color.BLACK);
        }

        if (e.getSource() == this.whitePlate) {
            this.imageFrame.setBrushColor(Color.WHITE);
            this.currentColor.setColor(Color.WHITE);
        }

        if (e.getSource() == this.yellowPlate) {
            this.imageFrame.setBrushColor(Color.YELLOW);
            this.currentColor.setColor(Color.YELLOW);
        }

        if (e.getSource() == this.grayPlate) {
            this.imageFrame.setBrushColor(Color.GRAY);
            this.currentColor.setColor(Color.GRAY);
        }

        if (e.getSource() == this.dark_grayPlate) {
            this.imageFrame.setBrushColor(Color.DARK_GRAY);
            this.currentColor.setColor(Color.DARK_GRAY);
        }

        if (e.getSource() == this.cyanPlate) {
            this.imageFrame.setBrushColor(Color.CYAN);
            this.currentColor.setColor(Color.CYAN);
        }

        if (e.getSource() == this.currentColor) {
            this.openSelectColor();
        }

        this.buttonStateTracking();
        if (this.imageFrame.getBufImageState() == BufImageState.MODIFIED) {
            this.editstate = StickyPoint.EditState.MODIFIED;
        }

        this.stackAvailableSpaceNoitifier();
    }

    private void openSelectColor() {
        ColorSelectDialog cls = new ColorSelectDialog(this, this.select_color_title, true, this.imageFrame.getBrushColor(), this.viLang);
        cls.show();
        Color c = cls.getColor();
        this.imageFrame.setBrushColor(c);
        this.currentColor.setColor(c);
    }

    public void stackAvailableSpaceNoitifier() {
        int a = this.imageFrame.stSize - this.imageFrame.s.len;
        this.state.setBackground(Color.WHITE);
        if (a == 0) {
            this.state.setBackground(LIGHT_RED);
        }

        if (this.cmUnit) {
            float f = (float)this.imageFrame.gridAmount * this.imageFrame.scale;
            f = Math2.round(f);
            this.state.setText("   s: " + Integer.toString(a) + "\t   | \t  g: " + f + " cm");
        } else {
            this.state.setText("   s: " + Integer.toString(a) + "\t   | \t  g:" + this.imageFrame.gridAmount);
        }

    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent e) {
        this._start = e.getPoint();
        this.currentScrollPosition = this.imgScrollPane.getScrollPosition();
        if (this.imageFrame.mouseMode == MouseMode.ZOOM && e.getSource() != this.imageFrame.pop && e.getSource() == this.imageFrame && e.getButton() == 1) {
            this.zoomImg(e.getPoint());
        }

    }

    public void zoomImg(Point zClk) {
        int zoomAction = -1;
        if (this.imageFrame.zoom) {
            zoomAction = this.imageFrame.zoom(true);
            this.currentZoom = this.imageFrame.currentzoom;
        }

        if (!this.imageFrame.zoom) {
            zoomAction = this.imageFrame.zoom(false);
            this.currentZoom = this.imageFrame.currentzoom;
        }

        if (this.imageFrame.w > this.editPanel.getWidth() || this.imageFrame.h > this.editPanel.getHeight()) {
            this.fixed();
        }

        this.currentZoomState.setText(Float.toString(Math2.round(this.currentZoom * 100.0F)) + "%");
        Noitifier.printConsole("Zoom Click aft:" + zClk.x + ";" + zClk.y);
        Noitifier.printConsole("Zoom in:" + this.imageFrame.zoom);
        Noitifier.printConsole("current Zoom:" + this.currentZoom);
        this.imgScrollPane.doLayout();
        if (this.imageFrame.w > this.editPanel.getWidth() || this.imageFrame.h > this.editPanel.getHeight()) {
            this.setZoomPosition(zClk, zoomAction);
        }

        if (this.imageFrame.w < this.editPanel.getWidth() || this.imageFrame.h < this.editPanel.getHeight()) {
            this.floating = true;
        }

        if (this.floating) {
            this.floattingScrPanel();
        }

        Noitifier.printConsole("------------->floating:" + this.floating);
    }

    public void floattingScrPanel() {
        int W = this.editPanel.getWidth();
        int H = this.editPanel.getHeight();
        int frW12 = W / 2;
        int frH12 = H / 2;
        int imgW12 = this.imageFrame.w / 2;
        int imgH12 = this.imageFrame.h / 2;
        int tx = frW12 - imgW12;
        int ty = frH12 - imgH12;
        this.imgScrollPane.setBounds(tx, ty, this.imageFrame.w + 5, this.imageFrame.h + 5);
        Noitifier.printConsole("imgW:" + this.imageFrame.w + ";imgH:" + this.imageFrame.h + ";tx:" + tx + "; ty:" + ty + ";imgScrollPane:" + this.imgScrollPane.getWidth() + "x" + this.imgScrollPane.getHeight());
        Noitifier.printConsole("editPanel H:" + this.getHeight());
    }

    public void mouseReleased(MouseEvent e) {
        if (this.imageFrame.mouseMode == MouseMode.GRID && e.getSource() == this.imageFrame) {
            this.imageFrame.changeDragMode();
            this.mouseMode = MouseMode.DRAG;
            this.dragButton.setEnabled(false);
            this.gridButton.setEnabled(true);
        }

        if (this.mouseMode == MouseMode.ZOOM) {
            this.imgScrollPane.doLayout();
        }

        if (e.getSource() == this.imgScrollPane && (this.imgScrollPane.getWidth() < this.editPanel.getWidth() || this.imgScrollPane.getHeight() < this.editPanel.getHeight())) {
            this.imgScrollPane.doLayout();
            Noitifier.printConsole("rotate - dolayout done");
        }

        this.stackAvailableSpaceNoitifier();
    }

    public void setZoomPosition(Point zClk, int zoomAction) {
        Point pos = new Point();
        int X = 0;
        int Y = 0;
        int w12 = 0;
        int h12 = 0;
        if (zoomAction == 1) {
            int imgclkPosX = Math.round((float)zClk.x * 1.5F / this.currentZoom);
            int imgclkPosY = Math.round((float)zClk.y * 1.5F / this.currentZoom);
            X = Math.round((float)imgclkPosX * this.currentZoom);
            Y = Math.round((float)imgclkPosY * this.currentZoom);
            w12 = Math.round((float)this.imgScrollPane.getViewportSize().width / 2.0F);
            h12 = Math.round((float)this.imgScrollPane.getViewportSize().height / 2.0F);
            pos.x = X - w12;
            pos.y = Y - h12;
            this.imgScrollPane.setScrollPosition(pos);
        }

        if (zoomAction == 0) {
            int imgclkPosX = Math.round((float)zClk.x / (this.currentZoom * 1.5F));
            int imgclkPosY = Math.round((float)zClk.y / (this.currentZoom * 1.5F));
            X = Math.round((float)imgclkPosX * this.currentZoom);
            Y = Math.round((float)imgclkPosY * this.currentZoom);
            w12 = Math.round((float)this.imgScrollPane.getViewportSize().width / 2.0F);
            h12 = Math.round((float)this.imgScrollPane.getViewportSize().height / 2.0F);
            pos.x = X - w12;
            pos.y = Y - h12;
            this.imgScrollPane.setScrollPosition(pos);
        }

        Noitifier.printConsole("----------------->setZoomPositon: - zClk:" + zClk.x + "; " + zClk.y);
        Noitifier.printConsole("imgSize:" + this.imageFrame.w + "x" + this.imageFrame.h);
        Noitifier.printConsole("pos:" + pos.x + ";" + pos.y);
        Noitifier.printConsole("X2:" + X + "; Y2:" + Y);
        Noitifier.printConsole("w12:" + w12 + "; h12:" + h12);
        Noitifier.printConsole("floating:" + this.floating);
        Noitifier.printConsole("zoomAction:" + zoomAction + "<---------- (0)zoom Out, (1) zoom in");
        if (zoomAction == -1) {
            Noitifier.printConsole("Khong zoom dc");
        }

        Noitifier.printConsole("------------------->end setZoomPoisition.");
    }

    public void mouseDragged(MouseEvent e) {
        if (this.imageFrame.mouseMode == MouseMode.DRAG) {
            int amountY = this._start.y - e.getPoint().y;
            int amountX = this._start.x - e.getPoint().x;
            int aX = e.getPoint().x - this._start.x;
            int aY = e.getPoint().y - this._start.y;
            if (this.imageFrame.h < this.getHeight() || this.imageFrame.w < this.getWidth() && this.floating) {
                Point cur = this.imageFrame.getLocation();
                Point cur2 = this.imgScrollPane.getLocation();
                cur.translate(aX, aY);
                cur2.translate(aX, aY);
                this.imgScrollPane.setLocation(cur2);
                Point lo = this.imgScrollPane.getLocation();
                if (lo.x > 0 || lo.y > 0) {
                    this.imgScrollPane.setSize(this.imageFrame.w + 5, this.imageFrame.h + 5);
                }

                this.imgScrollPane.doLayout();
            } else {
                this.currentScrollPosition.translate(amountX, amountY);
                this.imgScrollPane.setScrollPosition(this.currentScrollPosition);
            }
        }

        if (this.imageFrame.mouseMode == MouseMode.P2P) {
            int Xrange = this.imgScrollPane.getViewportSize().width - 15 + this.imgScrollPane.getScrollPosition().x;
            if (e.getPoint().x > Xrange && e.getPoint().x < this.imageFrame.w - 15) {
                Noitifier.printConsole("Width Bound reached!!");
                this.currentScrollPosition = this.imgScrollPane.getScrollPosition();
                this.currentScrollPosition.translate(2, 0);
                this.imgScrollPane.setScrollPosition(this.currentScrollPosition);
            }

            int Yrange = this.imgScrollPane.getViewportSize().height - 15 + this.imgScrollPane.getScrollPosition().y;
            if (e.getPoint().y > Yrange && e.getPoint().y < this.imageFrame.h - 15) {
                Noitifier.printConsole("Height Bound reached!!");
                this.currentScrollPosition = this.imgScrollPane.getScrollPosition();
                this.currentScrollPosition.translate(0, 2);
                this.imgScrollPane.setScrollPosition(this.currentScrollPosition);
            }

            int Left = this.imgScrollPane.getScrollPosition().x + 15;
            if (e.getPoint().x < Left && e.getPoint().x > 15) {
                Noitifier.printConsole("Left Bound reached!!");
                this.currentScrollPosition = this.imgScrollPane.getScrollPosition();
                this.currentScrollPosition.translate(-2, 0);
                this.imgScrollPane.setScrollPosition(this.currentScrollPosition);
            }

            int Top = this.imgScrollPane.getScrollPosition().y + 15;
            if (e.getPoint().y < Top && e.getPoint().y > 15) {
                Noitifier.printConsole("Top Bound reached!!");
                this.currentScrollPosition = this.imgScrollPane.getScrollPosition();
                this.currentScrollPosition.translate(0, -2);
                this.imgScrollPane.setScrollPosition(this.currentScrollPosition);
            }
        }

    }

    public void mouseMoved(MouseEvent e) {
        if (this.filePath != "Untitled-00.jpg" && this.imageFrame.zoomdialog != null && this.imageFrame.zoomdialog.iz.move) {
            Point p = e.getPoint();
            p.x = Math.round((float)p.x / this.currentZoom);
            p.y = Math.round((float)p.y / this.currentZoom);
            this.imageFrame.zoomdialog.imgMove(p.x, p.y);
            this.imageFrame.zoomdialog.texf.setText("X:" + p.x + "; Y:" + p.y);
        }

        if (this.imageFrame.mouseMode == MouseMode.GRID) {
            Point p = e.getPoint();
            this.currentMousePosition = p;
            this.repaint(p.x - 1, 0, 2, 700);
            this.repaint(0, p.y - 1, 1200, 2);
        }

        if (this.imageFrame.mouseMode == MouseMode.P2P) {
            Point p = e.getPoint();
            this.currentMousePosition = p;
            this.repaint(p.x - 1, p.y - 25, 25, 25);
        }

    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 32 && !this.dragMode) {
            this.imageFrame.changeDragMode(true);
            this.stickButton.setEnabled(true);
            this.gridButton.setEnabled(true);
            this.dragButton.setEnabled(false);
            this.pointButton.setEnabled(true);
        }

        if (e.getKeyCode() == 17) {
            this.Ctrl = Key.PRESSED;
        }

        if (e.getKeyCode() == 18) {
            this.Alt = Key.PRESSED;
        }

        if (e.getKeyCode() == 16) {
            this.Shf = Key.PRESSED;
        }

        if (e.getKeyCode() == 16 && this.Shf == Key.PRESSED && this.mouseMode == MouseMode.ZOOM) {
            this.imageFrame.changeZoomMode(false);
        }

        if (e.getKeyCode() == 33 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.messureLineUp(5);
        }

        if (e.getKeyCode() == 34 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.messureLineDown(5);
        }

        if (e.getKeyCode() == 35 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.messureLineRight(5);
        }

        if (e.getKeyCode() == 36 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.messureLineLeft(5);
        }

        if (e.getKeyCode() == 37 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.messureLineLeft(1);
        }

        if (e.getKeyCode() == 38 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.messureLineUp(1);
        }

        if (e.getKeyCode() == 39 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.messureLineRight(1);
        }

        if (e.getKeyCode() == 40 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.messureLineDown(1);
        }

        if (e.getKeyCode() == 39 && this.imageFrame.zoomdialog == null && this.imageFrame.mouseMode == MouseMode.GRID) {
            ImgFrame var10000 = this.imageFrame;
            var10000.gridAmount += 2;
            if (!this.cmUnit) {
                this.state.setText(this.grid_size + this.imageFrame.gridAmount);
            } else {
                float f = (float)this.imageFrame.gridAmount * this.imageFrame.scale;
                f = Math2.round(f);
                this.state.setText(this.grid_size + f + " cm");
            }

            this.stackAvailableSpaceNoitifier();
            this.repaint(0, this.currentMousePosition.y - 4, 1200, 8);
        }

        if (e.getKeyCode() == 37 && this.imageFrame.zoomdialog == null && this.imageFrame.mouseMode == MouseMode.GRID) {
            ImgFrame var8 = this.imageFrame;
            var8.gridAmount -= 2;
            if (!this.cmUnit) {
                this.state.setText(this.grid_size + this.imageFrame.gridAmount);
            } else {
                float f = (float)this.imageFrame.gridAmount * this.imageFrame.scale;
                f = Math2.round(f);
                this.state.setText(this.grid_size + f + " cm");
            }

            this.stackAvailableSpaceNoitifier();
            this.repaint(0, this.currentMousePosition.y - 4, 1200, 8);
        }

        if (e.getKeyCode() == 37 && this.imageFrame.mouseMode == MouseMode.ZOOM) {
            Point p = this.getMousePosition();
            p.translate(-5, 120);
            this.imgScrollPane.layoutContent(p);
        }

        if (e.getKeyCode() == 39 && this.imageFrame.mouseMode == MouseMode.ZOOM) {
            Point p = this.getMousePosition();
            p.translate(5, 5);
            this.imgScrollPane.layoutContent(p);
        }

        if (e.getModifiers() == 1 && e.getKeyChar() == 'z') {
            this.openZoomBox();
            Noitifier.printConsole("z psssssssss");
        }

    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == 32 && !this.dragMode) {
            if (this.mouseMode == MouseMode.STICK) {
                this.imageFrame.changeStickMode();
                this.stickButton.setEnabled(false);
                this.dragButton.setEnabled(true);
                this.gridButton.setEnabled(true);
                this.pointButton.setEnabled(true);
            }

            if (this.mouseMode == MouseMode.GRID) {
                this.imageFrame.changeGridMode();
                this.gridButton.setEnabled(false);
                this.dragButton.setEnabled(true);
                this.stickButton.setEnabled(true);
                this.pointButton.setEnabled(true);
            }

            if (this.mouseMode == MouseMode.P2P) {
                this.imageFrame.changeP2PMode(true);
                this.gridButton.setEnabled(true);
                this.stickButton.setEnabled(true);
                this.dragButton.setEnabled(true);
                this.pointButton.setEnabled(false);
            }

            if (this.mouseMode == MouseMode.ZOOM) {
                this.imageFrame.changeZoomMode(this.imageFrame.zoom);
                this.dragButton.setEnabled(true);
            }
        }

        if (e.getKeyChar() == 'f') {
            this.floating = true;
        }

        if (e.getKeyCode() == 48) {
            this.fixed();
            this.state.setBackground(WHITE);
            this.state.setText(this.filter_00);
        }

        if (e.getKeyCode() == 67 && this.imageFrame.zoomdialog != null && this.Alt == Key.PRESSED) {
            this.imageFrame.zoomdialog.iz.showCross();
        }

        if ((e.getKeyCode() == 67 || e.getKeyChar() == 't') && this.Alt == Key.NONE_PRESSED) {
            this.imageFrame.changeStickMode();
            this.buttonStateTracking();
            this.dragMode = false;
            this.mouseMode = MouseMode.STICK;
        }

        if (e.getKeyChar() == 'v' || e.getKeyChar() == 'g') {
            this.imageFrame.changeGridMode();
            this.buttonStateTracking();
            this.dragMode = false;
            this.mouseMode = MouseMode.GRID;
        }

        if (e.getKeyChar() == 'b' || e.getKeyChar() == 'p') {
            this.imageFrame.changeP2PMode();
            this.buttonStateTracking();
            this.dragMode = false;
            this.mouseMode = MouseMode.P2P;
            this.printScale(this.imageFrame.scale);
        }

        if (e.getKeyCode() == 90 && this.Ctrl == Key.NONE_PRESSED && this.Alt == Key.NONE_PRESSED && this.Shf == Key.PRESSED) {
            this.openZoomBox();
            Noitifier.printConsole("Z typed");
        }

        if (e.getKeyCode() == 90 && this.Ctrl == Key.NONE_PRESSED && this.Alt == Key.NONE_PRESSED && this.Shf == Key.NONE_PRESSED) {
            this.imageFrame.changeZoomMode(true);
            this.dragMode = false;
            this.mouseMode = MouseMode.ZOOM;
            this.buttonStateTracking();
        }

        if (e.getKeyCode() == 90 && this.Ctrl == Key.PRESSED && !this.imageFrame.action.isEmpty()) {
            this.undo();
            this.stackAvailableSpaceNoitifier();
            this.buttonStateTracking();
        }

        if (e.getKeyCode() == 17) {
            this.Ctrl = Key.NONE_PRESSED;
        }

        if (e.getKeyCode() == 18) {
            this.Alt = Key.NONE_PRESSED;
        }

        if (e.getKeyCode() == 16) {
            this.Shf = Key.NONE_PRESSED;
        }

        if (e.getKeyCode() == 127) {
            this.imgScrollPane.doLayout();
        }

        if (e.getKeyCode() == 16 && this.Shf == Key.NONE_PRESSED && this.mouseMode == MouseMode.ZOOM) {
            this.imageFrame.changeZoomMode(true);
            this.imageFrame.requestFocus();
        }

        if (e.getKeyCode() == 37 && this.imageFrame.mouseMode == MouseMode.STICK) {
            this.imageFrame.changeDirection(37);
        }

        if (e.getKeyCode() == 38 && this.imageFrame.mouseMode == MouseMode.STICK) {
            this.imageFrame.changeDirection(38);
        }

        if (e.getKeyCode() == 39 && this.imageFrame.mouseMode == MouseMode.STICK) {
            this.imageFrame.changeDirection(39);
        }

        if (e.getKeyCode() == 40 && this.imageFrame.mouseMode == MouseMode.STICK) {
            this.imageFrame.changeDirection(40);
        }

        if (e.getKeyCode() == 51) {
            this.imageFrame.Test(3);
            this.state.setBackground(WHITE);
            this.state.setText(this.filter_03);
        }

        if (e.getKeyCode() == 50) {
            this.imageFrame.Test(2);
            this.state.setBackground(WHITE);
            this.state.setText(this.filter_02);
        }

        if (e.getKeyCode() == 49) {
            this.imageFrame.Test(1);
            this.state.setBackground(WHITE);
            this.state.setText(this.filter_01);
        }

        if (e.getKeyCode() == 52) {
            this.imageFrame.Test(4);
            this.state.setBackground(WHITE);
            this.state.setText(this.filter_04);
        }

        if (e.getKeyCode() == 53) {
            this.imageFrame.Test(5);
            this.state.setBackground(WHITE);
            this.state.setText(this.filter_05);
        }

        if (e.getKeyCode() == 54) {
            this.imageFrame.Test(6);
            this.state.setBackground(WHITE);
            this.state.setText(this.filter_06);
        }

        if (e.getKeyCode() == 55) {
            this.imageFrame.Test(7);
            this.state.setBackground(WHITE);
            this.state.setText(this.filter_07);
        }

        if (e.getKeyCode() == 56) {
            this.imageFrame.Test(8);
            this.state.setBackground(WHITE);
            this.state.setText(this.filter_08);
        }

        if (e.getKeyCode() == 57) {
            this.imageFrame.Test(9);
            this.state.setBackground(WHITE);
            this.state.setText(this.filter_09);
        }

        if (e.getKeyChar() == 'q') {
            FilterBox fb = new FilterBox(this, "Image Filter", true, this.imageFrame.getBufferedImage());
            fb.box.setVisible(true);
            Image i = fb.getImage();
            Graphics g = this.imageFrame.getBufferedImage().getGraphics();
            g.drawImage(i, 0, 0, this.imageFrame.imgObserver);
            this.imageFrame.repaint();
        }

        this.zoomDialogActionKey(e);
        if (e.getKeyCode() == 73) {
            this.imageFrame.writePixel();
        }

        if (e.getKeyChar() == 'w') {
            this.imageFrame.showImg();
        }

        if (e.getKeyChar() == 'm') {
            this.imageFrame.Test(12);
        }

        Noitifier.printConsole("e.getKeyCode:" + e.getKeyCode());
        Noitifier.printConsole("Current Scrll Pos:" + this.imgScrollPane.getScrollPosition().x + ";" + this.imgScrollPane.getScrollPosition().y);
    }

    public void zoomDialogActionKey(KeyEvent e) {
        if (e.getKeyCode() == 76 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.lock();
        }

        if (e.getKeyChar() == 'r' && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.changeRulerMode();
        }

        if (e.getKeyCode() == 46 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.increRulerUnit(2);
        }

        if (e.getKeyCode() == 44 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.decreRulerUnit(2);
        }

        if (e.getKeyCode() == 93 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.increRulerUnit(8);
        }

        if (e.getKeyCode() == 91 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.decreRulerUnit(8);
        }

        if (e.getKeyCode() == 47 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.resetRulerUnit();
        }

        if (e.getKeyCode() == 97 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.setCmScale(1);
        }

        if (e.getKeyCode() == 98 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.setCmScale(2);
        }

        if (e.getKeyCode() == 99 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.setCmScale(3);
        }

        if (e.getKeyCode() == 100 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.setCmScale(4);
        }

        if (e.getKeyCode() == 101 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.setCmScale(5);
        }

        if (e.getKeyCode() == 102 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.setCmScale(6);
        }

        if (e.getKeyCode() == 103 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.setCmScale(7);
        }

        if (e.getKeyCode() == 104 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.setCmScale(8);
        }

        if (e.getKeyCode() == 105 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.setCmScale(9);
        }

        if (e.getKeyCode() == 78 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.showUnit();
        }

        if (e.getKeyCode() == 65 && this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.messureMode();
        }

    }

    public void fixed() {
        this.floating = false;
        this.imgScrollPane.setLocation(0, 0);
        if (this.editPanel.getWidth() == 0) {
            this.imgScrollPane.setSize(784, 469);
        } else {
            this.imgScrollPane.setSize(this.editPanel.getSize());
        }

        this.imgScrollPane.doLayout();
        Noitifier.printConsole("fixed called - editPanelSize:" + this.editPanel.getSize());
        Noitifier.printConsole("fixed called - editPanelLocation:" + this.editPanel.getLocation());
        Noitifier.printConsole("fixed called - imgScrollPaneSize:" + this.imgScrollPane.getSize());
        Noitifier.printConsole("fixed called - imgScrollPane Location:" + this.imgScrollPane.getLocation());
        Noitifier.printConsole("fixed called - floating:" + this.floating);
    }

    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == 90) {
            this.openZoomBox();
            Noitifier.printConsole("Z typed");
        }

        if (e.getKeyChar() == 'h') {
            this.imageFrame.changeDragMode();
            this.buttonStateTracking();
            this.dragMode = true;
            this.mouseMode = MouseMode.DRAG;
        }

    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        e.getWheelRotation();
        if (this.imageFrame.zoomdialog != null) {
            this.imageFrame.zoomdialog.iz.zoom(e.getWheelRotation());
            this.imageFrame.zoomdialog.zoomTexf.setText("Zoom:" + (int)(this.imageFrame.zoomdialog.iz.getZoomrate() * 100.0F) + "%");
        }

        Noitifier.printConsole("e.getWheelRotation():" + e.getWheelRotation());
    }

    public static enum EditState {
        MODIFIED,
        SAVED,
        NOT_SAVED;
    }
}