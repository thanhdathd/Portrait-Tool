package ui.dialogs;

import core.image.ImageResizer;
import net.miginfocom.swing.MigLayout;
import transform.ResizeBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ResizeDialog extends JDialog {

    private final BufferedImage sourceImage;
    private final BiConsumer<BufferedImage, ResizeProps> onApply;

    private JTextField widthField;
    private JTextField heightField;
    private JComboBox<String> unitBox;
    private JCheckBox lockAspectRatio;
    private JComboBox<String> interpolationBox;
    
    private boolean isUpdating = false;

    public ResizeDialog(Frame owner, BufferedImage sourceImage, BiConsumer<BufferedImage, ResizeProps> onApply) {
        super(owner, "Resize Image", true);
        this.sourceImage = sourceImage;
        this.onApply = onApply;
        
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new MigLayout("wrap 2, insets 15", "[right][grow,fill]"));

        widthField = new JTextField(String.valueOf(sourceImage.getWidth()), 10);
        heightField = new JTextField(String.valueOf(sourceImage.getHeight()), 10);
        
        unitBox = new JComboBox<>(new String[]{"Pixels", "Percent"});
        lockAspectRatio = new JCheckBox("Lock Aspect Ratio", true);
        
        interpolationBox = new JComboBox<>(new String[]{
            "Nearest Neighbor", "Bilinear", "Bicubic"
        });
        interpolationBox.setSelectedIndex(2);

        unitBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateFieldsForUnit();
            }
        });

        widthField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { syncSize(true); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { syncSize(true); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { syncSize(true); }
        });

        heightField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { syncSize(false); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { syncSize(false); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { syncSize(false); }
        });

        JButton applyBtn = new JButton("Apply");
        applyBtn.addActionListener(e -> applyResize());
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        add(new JLabel("Width:"));
        add(widthField);
        
        add(new JLabel("Height:"));
        add(heightField);
        
        add(new JLabel("Unit:"));
        add(unitBox);
        
        add(new JLabel(""), "span 1");
        add(lockAspectRatio);
        
        add(new JLabel("Interpolation:"));
        add(interpolationBox);
        
        add(applyBtn, "span 2, split 2, right");
        add(cancelBtn, "right");
    }

    private void updateFieldsForUnit() {
        isUpdating = true;
        if (unitBox.getSelectedIndex() == 0) { // Pixels
            widthField.setText(String.valueOf(sourceImage.getWidth()));
            heightField.setText(String.valueOf(sourceImage.getHeight()));
        } else { // Percent
            widthField.setText("100");
            heightField.setText("100");
        }
        isUpdating = false;
    }

    private void syncSize(boolean fromWidth) {
        if (isUpdating || !lockAspectRatio.isSelected()) return;
        
        try {
            double ratio = (double) sourceImage.getHeight() / sourceImage.getWidth();
            isUpdating = true;
            if (fromWidth) {
                int w = Integer.parseInt(widthField.getText());
                int h;
                if (unitBox.getSelectedIndex() == 1) { // Percent
                    h = w;
                } else {
                    h = (int) Math.round(w * ratio);
                }
                heightField.setText(String.valueOf(h));
            } else {
                int h = Integer.parseInt(heightField.getText());
                int w;
                if (unitBox.getSelectedIndex() == 1) { // Percent
                    w = h;
                } else {
                    w = (int) Math.round(h / ratio);
                }
                widthField.setText(String.valueOf(w));
            }
        } catch (NumberFormatException ignored) {}
        finally { isUpdating = false; }
    }

    private void applyResize() {
        try {
            int w = Integer.parseInt(widthField.getText());
            int h = Integer.parseInt(heightField.getText());
            
            if (unitBox.getSelectedIndex() == 1) { // Percent
                w = (int) Math.round(sourceImage.getWidth() * (w / 100.0));
                h = (int) Math.round(sourceImage.getHeight() * (h / 100.0));
            }
            
            if (w <= 0 || h <= 0) {
                JOptionPane.showMessageDialog(this, "Dimensions must be positive.");
                return;
            }

            Object hint = switch (interpolationBox.getSelectedIndex()) {
                case 0 -> RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                case 1 -> RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                default -> RenderingHints.VALUE_INTERPOLATION_BICUBIC;
            };

            BufferedImage newImg = ImageResizer.resize(sourceImage, w, h, hint);
            ResizeProps props = new ResizeProps(w, h, interpolationBox.getSelectedIndex());
            onApply.accept(newImg, props);
            dispose();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.");
        }
    }

    public class ResizeProps {
        public int  width;
        public int height;
        public int hint;

        public ResizeProps(int width, int height, int hint) {
            this.width = width;
            this.height = height;
            this.hint = hint;
        }
    }
}
