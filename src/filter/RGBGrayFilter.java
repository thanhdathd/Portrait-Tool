package filter;

import java.awt.image.RGBImageFilter;

public class RGBGrayFilter extends RGBImageFilter {
    private int Alpha = -1;
    private int Red = -1;
    private int Green = -1;
    private int Blue = -1;
    private int gr = -1;

    public RGBGrayFilter() {
        this.Alpha = this.Red = this.Green = this.Blue = this.gr = -1;
    }

    public RGBGrayFilter(int red, int gre, int blu, int alp) {
        this.Red = red;
        this.Green = gre;
        this.Blue = blu;
        this.Alpha = alp;
    }

    public RGBGrayFilter(int red, int gre, int blu, int alp, int gray) {
        this.Red = red;
        this.Green = gre;
        this.Blue = blu;
        this.Alpha = alp;
        this.gr = gray;
    }

    public int filterRGB(int x, int y, int rgb) {
        int alpha = rgb & -16777216;
        int red = (rgb & 16711680) >> 16;
        int green = (rgb & '\uff00') >> 8;
        int blue = rgb & 255;
        if (this.Red != -1) {
            red = (int)((float)(red * this.Red) / 100.0F);
            green = (int)((float)(green * this.Green) / 100.0F);
            blue = (int)((float)(blue * this.Blue) / 100.0F);
            alpha = (int)((float)(255 * this.Alpha) / 100.0F);
            if (red > 255) {
                red = 255;
            }

            if (blue > 255) {
                blue = 255;
            }

            if (green > 255) {
                green = 255;
            }

            if (alpha < 255) {
                alpha <<= 24;
            } else {
                alpha = rgb & -16777216;
            }

            int grayLevel;
            if (this.gr == -1) {
                grayLevel = Math.max(red, Math.max(green, blue));
            } else {
                int max = Math.max(red, Math.max(green, blue));
                max = this.grayProcess(max);
                grayLevel = max;
            }

            return alpha + (grayLevel << 16) + (grayLevel << 8) + grayLevel;
        } else {
            int grayLevel = Math.max(red, Math.max(green, blue));
            return alpha + (grayLevel << 16) + (grayLevel << 8) + grayLevel;
        }
    }

    public int grayProcess(int max) {
        if (this.gr < 2) {
            this.gr = 2;
        }

        if (this.gr % 2 != 0) {
            ++this.gr;
        }

        for(int i = 0; i < this.gr; ++i) {
            if (max >= i * 256 / this.gr && max <= (i + 1) * 256 / this.gr) {
                if (max <= i * 256 / this.gr + 128 / this.gr) {
                    max = i * 256 / this.gr;
                } else {
                    max = (i + 1) * 256 / this.gr;
                }
                break;
            }
        }

        if (max > 255) {
            max = 255;
        }

        return max;
    }

    public static enum RGB {
        ALPHA,
        RED,
        GREEN,
        BLUE,
        GRAY;
    }
}
