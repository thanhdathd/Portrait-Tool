package core.fileio;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class ThumbnailDiskCache {
    private static final File CACHE_DIR = new File(System.getProperty("user.home"), ".myapp/thumbnails");
    private static final int MAX_CACHE_FILES = 5000;
    private static final int PRUNE_TO_FILES = 3000;

    static {
        if (!CACHE_DIR.exists()) {
            CACHE_DIR.mkdirs();
        }
    }

    private static String getHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }

    static File getCacheFile(File sourceFile, int size) {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }
        String hash = getHash(sourceFile.getAbsolutePath());
        String fileName = hash + "_" + sourceFile.lastModified() + "_" + sourceFile.length() + "_" + size + ".png";
        return new File(CACHE_DIR, fileName);
    }

    /**
     * Retrieve the cached thumbnail if it exists and is valid.
     */
    public static BufferedImage get(File sourceFile, int size) {
        File cacheFile = getCacheFile(sourceFile, size);
        if (cacheFile == null || !cacheFile.exists()) {
            return null;
        }
        try {
            return ImageIO.read(cacheFile);
        } catch (IOException e) {
            // Delete corrupted or unreadable cached image
            cacheFile.delete();
            return null;
        }
    }

    /**
     * Cache the thumbnail image on disk.
     */
    public static void put(File sourceFile, int size, BufferedImage thumbnail) {
        if (sourceFile == null || !sourceFile.exists() || thumbnail == null) {
            return;
        }
        String hash = getHash(sourceFile.getAbsolutePath());
        File cacheFile = getCacheFile(sourceFile, size);
        if (cacheFile == null) {
            return;
        }

        // Delete any old cached versions of this file for the same size
        File[] oldThumbnails = CACHE_DIR.listFiles((dir, name) -> 
            name.startsWith(hash + "_") && name.endsWith("_" + size + ".png")
        );
        if (oldThumbnails != null) {
            for (File oldFile : oldThumbnails) {
                if (!oldFile.getName().equals(cacheFile.getName())) {
                    oldFile.delete();
                }
            }
        }

        try {
            ImageIO.write(thumbnail, "png", cacheFile);
        } catch (IOException e) {
            System.err.println("Failed to write thumbnail cache: " + e.getMessage());
        }

        // Prune if cache is getting too large
        try {
            pruneCacheIfNeeded();
        } catch (Exception e) {
            // Ignore pruning errors
        }
    }

    private static synchronized void pruneCacheIfNeeded() {
        String[] filenames = CACHE_DIR.list();
        if (filenames != null && filenames.length > MAX_CACHE_FILES) {
            File[] files = CACHE_DIR.listFiles();
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
                int toDelete = files.length - PRUNE_TO_FILES;
                for (int i = 0; i < toDelete; i++) {
                    files[i].delete();
                }
            }
        }
    }
}
