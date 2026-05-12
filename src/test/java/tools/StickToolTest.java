package tools;

import core.state.AppState;
import core.history.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.canvas.ImageCanvas;
import userpackage.SPoint;

import java.awt.event.MouseEvent;
import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.*;

class StickToolTest {

    private AppState appState;
    private ImageCanvas canvas;
    private StickTool stickTool;

    @BeforeEach
    void setUp() {
        appState = new AppState();
        canvas = new ImageCanvas(appState);
        stickTool = new StickTool();
    }

    @Test
    void testStickToolCreatesPointOnMouseReleased() {
        // Arrange
        MouseEvent releaseEvent = new MouseEvent(new JPanel(), MouseEvent.MOUSE_RELEASED, 
                System.currentTimeMillis(), 0, 100, 150, 1, false, MouseEvent.BUTTON1);

        // Act
        stickTool.onMouseReleased(releaseEvent, appState, canvas);

        // Assert
        // The tool should have pushed a command to the HistoryManager
        assertTrue(appState.getHistoryManager().canUndo(), "History manager should have an undoable command");
        
        // And the canvas state should contain exactly one point at 100, 150
        assertEquals(1, appState.getCanvasState().getStickyPoints().size());
        SPoint point = appState.getCanvasState().getStickyPoints().get(0);
        assertEquals(100 - ui.canvas.ImageCanvas.CANVAS_PADDING, point.X);
        assertEquals(150 - ui.canvas.ImageCanvas.CANVAS_PADDING, point.Y);
    }
}
