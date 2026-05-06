package core.image.crop;

/**
 * A user-created crop profile that stores the original width, height and unit
 * entered by the user, in addition to the derived ratio used by the renderer.
 * Keeping the raw input allows the dialog to restore exact values when editing.
 */
public class CustomCropProfile {
    public enum Unit { CM, PX }

    public static final int MAX_DESCRIPTION_LENGTH = 80;
    /** Minimum allowed w/h ratio (prevents extremely tall frames). */
    public static final float MIN_RATIO = 0.05f;
    /** Maximum allowed w/h ratio (prevents extremely wide frames). */
    public static final float MAX_RATIO = 20.0f;

    public final String name;
    /** Width as entered by the user (in {@link #unit}). */
    public final float width;
    /** Height as entered by the user (in {@link #unit}). */
    public final float height;
    /** The unit chosen by the user (cm or px). */
    public final Unit unit;
    /** Derived ratio width/height, used by the crop renderer. Always > 0. */
    public final float ratio;
    /** The guide to draw. Only RULE_OF_THIRDS and CROSSHAIR are supported. */
    public final CropGuide guide;
    /** Short description shown on the crop frame. May be empty but never null. */
    public final String description;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /** Legacy constructor for configs that only stored ratio (no unit info). */
    public CustomCropProfile(String name, float ratio, CropGuide guide) {
        this(name, ratio, 1.0f, Unit.CM, ratio, guide, "");
    }

    /** Legacy constructor for configs that only stored ratio + description. */
    public CustomCropProfile(String name, float ratio, CropGuide guide, String description) {
        this(name, ratio, 1.0f, Unit.CM, ratio, guide, description);
    }

    /** Full constructor — use {@link #create} instead of calling this directly. */
    public CustomCropProfile(String name, float width, float height, Unit unit,
                             float ratio, CropGuide guide, String description) {
        this.name        = name;
        this.width       = width;
        this.height      = height;
        this.unit        = unit;
        this.ratio       = ratio;
        this.guide       = guide;
        this.description = description == null ? "" : description;
    }

    // -----------------------------------------------------------------------
    // Factory methods
    // -----------------------------------------------------------------------

    /** Build from raw width/height + unit. */
    public static CustomCropProfile create(String name, float width, float height,
                                           Unit unit, CropGuide guide) {
        return create(name, width, height, unit, guide, "");
    }

    /** Build from raw width/height + unit + description. */
    public static CustomCropProfile create(String name, float width, float height,
                                           Unit unit, CropGuide guide, String description) {
        if (height <= 0) throw new IllegalArgumentException("Height must be > 0");
        float ratio = width / height;
        return new CustomCropProfile(name, width, height, unit, ratio, guide, description);
    }

    @Override
    public String toString() {
        return name;
    }
}

