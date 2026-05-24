package core.image;

import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class ImageTransformUtilsTest {
    @Test
    void testTransforms() {
        BufferedImage img = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
        
        BufferedImage rot90cw = ImageTransformUtils.transform(img, ImageTransformUtils.TransformType.ROTATE_90_CW);
        assertEquals(200, rot90cw.getWidth());
        assertEquals(100, rot90cw.getHeight());

        BufferedImage flipH = ImageTransformUtils.transform(img, ImageTransformUtils.TransformType.FLIP_H);
        assertEquals(100, flipH.getWidth());
        assertEquals(200, flipH.getHeight());
    }
}
