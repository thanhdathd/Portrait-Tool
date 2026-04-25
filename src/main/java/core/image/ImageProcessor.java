package core.image;

import filter.FilterProperties;
import filter.RGBFilter;
import filter.RGBGrayFilter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

/**
 * Encapsulates all image processing logic, completely decoupled from the UI.
 * Provides pure functions that accept an image and return a processed image.
 */
public class ImageProcessor {

    /**
     * Applies the given FilterProperties to the source BufferedImage.
     */
    public static BufferedImage applyFilter(BufferedImage source, FilterProperties props) {
        if (source == null || props == null) {
            return source;
        }

        RGBImageFilter filter;
        
        // Mode 0 = RGB, -1 = Black & White (Grayscale)
        if (props.getMode() == 0) {
            filter = new RGBFilter(props.red, props.gre, props.blu, props.alp);
        } else {
            // Legacy code checks if gra < 128
            if (props.gra < 128) {
                filter = new RGBGrayFilter(props.red, props.gre, props.blu, props.alp, props.gra);
            } else {
                filter = new RGBGrayFilter(props.red, props.gre, props.blu, props.alp);
            }
        }

        ImageProducer producer = new FilteredImageSource(source.getSource(), filter);
        
        // Convert ImageProducer back to BufferedImage
        Image filteredImage = Toolkit.getDefaultToolkit().createImage(producer);
        
        // Wait for the image to be fully loaded
        MediaTracker tracker = new MediaTracker(new Component() {});
        tracker.addImage(filteredImage, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        BufferedImage result = new BufferedImage(
                source.getWidth(), 
                source.getHeight(), 
                BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(filteredImage, 0, 0, null);
        g2d.dispose();

        return result;
    }
}
