package filter;

import java.awt.image.RGBImageFilter;

public class GreenOutGrayFilter extends RGBImageFilter {
    public int filterRGB(int x, int y, int rgb) {
        int alpha = rgb & -16777216;
        int green = (rgb & '\uff00') >> 8;
        return alpha + (green << 16) + (green << 8) + green;
    }
}
