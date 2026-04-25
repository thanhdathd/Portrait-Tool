package workers;

import ui.canvas.ImageCanvas;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Saves the fully composed canvas (Image + Points + Grids) to a file on a background thread.
 */
public class SaveWorker extends SwingWorker<Void, Void> {

    private final ImageCanvas canvas;
    private final File outputFile;

    public SaveWorker(ImageCanvas canvas, File outputFile) {
        this.canvas = canvas;
        this.outputFile = outputFile;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // We must compose the image on the EDT to safely read the canvas state
        final BufferedImage[] composedImage = new BufferedImage[1];
        
        SwingUtilities.invokeAndWait(() -> {
            int w = canvas.getWidth();
            int h = canvas.getHeight();
            if (w == 0 || h == 0) {
                w = 800; h = 600;
            }
            composedImage[0] = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = composedImage[0].createGraphics();
            // Fill background white just in case
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            
            // Paint the canvas contents onto our image
            canvas.paint(g2);
            g2.dispose();
        });

        // Write to disk off the EDT
        ImageIO.write(composedImage[0], "png", outputFile);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
            JOptionPane.showMessageDialog(null, "Image successfully saved to:\n" + outputFile.getAbsolutePath(), "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to save image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
