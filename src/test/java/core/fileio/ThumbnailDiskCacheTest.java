package core.fileio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ThumbnailDiskCacheTest {

    @TempDir
    Path tempDir;

    @Test
    void testCachePutGetAndStaleHandling() throws IOException, InterruptedException {
        // 1. Create a dummy source image file
        File sourceFile = new File(tempDir.toFile(), "source.png");
        BufferedImage originalImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        assertTrue(ImageIO.write(originalImage, "png", sourceFile));

        int size = 32;
        BufferedImage thumbnail = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        // Verify cache is empty initially
        BufferedImage cachedBefore = ThumbnailDiskCache.get(sourceFile, size);
        assertNull(cachedBefore);

        // 2. Put into cache
        ThumbnailDiskCache.put(sourceFile, size, thumbnail);

        // Get the cache file to clean it up later and verify it exists
        File cacheFile = ThumbnailDiskCache.getCacheFile(sourceFile, size);
        assertNotNull(cacheFile);
        assertTrue(cacheFile.exists());

        // 3. Get from cache (hit)
        BufferedImage cachedAfter = ThumbnailDiskCache.get(sourceFile, size);
        assertNotNull(cachedAfter);
        assertEquals(size, cachedAfter.getWidth());
        assertEquals(size, cachedAfter.getHeight());

        // 4. Stale Handling: Modify the source file's last modified time
        long originalModTime = sourceFile.lastModified();
        sourceFile.setLastModified(originalModTime + 5000); // 5 seconds later
        
        // Now get from cache should return null because lastModified changed (stale)
        BufferedImage cachedStale = ThumbnailDiskCache.get(sourceFile, size);
        assertNull(cachedStale);

        // The old cache file should still exist on disk
        assertTrue(cacheFile.exists());
        
        // Put the new cache
        BufferedImage newThumbnail = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        ThumbnailDiskCache.put(sourceFile, size, newThumbnail);

        // Check that the old cache file is deleted, and the new cache file is created
        assertFalse(cacheFile.exists());
        File newCacheFile = ThumbnailDiskCache.getCacheFile(sourceFile, size);
        assertNotNull(newCacheFile);
        assertTrue(newCacheFile.exists());

        // Clean up test cache files
        newCacheFile.delete();
    }
}
