package ui.dialogs;

import filter.FilterProperties;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import workers.FilterWorker;

public class FilterDialog extends JDialog {

    private final BufferedImage originalImage;
    private final Consumer<BufferedImage> onApply;

    private JSlider redSlider, greenSlider, blueSlider, alphaSlider, graySlider;
    private JComboBox<String> filterModeCombo;
    private JLabel previewLabel;
    
    private FilterWorker currentWorker; // Keep track to avoid too many running

    public FilterDialog(Frame owner, BufferedImage image, Consumer<BufferedImage> onApply) {
        super(owner, "Image Filters", false); // Non-modal so user can see main canvas
        this.originalImage = image;
        this.onApply = onApply;

        setupUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        // Preview Area
        previewLabel = new JLabel();
        previewLabel.setPreferredSize(new Dimension(300, 300));
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // Initial preview (scaled down if needed)
        if (originalImage != null) {
            Image scaled = originalImage.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            previewLabel.setIcon(new ImageIcon(scaled));
        }
        
        add(new JScrollPane(previewLabel), BorderLayout.CENTER);

        // Controls Area
        JPanel controlsPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        redSlider = createSlider("Red", 0, 200, 100, controlsPanel);
        greenSlider = createSlider("Green", 0, 200, 100, controlsPanel);
        blueSlider = createSlider("Blue", 0, 200, 100, controlsPanel);
        alphaSlider = createSlider("Alpha", 0, 100, 100, controlsPanel);
        graySlider = createSlider("Gray Threshold", 0, 255, 128, controlsPanel);

        // Mode Combo
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.add(new JLabel("Mode:"));
        filterModeCombo = new JComboBox<>(new String[]{"RGB", "Black & White"});
        filterModeCombo.addActionListener(e -> firePreviewUpdate());
        modePanel.add(filterModeCombo);
        controlsPanel.add(modePanel);

        add(controlsPanel, BorderLayout.EAST);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApply = new JButton("Apply");
        JButton btnCancel = new JButton("Cancel");

        btnApply.addActionListener(e -> {
            btnApply.setEnabled(false);
            btnApply.setText("Applying...");
            
            // Run full apply in background
            new FilterWorker(originalImage, getCurrentProperties(), result -> {
                if (onApply != null) {
                    onApply.accept(result);
                }
                dispose();
            }).execute();
        });

        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnApply);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JSlider createSlider(String name, int min, int max, int value, JPanel parent) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(new JLabel(name), BorderLayout.WEST);
        
        JSlider slider = new JSlider(min, max, value);
        slider.setMajorTickSpacing((max - min) / 2);
        slider.setPaintTicks(true);
        slider.addChangeListener(e -> firePreviewUpdate());
        
        panel.add(slider, BorderLayout.CENTER);
        
        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setPreferredSize(new Dimension(30, 20));
        slider.addChangeListener((ChangeEvent e) -> valueLabel.setText(String.valueOf(slider.getValue())));
        panel.add(valueLabel, BorderLayout.EAST);
        
        parent.add(panel);
        return slider;
    }

    private FilterProperties getCurrentProperties() {
        int r = redSlider.getValue();
        int g = greenSlider.getValue();
        int b = blueSlider.getValue();
        int a = alphaSlider.getValue();
        int gray = graySlider.getValue();
        int mode = filterModeCombo.getSelectedIndex() == 0 ? 0 : -1; // 0 for RGB, -1 for BW in legacy
        
        return new FilterProperties(r, g, b, a, gray, mode);
    }

    private void firePreviewUpdate() {
        if (originalImage == null) return;
        
        FilterProperties props = getCurrentProperties();
        
        // Cancel the old worker if it's still running
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
        
        // Scale down original image for faster live preview processing
        Image scaledSource = originalImage.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
        BufferedImage previewSource = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = previewSource.createGraphics();
        g2.drawImage(scaledSource, 0, 0, null);
        g2.dispose();
        
        // Spawn a background worker just for the live preview window
        currentWorker = new FilterWorker(previewSource, props, resultImage -> {
            previewLabel.setIcon(new ImageIcon(resultImage));
        });
        currentWorker.execute();
    }
}
