package filter;

import java.awt.image.RGBImageFilter;

public class RGBFilter extends RGBImageFilter {
    int Red;
    int Green;
    int Blue;
    int Alpha;

    public RGBFilter(int red, int gre, int blu, int alp) {
        this.Red = red;
        this.Blue = blu;
        this.Green = gre;
        this.Alpha = alp;
    }

    public int filterRGB(int x, int y, int rgb) {
        int a = rgb & -16777216;
        int r = (rgb & 16711680) >> 16;
        int g = (rgb & '\uff00') >> 8;
        int b = rgb & 255;
        r = (int)((float)r * ((float)this.Red / 100.0F));
        if (r > 255) {
            r = 255;
        }

        g = (int)((float)g * ((float)this.Green / 100.0F));
        if (g > 255) {
            g = 255;
        }

        b = (int)((float)b * ((float)this.Blue / 100.0F));
        if (b > 255) {
            b = 255;
        }

        a = (int)((float)(this.Alpha * 255) / 100.0F);
        return this.Alpha < 255 ? (a << 24) + (r << 16) + (g << 8) + b : a + (r << 16) + (g << 8) + b;
    }
}
