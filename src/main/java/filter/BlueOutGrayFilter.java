package filter;

import java.awt.image.RGBImageFilter;

public class BlueOutGrayFilter extends RGBImageFilter {
    public int filterRGB(int x, int y, int rgb) {
        int alpha = rgb & -16777216;
        int blue = rgb & 255;
        return alpha + (blue << 16) + (blue << 8) + blue;
    }
}
