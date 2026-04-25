package userpackage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.Enum.MouseMode;
import userpackage.ImgFrame;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class ImgFrameTest {

    private ImgFrame imgFrame;

    @BeforeEach
    void setUp() {
        // Create a dummy image for testing
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        imgFrame = new ImgFrame(dummyImage);
    }

    @Test
    void testInitialState() {
        assertEquals(MouseMode.DRAG, imgFrame.mouseMode, "Initial mouse mode should be DRAG");
        assertTrue(imgFrame.s.isEmpty(), "Stack 's' should be empty initially");
        assertTrue(imgFrame.rs.isEmpty(), "Stack 'rs' should be empty initially");
        assertTrue(imgFrame.action.isEmpty(), "Stack 'action' should be empty initially");
        assertTrue(imgFrame.reaction.isEmpty(), "Stack 'reaction' should be empty initially");
    }

    @Test
    void testResetAll() {
        // Manipulate state
        imgFrame.mouseMode = MouseMode.STICK;
        imgFrame.s.push(new SPoint(1, 10, 10));
        imgFrame.action.push(new SPoint(1, 10, 10));

        assertFalse(imgFrame.s.isEmpty());
        assertFalse(imgFrame.action.isEmpty());

        imgFrame.resetAll();

        assertTrue(imgFrame.s.isEmpty(), "Stack 's' should be empty after resetAll");
        assertTrue(imgFrame.action.isEmpty(), "Stack 'action' should be empty after resetAll");
    }
}
