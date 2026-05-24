package filter;

import java.awt.image.RGBImageFilter;

public class OrangeOutGrayFilter extends RGBImageFilter {
    public int filterRGB(int x, int y, int rgb) {
        int alpha = rgb & -16777216;
        int red = (rgb & 16711680) >> 16;
        int green = (rgb & '\uff00') >> 8;
        int blue = 0;
        red = Math.round((float)red * 0.92F);
        green = Math.round((float)green * 1.08F);
        if (green > 255) {
            green = 255;
        }

        int grayLevel = Math.max(red, Math.max(green, blue));
        return alpha + (grayLevel << 16) + (grayLevel << 8) + grayLevel;
    }
}
