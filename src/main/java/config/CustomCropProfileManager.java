package config;

import core.image.crop.CropGuide;
import core.image.crop.CustomCropProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Reads and writes custom crop profiles to/from a Properties object.
 * Profiles are stored as:
 *   customCrop.N.name  = My Profile
 *   customCrop.N.ratio = 1.333
 *   customCrop.N.guide = RULE_OF_THIRDS
 *
 * This class is intentionally decoupled from ConfigManager so that the
 * ConfigManager can delegate to it without growing further.
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
                String name  = props.getProperty(PREFIX + i + ".name", "");
                float ratio  = Float.parseFloat(props.getProperty(PREFIX + i + ".ratio", "1.0"));
                String guideStr = props.getProperty(PREFIX + i + ".guide", "RULE_OF_THIRDS");
                CropGuide guide = CropGuide.valueOf(guideStr);
                if (!name.isEmpty() && ratio > 0) {
                    profiles.add(new CustomCropProfile(name, ratio, guide));
                }
            } catch (Exception ignored) {}
        }
    }

    /** Write profiles into a Properties object (call inside ConfigManager.save). */
    public void save(Properties props) {
        props.setProperty(KEY_COUNT, String.valueOf(profiles.size()));
        for (int i = 0; i < profiles.size(); i++) {
            CustomCropProfile p = profiles.get(i);
            props.setProperty(PREFIX + i + ".name",  p.name);
            props.setProperty(PREFIX + i + ".ratio", String.valueOf(p.ratio));
            props.setProperty(PREFIX + i + ".guide", p.guide.name());
        }
    }

    public void add(CustomCropProfile profile) {
        profiles.add(profile);
    }

    public void remove(int index) {
        if (index >= 0 && index < profiles.size()) {
            profiles.remove(index);
        }
    }
}
