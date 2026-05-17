package ui.components;

import core.history.StickCommand;
import core.state.AppState;
import ui.canvas.ImageCanvas;
import user.Enum.Direction;
import userpackage.SPoint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class PointPropertyPanel extends JPanel {

    private final AppState appState;
    private final ImageCanvas canvas;

    private JButton  colorBtn;
    private JTextField xField;
    private JTextField yField;
    private JComboBox<String> dirCombo;
    private JCheckBox customCheck;
    private JLabel angleLabel;
    private JSpinner angleSpinner;
    private JLabel gapLabel;
    private JSpinner gapSpinner;
    private JButton  deleteBtn;

    private boolean isUpdatingUI = false;

    public PointPropertyPanel(AppState appState, ImageCanvas canvas) {
        this.appState = appState;
        this.canvas   = canvas;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        setBackground(new Color(245, 245, 245, 230));
        setOpaque(false);

        Dimension maxDim = new Dimension(130, 280);
        setMaximumSize(maxDim);

        // --- Title ---
        JLabel title = new JLabel("Point Properties");
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);
        add(Box.createRigidArea(new Dimension(0, 8)));

        // --- Color ---
        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.setOpaque(false);
        colorBtn = new JButton();
        colorBtn.setPreferredSize(new Dimension(100, 20));
        colorBtn.addActionListener(e -> changeColor());
        colorPanel.add(colorBtn, BorderLayout.CENTER);
        add(colorPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        // --- XY on one row ---
        JPanel xyPanel = new JPanel(new BorderLayout(5, 0));
        xyPanel.setOpaque(false);
        xyPanel.add(new JLabel("XY:"), BorderLayout.WEST);
        JPanel xyFields = new JPanel(new GridLayout(1, 2, 3, 0));
        xyFields.setOpaque(false);
        xField = new JTextField();
        yField = new JTextField();
        xyFields.add(xField);
        xyFields.add(yField);
        xyPanel.add(xyFields, BorderLayout.CENTER);
        add(xyPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        // --- Direction ---
        JPanel dirPanel = new JPanel(new BorderLayout(5, 0));
        dirPanel.setOpaque(false);
        dirPanel.add(new JLabel("Dir:"), BorderLayout.WEST);
        dirCombo = new JComboBox<>(new String[]{"EAST", "WEST", "NORTH", "SOUTH"});
        dirCombo.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dirPanel.add(dirCombo, BorderLayout.CENTER);
        add(dirPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        // --- Custom Placement checkbox ---
        customCheck = new JCheckBox("Custom");
        customCheck.setOpaque(false);
        customCheck.setFont(new Font("SansSerif", Font.PLAIN, 10));
        add(customCheck);

        // --- Angle row (hidden when unchecked) ---
        JPanel anglePanel = new JPanel(new BorderLayout(5, 0));
        anglePanel.setOpaque(false);
        angleLabel = new JLabel("Angle:");
        angleLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        anglePanel.add(angleLabel, BorderLayout.WEST);
        // Unbounded model: cho phép nhập số âm và > 360, sẽ được normalize về [0, 359]
        angleSpinner = new JSpinner(new SpinnerNumberModel(0, null, null, 1));
        angleSpinner.setFont(new Font("SansSerif", Font.PLAIN, 10));
        anglePanel.add(angleSpinner, BorderLayout.CENTER);
        add(anglePanel);
        add(Box.createRigidArea(new Dimension(0, 3)));

        // --- Gap row (hidden when unchecked) ---
        JPanel gapPanel = new JPanel(new BorderLayout(5, 0));
        gapPanel.setOpaque(false);
        gapLabel = new JLabel("Gap:");
        gapLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        gapPanel.add(gapLabel, BorderLayout.WEST);
        // Clamp gap tới [10, 50]
        gapSpinner = new JSpinner(new SpinnerNumberModel(30, 10, 50, 1));
        gapSpinner.setFont(new Font("SansSerif", Font.PLAIN, 10));
        gapPanel.add(gapSpinner, BorderLayout.CENTER);
        add(gapPanel);
        add(Box.createRigidArea(new Dimension(0, 8)));

        // --- Delete ---
        deleteBtn = new JButton("Delete");
        try {
            deleteBtn.setIcon(new FlatSVGIcon("icons/ic_trash.svg", 16, 16));
        } catch (Exception ignored) {}
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.setForeground(Color.RED);
        deleteBtn.addActionListener(e -> deleteSelectedPoint());
        add(deleteBtn);

        // --- Listeners ---
        appState.getCanvasState().setPointSelectionListener(point -> updateUIFromPoint(point));

        FocusAdapter applyOnFocusLost = new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { applyChangesToPoint(); }
        };
        xField.addFocusListener(applyOnFocusLost);
        yField.addFocusListener(applyOnFocusLost);
        xField.addActionListener(e -> applyChangesToPoint());
        yField.addActionListener(e -> applyChangesToPoint());

        dirCombo.addActionListener(e -> applyChangesToPoint());

        customCheck.addActionListener(e -> {
            boolean custom = customCheck.isSelected();
            angleLabel.setVisible(custom);
            angleSpinner.setVisible(custom);
            gapLabel.setVisible(custom);
            gapSpinner.setVisible(custom);
            applyChangesToPoint();
        });

        angleSpinner.addChangeListener(e -> {
            if (isUpdatingUI) return;
            // Normalize angle về [0, 359] ngay khi user thay đổi
            int raw = ((Number) angleSpinner.getValue()).intValue();
            int normalized = ((raw % 360) + 360) % 360;
            if (normalized != raw) {
                isUpdatingUI = true;
                angleSpinner.setValue(normalized);
                isUpdatingUI = false;
            }
            applyChangesToPoint();
        });
        gapSpinner.addChangeListener(e -> {
            if (isUpdatingUI) return;
            // Clamp gap về [10, 50]
            int raw = ((Number) gapSpinner.getValue()).intValue();
            int clamped = Math.max(10, Math.min(50, raw));
            if (clamped != raw) {
                isUpdatingUI = true;
                gapSpinner.setValue(clamped);
                isUpdatingUI = false;
            }
            applyChangesToPoint();
        });

        // Consume mouse events so they don't fall through to canvas
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

    private void updateUIFromPoint(SPoint p) {
        if (p == null) {
            setVisible(false);
            return;
        }
        isUpdatingUI = true;

        colorBtn.setBackground(p.c);
        xField.setText(String.valueOf(p.X));
        yField.setText(String.valueOf(p.Y));

        // Direction
        dirCombo.setSelectedItem(p.dr != null ? p.dr.name() : "EAST");
        dirCombo.setEnabled(!p.isCustomPlacement);

        // Custom placement
        customCheck.setSelected(p.isCustomPlacement);
        angleLabel.setVisible(p.isCustomPlacement);
        angleSpinner.setVisible(p.isCustomPlacement);
        gapLabel.setVisible(p.isCustomPlacement);
        gapSpinner.setVisible(p.isCustomPlacement);

        if (p.isCustomPlacement) {
            angleSpinner.setValue(((p.customAngle % 360) + 360) % 360);
            gapSpinner.setValue(Math.max(10, Math.min(50, p.customGap)));
        } else {
            // Default from appState when first enabling custom
            angleSpinner.setValue(appState.getCustomAngle());
            gapSpinner.setValue(Math.max(1, appState.getCustomGap()));
        }

        setVisible(true);
        isUpdatingUI = false;

        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }

    private void applyChangesToPoint() {
        if (isUpdatingUI) return;
        SPoint p = appState.getCanvasState().getSelectedPoint();
        if (p == null) return;

        try {
            int newX = Integer.parseInt(xField.getText().trim());
            int newY = Integer.parseInt(yField.getText().trim());
            Direction newDir = Direction.valueOf((String) dirCombo.getSelectedItem());
            boolean newCustom = customCheck.isSelected();
            int rawAngle = ((Number) angleSpinner.getValue()).intValue();
            int newAngle = ((rawAngle % 360) + 360) % 360; // Normalize về [0, 359]
            int newGap   = Math.max(10, Math.min(50, ((Number) gapSpinner.getValue()).intValue()));

            // Check if anything changed
            boolean changed = newX != p.X || newY != p.Y
                    || newDir != p.dr
                    || newCustom != p.isCustomPlacement
                    || (newCustom && (newAngle != p.customAngle || newGap != p.customGap));

            if (changed) {
                SPoint oldState = p.copy();
                SPoint newState = p.copy();
                newState.X = newX;
                newState.Y = newY;
                newState.dr = newDir;
                newState.isCustomPlacement = newCustom;
                if (newCustom) {
                    newState.customAngle = newAngle;
                    newState.customGap   = newGap;
                }

                StickCommand cmd = new StickCommand(
                        appState.getCanvasState(), canvas, p,
                        StickCommand.Action.EDIT, oldState, newState);
                appState.getHistoryManager().push(cmd);

                // Enable/disable dir combo based on custom
                dirCombo.setEnabled(!newCustom);
            }
        } catch (IllegalArgumentException ignored) {
            // Revert invalid input
            updateUIFromPoint(p);
        }
    }

    private void changeColor() {
        SPoint p = appState.getCanvasState().getSelectedPoint();
        if (p == null) return;

        Color newColor = JColorChooser.showDialog(this, "Select Point Color", p.c);
        if (newColor != null && !newColor.equals(p.c)) {
            SPoint oldState = p.copy();
            SPoint newState = p.copy();
            newState.c = newColor;

            StickCommand cmd = new StickCommand(
                    appState.getCanvasState(), canvas, p,
                    StickCommand.Action.EDIT, oldState, newState);
            appState.getHistoryManager().push(cmd);
            colorBtn.setBackground(newColor);
        }
    }

    private void deleteSelectedPoint() {
        SPoint p = appState.getCanvasState().getSelectedPoint();
        if (p != null) {
            StickCommand cmd = new StickCommand(
                    appState.getCanvasState(), canvas, p,
                    StickCommand.Action.DELETE, p.copy(), null);
            appState.getHistoryManager().push(cmd);
        }
    }
}
