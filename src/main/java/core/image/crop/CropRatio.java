package core.image.crop;

public enum CropRatio {
    SQUARE(1.0f, "1:1"),
    GOLDEN(1.618f, "Golden Ratio"),
    A4(1.414f, "A4 Print"),
    FREEFORM(0f, "Freeform");

    public final float ratio;
    public final String label;

    CropRatio(float ratio, String label) {
        this.ratio = ratio;
        this.label = label;
    }
}
