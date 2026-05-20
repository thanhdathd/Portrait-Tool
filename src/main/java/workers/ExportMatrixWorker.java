package workers;

import core.state.CanvasState;
import core.state.SPoint;
import core.state.Direction;
import ui.components.ToastNotification;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Replaces the legacy PointMatrix functionality natively in Swing via a background thread.
 * Clears cutouts around the placed sticky points and saves the resulting image matrix.
 */
public class ExportMatrixWorker extends SwingWorker<Void, Integer> {

    private final BufferedImage originalImage;
    private final CanvasState canvasState;
    private final File outputFile;
    private final JProgressBar progressBar;

    public ExportMatrixWorker(BufferedImage originalImage, CanvasState canvasState, File outputFile, JProgressBar progressBar) {
        // Clone image to prevent mutating the original canvas background directly
        this.originalImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = this.originalImage.createGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
        
        this.canvasState = canvasState;
        this.outputFile = outputFile;
        this.progressBar = progressBar;
    }

    @Override
    protected Void doInBackground() throws Exception {
        Graphics2D g2 = originalImage.createGraphics();
        g2.setBackground(new Color(0, 0, 0, 0)); // Transparent clear
        g2.setComposite(AlphaComposite.Clear);
        
        java.util.List<SPoint> points = canvasState.getStickyPoints();
        int total = points.size();
        
        for (int i = 0; i < total; i++) {
            SPoint p = points.get(i);
            int x = p.X;
            int y = p.Y;
            
            // Legacy clear dimensions based on Direction
            if (p.dr == Direction.EAST) {
                g2.fillRect(x + 3, y + 2, 32, 13);
            } else if (p.dr == Direction.WEST) {
                g2.fillRect(x - 35, y + 2, 45, 16);
            } else if (p.dr == Direction.SOUTH) {
                g2.fillRect(x - 5, y + 3, 25, 22);
            } else if (p.dr == Direction.NORTH) {
                g2.fillRect(x - 15, y - 25, 35, 22);
            }
            
            int progress = (int) (((i + 1) / (float) total) * 100);
            publish(progress);
        }
        g2.dispose();
        
        ImageIO.write(originalImage, "png", outputFile);
        return null;
    }

    @Override
    protected void process(java.util.List<Integer> chunks) {
        if (progressBar != null) {
            int latestProgress = chunks.get(chunks.size() - 1);
            progressBar.setValue(latestProgress);
        }
    }

    @Override
    protected void done() {
        try {
            get();
            ToastNotification.show("Matrix Exported Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
