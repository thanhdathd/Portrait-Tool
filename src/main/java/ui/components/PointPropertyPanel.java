package ui.components;

import core.history.BatchStickCommand;
import core.history.StickCommand;
import core.state.AppState;
import ui.canvas.ImageCanvas;
import core.state.Direction;
import core.state.SPoint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;
import java.util.List;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class PointPropertyPanel extends JPanel {

    private final AppState appState;
    private final ImageCanvas canvas;

    // Single-select controls
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

    // Multi-select overlay label
    private JLabel multiLabel;

    // Panels for visibility toggling
    private JPanel singleOnlyPanel; // wraps XY, Dir, Custom/Angle/Gap

    private boolean isUpdatingUI = false;

    // Spinner debounce — to avoid flooding history stack on hold-click
    private SPoint spinnerOldState   = null;
    private SPoint spinnerPointRef   = null;
    private javax.swing.Timer spinnerDebounceTimer = null;
    private static final int SPINNER_DEBOUNCE_MS   = 400;

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
        setMaximumSize(new Dimension(130, 320));

        // --- Title ---
        JLabel title = new JLabel("Point Properties");
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);
        add(Box.createRigidArea(new Dimension(0, 5)));

        // --- Multi-select label ---
        multiLabel = new JLabel("", SwingConstants.CENTER);
        multiLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        multiLabel.setForeground(new Color(80, 80, 80));
        multiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        multiLabel.setVisible(false);
        add(multiLabel);
        add(Box.createRigidArea(new Dimension(0, 4)));

        // --- Color ---
        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.setOpaque(false);
        colorBtn = new JButton();
        colorBtn.setPreferredSize(new Dimension(100, 20));
        colorBtn.addActionListener(e -> changeColor());
        colorPanel.add(colorBtn, BorderLayout.CENTER);
        add(colorPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        // --- Single-only controls (wrapped in a sub-panel for easy hide/show) ---
        singleOnlyPanel = new JPanel();
        singleOnlyPanel.setLayout(new BoxLayout(singleOnlyPanel, BoxLayout.Y_AXIS));
        singleOnlyPanel.setOpaque(false);

        // XY row
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
        singleOnlyPanel.add(xyPanel);
        singleOnlyPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Direction row
        JPanel dirPanel = new JPanel(new BorderLayout(5, 0));
        dirPanel.setOpaque(false);
        dirPanel.add(new JLabel("Dir:"), BorderLayout.WEST);
        dirCombo = new JComboBox<>(new String[]{"EAST", "WEST", "NORTH", "SOUTH"});
        dirCombo.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dirPanel.add(dirCombo, BorderLayout.CENTER);
        singleOnlyPanel.add(dirPanel);
        singleOnlyPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Custom checkbox
        customCheck = new JCheckBox("Custom");
        customCheck.setOpaque(false);
        customCheck.setFont(new Font("SansSerif", Font.PLAIN, 10));
        singleOnlyPanel.add(customCheck);

        // Angle row
        JPanel anglePanel = new JPanel(new BorderLayout(5, 0));
        anglePanel.setOpaque(false);
        angleLabel = new JLabel("Angle:");
        angleLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        anglePanel.add(angleLabel, BorderLayout.WEST);
        angleSpinner = new JSpinner(new SpinnerNumberModel(0, null, null, 1));
        angleSpinner.setFont(new Font("SansSerif", Font.PLAIN, 10));
        anglePanel.add(angleSpinner, BorderLayout.CENTER);
        singleOnlyPanel.add(anglePanel);
        singleOnlyPanel.add(Box.createRigidArea(new Dimension(0, 3)));

        // Gap row
        JPanel gapPanel = new JPanel(new BorderLayout(5, 0));
        gapPanel.setOpaque(false);
        gapLabel = new JLabel("Gap:");
        gapLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        gapPanel.add(gapLabel, BorderLayout.WEST);
        gapSpinner = new JSpinner(new SpinnerNumberModel(30, 10, 50, 1));
        gapSpinner.setFont(new Font("SansSerif", Font.PLAIN, 10));
        gapPanel.add(gapSpinner, BorderLayout.CENTER);
        singleOnlyPanel.add(gapPanel);
        singleOnlyPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        add(singleOnlyPanel);

        // --- Delete ---
        deleteBtn = new JButton("Delete");
        try { deleteBtn.setIcon(new FlatSVGIcon("icons/ic_trash.svg", 16, 16)); } catch (Exception ignored) {}
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.setForeground(Color.RED);
        deleteBtn.addActionListener(e -> deleteSelectedPoints());
        add(deleteBtn);

        // --- Selection listener ---
        appState.getCanvasState().setPointSelectionListener(points -> updateUIFromPoints(points));

        // --- Field listeners ---
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
            // 1. Normalize
            int raw = ((Number) angleSpinner.getValue()).intValue();
            int normalized = ((raw % 360) + 360) % 360;
            if (normalized != raw) {
                isUpdatingUI = true;
                angleSpinner.setValue(normalized);
                isUpdatingUI = false;
                return; // listener re-fires with normalized value
            }
            // 2. Apply visual + debounce history
            handleSpinnerChange();
        });

        gapSpinner.addChangeListener(e -> {
            if (isUpdatingUI) return;
            // 1. Clamp
            int raw = ((Number) gapSpinner.getValue()).intValue();
            int clamped = Math.max(10, Math.min(50, raw));
            if (clamped != raw) {
                isUpdatingUI = true;
                gapSpinner.setValue(clamped);
                isUpdatingUI = false;
                return; // listener re-fires with clamped value
            }
            // 2. Apply visual + debounce history
            handleSpinnerChange();
        });

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

    // ----------------------------------------------------------------
    // UI update
    // ----------------------------------------------------------------

    private void updateUIFromPoints(Set<SPoint> points) {
        if (points == null || points.isEmpty()) {
            setVisible(false);
            return;
        }

        isUpdatingUI = true;

        if (points.size() == 1) {
            // Single-select: full UI
            SPoint p = points.iterator().next();
            multiLabel.setVisible(false);
            singleOnlyPanel.setVisible(true);

            colorBtn.setBackground(p.c);
            xField.setText(String.valueOf(p.X));
            yField.setText(String.valueOf(p.Y));
            dirCombo.setSelectedItem(p.dr != null ? p.dr.name() : "EAST");
            dirCombo.setEnabled(!p.isCustomPlacement);
            customCheck.setSelected(p.isCustomPlacement);
            angleLabel.setVisible(p.isCustomPlacement);
            angleSpinner.setVisible(p.isCustomPlacement);
            gapLabel.setVisible(p.isCustomPlacement);
            gapSpinner.setVisible(p.isCustomPlacement);
            if (p.isCustomPlacement) {
                angleSpinner.setValue(((p.customAngle % 360) + 360) % 360);
                gapSpinner.setValue(Math.max(10, Math.min(50, p.customGap)));
            } else {
                angleSpinner.setValue(appState.getCustomAngle());
                gapSpinner.setValue(Math.max(10, Math.min(50, appState.getCustomGap())));
            }
        } else {
            // Multi-select: only color + delete
            multiLabel.setText(points.size() + " points selected");
            multiLabel.setVisible(true);
            singleOnlyPanel.setVisible(false);

            colorBtn.setBackground(dominantColor(points));
        }

        setVisible(true);
        isUpdatingUI = false;

        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }

    /**
     * Returns the dominant color in the selection (most frequent; random tie-break).
     */
    private Color dominantColor(Set<SPoint> points) {
        Map<Color, Integer> freq = new LinkedHashMap<>();
        for (SPoint p : points) {
            freq.merge(p.c, 1, Integer::sum);
        }
        int max = Collections.max(freq.values());
        List<Color> candidates = new ArrayList<>();
        for (Map.Entry<Color, Integer> entry : freq.entrySet()) {
            if (entry.getValue() == max) candidates.add(entry.getKey());
        }
        return candidates.get(new Random().nextInt(candidates.size()));
    }

    // ----------------------------------------------------------------
    // Single-point apply
    // ----------------------------------------------------------------

    private void handleSpinnerChange() {
        if (isUpdatingUI) return;
        Set<SPoint> sel = appState.getCanvasState().getSelectedPoints();
        if (sel.size() != 1) return;
        SPoint p = sel.iterator().next();

        int newAngle = ((Number) angleSpinner.getValue()).intValue();
        int newGap   = ((Number) gapSpinner.getValue()).intValue();
        
        // If nothing changed visually, do nothing
        if (p.customAngle == newAngle && p.customGap == newGap) return;

        // Capture initial state if a new sequence is starting
        if (spinnerDebounceTimer == null || !spinnerDebounceTimer.isRunning() || spinnerPointRef != p) {
            spinnerPointRef = p;
            spinnerOldState = p.copy();
        }

        // Apply visually immediately
        p.customAngle = newAngle;
        p.customGap = newGap;
        canvas.repaint();

        // Reset debounce timer
        if (spinnerDebounceTimer != null) {
            spinnerDebounceTimer.stop();
        }
        
        final SPoint capturedOldState = spinnerOldState;
        
        spinnerDebounceTimer = new javax.swing.Timer(SPINNER_DEBOUNCE_MS, evt -> {
            Set<SPoint> currSel = appState.getCanvasState().getSelectedPoints();
            if (currSel.size() == 1) {
                SPoint current = currSel.iterator().next();
                if (current == spinnerPointRef) {
                    SPoint newState = current.copy();
                    StickCommand cmd = new StickCommand(
                            appState.getCanvasState(), canvas, current,
                            StickCommand.Action.EDIT,
                            capturedOldState, newState);
                    appState.getHistoryManager().push(cmd);
                }
            }
            spinnerDebounceTimer = null;
            spinnerPointRef = null;
            spinnerOldState = null;
        });
        spinnerDebounceTimer.setRepeats(false);
        spinnerDebounceTimer.start();
    }

    private void applyChangesToPoint() {
        if (isUpdatingUI) return;
        Set<SPoint> sel = appState.getCanvasState().getSelectedPoints();
        if (sel.size() != 1) return;
        SPoint p = sel.iterator().next();

        try {
            int newX = Integer.parseInt(xField.getText().trim());
            int newY = Integer.parseInt(yField.getText().trim());
            Direction newDir = Direction.valueOf((String) dirCombo.getSelectedItem());
            boolean newCustom = customCheck.isSelected();
            int rawAngle = ((Number) angleSpinner.getValue()).intValue();
            int newAngle = ((rawAngle % 360) + 360) % 360;
            int newGap   = Math.max(10, Math.min(50, ((Number) gapSpinner.getValue()).intValue()));

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
                StickCommand cmd = new StickCommand(appState.getCanvasState(), canvas, p,
                        StickCommand.Action.EDIT, oldState, newState);
                appState.getHistoryManager().push(cmd);
                dirCombo.setEnabled(!newCustom);
            }
        } catch (IllegalArgumentException ignored) {
            updateUIFromPoints(appState.getCanvasState().getSelectedPoints());
        }
    }

    // ----------------------------------------------------------------
    // Color change (single or multi)
    // ----------------------------------------------------------------

    private void changeColor() {
        Set<SPoint> sel = appState.getCanvasState().getSelectedPoints();
        if (sel.isEmpty()) return;

        Color initial = sel.size() == 1 ? sel.iterator().next().c : dominantColor(sel);
        Color newColor = JColorChooser.showDialog(this, "Select Point Color", initial);
        if (newColor == null) return;

        if (sel.size() == 1) {
            SPoint p = sel.iterator().next();
            if (!newColor.equals(p.c)) {
                SPoint oldState = p.copy();
                SPoint newState = p.copy();
                newState.c = newColor;
                StickCommand cmd = new StickCommand(appState.getCanvasState(), canvas, p,
                        StickCommand.Action.EDIT, oldState, newState);
                appState.getHistoryManager().push(cmd);
                colorBtn.setBackground(newColor);
            }
        } else {
            // Batch color change
            List<SPoint> targets = new ArrayList<>(sel);
            BatchStickCommand cmd = new BatchStickCommand(appState.getCanvasState(), canvas, targets, newColor);
            appState.getHistoryManager().push(cmd);
            colorBtn.setBackground(newColor);
        }
    }

    // ----------------------------------------------------------------
    // Delete (single or multi)
    // ----------------------------------------------------------------

    private void deleteSelectedPoints() {
        Set<SPoint> sel = appState.getCanvasState().getSelectedPoints();
        if (sel.isEmpty()) return;

        if (sel.size() == 1) {
            SPoint p = sel.iterator().next();
            StickCommand cmd = new StickCommand(appState.getCanvasState(), canvas, p,
                    StickCommand.Action.DELETE, p.copy(), null);
            appState.getHistoryManager().push(cmd);
        } else {
            List<SPoint> targets = new ArrayList<>(sel);
            BatchStickCommand cmd = new BatchStickCommand(appState.getCanvasState(), canvas, targets);
            appState.getHistoryManager().push(cmd);
        }
    }
}
