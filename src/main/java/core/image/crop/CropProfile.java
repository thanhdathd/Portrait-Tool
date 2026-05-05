package core.image.crop;

public class CropProfile {
    public final CropRatio ratio;
    public final CropGuide guide;

    public CropProfile(CropRatio ratio, CropGuide guide) {
        this.ratio = ratio;
        this.guide = guide;
    }
}
