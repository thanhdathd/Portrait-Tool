package core.image;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class ImageTransformUtils {

    public enum TransformType {
        ROTATE_90_CW, ROTATE_90_CCW, ROTATE_180, FLIP_H, FLIP_V
    }

    public static BufferedImage transform(BufferedImage src, TransformType type) {
        int w = src.getWidth();
        int h = src.getHeight();
        AffineTransform at = new AffineTransform();

        switch (type) {
            case ROTATE_90_CW:
                at.translate(h, 0);
                at.rotate(Math.PI / 2);
                break;
            case ROTATE_90_CCW:
                at.translate(0, w);
                at.rotate(-Math.PI / 2);
                break;
            case ROTATE_180:
                at.translate(w, h);
                at.rotate(Math.PI);
                break;
            case FLIP_H:
                at.translate(w, 0);
                at.scale(-1, 1);
                break;
            case FLIP_V:
                at.translate(0, h);
                at.scale(1, -1);
                break;
        }

        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        
        BufferedImage dest;
        int imgType = src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType();
        if (type == TransformType.ROTATE_90_CW || type == TransformType.ROTATE_90_CCW) {
            dest = new BufferedImage(h, w, imgType);
        } else {
            dest = new BufferedImage(w, h, imgType);
        }

        return op.filter(src, dest);
    }
}
