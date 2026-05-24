package core.launcher;

import com.formdev.flatlaf.FlatLightLaf;
import ui.MainFrame;

import javax.swing.*;

public class AppLauncher {

    public static void main(String[] args) {
        // Enforce EDT initialization
        SwingUtilities.invokeLater(() -> {
            // Initialize FlatLaf globally
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception ex) {
                System.err.println("Failed to initialize FlatLaf");
            }

            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);

            // Handle file association (double click to open)
            if (args.length > 0) {
                mainFrame.openExternalFile(new java.io.File(args[0]));
            }
        });
    }
}
