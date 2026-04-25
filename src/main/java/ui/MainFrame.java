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
        add(canvas, BorderLayout.CENTER);
        
        initMenuBar();
        initToolBar();
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                
                // Show loading state
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
        });
        
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                if (!file.getName().endsWith(".png")) {
                    file = new java.io.File(file.getAbsolutePath() + ".png");
                }
                new workers.SaveWorker(canvas, file).execute();
            }
        });
        
        JMenuItem exportMatrixItem = new JMenuItem("Export Matrix...");
        exportMatrixItem.addActionListener(e -> {
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
                progressDialog.setVisible(true); // Blocks until worker calls dispose
            }
        });

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
            }
        });

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        redoItem.addActionListener(e -> {
            if (appState.getHistoryManager().canRedo()) {
                appState.getHistoryManager().redo();
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
        filterItem.addActionListener(e -> {
            BufferedImage currentImage = canvas.getBackgroundImage();
            if (currentImage != null) {
                new ui.dialogs.FilterDialog(this, currentImage, (newImage) -> {
                    // Apply lambda
                    core.history.Command filterCmd = new core.history.FilterCommand(canvas, currentImage, newImage);
                    appState.getHistoryManager().push(filterCmd);
                }).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Please open an image first.", "No Image", JOptionPane.WARNING_MESSAGE);
            }
        });
        imageMenu.add(filterItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(imageMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }
    
    private void initToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setOrientation(JToolBar.VERTICAL);
        
        JButton stickToolBtn = new JButton("Stick");
        stickToolBtn.addActionListener(e -> canvas.setActiveTool(new tools.StickTool()));
        
        JButton gridToolBtn = new JButton("Grid");
        gridToolBtn.addActionListener(e -> canvas.setActiveTool(new tools.GridTool(40))); // Default grid size
        
        JButton p2pToolBtn = new JButton("P2P");
        p2pToolBtn.addActionListener(e -> canvas.setActiveTool(new tools.P2PTool()));

        toolBar.add(stickToolBtn);
        toolBar.add(gridToolBtn);
        toolBar.add(p2pToolBtn);
        
        toolBar.addSeparator();
        
        JButton colorBtn = new JButton(" Color ");
        colorBtn.setBackground(appState.getBrushColor());
        colorBtn.setOpaque(true);
        colorBtn.setBorderPainted(false);
        colorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Select Brush Color", appState.getBrushColor());
            if (newColor != null) {
                appState.setBrushColor(newColor);
                colorBtn.setBackground(newColor);
            }
        });
        toolBar.add(colorBtn);
        
        add(toolBar, BorderLayout.WEST);
    }
}
