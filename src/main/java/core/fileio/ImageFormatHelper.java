package core.fileio;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility helper to handle image and project file validation and supported extensions.
 */
public class ImageFormatHelper {

    /**
     * Checks if the file is a project file (.pdw).
     */
    public static boolean isProjectFile(File file) {
        if (file == null) return false;
        return file.getName().toLowerCase().endsWith(".pdw");
    }

    /**
     * Checks if the file is a valid image format.
     * Uses ImageIO stream readers to detect format, with fallback for custom/future types (e.g. webp).
     */
    public static boolean isValidImage(File file) {
        if (file == null || !file.exists() || file.length() == 0) {
            return false;
        }



        ImageInputStream iis = null;
        try {
            iis = ImageIO.createImageInputStream(file);
            if (iis == null) {
                return false;
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                String formatName = reader.getFormatName();
                System.out.println("Detected format: " + formatName + " | File: " + file.getName());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra file " + file.getName() + ": " + e.getMessage());
            return false;
        } finally {
            try {
                if (iis != null) {
                    iis.close();
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * Returns a set of all lower-case image extensions supported by ImageIO,
     * plus any custom/future overrides (like "webp" and "pdw").
     */
    public static Set<String> getSupportedExtensions() {
        Set<String> extensions = new LinkedHashSet<>();
        
        // 1. Standard formats supported by ImageIO
        String[] readerFormats = ImageIO.getReaderFormatNames();
        if (readerFormats != null) {
            for (String format : readerFormats) {
                extensions.add(format.toLowerCase());
            }
        }
        
        // 2. Custom/future fallback formats
        extensions.add("webp");
        extensions.add("pdw");
        
        return extensions;
    }
}
