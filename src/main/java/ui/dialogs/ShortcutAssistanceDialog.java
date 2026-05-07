package ui.dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * A dialog that presents all keyboard shortcuts in a clear, tabular format.
 */
public class ShortcutAssistanceDialog extends JDialog {

    public ShortcutAssistanceDialog(Frame owner) {
        super(owner, "Keyboard Shortcuts Assist", true);
        initComponents();
        setSize(700, 600); // Increased height to accommodate more shortcuts
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        String[] columnNames = {"Key", "Action / Description"};
        Object[][] data = {
            {"[ GENERAL ]", ""},
            {"Ctrl + Z", "Undo last action"},
            {"Ctrl + Y", "Redo last undone action"},
            {"Space (Hold)", "Temporary Hand Tool (Pan image)"},
            {"H", "Hand Tool (Pan image)"},
            {"Z", "Zoom Tool (Click to zoom, Shift+Click to invert)"},
            {"C / S", "Stick Tool (Mark sticky points)"},
            {"V / G", "Grid Tool (Draw lines)"},
            {"P / B", "P2P Tool (Measure distances / Draw lines)"},
            {"M", "Toggle Zoom Window (Caliper mode)"},
            {"", ""},
            {"[ ZOOM WINDOW ]", ""},
            {"Arrow Keys", "Set label direction (North, South, East, West)"},
            {"A", "Toggle Custom Label Mode (with target circle)"},
            {"[ or -", "Decrease circle size / label gap"},
            {"] or =", "Increase circle size / label gap"},
            {", ( < )", "Rotate label counter-clockwise (Custom Mode)"},
            {". ( > )", "Rotate label clockwise (Custom Mode)"},
            {"", ""},
            {"[ CROP TOOL ]", ""},
            {"Enter", "Lock Frame (Review mode) / Apply Crop"},
            {"ESC", "Exit Snap -> Unlock Frame -> Exit Tool"},
            {"R", "Rotate Frame (Portrait / Landscape)"},
            {"Shift + R", "Cycle Golden Spiral variant (if active)"},
            {"Ctrl + V", "Toggle Vertical Snap to image edges"},
            {"Ctrl + H", "Toggle Horizontal Snap to image edges"},
            {"Left / Right", "Switch Profile (Fly mode) / Move Frame X (Review mode)"},
            {"Up / Down", "Move Frame Y (Review mode)"},
            {"Shift + Wheel", "Adjust Frame size"},
            {"", ""},
            {"[ P2P & GRID TOOL ]", ""},
            {"Shift (Drag)", "Snap line to Horizontal or Vertical (P2P Tool)"},
            {"E", "Delete last measurement line (P2P Tool)"},
            {"Ctrl + E", "Delete All measurement line (P2P Tool)"},
            {"Alt (Drag)", "Precision movement (Grid Tool)"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Styling the table columns
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(204); // Key column
        columnModel.getColumn(1).setPreferredWidth(476); // Description column

        // Custom renderer for professional look with clear borders
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String val = (value != null) ? value.toString() : "";
                
                // Colors
                Color gridColor = new Color(210, 210, 210);
                Color categoryBg = new Color(235, 245, 250);
                Color categoryBorder = new Color(180, 210, 230);

                if (val.startsWith("[") && val.endsWith("]")) {
                    // Category row
                    c.setBackground(categoryBg);
                    c.setFont(c.getFont().deriveFont(Font.BOLD, 13f));
                    c.setForeground(new Color(0, 80, 130));
                    // Category row has top and bottom borders only
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 1, 0, categoryBorder),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                    ));
                } else if (val.isEmpty() && column == 0) {
                    // Spacer row
                    c.setBackground(table.getBackground());
                    setBorder(null);
                } else {
                    // Normal cell
                    c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    c.setFont(c.getFont().deriveFont(Font.PLAIN, 12f));
                    c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    
                    // Grid effect: Bottom and Right border
                    // Column 0 also gets a Left border for the first column
                    int left = (column == 0) ? 1 : 0;
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, left, 1, 1, gridColor),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                    ));
                }
                
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Header Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("Keyboard Shortcuts Reference");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Bottom Button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        JButton closeBtn = new JButton("Close");
        closeBtn.setPreferredSize(new Dimension(80, 30));
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }
}
