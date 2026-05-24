package ui.components;

import org.junit.jupiter.api.Test;
import javax.swing.JFrame;
import static org.junit.jupiter.api.Assertions.*;

class ToastNotificationTest {

    @Test
    void testFindMainFrameReturnsActiveVisibleFrame() {
        JFrame frame = new JFrame("Test Frame");
        frame.setVisible(true);
        try {
            JFrame resolved = ToastNotification.findMainFrame();
            assertNotNull(resolved, "Should find the active frame");
            assertEquals("Test Frame", resolved.getTitle());
        } finally {
            frame.dispose();
        }
    }
}
