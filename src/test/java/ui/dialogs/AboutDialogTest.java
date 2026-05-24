package ui.dialogs;

import org.junit.jupiter.api.Test;
import javax.swing.JFrame;
import static org.junit.jupiter.api.Assertions.*;

class AboutDialogTest {

    @Test
    void testAboutDialogInitialization() {
        JFrame parent = new JFrame();
        parent.setVisible(true);
        try {
            AboutDialog dialog = new AboutDialog(parent);
            assertEquals("About Portrait Tool", dialog.getTitle());
            assertTrue(dialog.isModal(), "AboutDialog should be modal");
            assertNotNull(getClass().getResource("/icons/app_logo.svg"), "App logo resource must exist");
        } finally {
            parent.dispose();
        }
    }
}
