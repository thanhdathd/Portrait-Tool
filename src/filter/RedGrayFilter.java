package filter;

import java.awt.image.RGBImageFilter;

public class RedGrayFilter extends RGBImageFilter {
    public int filterRGB(int x, int y, int rgb) {
        rgb &= -16711681;
        int alpha = rgb & -16777216;
        int red = (rgb & 16711680) >> 16;
        int green = (rgb & '\uff00') >> 8;
        int blue = rgb & 255;
        int grayLevel = Math.max(red, Math.max(green, blue));
        return alpha + (grayLevel << 16) + (grayLevel << 8) + grayLevel;
    }
}
