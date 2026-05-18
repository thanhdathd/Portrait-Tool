package ui.components;

import core.history.LineCommand;
import core.state.AppState;
import ui.canvas.ImageCanvas;
import userpackage.SLine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class LinePropertyPanel extends JPanel {

    private final AppState appState;
    private final ImageCanvas canvas;

    private JLabel idLabel;
    private JButton colorBtn;
    private JComboBox<Integer> strokeWidthComboBox;
    private JButton deleteBtn;

    private boolean isUpdatingUI = false;

    public LinePropertyPanel(AppState appState, ImageCanvas canvas) {
        this.appState = appState;
        this.canvas = canvas;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        setBackground(new Color(245, 245, 245, 230));
        setOpaque(false);
        setMaximumSize(new Dimension(130, 200));

        // --- Title ---
        JLabel title = new JLabel("Line Properties");
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);
        add(Box.createRigidArea(new Dimension(0, 5)));

        // --- ID Label ---
        idLabel = new JLabel("ID: ");
        idLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(idLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        // --- Color ---
        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.setOpaque(false);
        colorBtn = new JButton();
        colorBtn.setPreferredSize(new Dimension(100, 20));
        colorBtn.addActionListener(e -> changeColor());
        colorPanel.add(colorBtn, BorderLayout.CENTER);
        add(colorPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        // --- Thickness Section ---
        add(new SectionDivider("Thickness", 6, 3));
        JPanel sizePanel = new JPanel(new BorderLayout(5, 0));
        sizePanel.setOpaque(false);

        strokeWidthComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 5, 8, 13});
        strokeWidthComboBox.setToolTipText("Line Stroke Width");
        strokeWidthComboBox.setRenderer(new ListCellRenderer<Integer>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index, boolean isSelected, boolean cellHasFocus) {
                int thickness = (value != null) ? value : 2;
                JPanel panel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        SLine currentLine = appState.getCanvasState().getSelectedLine();
                        Color strokeColor = (currentLine != null) ? currentLine.strokeColor : appState.getBrushColor();
                        g2.setColor(strokeColor);
                        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        int y = getHeight() / 2;
                        g2.drawLine(10, y, getWidth() - 10, y);
                    }
                };
                panel.setPreferredSize(new Dimension(80, 24));
                if (isSelected) {
                    panel.setBackground(list.getSelectionBackground());
                } else {
                    panel.setBackground(list.getBackground());
                }
                return panel;
            }
        });
        strokeWidthComboBox.addActionListener(e -> applyStrokeWidthChange());
        sizePanel.add(strokeWidthComboBox, BorderLayout.CENTER);
        add(sizePanel);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Delete ---
        deleteBtn = new JButton("Delete");
        try {
            deleteBtn.setIcon(new FlatSVGIcon("icons/ic_trash.svg", 16, 16));
        } catch (Exception ignored) {}
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.setForeground(Color.RED);
        deleteBtn.addActionListener(e -> deleteSelectedLine());
        add(deleteBtn);

        // --- Selection listener ---
        appState.getCanvasState().setLineSelectionListener(line -> updateUIFromLine(line));

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

    private void updateUIFromLine(SLine line) {
        if (line == null) {
            setVisible(false);
            return;
        }

        isUpdatingUI = true;
        idLabel.setText("ID: " + line.id);
        colorBtn.setBackground(line.strokeColor);
        strokeWidthComboBox.setSelectedItem(line.strokeWidth);
        setVisible(true);
        isUpdatingUI = false;

        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }

    private void applyStrokeWidthChange() {
        if (isUpdatingUI) return;
        SLine line = appState.getCanvasState().getSelectedLine();
        if (line == null) return;

        Integer selectedWidth = (Integer) strokeWidthComboBox.getSelectedItem();
        if (selectedWidth != null && selectedWidth != line.strokeWidth) {
            SLine oldState = line.copy();
            SLine newState = line.copy();
            newState.strokeWidth = selectedWidth;

            LineCommand cmd = new LineCommand(
                    appState.getCanvasState(),
                    canvas,
                    line,
                    LineCommand.Action.EDIT,
                    oldState,
                    newState
            );
            appState.getHistoryManager().push(cmd);
        }
    }

    private void changeColor() {
        SLine line = appState.getCanvasState().getSelectedLine();
        if (line == null) return;

        Color newColor = JColorChooser.showDialog(this, "Select Line Color", line.strokeColor);
        if (newColor != null && !newColor.equals(line.strokeColor)) {
            SLine oldState = line.copy();
            SLine newState = line.copy();
            newState.strokeColor = newColor;

            LineCommand cmd = new LineCommand(
                    appState.getCanvasState(),
                    canvas,
                    line,
                    LineCommand.Action.EDIT,
                    oldState,
                    newState
            );
            appState.getHistoryManager().push(cmd);
            colorBtn.setBackground(newColor);
            strokeWidthComboBox.repaint();
        }
    }

    private void deleteSelectedLine() {
        SLine line = appState.getCanvasState().getSelectedLine();
        if (line != null) {
            LineCommand cmd = new LineCommand(
                    appState.getCanvasState(),
                    canvas,
                    line,
                    LineCommand.Action.DELETE,
                    line.copy(),
                    null
            );
            appState.getHistoryManager().push(cmd);
        }
    }
}
