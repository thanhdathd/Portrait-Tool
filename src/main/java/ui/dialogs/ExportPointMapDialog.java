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
    private JComboBox<String> formatComboBox; // NEW: JComboBox for format
    private JFileChooser chooser;

    private boolean isUpdating = false;

    public ExportPointMapDialog(Frame owner, ImageCanvas canvas, AppState appState, JFileChooser chooser) {
        super(owner, "Export Point Map Settings", true);
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
        setSize(400, 450); // Increased height slightly to accommodate the new combobox
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

        // 2. Format Selection Section (NEW)
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Export Format:"), gbc);

        formatComboBox = new JComboBox<>(new String[]{"PDF Document (*.pdf)", "PNG Image (*.png)"});
        gbc.gridx = 1;
        mainPanel.add(formatComboBox, gbc);

        // 3. DPI Section
        gbc.gridx = 0; gbc.gridy = 3;
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

        gbc.gridy = 4;
        mainPanel.add(dpiSlider, gbc);

        dpiLabel = new JLabel("Current DPI: 300");
        dpiLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 5;
        mainPanel.add(dpiLabel, gbc);

        // 4. Result Info Section
        resultSizeLabel = new JLabel("Result size: 0 x 0 px");
        resultSizeLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        gbc.gridy = 6;
        mainPanel.add(resultSizeLabel, gbc);

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
            public void insertUpdate(DocumentEvent e) { updateHeightFromWidth(); }
            public void removeUpdate(DocumentEvent e) { updateHeightFromWidth(); }
            public void changedUpdate(DocumentEvent e) { updateHeightFromWidth(); }
        });

        heightField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateWidthFromHeight(); }
            public void removeUpdate(DocumentEvent e) { updateWidthFromHeight(); }
            public void changedUpdate(DocumentEvent e) { updateWidthFromHeight(); }
        });

        dpiSlider.addChangeListener(e -> {
            dpiLabel.setText("Current DPI: " + dpiSlider.getValue());
            updateInfo();
        });
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
                
                // Use a modified SavePointMapWorker that takes these parameters
                SavePointMapWorker worker = new SavePointMapWorker(canvas, file, wCm, dpi);
                worker.execute();
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}