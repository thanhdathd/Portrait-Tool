package tools;

import core.state.AppState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.canvas.ImageCanvas;
import core.state.SPoint;

import java.awt.event.MouseEvent;
import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.*;

class GridToolTest {

    private AppState appState;
    private ImageCanvas canvas;
    private GridTool gridTool;

    @BeforeEach
    void setUp() {
        appState = new AppState();
        canvas = new ImageCanvas(appState);
        gridTool = new GridTool(); // 40 is the grid size
    }

    @Test
    void testGridToolCreatesGridOnMouseReleased() {
        // Arrange
        MouseEvent releaseEvent = new MouseEvent(new JPanel(), MouseEvent.MOUSE_RELEASED, 
                System.currentTimeMillis(), 0, 200, 250, 1, false, MouseEvent.BUTTON1);

        // Act
        gridTool.onMouseMoved(releaseEvent, appState, canvas);
        gridTool.onMouseReleased(releaseEvent, appState, canvas);

        // Assert
        assertTrue(appState.getHistoryManager().canUndo(), "History manager should have an undoable command");
        
        assertEquals(1, appState.getCanvasState().getGrids().size());
        SPoint grid = appState.getCanvasState().getGrids().get(0);
        assertEquals(40, grid.id); // Storing size in ID as per legacy pattern
        assertEquals(200 - ui.canvas.ImageCanvas.CANVAS_PADDING, grid.X);
        assertEquals(160, grid.Y); // Snapped from 170 (250-80) to nearest 40 multiple
    }
}
