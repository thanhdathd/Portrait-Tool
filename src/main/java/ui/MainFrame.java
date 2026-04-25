package ui;

import core.state.AppState;
import ui.canvas.ImageCanvas;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

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
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
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

        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

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

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
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
