package ui;

import core.state.AppState;
import ui.canvas.ImageCanvas;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class MainFrame extends JFrame {

    private final AppState appState;
    private final ImageCanvas canvas;

    public MainFrame() {
        this.appState = new AppState();
        this.canvas = new ImageCanvas(appState);
        
        setTitle("Portrait Tool Modernized");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center on screen
        
        setLayout(new BorderLayout());
        
        // Wrap canvas in JScrollPane
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(null); // Clean look
        // Improve panning speed
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
        
        initMenuBar();
        initToolBar();
        
        // Default tool
        canvas.setActiveTool(new tools.StickTool());
    }

    // --- Action Methods to share between Menu and Toolbar ---

    private void performOpenFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            setTitle("Portrait Tool Modernized - Loading...");
            new workers.ImageLoadWorker(file, image -> {
                canvas.setBackgroundImage(image);
                appState.setFilePath(file.getAbsolutePath());
                setTitle("Portrait Tool Modernized - " + file.getName());
            }, ex -> {
                JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                setTitle("Portrait Tool Modernized");
            }).execute();
        }
    }

    private void performSaveFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".png")) {
                file = new java.io.File(file.getAbsolutePath() + ".png");
            }
            new workers.SaveWorker(canvas, file).execute();
        }
    }

    private void performSaveTicksOnly() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".png")) {
                file = new java.io.File(file.getAbsolutePath() + ".png");
            }
            
            // Temporarily disable labels
            canvas.setDrawLabels(false);
            
            workers.SaveWorker worker = new workers.SaveWorker(canvas, file) {
                @Override
                protected void done() {
                    super.done();
                    canvas.setDrawLabels(true); // Restore labels
                    canvas.repaint();
                }
            };
            worker.execute();
        }
    }

    private void performExportMatrix() {
        if (canvas.getBackgroundImage() == null) {
            JOptionPane.showMessageDialog(this, "Please open an image first.", "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".png")) {
                file = new java.io.File(file.getAbsolutePath() + ".png");
            }
            
            JProgressBar progress = new JProgressBar(0, 100);
            progress.setStringPainted(true);
            
            JDialog progressDialog = new JDialog(this, "Exporting Matrix...", true);
            progressDialog.setLayout(new BorderLayout(10, 10));
            progressDialog.add(new JLabel("Processing Point Matrix..."), BorderLayout.NORTH);
            progressDialog.add(progress, BorderLayout.CENTER);
            progressDialog.pack();
            progressDialog.setLocationRelativeTo(this);
            
            workers.ExportMatrixWorker worker = new workers.ExportMatrixWorker(
                    canvas.getBackgroundImage(), appState.getCanvasState(), file, progress) {
                @Override
                protected void done() {
                    super.done();
                    progressDialog.dispose();
                }
            };
            
            worker.execute();
            progressDialog.setVisible(true);
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
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".csv")) {
                file = new java.io.File(file.getAbsolutePath() + ".csv");
            }
            
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("ID,X,Y,ColorRGB");
                for (userpackage.SPoint p : appState.getCanvasState().getStickyPoints()) {
                    pw.printf("%d,%d,%d,%d\n", p.id, p.X, p.Y, p.c.getRGB());
                }
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
        
        JMenuItem exportMatrixItem = new JMenuItem("Export Matrix...");
        exportMatrixItem.addActionListener(e -> performExportMatrix());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(exportMatrixItem);
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
            if (canvas.getBackgroundImage() == null) {
                JOptionPane.showMessageDialog(this, "Please open an image first.", "No Image", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Ideally we'd hold a single instance of ZoomWindow, but for now we create one and show it
            ui.dialogs.ZoomWindow zoom = new ui.dialogs.ZoomWindow(this, appState);
            canvas.setZoomWindow(zoom);
            zoom.updateImage(canvas.getBackgroundImage(), new Point(canvas.getWidth()/2, canvas.getHeight()/2));
            zoom.setVisible(true);
            zoom.toggleMeasurement();
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
        appState.getHistoryManager().addListener((canUndo, canRedo) -> {
            undoBtn.setEnabled(canUndo);
            redoBtn.setEnabled(canRedo);
        });
        
        toolBar.add(undoBtn);
        toolBar.add(redoBtn);
        toolBar.addSeparator();
        
        // Mouse Modes
        JButton handBtn = createIconButton("icon6.png", "Hand");
        handBtn.addActionListener(e -> canvas.setActiveTool(new tools.HandTool()));
        
        JButton stickBtn = createIconButton("icon11.png", "Stick");
        stickBtn.addActionListener(e -> canvas.setActiveTool(new tools.StickTool()));
        
        JButton p2pBtn = createIconButton("icon8.png", "P2P");
        p2pBtn.addActionListener(e -> canvas.setActiveTool(new tools.P2PTool()));
        
        JButton gridBtn = createIconButton("icon4.png", "Grid");
        gridBtn.addActionListener(e -> canvas.setActiveTool(new tools.GridTool(appState.getGridSize())));
        
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
