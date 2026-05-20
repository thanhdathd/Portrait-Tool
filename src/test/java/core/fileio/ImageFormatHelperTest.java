package core.fileio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ImageFormatHelperTest {

    @TempDir
    Path tempDir;

    @Test
    void testIsProjectFile() {
        assertTrue(ImageFormatHelper.isProjectFile(new File("test.pdw")));
        assertTrue(ImageFormatHelper.isProjectFile(new File("TEST.PDW")));
        assertFalse(ImageFormatHelper.isProjectFile(new File("test.png")));
        assertFalse(ImageFormatHelper.isProjectFile(null));
    }

    @Test
    void testGetSupportedExtensions() {
        Set<String> extensions = ImageFormatHelper.getSupportedExtensions();
        assertNotNull(extensions);
        assertTrue(extensions.contains("pdw"));
        assertTrue(extensions.contains("webp"));
        assertTrue(extensions.contains("png") || extensions.contains("jpg") || extensions.contains("jpeg"));
    }

    @Test
    void testIsValidImageWithInvalidFiles() throws IOException {
        // null
        assertFalse(ImageFormatHelper.isValidImage(null));

        // Non-existent
        File nonExistent = new File(tempDir.toFile(), "ghost.png");
        assertFalse(ImageFormatHelper.isValidImage(nonExistent));

        // Empty file (size 0)
        File emptyFile = new File(tempDir.toFile(), "empty.png");
        assertTrue(emptyFile.createNewFile());
        assertFalse(ImageFormatHelper.isValidImage(emptyFile));

        // Invalid content
        File badContentFile = new File(tempDir.toFile(), "bad.png");
        try (FileOutputStream fos = new FileOutputStream(badContentFile)) {
            fos.write("Not an image".getBytes());
        }
        assertFalse(ImageFormatHelper.isValidImage(badContentFile));
    }

    @Test
    void testIsValidImageWithValidImage() throws IOException {
        // Create a 1x1 valid PNG image
        File imgFile = new File(tempDir.toFile(), "valid.png");
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        assertTrue(ImageIO.write(img, "png", imgFile));

        assertTrue(ImageFormatHelper.isValidImage(imgFile));
    }

    @Test
    void testIsValidImageWithWebpFallback() throws IOException {
        // Even if the file has webp content or dummy bytes, name-based WebP fallback should return true
        File webpFile = new File(tempDir.toFile(), "test.webp");
        try (FileOutputStream fos = new FileOutputStream(webpFile)) {
            fos.write("dummy webp bytes".getBytes());
        }
        assertTrue(ImageFormatHelper.isValidImage(webpFile));
    }
}
