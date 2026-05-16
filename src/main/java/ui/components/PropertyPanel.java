package ui.components;

import core.history.GridCommand;
import core.state.AppState;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class PropertyPanel extends JPanel {
    private final AppState appState;
    private final ImageCanvas canvas;

    private JButton colorBtn;
    private JTextField xField;
    private JTextField yField;
    private JTextField sizeField;
    private JComboBox<String> unitCombo;
    private JButton deleteBtn;

    private boolean isUpdatingUI = false;

    public PropertyPanel(AppState appState, ImageCanvas canvas) {
        this.appState = appState;
        this.canvas = canvas;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        setBackground(new Color(245, 245, 245, 230)); // Slightly transparent
        setOpaque(false); // Important for transparent backgrounds in Swing
        
        // Define maximum width
        Dimension maxDim = new Dimension(80, 200);
        setMaximumSize(maxDim);
        
        // --- Header ---
        JLabel title = new JLabel("Grid Properties");
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);
        add(Box.createRigidArea(new Dimension(0, 10)));
        
        // --- Color ---
        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.setOpaque(false);
        colorBtn = new JButton();
        colorBtn.setPreferredSize(new Dimension(60, 20));
        colorBtn.addActionListener(e -> changeColor());
        colorPanel.add(colorBtn, BorderLayout.CENTER);
        add(colorPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        
        // --- X / Y ---
        JPanel xyPanel = new JPanel(new BorderLayout(5, 0));
        xyPanel.setOpaque(false);
        xyPanel.add(new JLabel("XY:"), BorderLayout.WEST);
        
        JPanel xyFieldsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        xyFieldsPanel.setOpaque(false);
        xField = new JTextField();
        yField = new JTextField();
        xyFieldsPanel.add(xField);
        xyFieldsPanel.add(yField);
        
        xyPanel.add(xyFieldsPanel, BorderLayout.CENTER);
        add(xyPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        
        // --- Size ---
        add(new SectionDivider("Size", 6, 3));
        JPanel sizePanel = new JPanel(new BorderLayout(5, 0));
        sizePanel.setOpaque(false);
//        sizePanel.add(new JLabel("Size:"), BorderLayout.WEST);
        
        JPanel sizeFieldsPanel = new JPanel(new BorderLayout(2, 0));
        sizeFieldsPanel.setOpaque(false);
        sizeField = new JTextField();
        
        unitCombo = new JComboBox<>(new String[]{"px", "cm"});
        unitCombo.setFont(new Font("SansSerif", Font.PLAIN, 10));
        // Mặc định unit là từ appState
        if (appState.isCmUnit() || appState.isGridInCm()) {
            unitCombo.setSelectedIndex(1);
        }
        
        sizeFieldsPanel.add(sizeField, BorderLayout.CENTER);
        sizeFieldsPanel.add(unitCombo, BorderLayout.EAST);
        
        sizePanel.add(sizeFieldsPanel, BorderLayout.CENTER);
        add(sizePanel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        
        // --- Delete ---
        deleteBtn = new JButton("Delete");
        try {
            deleteBtn.setIcon(new FlatSVGIcon("icons/ic_trash.svg", 16, 16));
        } catch (Exception e) {}
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.setForeground(Color.RED);
        deleteBtn.addActionListener(e -> deleteSelectedGrid());
        add(deleteBtn);

        // --- Listeners ---
        appState.getCanvasState().setGridSelectionListener(grid -> {
            updateUIFromGrid(grid);
        });

        FocusAdapter saveChangesListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                applyChangesToGrid();
            }
        };
        xField.addFocusListener(saveChangesListener);
        yField.addFocusListener(saveChangesListener);
        sizeField.addFocusListener(saveChangesListener);
        xField.addActionListener(e -> applyChangesToGrid());
        yField.addActionListener(e -> applyChangesToGrid());
        sizeField.addActionListener(e -> applyChangesToGrid());
        unitCombo.addActionListener(e -> applyChangesToGrid());

        // Chặn sự kiện chuột rơi xuyên qua bảng thuộc tính xuống Canvas
        addMouseListener(new java.awt.event.MouseAdapter() {});
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});

        setVisible(false);
    }


    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    private void updateUIFromGrid(SPoint grid) {
        if (grid == null) {
            setVisible(false);
            return;
        }
        isUpdatingUI = true;
        
        colorBtn.setBackground(grid.c);
        xField.setText(String.valueOf(grid.X));
        yField.setText(String.valueOf(grid.Y));
        
        boolean useCm = unitCombo.getSelectedIndex() == 1;
        float displaySize = grid.id; // grid.id stores pixel size
        if (useCm) {
            float scale = appState.getScale();
            if (scale > 0) {
                displaySize = displaySize * scale;
            }
        }
        
        // Display as integer if possible
        if (displaySize == Math.round(displaySize)) {
            sizeField.setText(String.valueOf(Math.round(displaySize)));
        } else {
            sizeField.setText(String.format("%.2f", displaySize).replace(",", "."));
        }
        
        setVisible(true);
        isUpdatingUI = false;
        
        // Force revalidation on parent if layered
        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }

    private void applyChangesToGrid() {
        if (isUpdatingUI) return;
        SPoint grid = appState.getCanvasState().getSelectedGrid();
        if (grid == null) return;

        try {
            int newX = Integer.parseInt(xField.getText());
            int newY = Integer.parseInt(yField.getText());
            
            float inputSize = Float.parseFloat(sizeField.getText());
            int pixelSize;
            if (unitCombo.getSelectedIndex() == 1) { // cm
                float scale = appState.getScale();
                pixelSize = (scale > 0) ? Math.round(inputSize / scale) : Math.round(inputSize);
            } else { // px
                pixelSize = Math.round(inputSize);
            }
            
            // If changed, push command
            if (newX != grid.X || newY != grid.Y || pixelSize != grid.id) {
                SPoint oldState = grid.copy();
                SPoint newState = grid.copy();
                newState.X = newX;
                newState.Y = newY;
                newState.id = pixelSize;
                
                GridCommand cmd = new GridCommand(appState.getCanvasState(), canvas, grid, GridCommand.Action.EDIT, oldState, newState);
                appState.getHistoryManager().push(cmd);
                cmd.execute();
            }
        } catch (NumberFormatException e) {
            // Revert invalid input
            updateUIFromGrid(grid);
        }
    }

    private void changeColor() {
        SPoint grid = appState.getCanvasState().getSelectedGrid();
        if (grid == null) return;
        
        Color newColor = JColorChooser.showDialog(this, "Select Grid Color", grid.c);
        if (newColor != null && !newColor.equals(grid.c)) {
            SPoint oldState = grid.copy();
            SPoint newState = grid.copy();
            newState.c = newColor;
            
            GridCommand cmd = new GridCommand(appState.getCanvasState(), canvas, grid, GridCommand.Action.EDIT, oldState, newState);
            appState.getHistoryManager().push(cmd);
            cmd.execute();
            
            colorBtn.setBackground(newColor);
        }
    }

    private void deleteSelectedGrid() {
        SPoint grid = appState.getCanvasState().getSelectedGrid();
        if (grid != null) {
            GridCommand cmd = new GridCommand(appState.getCanvasState(), canvas, grid, GridCommand.Action.DELETE, grid.copy(), null);
            appState.getHistoryManager().push(cmd);
            cmd.execute();
        }
    }
}
