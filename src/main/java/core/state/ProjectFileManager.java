package core.state;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ProjectFileManager {

    private static final String DATA_ENTRY = "data.json";
    private static final String IMAGE_ENTRY = "image.png";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .setPrettyPrinting()
            .create();

    public static class LoadedProject {
        public BufferedImage image;
        public ProjectData data;
    }

    /**
     * Saves the current project state into a compressed .pdw file.
     */
    public static void saveProject(File file, BufferedImage image, ProjectData data) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            // 1. Write JSON Data
            zos.putNextEntry(new ZipEntry(DATA_ENTRY));
            String json = gson.toJson(data);
            zos.write(json.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 2. Write Image
            zos.putNextEntry(new ZipEntry(IMAGE_ENTRY));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            zos.write(baos.toByteArray());
            zos.closeEntry();
        }
    }

    /**
     * Loads a project from a .pdw file.
     */
    public static LoadedProject loadProject(File file) throws IOException {
        LoadedProject project = new LoadedProject();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (DATA_ENTRY.equals(entry.getName())) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
                    project.data = gson.fromJson(reader, ProjectData.class);
                } else if (IMAGE_ENTRY.equals(entry.getName())) {
                    // We must copy the stream because ImageIO.read closes it or gets confused by ZipInputStream
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = zis.read(buffer)) != -1) {
                        baos.write(buffer, 0, n);
                    }
                    project.image = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
                }
                zis.closeEntry();
            }
        }

        if (project.image == null || project.data == null) {
            throw new IOException("Invalid .pdw file: Missing required entries.");
        }
        return project;
    }
}
