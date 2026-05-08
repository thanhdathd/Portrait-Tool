package workers;

import core.state.AppState;
import ui.canvas.ImageCanvas;
import ui.canvas.RenderUtils;
import userpackage.SPoint;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Saves the point map (SPoints with markers on a transparent background) to a file.
 */
public class SavePointMapWorker extends SwingWorker<Void, Void> {
    private final ImageCanvas canvas;
    private final File outputFile;
    private final int width;
    private final int height;

    public SavePointMapWorker(ImageCanvas canvas, File outputFile) {
        this.canvas = canvas;
        this.outputFile = outputFile;
        BufferedImage originalImage = canvas.getBackgroundImage();
        if (originalImage != null) {
            this.width = originalImage.getWidth();
            this.height = originalImage.getHeight();
        } else {
            this.width = canvas.getWidth();
            this.height = canvas.getHeight();
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        AppState appState = canvas.getAppState();
        List<SPoint> sPoints = appState.getCanvasState().getStickyPoints();
        float scale = appState.getScale();

        // Create transparent image (TYPE_INT_ARGB)
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        
        // Use high quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        for (SPoint p : sPoints) {
            RenderUtils.drawPointMarker(g2d, p, scale);
        }
        
        g2d.dispose();

        ImageIO.write(result, "png", outputFile);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
            JOptionPane.showMessageDialog(null, "Point map successfully saved to:\n" + outputFile.getAbsolutePath(), "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to save point map: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
