package ui.dialogs;

import core.state.AppState;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsDialog extends JDialog {

    private final AppState appState;
    
    private JComboBox<String> historySizeCombo;
    private JTextField gridSizeField;
    private JTextField scaleField;
    private JRadioButton pxRadio;
    private JRadioButton cmRadio;
    private JComboBox<String> languageCombo;
    private JCheckBox roundCheck;

    public SettingsDialog(Frame owner, AppState appState) {
        super(owner, "Settings", true);
        this.appState = appState;
        
        setupUI();
        loadState();
        
        pack();
        setLocationRelativeTo(owner);
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        
        // Form Fields
        formPanel.add(new JLabel("History Stack Size:"));
        historySizeCombo = new JComboBox<>(new String[]{"15", "30", "50", "100", "200", "300"});
        formPanel.add(historySizeCombo);
        
        formPanel.add(new JLabel("Grid Size:"));
        gridSizeField = new JTextField(5);
        formPanel.add(gridSizeField);
        
        formPanel.add(new JLabel("Scale Ratio (cm/px):"));
        scaleField = new JTextField(5);
        formPanel.add(scaleField);
        
        formPanel.add(new JLabel("Distance Unit:"));
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ButtonGroup unitGroup = new ButtonGroup();
        pxRadio = new JRadioButton("Pixels");
        cmRadio = new JRadioButton("Centimeters");
        unitGroup.add(pxRadio);
        unitGroup.add(cmRadio);
        radioPanel.add(pxRadio);
        radioPanel.add(cmRadio);
        formPanel.add(radioPanel);
        
        formPanel.add(new JLabel("Rounding:"));
        roundCheck = new JCheckBox("Round Results");
        formPanel.add(roundCheck);
        
        formPanel.add(new JLabel("Language:"));
        languageCombo = new JComboBox<>(new String[]{"English", "Vietnamese"});
        formPanel.add(languageCombo);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");
        
        btnOk.addActionListener(e -> saveState());
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadState() {
        // AppState only has some of these variables currently. 
        // For history size, we could theoretically resize the deque but for now it's static in AppState init
        gridSizeField.setText(String.valueOf(appState.getGridSize()));
        scaleField.setText(String.valueOf(appState.getScale()));
        
        if (appState.isCmUnit()) {
            cmRadio.setSelected(true);
        } else {
            pxRadio.setSelected(true);
        }
        
        roundCheck.setSelected(appState.isRound());
        languageCombo.setSelectedIndex(appState.isViLang() ? 1 : 0);
    }

    private void saveState() {
        try {
            int newGridSize = Integer.parseInt(gridSizeField.getText());
            appState.setGridSize(newGridSize);
            
            float newScale = Float.parseFloat(scaleField.getText());
            appState.setScale(newScale);
            
            appState.setCmUnit(cmRadio.isSelected());
            appState.setRound(roundCheck.isSelected());
            appState.setViLang(languageCombo.getSelectedIndex() == 1);
            
            // Optionally tell the parent to repaint or rebuild UI languages
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Grid Size and Scale.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
