package ui.dialogs;

import com.formdev.flatlaf.FlatClientProperties;
import core.state.AppState;
import net.miginfocom.swing.MigLayout;
import ui.button.RoundToggleButton;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {

    private final AppState appState;
    
    private JComboBox<String> historySizeCombo;
    private JTextField gridSizeField;
    private JComboBox<String> gridUnitCombo;
    private JTextField scaleField;
    private JRadioButton pxRadio;
    private JRadioButton cmRadio;
    private JComboBox<String> languageCombo;
    private JCheckBox roundCheck;
    private JCheckBox showHelpCheck;
    private JCheckBox autoSaveCheck;
    private JSpinner autoSaveIntervalSpinner;
    private String[] comboItems = new String[]{"15", "30", "50", "100", "200", "300"};

    // Segmented control for checker size
    private static final int[] CHECKER_SIZES  = {20, 40, 80};
    private static final String[] CHECKER_LABELS = {"Small", "Medium", "Large"};
    private final JToggleButton[] checkerBtns = new JToggleButton[CHECKER_SIZES.length];
    private final ButtonGroup checkerGroup = new ButtonGroup();

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
        historySizeCombo = new JComboBox<>(comboItems);
        formPanel.add(historySizeCombo);
        
        formPanel.add(new JLabel("Grid Size:"));
        gridSizeField = new JTextField(5);
        gridUnitCombo = new JComboBox<>(new String[]{"px", "cm"});
        formPanel.add(gridSizeField, "split 2, pushx, growx");
        formPanel.add(gridUnitCombo, "w 60!");
        
        formPanel.add(new JLabel("Scale Ratio (cm/px):"));
        scaleField = new JTextField(5);
        formPanel.add(scaleField, "split 2, pushx, growx");

        JButton calcBtn = new JButton("...");
        calcBtn.setToolTipText("Mở bộ tính tỷ lệ tự động");
        calcBtn.setFocusable(false);
        formPanel.add(calcBtn, "w 30!");
        calcBtn.addActionListener(e -> {
            // Mở sub-dialog và truyền scaleField vào để nó tự điền kết quả
            ScaleCalculatorDialog calcDialog = new ScaleCalculatorDialog(
                    (JDialog) SwingUtilities.getWindowAncestor(formPanel),
                    scaleField
            );
            calcDialog.setVisible(true);
        });
        
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
        roundCheck.setToolTipText("Round p2p measure result when convert from px to cm");
        formPanel.add(roundCheck);

        formPanel.add(new JLabel("Show help:"));
        showHelpCheck = new JCheckBox("Show help in crop tool");
        formPanel.add(showHelpCheck);

        formPanel.add(new JLabel("Checker size:"));
        // Use a small vertical gap (2px) in FlowLayout to prevent top/bottom clipping
        JPanel checkerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        checkerPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#f2f2f2")));
        checkerPanel.getInsets().set(2,2,2,2);
        checkerPanel.setOpaque(false);

        for (int i = 0; i < CHECKER_SIZES.length; i++) {
            final int idx = i;
            RoundToggleButton btn = new RoundToggleButton(CHECKER_LABELS[i]);
            btn.setFocusable(false);

            int width = (i == 1) ? 80 : 60;
            // Increased height to 26 to ensure the border isn't tight against the edge
            btn.setPreferredSize(new Dimension(width, 20));

            if (i == 0) {
                btn.setCornerRadius(10, 0, 0, 10);
            } else if (i == CHECKER_SIZES.length - 1) {
                btn.setCornerRadius(0, 10, 10, 0);
            }
            btn.addActionListener(e -> {
                int checkerSize = CHECKER_SIZES[idx];
                appState.setCheckerSize(checkerSize);
            });

            checkerBtns[i] = btn;
            checkerGroup.add(btn);
            checkerPanel.add(btn);
        }
        // Add gapx 2 to prevent the leftmost edge from touching the label/edge
        formPanel.add(checkerPanel, "gapx 2");

        formPanel.add(new JLabel("Language:"));
        languageCombo = new JComboBox<>(new String[]{"English", "Vietnamese"});
        formPanel.add(languageCombo);

        formPanel.add(new JLabel("Auto Save:"), "gap top 10");
        autoSaveCheck = new JCheckBox("Enable Auto Save");
        formPanel.add(autoSaveCheck, "gap top 10");

        formPanel.add(new JLabel("Save Interval (min):"));
        autoSaveIntervalSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        formPanel.add(autoSaveIntervalSpinner, "w 60!");
        
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
        int stackSize = appState.getStackSize();
        for (int i = 0; i < comboItems.length; i++) {
            if(String.valueOf(stackSize).equals(comboItems[i])) {
                historySizeCombo.setSelectedIndex(i);
                break;
            }
        }
        float gs = appState.getGridSize();
        gridSizeField.setText(gs == (int) gs ? String.valueOf((int) gs) : String.valueOf(gs));
        gridUnitCombo.setSelectedIndex(appState.isGridInCm() ? 1 : 0);
        scaleField.setText(String.valueOf(appState.getScale()));
        
        if (appState.isCmUnit()) {
            cmRadio.setSelected(true);
        } else {
            pxRadio.setSelected(true);
        }
        
        roundCheck.setSelected(appState.isRound());
        showHelpCheck.setSelected(appState.isShowCropHelp());
        languageCombo.setSelectedIndex(appState.isViLang() ? 1 : 0);
        autoSaveCheck.setSelected(appState.isAutoSaveEnabled());
        autoSaveIntervalSpinner.setValue(appState.getAutoSaveInterval());

        // Select the checker-size button that matches the current value
        int currentCheckerSize = appState.getCheckerSize();
        int matchIdx = 1; // default to Medium
        for (int i = 0; i < CHECKER_SIZES.length; i++) {
            if (CHECKER_SIZES[i] == currentCheckerSize) { matchIdx = i; break; }
        }
        checkerBtns[matchIdx].setSelected(true);
    }

    private void saveState() {
        try {
            float newGridSize = Float.parseFloat(gridSizeField.getText());
            appState.setGridSize(newGridSize);
            appState.setGridInCm(gridUnitCombo.getSelectedIndex() == 1);
            appState.resizeHistoryStack(Integer.parseInt((String) historySizeCombo.getSelectedItem()));
            
            float newScale = Float.parseFloat(scaleField.getText());
            appState.setScale(newScale);
            
            appState.setCmUnit(cmRadio.isSelected());
            appState.setRound(roundCheck.isSelected());
            appState.setShowCropHelp(showHelpCheck.isSelected());
            appState.setViLang(languageCombo.getSelectedIndex() == 1);
            appState.setAutoSaveEnabled(autoSaveCheck.isSelected());
            appState.setAutoSaveInterval((Integer) autoSaveIntervalSpinner.getValue());

            // Optionally tell the parent to repaint or rebuild UI languages
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Grid Size and Scale.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
