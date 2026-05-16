package core.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {

    private static ResourceBundle messages;
    private static String currentLangCode = "en";

    static {
        // Initialize with default (English)
        setLanguage("en");
    }

    public static void setLanguage(String langCode) {
        currentLangCode = langCode;
        Locale locale;
        switch (langCode) {
            case "vi":
                locale = new Locale("vi", "VN");
                break;
            case "zh":
                locale = new Locale("zh", "CN");
                break;
            case "en":
            default:
                locale = Locale.ENGLISH;
                break;
        }
        messages = ResourceBundle.getBundle("lang.messages", locale, new UTF8Control());
    }

    public static String getString(String key) {
        try {
            return messages.getString(key);
        } catch (Exception e) {
            return "!" + key + "!"; // Fallback visual indicator for missing keys
        }
    }

    public static String getCurrentLangCode() {
        return currentLangCode;
    }

    private static class UTF8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, java.io.IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            java.io.InputStream stream = null;
            if (reload) {
                java.net.URL url = loader.getResource(resourceName);
                if (url != null) {
                    java.net.URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try (java.io.InputStreamReader reader = new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)) {
                    bundle = new java.util.PropertyResourceBundle(reader);
                }
            }
            return bundle;
        }
    }
}
