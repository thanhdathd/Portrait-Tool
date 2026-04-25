package filter;

import java.awt.image.RGBImageFilter;

public class RedOutGrayFilter extends RGBImageFilter {
    public int filterRGB(int x, int y, int rgb) {
        int alpha = rgb & -16777216;
        int red = (rgb & 16711680) >> 16;
        return alpha + (red << 16) + (red << 8) + red;
    }
}
