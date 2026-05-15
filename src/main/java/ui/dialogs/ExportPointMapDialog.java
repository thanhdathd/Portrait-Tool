package ui.dialogs;

import core.state.AppState;
import ui.canvas.ImageCanvas;
import workers.SavePointMapWorker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;
import java.util.Locale;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class ExportPointMapDialog extends JDialog {
    private final ImageCanvas canvas;
    private final AppState appState;
    private final double originalAspectRatio;
    private final int originalWidth;
    private final int originalHeight;

    private JTextField widthField;
    private JTextField heightField;
    private JSlider dpiSlider;
    private JLabel dpiLabel;
    private JLabel resultSizeLabel;
    private JComboBox<String> formatComboBox;
    private JToggleButton lockBtn;
    private JLabel warningLabel;
    private JLabel limitWarningLabel;
    private JCheckBox printTitleCb;
    private JTextField titleField;
    private JFileChooser chooser;

    private JCheckBox previewCb;
    private final Frame ownerFrame;

    private boolean isUpdating = false;

    public ExportPointMapDialog(Frame owner, ImageCanvas canvas, AppState appState, JFileChooser chooser) {
        super(owner, "Export Point Map Settings", false); // Non-modal
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.ownerFrame = owner;
        this.canvas = canvas;
        this.appState = appState;
        this.chooser = chooser;

        BufferedImage img = canvas.getBackgroundImage();
        if (img != null) {
            this.originalWidth = img.getWidth();
            this.originalHeight = img.getHeight();
        } else {
            this.originalWidth = canvas.getWidth();
            this.originalHeight = canvas.getHeight();
        }
        this.originalAspectRatio = (double) originalWidth / originalHeight;

        initComponents();
        setSize(420, 550); // Increased height to accommodate new fields
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 1. Paper Size Section
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Paper Width (cm):"), gbc);
        
        widthField = new JTextField();
        gbc.gridx = 1;
        mainPanel.add(widthField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Paper Height (cm):"), gbc);
        
        heightField = new JTextField();
        gbc.gridx = 1;
        mainPanel.add(heightField, gbc);

        // Lock Button
        lockBtn = new JToggleButton(new FlatSVGIcon("icons/ic_lock.svg", 16, 16));
        lockBtn.setSelectedIcon(new FlatSVGIcon("icons/ic_unlock.svg", 16, 16));
        lockBtn.setToolTipText("Unlock to set custom size");
        lockBtn.setPreferredSize(new Dimension(30, 30));
        lockBtn.setContentAreaFilled(false);
        lockBtn.setBorderPainted(false);
        lockBtn.setFocusPainted(false);
        lockBtn.setOpaque(false);
        lockBtn.setSelected(false); // Default: locked

        gbc.gridx = 2; gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        mainPanel.add(lockBtn, gbc);

        // Reset gbc
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Warning Label
        warningLabel = new JLabel("Warning: Custom size breaks physical 1:1 scale");
        warningLabel.setForeground(Color.RED);
        warningLabel.setFont(warningLabel.getFont().deriveFont(Font.ITALIC, 11f));
        warningLabel.setVisible(false); // Hidden when locked
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 3;
        mainPanel.add(warningLabel, gbc);

        // Limit Warning Label
        limitWarningLabel = new JLabel("Preview clamped to safe bounds (10-120cm)");
        limitWarningLabel.setForeground(Color.RED);
        limitWarningLabel.setFont(limitWarningLabel.getFont().deriveFont(Font.BOLD | Font.ITALIC, 11f));
        limitWarningLabel.setVisible(false);
        
        gbc.gridy = 3;
        mainPanel.add(limitWarningLabel, gbc);
        
        // Reset gbc
        gbc.gridwidth = 1;
        
        widthField.setEnabled(false);
        heightField.setEnabled(false);

        // Map Title Checkbox
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        printTitleCb = new JCheckBox("Print Map Title");
        printTitleCb.setSelected(true);
        mainPanel.add(printTitleCb, gbc);

        // Map Title Field
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Title:"), gbc);
        
        titleField = new JTextField();
        String currentFile = appState.getFilePath();
        if (currentFile != null && !currentFile.isEmpty()) {
            titleField.setText(new File(currentFile).getName());
        } else {
            titleField.setText("Point_Map");
        }
        gbc.gridx = 1;
        mainPanel.add(titleField, gbc);

        // 2. Format Selection Section
        gbc.gridx = 0; gbc.gridy = 6;
        mainPanel.add(new JLabel("Export Format:"), gbc);

        formatComboBox = new JComboBox<>(new String[]{"PDF Document (*.pdf)", "PNG Image (*.png)"});
        gbc.gridx = 1;
        mainPanel.add(formatComboBox, gbc);

        // 3. DPI Section
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        mainPanel.add(new JLabel("Select Export DPI:"), gbc);

        dpiSlider = new JSlider(50, 400, 300);
        dpiSlider.setMajorTickSpacing(50);
        dpiSlider.setPaintTicks(true);
        dpiSlider.setSnapToTicks(false);
        
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(50, new JLabel("50"));
        labelTable.put(150, new JLabel("150"));
        labelTable.put(300, new JLabel("300"));
        labelTable.put(400, new JLabel("400"));
        dpiSlider.setLabelTable(labelTable);
        dpiSlider.setPaintLabels(true);

        gbc.gridy = 8;
        mainPanel.add(dpiSlider, gbc);

        dpiLabel = new JLabel("Current DPI: 300");
        dpiLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 9;
        mainPanel.add(dpiLabel, gbc);

        // 4. Result Info Section
        resultSizeLabel = new JLabel("Result size: 0 x 0 px");
        resultSizeLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        gbc.gridy = 10;
        mainPanel.add(resultSizeLabel, gbc);

        // 5. Live Preview Checkbox
        previewCb = new JCheckBox("Live Preview on Canvas");
        previewCb.setSelected(false);
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        mainPanel.add(previewCb, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportBtn = new JButton("Export");
        JButton cancelBtn = new JButton("Cancel");

        exportBtn.addActionListener(e -> performExport());
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(exportBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Listeners
        setupListeners();

        // Initial Values based on AppState scale
        double initialWidthCm = originalWidth * appState.getScale();
        double rW = round(initialWidthCm);
        String strWcm = rW - (int)rW == 0 ? String.valueOf((int)rW) : String.format(Locale.US, "%.2f", rW);
        widthField.setText(strWcm);
        updateHeightFromWidth();
        updateInfo();
    }

    private void setupListeners() {
        widthField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateHeightFromWidth(); updatePreviewState(); }
            public void removeUpdate(DocumentEvent e) { updateHeightFromWidth(); updatePreviewState(); }
            public void changedUpdate(DocumentEvent e) { updateHeightFromWidth(); updatePreviewState(); }
        });

        heightField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateWidthFromHeight(); updatePreviewState(); }
            public void removeUpdate(DocumentEvent e) { updateWidthFromHeight(); updatePreviewState(); }
            public void changedUpdate(DocumentEvent e) { updateWidthFromHeight(); updatePreviewState(); }
        });
        
        titleField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePreviewState(); }
            public void removeUpdate(DocumentEvent e) { updatePreviewState(); }
            public void changedUpdate(DocumentEvent e) { updatePreviewState(); }
        });

        dpiSlider.addChangeListener(e -> {
            dpiLabel.setText("Current DPI: " + dpiSlider.getValue());
            updateInfo();
        });

        lockBtn.addActionListener(e -> {
            boolean unlocked = lockBtn.isSelected();
            widthField.setEnabled(unlocked);
            heightField.setEnabled(unlocked);
            warningLabel.setVisible(unlocked);
            if (!unlocked) {
                // reset to strictly locked scale
                double initialWidthCm = originalWidth * appState.getScale();
                double rW = round(initialWidthCm);
                String strWcm = rW - (int)rW == 0 ? String.valueOf((int)rW) : String.format(Locale.US, "%.2f", rW);
                widthField.setText(strWcm);
                updateHeightFromWidth();
            }
            updatePreviewState();
        });

        printTitleCb.addActionListener(e -> {
            titleField.setEnabled(printTitleCb.isSelected());
            updatePreviewState();
        });
        
        previewCb.addActionListener(e -> updatePreviewState());
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                if (ownerFrame instanceof ui.MainFrame) {
                    ((ui.MainFrame) ownerFrame).setExportFocusMode(true);
                }
            }
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (ownerFrame instanceof ui.MainFrame) {
                    ((ui.MainFrame) ownerFrame).setExportFocusMode(false);
                }
                appState.getCanvasState().setExportPreview(false, 1.0, null);
                canvas.repaint();
            }
        });
    }

    private void updatePreviewState() {
        if (previewCb != null && previewCb.isSelected()) {
            try {
                double wCm = Double.parseDouble(widthField.getText());
                double hCm = Double.parseDouble(heightField.getText());
                
                // Clamping for preview
                boolean isClamped = false;
                if (wCm < 10.0) { wCm = 10.0; hCm = wCm / originalAspectRatio; isClamped = true; }
                if (hCm < 10.0) { hCm = 10.0; wCm = hCm * originalAspectRatio; isClamped = true; }
                if (wCm > 120.0) { wCm = 120.0; hCm = wCm / originalAspectRatio; isClamped = true; }
                if (hCm > 120.0) { hCm = 120.0; wCm = hCm * originalAspectRatio; isClamped = true; }
                
                if (isClamped && limitWarningLabel != null) {
                    limitWarningLabel.setText(String.format(Locale.US, "Preview clamped to %.1fx%.1f cm (10-120cm limit)", wCm, hCm));
                    limitWarningLabel.setVisible(true);
                } else if (limitWarningLabel != null) {
                    limitWarningLabel.setVisible(false);
                }

                double effectiveScale = wCm / originalWidth;
                String title = printTitleCb.isSelected() ? titleField.getText() : null;
                appState.getCanvasState().setExportPreview(true, effectiveScale, title);
            } catch (Exception e) {
                if (limitWarningLabel != null) limitWarningLabel.setVisible(false);
                appState.getCanvasState().setExportPreview(false, 1.0, null);
            }
        } else {
            if (limitWarningLabel != null) limitWarningLabel.setVisible(false);
            appState.getCanvasState().setExportPreview(false, 1.0, null);
        }
        canvas.repaint();
    }

    private void updateHeightFromWidth() {
        if (isUpdating) return;
        isUpdating = true;
        try {
            double w = Double.parseDouble(widthField.getText());
            double h = w / originalAspectRatio;
            double rh = round(h);
            String hStr = rh - (int)rh == 0 ? String.valueOf((int)rh) : String.format(Locale.US,"%.1f",rh);
            heightField.setText(hStr);
            updateInfo();
        } catch (NumberFormatException ignored) {}
        isUpdating = false;
    }

    public double round(double x) {
        return Math.round(x * 10.0) / 10.0;
    }

    private void updateWidthFromHeight() {
        if (isUpdating) return;
        isUpdating = true;
        try {
            double h = Double.parseDouble(heightField.getText());
            double w = h * originalAspectRatio;
            double rw = round(w);
            String wStr = rw - (int)rw == 0 ? String.valueOf((int)rw) : String.format(Locale.US,"%.1f", rw);
            widthField.setText(wStr);
            updateInfo();
        } catch (NumberFormatException ignored) {}
        isUpdating = false;
    }

    private void updateInfo() {
        try {
            double wCm = Double.parseDouble(widthField.getText());
            double hCm = Double.parseDouble(heightField.getText());
            int dpi = dpiSlider.getValue();
            
            int pxW = (int) Math.round((wCm / 2.54) * dpi);
            int pxH = (int) Math.round((hCm / 2.54) * dpi);
            
            resultSizeLabel.setText(String.format(Locale.US, "Result size: %d x %d px (%.2f x %.2f cm)", pxW, pxH, wCm, hCm));
        } catch (Exception ignored) {}
    }

    private void performExport() {
        try {
            double wCm = Double.parseDouble(widthField.getText());
            double hCm = Double.parseDouble(heightField.getText());
            
            if (wCm < 10.0 || wCm > 120.0 || hCm < 10.0 || hCm > 120.0) {
                JOptionPane.showMessageDialog(this, 
                        "Paper width and height must be between 10 cm and 120 cm.", 
                        "Invalid Size", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int dpi = dpiSlider.getValue();

            // 1. Determine user's selected format
            boolean isPdf = formatComboBox.getSelectedIndex() == 0;
            String extension = isPdf ? ".pdf" : ".png";
            String filterDesc = isPdf ? "PDF Documents (*.pdf)" : "PNG Images (*.png)";

            // 2. Configure the FileChooser based on selection
            chooser.setDialogTitle("Save Point Map");
            chooser.resetChoosableFileFilters(); // Clear old filters
            FileNameExtensionFilter filter = new FileNameExtensionFilter(filterDesc, extension.replace(".", ""));
            chooser.setFileFilter(filter);

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                String absolutePath = file.getAbsolutePath();

                // 3. Ensure the file has the correct extension
                if (!absolutePath.toLowerCase().endsWith(extension)) {
                    file = new File(absolutePath + extension);
                }
                
                String title = printTitleCb.isSelected() ? titleField.getText() : null;

                // Use a modified SavePointMapWorker that takes these parameters
                SavePointMapWorker worker = new SavePointMapWorker(canvas, file, wCm, dpi, title);
                worker.execute();
                appState.setLastOpenedDir(file.getParent());
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}