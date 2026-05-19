package core.history;

import core.state.AppState;
import core.state.CanvasState;
import ui.canvas.ImageCanvas;
import ui.dialogs.ResizeDialog;
import userpackage.SLine;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class ResizeCommandTest {

    @Test
    void testLineResizingAndUndo() {
        // 1. Setup AppState, canvas, images
        AppState appState = new AppState();
        CanvasState canvasState = appState.getCanvasState();
        
        BufferedImage oldImage = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
        BufferedImage newImage = new BufferedImage(200, 400, BufferedImage.TYPE_INT_ARGB); // 2x scale
        
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

        // 2. Add a line at (10, 20) -> (30, 40)
        SLine line = new SLine(1, new Point(10, 20), new Point(30, 40), 2, java.awt.Color.BLACK);
        canvasState.getLines().add(line);

        // 3. Execute ResizeCommand
        ResizeDialog.ResizeProps props = new ResizeDialog.ResizeProps();
        props.width = 200;
        props.height = 400;
        props.hint = 0; // Nearest neighbor
        
        ResizeCommand resizeCmd = new ResizeCommand(canvas, canvasState, oldImage, newImage, props);
        resizeCmd.execute();

        // Expect coordinates scaled by 2.0x
        assertEquals(20, line.startPoint.x);
        assertEquals(40, line.startPoint.y);
        assertEquals(60, line.endPoint.x);
        assertEquals(80, line.endPoint.y);

        // Undo
        resizeCmd.undo();
        assertEquals(10, line.startPoint.x);
        assertEquals(20, line.startPoint.y);
        assertEquals(30, line.endPoint.x);
        assertEquals(40, line.endPoint.y);
    }
}
