package tools;

import core.state.AppState;
import core.history.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.canvas.ImageCanvas;
import core.state.SPoint;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SelectToolTest {

    private AppState appState;
    private ImageCanvas canvas;
    private SelectTool selectTool;

    @BeforeEach
    void setUp() {
        appState = new AppState();
        canvas = new ImageCanvas(appState);
        selectTool = new SelectTool();
        canvas.setActiveTool(selectTool);
    }

    @Test
    void testDragSelectedPoint() {
        // Create and add a point
        SPoint point = new SPoint(1, 100, 100);
        appState.getCanvasState().addStickyPoint(point);

        // Select the point
        appState.getCanvasState().setSelectedPoint(point);
        assertEquals(1, appState.getCanvasState().getSelectedPoints().size());

        // Press mouse on the point (image coord: 100, 100 -> screen coord: 100 + 80 = 180)
        MouseEvent pressEvent = new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0, 180, 180, 1, false, MouseEvent.BUTTON1);
        selectTool.onMousePressed(pressEvent, appState, canvas);

        // Track selection listener invocations
        final int[] listenerCallCount = {0};
        appState.getCanvasState().setPointSelectionListener(points -> {
            listenerCallCount[0]++;
            if (!points.isEmpty()) {
                SPoint p = points.iterator().next();
                // Ensure coordinate values are correct at the time the listener is notified
                assertEquals(120, p.X);
                assertEquals(130, p.Y);
            }
        });

        // Drag the mouse to a new position (image coord: 120, 130 -> screen coord: 120 + 80 = 200, 130 + 80 = 210)
        MouseEvent dragEvent = new MouseEvent(canvas, MouseEvent.MOUSE_DRAGGED,
                System.currentTimeMillis(), 0, 200, 210, 1, false, MouseEvent.BUTTON1);
        selectTool.onMouseDragged(dragEvent, appState, canvas);

        // Verify position updated in-flight and listener fired
        assertEquals(120, point.X);
        assertEquals(130, point.Y);
        assertTrue(listenerCallCount[0] > 0, "Point selection listener should fire during dragging");

        // Release mouse
        MouseEvent releaseEvent = new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(), 0, 200, 210, 1, false, MouseEvent.BUTTON1);
        selectTool.onMouseReleased(releaseEvent, appState, canvas);

        // Verify position is final
        assertEquals(120, point.X);
        assertEquals(130, point.Y);

        // Verify history command is pushed
        assertFalse(appState.getHistoryManager().getUndoStack().isEmpty());

        // Set up tracking flags for undo/redo assertions
        final int[] undoRedoNotifiedCount = {0};
        final int[] expectedCoords = {100, 100}; // initially expect undo position

        appState.getCanvasState().setPointSelectionListener(points -> {
            undoRedoNotifiedCount[0]++;
            if (!points.isEmpty()) {
                SPoint p = points.iterator().next();
                assertEquals(expectedCoords[0], p.X);
                assertEquals(expectedCoords[1], p.Y);
            }
        });

        // Undo the move
        expectedCoords[0] = 100;
        expectedCoords[1] = 100;
        appState.getHistoryManager().undo();
        assertEquals(100, point.X);
        assertEquals(100, point.Y);

        // Redo the move
        expectedCoords[0] = 120;
        expectedCoords[1] = 130;
        appState.getHistoryManager().redo();
        assertEquals(120, point.X);
        assertEquals(130, point.Y);

        assertTrue(undoRedoNotifiedCount[0] >= 2, "Selection listener should be triggered on undo and redo");
    }
}
