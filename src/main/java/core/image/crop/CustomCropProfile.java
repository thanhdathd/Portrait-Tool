package core.image.crop;

/**
 * A user-created crop profile with a custom name, aspect ratio, and guide type.
 * The ratio is stored as a float derived from width/height. Units are only used
 * during input (cm or px) and are resolved to a float ratio at creation time.
 */
public class CustomCropProfile {
    public enum Unit { CM, PX }

    public final String name;
    /** Width/height ratio. Always > 0. */
    public final float ratio;
    /** The guide to draw. Only RULE_OF_THIRDS and CROSSHAIR are supported. */
    public final CropGuide guide;

    public CustomCropProfile(String name, float ratio, CropGuide guide) {
        this.name = name;
        this.ratio = ratio;
        this.guide = guide;
    }

    /** Build from raw width/height values plus a unit. Ratio is unit-independent (w/h). */
    public static CustomCropProfile create(String name, float width, float height, Unit unit, CropGuide guide) {
        if (height <= 0) throw new IllegalArgumentException("Height must be > 0");
        return new CustomCropProfile(name, width / height, guide);
    }

    @Override
    public String toString() {
        return name;
    }
}
