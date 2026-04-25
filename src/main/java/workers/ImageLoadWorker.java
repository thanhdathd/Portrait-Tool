package workers;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

/**
 * Loads images from disk on a background thread.
 */
public class ImageLoadWorker extends SwingWorker<BufferedImage, Void> {

    private final File file;
    private final Consumer<BufferedImage> onComplete;
    private final Consumer<Exception> onError;

    public ImageLoadWorker(File file, Consumer<BufferedImage> onComplete, Consumer<Exception> onError) {
        this.file = file;
        this.onComplete = onComplete;
        this.onError = onError;
    }

    @Override
    protected BufferedImage doInBackground() throws Exception {
        return ImageIO.read(file);
    }

    @Override
    protected void done() {
        try {
            BufferedImage image = get();
            if (onComplete != null) {
                onComplete.accept(image);
            }
        } catch (Exception e) {
            if (onError != null) {
                onError.accept(e);
            }
        }
    }
}
