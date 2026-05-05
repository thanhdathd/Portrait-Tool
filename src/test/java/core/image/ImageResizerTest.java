package core.image;

import org.junit.jupiter.api.Test;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class ImageResizerTest {
    @Test
    void testResizeImage() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        BufferedImage resized = ImageResizer.resize(img, 50, 50, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        assertEquals(50, resized.getWidth());
        assertEquals(50, resized.getHeight());
    }
}
