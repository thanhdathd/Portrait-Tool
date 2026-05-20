package core.history;

import core.image.ImageTransformUtils.TransformType;
import core.state.AppState;
import core.state.CanvasState;
import ui.canvas.ImageCanvas;
import core.state.SLine;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TransformCommandTest {

    @Test
    void testLineTransformationAndUndo() {
        // 1. Setup real AppState and canvas
        AppState appState = new AppState();
        CanvasState canvasState = appState.getCanvasState();
        BufferedImage oldImage = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
        BufferedImage newImage = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
        
        ImageCanvas canvas = new ImageCanvas(appState) {
            private BufferedImage bg;
            @Override
            public void setBackgroundImage(BufferedImage img) {
                this.bg = img;
            }
            @Override
            public BufferedImage getBackgroundImage() {
                return this.bg;
            }
            @Override
            public void repaint() {}
        };
        canvas.setBackgroundImage(oldImage);

        // 2. Add a line: start (10, 20), end (30, 40)
        SLine line = new SLine(1, new Point(10, 20), new Point(30, 40), 2, java.awt.Color.BLACK);
        canvasState.getLines().add(line);

        // 3. Transform flip horizontal (FLIP_H) on width = 100
        // Expected X: w - 1 - x
        // start X: 100 - 1 - 10 = 89
        // end X: 100 - 1 - 30 = 69
        // Y remains same: start Y = 20, end Y = 40
        TransformCommand flipHCmd = new TransformCommand(canvas, canvasState, oldImage, newImage, TransformType.FLIP_H);
        flipHCmd.execute();

        assertEquals(89, line.startPoint.x);
        assertEquals(20, line.startPoint.y);
        assertEquals(69, line.endPoint.x);
        assertEquals(40, line.endPoint.y);

        // Undo should restore points to (10, 20) and (30, 40)
        flipHCmd.undo();
        assertEquals(10, line.startPoint.x);
        assertEquals(20, line.startPoint.y);
        assertEquals(30, line.endPoint.x);
        assertEquals(40, line.endPoint.y);

        // 4. Transform rotate 90 CW
        // rotation changes bounds (oldImage width=100, height=200)
        // CW: p.X = h - 1 - y; p.Y = x;
        // start: (200 - 1 - 20, 10) = (179, 10)
        // end: (200 - 1 - 40, 30) = (159, 30)
        TransformCommand rotCWCmd = new TransformCommand(canvas, canvasState, oldImage, newImage, TransformType.ROTATE_90_CW);
        rotCWCmd.execute();

        assertEquals(179, line.startPoint.x);
        assertEquals(10, line.startPoint.y);
        assertEquals(159, line.endPoint.x);
        assertEquals(30, line.endPoint.y);

        // Undo
        rotCWCmd.undo();
        assertEquals(10, line.startPoint.x);
        assertEquals(20, line.startPoint.y);
        assertEquals(30, line.endPoint.x);
        assertEquals(40, line.endPoint.y);
    }
}
