package ui.dialogs;

import core.image.crop.CropGuide;
import core.image.crop.CustomCropProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for creating a new custom crop profile.
 * Returns a CustomCropProfile on OK, or null on cancel.
 */
public class AddCustomProfileDialog extends JDialog {

    private CustomCropProfile result = null;

    private final JTextField nameField     = new JTextField(18);
    private final JTextField widthField    = new JTextField("21.0", 8);
    private final JTextField heightField   = new JTextField("29.7", 8);
    private final JComboBox<String> unitBox = new JComboBox<>(new String[]{"cm", "px"});
    private final JComboBox<String> guideBox = new JComboBox<>(new String[]{"Rule of Thirds", "Crosshair"});

    public AddCustomProfileDialog(Frame owner) {
        super(owner, "Add Custom Crop Profile", true);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
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

        content.add(form, BorderLayout.CENTER);

        // ----- Buttons -----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton okBtn     = new JButton("Add");
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

        CustomCropProfile.Unit unit = unitBox.getSelectedIndex() == 0
                ? CustomCropProfile.Unit.CM
                : CustomCropProfile.Unit.PX;

        CropGuide guide = guideBox.getSelectedIndex() == 0
                ? CropGuide.RULE_OF_THIRDS
                : CropGuide.CROSSHAIR;

        result = CustomCropProfile.create(name, w, h, unit, guide);
        dispose();
    }

    /** Returns the newly created profile, or null if the user cancelled. */
    public CustomCropProfile getResult() {
        return result;
    }
}
