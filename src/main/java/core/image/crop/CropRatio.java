package core.image.crop;

public enum CropRatio {
    SQUARE(1.0f, "1:1"),
    RATIO_4_3(4.0f/3.0f, "4:3"),
    RATIO_3_2(3.0f/2.0f, "9:6"),
    GOLDEN(1.618f, "Golden Ratio"),
    A4(1.414f, "A4 Print");

    public final float ratio;
    public final String label;

    CropRatio(float ratio, String label) {
        this.ratio = ratio;
        this.label = label;
    }
}
