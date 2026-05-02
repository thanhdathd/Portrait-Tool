package ui;

import config.ConfigManager;
import core.actions.KeyAction;
import core.fileio.GridOptionInjector;
import core.fileio.ThumbnailFileView;
import core.state.AppState;
import tools.ToolManager;
import ui.canvas.ImageCanvas;
import ui.dialogs.ImagePreviewPanel;
import ui.dialogs.ZoomWindow;
import utils.ExcelExportUtils;
import workers.ImageLoadWorker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

public class MainFrame extends JFrame {

    private final AppState appState;
    private final ImageCanvas canvas;
    private final ConfigManager configManager;
    private final ToolManager tool;
    private ZoomWindow zoom = null;

    public MainFrame() {
        this.appState = new AppState();
        this.canvas = new ImageCanvas(appState);
        configManager = new ConfigManager();
        configManager.load(appState);
        tool = ToolManager.initializeTools();
        
        setTitle("Portrait Tool Modernized");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//        setSize(1000, 750);
//        setLocationRelativeTo(null); // Center on screen
        applyWindowSettings();

        // Handle window closing for unsaved changes
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                attemptClose();
            }
        });
        
        // Listen to history to mark as dirty
        appState.getHistoryManager().addListener((canUndo, canRedo, isModified) -> {
            if(isModified)appState.setEditState(AppState.EditState.MODIFIED);
        });
        
        setLayout(new BorderLayout());
        
        // Wrap canvas in JScrollPane
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(null); // Clean look
        // Improve panning speed
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        disableScrollByArrowKey(scrollPane);
        
        add(scrollPane, BorderLayout.CENTER);
        
        initMenuBar();
        initToolBar();
        setupGlobalShortcuts();
        
        // Default tool
        canvas.setActiveTool(new tools.HandTool());
        autoOpenFile();
    }

    private void setupGlobalShortcuts() {
        JRootPane rootPane = this.getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rootPane.getActionMap();

        // Lấy phím Modifier hệ thống (Ctrl trên Win/Linux, Cmd trên macOS)
        int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        // 1. Phím tắt UNDO (Ctrl + Z)
        keyBindingHelper(im,am,KeyEvent.VK_Z, shortcutMask, "UndoAction", e -> {
            System.out.println("Thực hiện Undo!");
            if (appState.getHistoryManager().canUndo()) {
                appState.getHistoryManager().undo();
                canvas.repaint();
            }
        });

        // 2. Phím tắt REDO (Ctrl + Y)
        keyBindingHelper(im,am,KeyEvent.VK_Y, shortcutMask, "RedoAction", e -> {
            System.out.println("Thực hiện Redo!");
            if(appState.getHistoryManager().canRedo()) {
                appState.getHistoryManager().redo();
                canvas.repaint();
            }
        });

        // 3. Phím tắt chuyển Tool (Ví dụ: phím 'H' cho Hand Tool)
        keyBindingHelper(im,am,KeyEvent.VK_H, 0, "HandToolAction", e -> {
            canvas.setActiveTool(tool.handTool);
        });

        keyBindingHelper(im,am, new int[]{KeyEvent.VK_C, KeyEvent.VK_S}, 0, "StickToolAction", e -> {
            canvas.setActiveTool(tool.stickTool);
        });

        keyBindingHelper(im,am,new int[]{KeyEvent.VK_V,KeyEvent.VK_G}, 0, "GridToolAction", e -> {
            canvas.setActiveTool(tool.gridTool);
        });

        // Phím 'P' cho Point/Pen Tool
        keyBindingHelper(im,am, new int[]{KeyEvent.VK_P, KeyEvent.VK_B}, 0, "PointToolActionP", e -> {
            tool.p2pTool.clearCompletedLines();
            canvas.setActiveTool(tool.p2pTool);
        });
        keyBindingHelper(im,am,KeyEvent.VK_Z, 0, "ZoomToolAction", e -> {
            if (canvas.getActiveTool() instanceof tools.ZoomCanvasTool) {
                ((tools.ZoomCanvasTool) canvas.getActiveTool()).toggleMode();
                canvas.updateCursor();
            } else {
                canvas.setActiveTool(new tools.ZoomCanvasTool());
            }
        });
        keyBindingHelper(im,am,KeyEvent.VK_M, 0, "OpenZoomWindow", e -> {
            openZoomWindow();
        });
    }

    private void keyBindingHelper(InputMap im, ActionMap am, int keyEvent,
                                  int modifiers, String mapkey, KeyAction keyAction) {
        im.put(KeyStroke.getKeyStroke(keyEvent, modifiers), mapkey);
        am.put(mapkey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keyAction.perform(e);
            }
        });
    }

    private void keyBindingHelper(InputMap im, ActionMap am, int[] keyEvents,
                                  int modifiers, String mapkey, KeyAction keyAction) {
        for (int key : keyEvents) {
            im.put(KeyStroke.getKeyStroke(key, modifiers), mapkey);
        }
        am.put(mapkey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keyAction.perform(e);
            }
        });
    }

    /**
     * Disable action scroll content inside scroll pane with arrow key
     * We use arrow key for other action
     * @param scrollPane
     */
    private void disableScrollByArrowKey(JScrollPane scrollPane) {
        // Giả sử scrollPane là đối tượng JScrollPane chứa ImageCanvas của bạn
        InputMap scrollIm = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Ghi đè hành động mặc định bằng "none" để vô hiệu hóa
        scrollIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        scrollIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        scrollIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
        scrollIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
    }

    private void applyWindowSettings() {
        setLocation(appState.getWindowX(), appState.getWindowY());
        setSize(appState.getWindowWidth(), appState.getWindowHeight());
    }

    private void autoOpenFile() {
        File file = new File(appState.getFilePath());
        if(!file.exists()) {
            System.out.println("Can not open file");
            return;
        }
        setTitle("Loading...");
        System.out.println("Loading... "+file.getAbsolutePath());
        new ImageLoadWorker(file, image -> {
            canvas.setBackgroundImage(image);
            setTitle(file.getAbsolutePath()+" - "+image.getWidth()+"x"+image.getHeight());
            appState.getHistoryManager().markAsSaved();
        }, ex -> {
            JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            setTitle("Portrait Tool Modernized");
        }).execute();
    }

    private void attemptClose() {
        updateUIState(appState);
        configManager.save(appState);
        if (appState.getEditState() == AppState.EditState.MODIFIED) {
            int result = JOptionPane.showOptionDialog(this,
                    "You have unsaved changes. Do you want to save before exiting?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new String[]{"Save", "Don't Save", "Cancel"},
                    "Save");
            
            if (result == JOptionPane.YES_OPTION) {
                performSaveFile();
                if (appState.getEditState() != AppState.EditState.MODIFIED) {
                    System.exit(0);
                }
            } else if (result == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
            // Cancel does nothing
        } else {
            System.exit(0);
        }
    }

    private void updateUIState(AppState appState) {
        appState.setWindowX(this.getX());
        appState.setWindowY(this.getY());
        appState.setWindowWidth(this.getWidth());
        appState.setWindowHeight(this.getHeight());
    }

    // --- Action Methods to share between Menu and Toolbar ---
    private void performOpenFile() {
        JFileChooser chooser = prepareChooserDialog();

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            setTitle("Portrait Tool Modernized - Loading...");
            new workers.ImageLoadWorker(file, image -> {
                canvas.setBackgroundImage(image);
                appState.setFilePath(file.getAbsolutePath());
                appState.setLastOpenedDir(file.getParent());
                setTitle(file.getAbsolutePath()+" - "+image.getWidth()+"x"+image.getHeight());
                appState.getHistoryManager().markAsSaved();
            }, ex -> {
                JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                setTitle("Portrait Tool Modernized");
            }).execute();
        }
    }

    /**
     * Xác định thư mục khởi tạo cho FileChooser
     * @return Thư mục chứa file ảnh hiện tại (nếu có), hoặc thư mục Documents
     */
    private File getInitialDirectory() {
        String lastOpenedDir = appState.getLastOpenedDir();
        if (lastOpenedDir != null && !lastOpenedDir.isEmpty()) {
            File dir = new File(lastOpenedDir);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }

        String currentFilePath = appState.getFilePath();
        // Kiểm tra xem đã có file ảnh hợp lệ chưa
        if (currentFilePath != null && !currentFilePath.isEmpty()) {
            File currentFile = new File(currentFilePath);
            if (currentFile.exists() && currentFile.isFile()) {
                // Trả về thư mục cha của file hiện tại
                return currentFile.getParentFile();
            }
        }

        // Nếu chưa có file nào, mở ra thư mục Documents mặc định
        return getDefaultDocumentsDirectory();
    }

    /**
     * Lấy thư mục Documents mặc định của hệ thống (hoạt động trên cả Windows, macOS, Linux)
     */
    private File getDefaultDocumentsDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            // Windows: C:\Users\<username>\Documents
            return new File(userHome, "Documents");
        } else if (os.contains("mac")) {
            // macOS: /Users/<username>/Documents
            return new File(userHome, "Documents");
        } else {
            // Linux: /home/<username>/Documents hoặc ~/Documents
            File documents = new File(userHome, "Documents");
            if (documents.exists()) {
                return documents;
            }
            // Nếu không có thư mục Documents, trả về thư mục home
            return new File(userHome);
        }
    }

    private void performSaveFile() {
        JFileChooser chooser = prepareChooserDialog();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".png")) {
                file = new java.io.File(file.getAbsolutePath() + ".png");
            }
            new workers.SaveWorker(canvas, file, true, true).execute();
            appState.setEditState(AppState.EditState.SAVED);
            appState.getHistoryManager().markAsSaved();
        }
    }

    private JFileChooser prepareChooserDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setPreferredSize(new Dimension(900, 600));
        FileNameExtensionFilter imageFilter =
                new FileNameExtensionFilter(
                        "Image Files (JPG, JPEG, PNG, GIF, BMP, WEBP)",
                        "jpg", "jpeg", "png", "gif", "bmp", "webp"
                );
        chooser.setFileFilter(imageFilter);


        // Thumbnail view for file list
        chooser.setFileView(new ThumbnailFileView(chooser, 32));
        GridOptionInjector.inject(chooser, 32);

        File initialDirectory = getInitialDirectory();
        if (initialDirectory != null && initialDirectory.exists()) {
            chooser.setCurrentDirectory(initialDirectory);
        }

        // ========== Preview Panel cải tiến ==========
        ImagePreviewPanel previewPanel = new ImagePreviewPanel(chooser);
        chooser.setAccessory(previewPanel);

        return chooser;
    }

    private void performSaveTicksOnly() {
        JFileChooser chooser = prepareChooserDialog();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".png")) {
                file = new java.io.File(file.getAbsolutePath() + ".png");
            }
            
            workers.SaveWorker worker = new workers.SaveWorker(canvas, file, false, false);
            worker.execute();
        }
    }


    private void performOpenFilter() {
        BufferedImage currentImage = canvas.getBackgroundImage();
        if (currentImage != null) {
            new ui.dialogs.FilterDialog(this, currentImage, (newImage) -> {
                core.history.Command filterCmd = new core.history.FilterCommand(canvas, currentImage, newImage);
                appState.getHistoryManager().push(filterCmd);
                canvas.repaint();
            }).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please open an image first.", "No Image", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void performExportToExcel() {
        if (canvas.getBackgroundImage() == null) {
            JOptionPane.showMessageDialog(this, "Please open an image first.", "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setPreferredSize(new Dimension(900, 600));
        FileNameExtensionFilter imageFilter =
                new FileNameExtensionFilter(
                        "Excel File (xls, xlsx)",
                        "xls", "xlsx", "csv", "txt"
                );
        chooser.setFileFilter(imageFilter);
        File initialDirectory = getInitialDirectory();
        chooser.setCurrentDirectory(initialDirectory);
        chooser.setDialogTitle("Export to Excel (.xls)");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".xlsx")) {
                file = new java.io.File(file.getAbsolutePath() + ".xlsx");
            }
            
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                ExcelExportUtils.exportToXlsx(
                        appState.getCanvasState().getStickyPoints(),
                        appState.getScale(),
                        outputStream
                );
                JOptionPane.showMessageDialog(this, "Successfully exported to " + file.getName(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> performOpenFile());
        
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> performSaveFile());
        
        JMenuItem exportPointData = new JMenuItem("Export Data");
        exportPointData.addActionListener(e -> performExportToExcel());

        JMenuItem savePointOnly = new JMenuItem("Save Point Only");
        savePointOnly.addActionListener(e -> performSaveTicksOnly());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> attemptClose());

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(exportPointData);
        fileMenu.add(savePointOnly);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        undoItem.addActionListener(e -> {
            if (appState.getHistoryManager().canUndo()) {
                appState.getHistoryManager().undo();
                canvas.repaint();
            }
        });

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        redoItem.addActionListener(e -> {
            if (appState.getHistoryManager().canRedo()) {
                appState.getHistoryManager().redo();
                canvas.repaint();
            }
        });
        
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        
        JMenuItem settingsItem = new JMenuItem("Settings...");
        settingsItem.addActionListener(e -> {
            new ui.dialogs.SettingsDialog(this, appState).setVisible(true);
        });
        editMenu.add(settingsItem);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        
        JMenuItem zoomItem = new JMenuItem("Toggle Zoom/Measure Calipers");
        zoomItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        zoomItem.addActionListener(e -> {
            openZoomWindow();
        });
        viewMenu.add(zoomItem);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this, 
            "Portrai-Tool Modernized\nA Swing-based Image Processing Tool", "About", JOptionPane.INFORMATION_MESSAGE));
            
        JMenuItem keyAssistItem = new JMenuItem("Key Assist");
        keyAssistItem.addActionListener(e -> JOptionPane.showMessageDialog(this, 
            "Key Assist:\n" +
            "Ctrl+Z: Undo\n" +
            "Ctrl+Y: Redo\n" +
            "Up/Down/Left/Right: Move measurement calipers in Zoom Mode\n", 
            "Keyboard Shortcuts", JOptionPane.INFORMATION_MESSAGE));
            
        helpMenu.add(keyAssistItem);
        helpMenu.add(aboutItem);

        // Image Menu
        JMenu imageMenu = new JMenu("Image");
        imageMenu.setMnemonic(KeyEvent.VK_I);
        
        JMenuItem filterItem = new JMenuItem("Filters...");
        filterItem.addActionListener(e -> performOpenFilter());
        imageMenu.add(filterItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(imageMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void openZoomWindow() {
        if (canvas.getBackgroundImage() == null) {
            JOptionPane.showMessageDialog(this, "Please open an image first.", "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(zoom != null && zoom.isShowing()) {
            zoom.setVisible(false);
            return;
        }

        if(zoom == null) {
            zoom = new ZoomWindow(this, appState);
        }
        canvas.setZoomWindow(zoom);
        zoom.updateImage(canvas.getBackgroundImage());
        zoom.setVisible(true);
    }

    private JButton createIconButton(String iconName, String tooltip) {
        JButton btn = new JButton();
        try {
            java.net.URL url = getClass().getClassLoader().getResource("icons/" + iconName);
            if (url != null) {
                btn.setIcon(new ImageIcon(url));
            } else {
                btn.setText(tooltip); // fallback
            }
        } catch (Exception e) {
            btn.setText(tooltip);
        }
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        return btn;
    }

    private void initToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setOrientation(JToolBar.HORIZONTAL);
        
        // File Ops
        JButton openBtn = createIconButton("icon3.png", "Open");
        openBtn.addActionListener(e -> performOpenFile());
        
        JButton saveBtn = createIconButton("icon2.png", "Save");
        saveBtn.addActionListener(e -> performSaveFile());
        
        JButton saveTicksBtn = createIconButton("icon9.png", "Save Image with Ticks Only");
        saveTicksBtn.addActionListener(e -> performSaveTicksOnly());
        
        toolBar.add(openBtn);
        toolBar.add(saveBtn);
        toolBar.add(saveTicksBtn);
        toolBar.addSeparator();
        
        // History Ops
        JButton undoBtn = createIconButton("icon12.png", "Undo");
        undoBtn.addActionListener(e -> {
            if (appState.getHistoryManager().canUndo()) {
                appState.getHistoryManager().undo();
                canvas.repaint();
            }
        });
        
        JButton redoBtn = createIconButton("icon5.png", "Redo");
        redoBtn.addActionListener(e -> {
            if (appState.getHistoryManager().canRedo()) {
                appState.getHistoryManager().redo();
                canvas.repaint();
            }
        });
        
        // Initial state
        undoBtn.setEnabled(appState.getHistoryManager().canUndo());
        redoBtn.setEnabled(appState.getHistoryManager().canRedo());
        
        // Listen to history changes
        appState.getHistoryManager().addListener((canUndo, canRedo, isModified) -> {
            undoBtn.setEnabled(canUndo);
            redoBtn.setEnabled(canRedo);
        });
        
        toolBar.add(undoBtn);
        toolBar.add(redoBtn);
        toolBar.addSeparator();
        
        // Mouse Modes
        JButton handBtn = createIconButton("icon6.png", "Hand");
        handBtn.addActionListener(e -> canvas.setActiveTool(tool.handTool));
        
        JButton stickBtn = createIconButton("icon11.png", "Stick");
        stickBtn.addActionListener(e -> canvas.setActiveTool(tool.stickTool));
        
        JButton p2pBtn = createIconButton("icon8.png", "P2P");
        p2pBtn.addActionListener(e -> canvas.setActiveTool(tool.p2pTool));
        
        JButton gridBtn = createIconButton("icon4.png", "Grid");
        gridBtn.addActionListener(e -> canvas.setActiveTool(tool.gridTool));
        
        JButton zoomBtn = createIconButton("icon1.png", "Zoom");
        zoomBtn.addActionListener(e -> {
            if (canvas.getActiveTool() instanceof tools.ZoomCanvasTool) {
                ((tools.ZoomCanvasTool) canvas.getActiveTool()).toggleMode();
                canvas.updateCursor();
            } else {
                canvas.setActiveTool(new tools.ZoomCanvasTool());
            }
        });
        
        // Listen to active tool changes
        canvas.addPropertyChangeListener(evt -> {
            if ("activeTool".equals(evt.getPropertyName())) {
                tools.Tool activeTool = canvas.getActiveTool();
                handBtn.setEnabled(!(activeTool instanceof tools.HandTool));
                stickBtn.setEnabled(!(activeTool instanceof tools.StickTool));
                p2pBtn.setEnabled(!(activeTool instanceof tools.P2PTool));
                gridBtn.setEnabled(!(activeTool instanceof tools.GridTool));
                // Zoom is toggleable, so always enabled
            }
        });

        toolBar.add(handBtn);
        toolBar.add(stickBtn);
        toolBar.add(p2pBtn);
        toolBar.add(gridBtn);
        toolBar.add(zoomBtn);
        
        toolBar.addSeparator();
        
        // Data & Settings
        JButton exportBtn = createIconButton("icon7.png", "Export to Excel");
        exportBtn.addActionListener(e -> performExportToExcel());
        
        JButton settingsBtn = createIconButton("icon10.png", "Settings");
        settingsBtn.addActionListener(e -> new ui.dialogs.SettingsDialog(this, appState).setVisible(true));
        
        toolBar.add(exportBtn);
        toolBar.add(settingsBtn);
        toolBar.addSeparator();
        
        // Color
        JButton colorBtn = new JButton("   ");
        colorBtn.setBackground(appState.getBrushColor());
        colorBtn.setOpaque(true);
        colorBtn.setBorderPainted(false);
        colorBtn.setToolTipText("Select Brush Color");
        colorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Select Brush Color", appState.getBrushColor());
            if (newColor != null) {
                appState.setBrushColor(newColor);
                colorBtn.setBackground(newColor);
            }
        });
        toolBar.add(colorBtn);
        
        add(toolBar, BorderLayout.NORTH);
    }
}
