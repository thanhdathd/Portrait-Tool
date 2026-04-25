package filter;

import java.awt.image.RGBImageFilter;

public class YelowOutGrayFilter extends RGBImageFilter {
    public int filterRGB(int x, int y, int rgb) {
        int alpha = rgb & -16777216;
        int red = (rgb & 16711680) >> 16;
        int green = (rgb & '\uff00') >> 8;
        int blue = 0;
        red = (int)((float)red * 0.68F);
        green = green;
        if (red > 255) {
            red = 255;
        }

        if (green > 255) {
            green = 255;
        }

        int grayLevel = Math.max(red, Math.max(green, blue));
        return alpha + (grayLevel << 16) + (grayLevel << 8) + grayLevel;
    }
}
