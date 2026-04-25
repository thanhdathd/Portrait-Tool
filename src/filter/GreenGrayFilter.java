package filter;

import java.awt.image.RGBImageFilter;

public class GreenGrayFilter extends RGBImageFilter {
    public int filterRGB(int x, int y, int rgb) {
        int alpha = rgb & -16777216;
        int red = (rgb & 16711680) >> 16;
        int green = 0;
        int blue = rgb & 255;
        int grayLevel = Math.max(red, Math.max(green, blue));
        return alpha + (grayLevel << 16) + (grayLevel << 8) + grayLevel;
    }
}
