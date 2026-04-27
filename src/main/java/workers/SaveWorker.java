package workers;

import core.state.AppState;
import ui.canvas.ImageCanvas;
import user.Enum.Direction;
import userpackage.SPoint;

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


    public SaveWorker(ImageCanvas canvas, File outputFile) {
        this.canvas = canvas;
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
        for(SPoint p: appState.getCanvasState().getStickyPoints()) {
            g2d.setColor(p.c);
            g2d.fillRect(p.X - 1, p.Y - 1, 2, 2);
            if(canvas.isDrawLabels()) {
                // Adjust label font size back so it doesn't scale massively with zoom

                // Label rendering
                if (p.dr == Direction.EAST) {
                    g2d.drawString(String.valueOf(p.id), p.X + 12, p.Y + 12);
                } else if (p.dr == Direction.WEST) {
                    g2d.drawString(String.valueOf(p.id), p.X - 30, p.Y + 15);
                } else if (p.dr == Direction.SOUTH) {
                    g2d.drawString(String.valueOf(p.id), p.X - 5, p.Y + 25);
                } else {
                    g2d.drawString(String.valueOf(p.id), p.X - 5, p.Y - 12);
                }

            }
        }

        // 4. Draw Grids
        List<SPoint> grids = appState.getCanvasState().getGrids();
        if (!grids.isEmpty() && canvas.isDrawGrids()) {
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
            JOptionPane.showMessageDialog(null, "Image successfully saved to:\n" + outputFile.getAbsolutePath(), "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to save image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
