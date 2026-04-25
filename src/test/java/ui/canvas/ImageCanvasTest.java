package ui.canvas;

import core.state.AppState;
import tools.StickTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import userpackage.SPoint;

import java.awt.Color;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.*;

class ImageCanvasTest {

    private AppState appState;
    private ImageCanvas canvas;

    @BeforeEach
    void setUp() {
        appState = new AppState();
        canvas = new ImageCanvas(appState);
    }

    @Test
    void testActiveToolDelegation() {
        // Arrange
        StickTool stickTool = new StickTool();
        canvas.setActiveTool(stickTool);

        // Act
        MouseEvent releaseEvent = new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, 
                System.currentTimeMillis(), 0, 100, 150, 1, false, MouseEvent.BUTTON1);
        canvas.getMouseListeners()[0].mouseReleased(releaseEvent); // Simulate event firing

        // Assert
        assertEquals(1, appState.getCanvasState().getStickyPoints().size(), 
            "ImageCanvas should delegate mouse events to active StickTool");
    }
}
