package ui.dialogs;

import config.ConfigManager;
import config.CustomCropProfileManager;
import core.image.crop.CustomCropProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialog for viewing, adding, editing, and deleting custom crop profiles.
 */
public class ManageProfilesDialog extends JDialog {

    private final CustomCropProfileManager manager;
    private final ConfigManager configManager;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> profileList = new JList<>(listModel);

    public ManageProfilesDialog(Frame owner, ConfigManager configManager) {
        super(owner, "Manage Custom Crop Profiles", true);
        this.configManager = configManager;
        this.manager = configManager.getCustomCropProfileManager();
        buildUI();
        refreshList();
        pack();
        setMinimumSize(new Dimension(360, 300));
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(14, 14, 12, 14));

        // List of profiles
        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profileList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(profileList);
        scroll.setPreferredSize(new Dimension(330, 200));
        content.add(scroll, BorderLayout.CENTER);

        // Buttons column on the right
        JPanel btnCol = new JPanel(new GridLayout(4, 1, 0, 6));
        JButton addBtn    = new JButton("Add…");
        JButton editBtn   = new JButton("Edit…");
        JButton deleteBtn = new JButton("Delete");
        JButton closeBtn  = new JButton("Close");

        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());
        closeBtn.addActionListener(e -> dispose());

        btnCol.add(addBtn);
        btnCol.add(editBtn);
        btnCol.add(deleteBtn);
        btnCol.add(closeBtn);
        content.add(btnCol, BorderLayout.EAST);

        setContentPane(content);
    }

    private void refreshList() {
        listModel.clear();
        List<CustomCropProfile> profiles = manager.getProfiles();
        for (CustomCropProfile p : profiles) {
            String ratio = String.format("%.3f", p.ratio);
            String guideLabel = p.guide == core.image.crop.CropGuide.RULE_OF_THIRDS ? "Thirds" : "Cross";
            listModel.addElement(String.format("%-20s  ratio: %s  [%s]", p.name, ratio, guideLabel));
        }
    }

    private void onAdd() {
        AddCustomProfileDialog dlg = new AddCustomProfileDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        CustomCropProfile newProfile = dlg.getResult();
        if (newProfile != null) {
            manager.add(newProfile);
            saveConfig();
            refreshList();
        }
    }

    private void onEdit() {
        int idx = profileList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Please select a profile to edit.");
            return;
        }
        CustomCropProfile existing = manager.getProfiles().get(idx);
        AddCustomProfileDialog dlg = new AddCustomProfileDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), existing);
        dlg.setVisible(true);
        CustomCropProfile updated = dlg.getResult();
        if (updated != null) {
            manager.update(idx, updated);
            saveConfig();
            refreshList();
            profileList.setSelectedIndex(idx);
        }
    }

    private void onDelete() {
        int idx = profileList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Please select a profile to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete selected profile?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.remove(idx);
            saveConfig();
            refreshList();
        }
    }

    /**
     * Triggers a save-only cycle. We pass a dummy AppState snapshot — in reality we need
     * the real AppState. The caller should pass it; for now the configManager.save() 
     * is called via the dedicated crop-profile save path below so we avoid full AppState dependency.
     */
    private void saveConfig() {
        // CustomCropProfileManager is already wired; call a lightweight "save profiles only" helper.
        // Since ConfigManager.save() requires an AppState we can't access here, we expose
        // a dedicated method in ConfigManager for this purpose.
        configManager.saveCropProfiles();
    }
}
