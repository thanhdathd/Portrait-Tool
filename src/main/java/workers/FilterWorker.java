package workers;

import core.image.ImageProcessor;
import filter.FilterProperties;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Executes image filtering algorithms on a background thread to prevent UI freezing.
 */
public class FilterWorker extends SwingWorker<BufferedImage, Void> {

    private final BufferedImage sourceImage;
    private final FilterProperties properties;
    private final Consumer<BufferedImage> onComplete;

    public FilterWorker(BufferedImage sourceImage, FilterProperties properties, Consumer<BufferedImage> onComplete) {
        this.sourceImage = sourceImage;
        this.properties = properties;
        this.onComplete = onComplete;
    }

    @Override
    protected BufferedImage doInBackground() throws Exception {
        // Execute the heavy filtering process on the background thread
        return ImageProcessor.applyFilter(sourceImage, properties, this::isCancelled);
    }

    @Override
    protected void done() {
        if (isCancelled()) {
            return;
        }

        try {
            BufferedImage result = get(); // Blocks until doInBackground is finished, but since we are in done(), it's immediate
            if (onComplete != null && result != null) {
                onComplete.accept(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while filtering the image.", "Filter Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
