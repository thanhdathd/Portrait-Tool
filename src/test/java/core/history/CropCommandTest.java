package core.history;

import core.state.AppState;
import core.state.CanvasState;
import core.state.SPoint;
import ui.canvas.ImageCanvas;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class CropCommandTest {

    private ImageCanvas createMockCanvas(AppState appState, BufferedImage initialImage) {
        return new ImageCanvas(appState) {
            private BufferedImage bg = initialImage;
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
    }

    @Test
    void testNormalCropInsideBounds() {
        AppState appState = new AppState();
        CanvasState canvasState = appState.getCanvasState();

        // 1. Create a 100x100 image
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.RED);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        ImageCanvas canvas = createMockCanvas(appState, img);

        // Add a point at (30, 40)
        SPoint p = new SPoint(1, 30, 40);
        canvasState.getStickyPoints().add(p);

        // Crop to 50x50 at (10, 10)
        Rectangle cropBounds = new Rectangle(10, 10, 50, 50);
        CropCommand cmd = new CropCommand(canvas, canvasState, img, cropBounds, 1.0f, 0, 0, false);
        cmd.execute();

        // Target image dimensions
        BufferedImage cropped = canvas.getBackgroundImage();
        assertNotNull(cropped);
        assertEquals(50, cropped.getWidth());
        assertEquals(50, cropped.getHeight());

        // Point should be shifted: (30-10, 40-10) -> (20, 30)
        assertEquals(20, p.X);
        assertEquals(30, p.Y);

        // Undo
        cmd.undo();
        assertEquals(100, canvas.getBackgroundImage().getWidth());
        assertEquals(30, p.X);
        assertEquals(40, p.Y);
    }

    @Test
    void testOversizeCropSolidBlack() {
        AppState appState = new AppState();
        CanvasState canvasState = appState.getCanvasState();

        // 1. Create a 50x50 RED image
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.RED);
        g.fillRect(0, 0, 50, 50);
        g.dispose();

        ImageCanvas canvas = createMockCanvas(appState, img);

        // Crop oversize to 100x100 at (-25, -25) with solid black background
        Rectangle cropBounds = new Rectangle(-25, -25, 100, 100);
        CropCommand cmd = new CropCommand(canvas, canvasState, img, cropBounds, 1.0f, 0, 0, false);
        cmd.execute();

        BufferedImage cropped = canvas.getBackgroundImage();
        assertNotNull(cropped);
        assertEquals(100, cropped.getWidth());
        assertEquals(100, cropped.getHeight());

        // Top-left pixel (0, 0) should be black
        int argbTopLeft = cropped.getRGB(0, 0);
        // Alpha could be 255, RGB all 0 (solid black)
        assertEquals(0xFF000000, argbTopLeft);

        // Center pixel (50, 50) should be RED
        int argbCenter = cropped.getRGB(50, 50);
        assertEquals(java.awt.Color.RED.getRGB(), argbCenter);
    }

    @Test
    void testOversizeCropBlurredBackground() {
        AppState appState = new AppState();
        CanvasState canvasState = appState.getCanvasState();

        // 1. Create a 50x50 RED image
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.RED);
        g.fillRect(0, 0, 50, 50);
        g.dispose();

        ImageCanvas canvas = createMockCanvas(appState, img);

        // Crop oversize to 100x100 at (-25, -25) with blurred background
        Rectangle cropBounds = new Rectangle(-25, -25, 100, 100);
        CropCommand cmd = new CropCommand(canvas, canvasState, img, cropBounds, 1.0f, 0, 0, true);
        cmd.execute();

        BufferedImage cropped = canvas.getBackgroundImage();
        assertNotNull(cropped);
        assertEquals(100, cropped.getWidth());
        assertEquals(100, cropped.getHeight());

        // Top-left pixel (0, 0) should NOT be black because of the blurred scaled RED image fill
        int argbTopLeft = cropped.getRGB(0, 0);
        assertNotEquals(0xFF000000, argbTopLeft);
        // Since original image is fully RED, the blurred background should be close to RED/translucent RED
        java.awt.Color color = new java.awt.Color(argbTopLeft, true);
        assertTrue(color.getRed() > 100, "Blurred background should retain RED color components");

        // Center pixel (50, 50) should still be RED (crisp overlay)
        int argbCenter = cropped.getRGB(50, 50);
        assertEquals(java.awt.Color.RED.getRGB(), argbCenter);
    }
}
