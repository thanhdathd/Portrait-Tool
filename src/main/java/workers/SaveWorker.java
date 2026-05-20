package workers;

import core.state.AppState;
import ui.canvas.ImageCanvas;
import ui.canvas.RenderUtils;
import core.state.Direction;
import core.state.SPoint;
import ui.components.ToastNotification;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Saves the fully composed canvas (Image + Points + Grids) to a file on a background thread.
 */
public class SaveWorker extends SwingWorker<Void, Void> {

    private final BufferedImage image;
    private final ImageCanvas canvas;
    private final File outputFile;
    private boolean drawLabels;
    private boolean drawGrids;


    public SaveWorker(ImageCanvas canvas, File outputFile, boolean drawLabels, boolean drawGrids) {
        this.canvas = canvas;
        this.drawLabels = drawLabels;
        this.drawGrids = drawGrids;
        BufferedImage originalImage = canvas.getBackgroundImage();
        this.image = new  BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        this.outputFile = outputFile;
    }

    @Override
    protected Void doInBackground() throws Exception {
        AppState appState = canvas.getAppState();
        Graphics2D g2d = image.createGraphics();
        List<SPoint> sPoints = appState.getCanvasState().getStickyPoints();
        RenderUtils.drawStickyPoints(g2d, sPoints, drawLabels);

        // Draw Lines
        RenderUtils.drawLines(g2d, appState.getCanvasState().getLines(), null, 1.0f);

        // 4. Draw Grids
        List<SPoint> grids = appState.getCanvasState().getGrids();
        if (!grids.isEmpty() && drawGrids) {
            Stroke oldStroke = g2d.getStroke();
            float strokeWidth = 1.0f;
            float[] dash = new float[]{2.0f, 4.0f};
            g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dash, 0));

            for (SPoint grid : grids) {
                g2d.setColor(grid.c);
                int gridSize = grid.id;
                int xR = grid.X;
                int yR = grid.Y;

                int width = image.getWidth();
                int height = image.getHeight();

                for (int x = xR; x < width; x += gridSize) {
                    g2d.drawLine(x, 0, x, height);
                }
                for (int x = xR; x > 0; x -= gridSize) {
                    g2d.drawLine(x, 0, x, height);
                }
                for (int y = yR; y < height; y += gridSize) {
                    g2d.drawLine(0, y, width, y);
                }
                for (int y = yR; y > 0; y -= gridSize) {
                    g2d.drawLine(0, y, width, y);
                }
            }
            g2d.setStroke(oldStroke);
        }

        g2d.dispose();


        // Write to disk off the EDT
        ImageIO.write(this.image, "png", outputFile);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
            ToastNotification.show("Image successfully saved to:\n" + outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to save image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
