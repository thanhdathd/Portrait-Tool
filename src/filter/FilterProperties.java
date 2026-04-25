package filter;

public class FilterProperties {
    private int mode;
    private int rgb;
    public int red;
    public int gre;
    public int blu;
    public int alp;
    public int gra;

    public FilterProperties(int red, int gre, int blu, int alp, int gra, int mode) {
        this.red = red;
        this.gre = gre;
        this.blu = blu;
        this.alp = alp;
        this.gra = gra;
        this.mode = mode;
        this.rgb = (alp << 24) + (red << 16) + (gre << 8) + blu;
    }

    public int getProperties() {
        return this.rgb;
    }

    public int getGrayLevel() {
        return this.gra;
    }

    public int getMode() {
        return this.mode;
    }

    public static int getAlp(int proper) {
        return (proper & -16777216) >> 24;
    }

    public static int getRed(int proper) {
        return (proper & 16711680) >> 16;
    }

    public static int getGre(int proper) {
        return (proper & '\uff00') >> 8;
    }

    public static int getBlu(int proper) {
        return proper & 255;
    }

    public String toString() {
        return "Filter Property ->[red:" + this.red + "; green:" + this.gre + "; blue:" + this.blu + "; alpha:" + this.alp + "; gray:" + this.gra + "; mode:" + this.mode + "]";
    }
}
