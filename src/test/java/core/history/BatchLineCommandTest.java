package core.history;

import core.state.AppState;
import core.state.CanvasState;
import ui.canvas.ImageCanvas;
import userpackage.SLine;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BatchLineCommandTest {
    @Test
    void testBatchEditAndUndo() {
        AppState appState = new AppState();
        CanvasState canvasState = appState.getCanvasState();
        ImageCanvas canvas = new ImageCanvas(appState) {
            @Override public void repaint() {}
        };

        SLine line1 = new SLine(1, new Point(0, 0), new Point(10, 10), 2, Color.BLACK);
        SLine line2 = new SLine(2, new Point(20, 20), new Point(30, 30), 2, Color.BLACK);
        canvasState.getLines().add(line1);
        canvasState.getLines().add(line2);

        List<BatchLineCommand.LineStatePair> pairs = new ArrayList<>();
        
        SLine line1New = line1.copy();
        line1New.strokeColor = Color.RED;
        line1New.startPoint.setLocation(5, 5);
        pairs.add(new BatchLineCommand.LineStatePair(line1, line1.copy(), line1New));

        SLine line2New = line2.copy();
        line2New.strokeColor = Color.RED;
        line2New.startPoint.setLocation(25, 25);
        pairs.add(new BatchLineCommand.LineStatePair(line2, line2.copy(), line2New));

        BatchLineCommand cmd = new BatchLineCommand(canvasState, canvas, BatchLineCommand.Action.EDIT, pairs);
        cmd.execute();

        assertEquals(Color.RED, line1.strokeColor);
        assertEquals(new Point(5, 5), line1.startPoint);
        assertEquals(Color.RED, line2.strokeColor);
        assertEquals(new Point(25, 25), line2.startPoint);

        cmd.undo();

        assertEquals(Color.BLACK, line1.strokeColor);
        assertEquals(new Point(0, 0), line1.startPoint);
        assertEquals(Color.BLACK, line2.strokeColor);
        assertEquals(new Point(20, 20), line2.startPoint);
    }
}
