package config;

import core.image.crop.CropGuide;
import core.image.crop.CustomCropProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Reads and writes custom crop profiles to/from a Properties object.
 * New format (v2):
 *   customCrop.N.name        = My Profile
 *   customCrop.N.width       = 21.0
 *   customCrop.N.height      = 29.7
 *   customCrop.N.unit        = CM
 *   customCrop.N.guide       = RULE_OF_THIRDS
 *   customCrop.N.description = (optional)
 * Legacy format (v1) only stored 'ratio'; those are loaded with width=ratio, height=1, unit=CM.
 */
public class CustomCropProfileManager {

    private static final String PREFIX = "customCrop.";
    private static final String KEY_COUNT = "customCrop.count";

    private final List<CustomCropProfile> profiles = new ArrayList<>();

    public List<CustomCropProfile> getProfiles() {
        return profiles;
    }

    /** Load profiles from a Properties snapshot (call inside ConfigManager.load). */
    public void load(Properties props) {
        profiles.clear();
        int count = 0;
        try {
            count = Integer.parseInt(props.getProperty(KEY_COUNT, "0"));
        } catch (NumberFormatException ignored) {}

        for (int i = 0; i < count; i++) {
            try {
                String name     = props.getProperty(PREFIX + i + ".name", "");
                String guideStr = props.getProperty(PREFIX + i + ".guide", "RULE_OF_THIRDS");
                CropGuide guide = CropGuide.valueOf(guideStr);
                String desc     = props.getProperty(PREFIX + i + ".description", "");

                float width, height;
                CustomCropProfile.Unit unit;

                // v2 format stores width/height/unit explicitly
                String widthStr  = props.getProperty(PREFIX + i + ".width");
                String heightStr = props.getProperty(PREFIX + i + ".height");
                String unitStr   = props.getProperty(PREFIX + i + ".unit");

                if (widthStr != null && heightStr != null) {
                    width  = Float.parseFloat(widthStr);
                    height = Float.parseFloat(heightStr);
                    unit   = unitStr != null ? CustomCropProfile.Unit.valueOf(unitStr)
                                            : CustomCropProfile.Unit.CM;
                } else {
                    // v1 fallback: only ratio was stored
                    float ratio = Float.parseFloat(props.getProperty(PREFIX + i + ".ratio", "1.0"));
                    width  = ratio;
                    height = 1.0f;
                    unit   = CustomCropProfile.Unit.CM;
                }

                if (!name.isEmpty() && width > 0 && height > 0) {
                    profiles.add(CustomCropProfile.create(name, width, height, unit, guide, desc));
                }
            } catch (Exception ignored) {}
        }
    }

    /** Write profiles into a Properties object (call inside ConfigManager.save). */
    public void save(Properties props) {
        props.setProperty(KEY_COUNT, String.valueOf(profiles.size()));
        for (int i = 0; i < profiles.size(); i++) {
            CustomCropProfile p = profiles.get(i);
            props.setProperty(PREFIX + i + ".name",        p.name);
            props.setProperty(PREFIX + i + ".width",       String.valueOf(p.width));
            props.setProperty(PREFIX + i + ".height",      String.valueOf(p.height));
            props.setProperty(PREFIX + i + ".unit",        p.unit.name());
            props.setProperty(PREFIX + i + ".guide",       p.guide.name());
            props.setProperty(PREFIX + i + ".description", p.description);
        }
    }

    public void add(CustomCropProfile profile) {
        profiles.add(profile);
    }

    public void update(int index, CustomCropProfile profile) {
        if (index >= 0 && index < profiles.size()) {
            profiles.set(index, profile);
        }
    }

    public void remove(int index) {
        if (index >= 0 && index < profiles.size()) {
            profiles.remove(index);
        }
    }
}
