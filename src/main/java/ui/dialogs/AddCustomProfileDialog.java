package ui.dialogs;

import core.image.crop.CropGuide;
import core.image.crop.CustomCropProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for creating or editing a custom crop profile.
 * Returns a CustomCropProfile on OK, or null on cancel.
 */
public class AddCustomProfileDialog extends JDialog {

    private CustomCropProfile result = null;
    private final boolean editMode;

    private final JTextField nameField     = new JTextField(18);
    private final JTextField widthField    = new JTextField("21.0", 8);
    private final JTextField heightField   = new JTextField("29.7", 8);
    private final JComboBox<String> unitBox = new JComboBox<>(new String[]{"cm", "px"});
    private final JComboBox<String> guideBox = new JComboBox<>(new String[]{"Rule of Thirds", "Crosshair"});
    private final JTextArea descArea;

    {
        descArea = new JTextArea(2, 18);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        // Limit input to MAX_DESCRIPTION_LENGTH characters
        descArea.setDocument(new javax.swing.text.PlainDocument() {
            @Override
            public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str == null) return;
                int max = CustomCropProfile.MAX_DESCRIPTION_LENGTH;
                int current = getLength();
                int allowed = max - current;
                if (allowed <= 0) return;
                if (str.length() > allowed) str = str.substring(0, allowed);
                super.insertString(offs, str, a);
            }
        });
    }

    public AddCustomProfileDialog(Frame owner, CustomCropProfile profile) {
        super(owner, profile == null ? "Add Custom Crop Profile" : "Edit Crop Profile", true);
        this.editMode = (profile != null);
        if (profile != null) prefill(profile);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    /** Pre-fill all form fields from an existing profile. */
    private void prefill(CustomCropProfile p) {
        nameField.setText(p.name);
        // Restore exact values the user originally entered
        widthField.setText(formatDimension(p.width));
        heightField.setText(formatDimension(p.height));
        unitBox.setSelectedIndex(p.unit == CustomCropProfile.Unit.CM ? 0 : 1);
        guideBox.setSelectedIndex(p.guide == CropGuide.RULE_OF_THIRDS ? 0 : 1);
        if (!p.description.isEmpty()) {
            try {
                descArea.getDocument().insertString(0, p.description, null);
            } catch (javax.swing.text.BadLocationException ignored) {}
        }
    }

    /** Format a dimension value: show as integer when it has no fractional part. */
    private static String formatDimension(float v) {
        return (v == Math.floor(v) && !Float.isInfinite(v))
                ? String.valueOf((int) v)
                : String.valueOf(v);
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(16, 16, 12, 16));

        // ----- Form grid -----
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(4, 0, 4, 8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.anchor = GridBagConstraints.WEST;
        fc.insets = new Insets(4, 0, 4, 0);
        fc.fill   = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;

        int row = 0;

        lc.gridx = 0; lc.gridy = row;
        fc.gridx = 1; fc.gridy = row++;
        form.add(new JLabel("Profile name:"), lc);
        form.add(nameField, fc);

        lc.gridy = row;
        fc.gridy = row++;
        form.add(new JLabel("Width:"), lc);
        JPanel wPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wPanel.add(widthField);
        wPanel.add(Box.createHorizontalStrut(6));
        wPanel.add(unitBox);
        form.add(wPanel, fc);

        lc.gridy = row;
        fc.gridy = row++;
        form.add(new JLabel("Height:"), lc);
        form.add(heightField, fc);

        lc.gridy = row;
        fc.gridy = row++;
        form.add(new JLabel("Guide:"), lc);
        form.add(guideBox, fc);

        // Description row
        lc.gridy = row;
        fc.gridy = row;
        lc.anchor = GridBagConstraints.NORTHWEST; // top-align label with textarea
        form.add(new JLabel("Description:"), lc);
        JScrollPane descScroll = new JScrollPane(descArea,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        form.add(descScroll, fc);
        row++;

        // Hint text below description
        GridBagConstraints hc = new GridBagConstraints();
        hc.gridx = 1; hc.gridy = row++;
        hc.anchor = GridBagConstraints.WEST;
        hc.insets = new Insets(0, 0, 4, 0);
        JLabel hint = new JLabel("<html><small>This text will be shown on the crop frame when this profile is selected. Max 80 characters.</small></html>");
        hint.setForeground(UIManager.getColor("Label.disabledForeground"));
        form.add(hint, hc);

        content.add(form, BorderLayout.CENTER);

        // ----- Buttons -----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton okBtn     = new JButton(editMode ? "Save" : "Add");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> onOk());
        cancelBtn.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(okBtn);
        btnPanel.add(cancelBtn);
        btnPanel.add(okBtn);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void onOk() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a profile name.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        float w, h;
        try {
            w = Float.parseFloat(widthField.getText().trim());
            h = Float.parseFloat(heightField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Width and height must be valid numbers.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (w <= 0 || h <= 0) {
            JOptionPane.showMessageDialog(this, "Width and height must be greater than zero.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        float ratio = w / h;
        if (ratio < 0.1f || ratio > 10.0f) {
            JOptionPane.showMessageDialog(this, "Aspect ratio is too extreme (must be between 0.1 and 10.0).", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CustomCropProfile.Unit unit = unitBox.getSelectedIndex() == 0
                ? CustomCropProfile.Unit.CM
                : CustomCropProfile.Unit.PX;

        CropGuide guide = guideBox.getSelectedIndex() == 0
                ? CropGuide.RULE_OF_THIRDS
                : CropGuide.CROSSHAIR;

        result = CustomCropProfile.create(name, w, h, unit, guide, descArea.getText().trim());
        dispose();
    }

    /** Returns the newly created profile, or null if the user cancelled. */
    public CustomCropProfile getResult() {
        return result;
    }
}
