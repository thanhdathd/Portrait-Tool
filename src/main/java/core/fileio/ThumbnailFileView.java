package core.fileio;

import javax.swing.*;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class ThumbnailFileView extends FileView {
    private final ConcurrentHashMap<String, Icon> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> loadingFlags = new ConcurrentHashMap<>();
    private int iconSize;
    private final JFileChooser chooser;
    private final Icon placeholderIcon;
    private Icon pdwIcon;

    public ThumbnailFileView(JFileChooser chooser, int iconSize) {
        this.chooser = chooser;
        this.iconSize = iconSize;
        this.placeholderIcon = createPlaceholderIcon();
    }

    // ... các phương thức createPlaceholderIcon, createResizedIcon, isImageFile, createThumbnailIcon như cũ ...

    private Icon createPlaceholderIcon() {
        BufferedImage img = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(200, 200, 200, 80));
        g.fillRect(0, 0, iconSize, iconSize);
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, iconSize - 1, iconSize - 1);
        g.dispose();
        return new ImageIcon(img);
    }

    private Icon createResizedIcon(Icon originalIcon) {
        if (originalIcon == null) return placeholderIcon;
        BufferedImage img = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, iconSize, iconSize);
        int w = originalIcon.getIconWidth();
        int h = originalIcon.getIconHeight();
        int x = (iconSize - w) / 2;
        int y = (iconSize - h) / 2;
        originalIcon.paintIcon(null, g, x, y);
        g.dispose();
        return new ImageIcon(img);
    }

    @Override
    public Icon getIcon(File f) {
        String path = f.getAbsolutePath();

        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        if (f.isDirectory()) {
            Icon dirIcon = UIManager.getIcon("FileView.directoryIcon");
            Icon resized = createResizedIcon(dirIcon);
            cache.put(path, resized);
            return resized;
        }

        if (f.getName().toLowerCase().endsWith(".pdw")) {
            if (pdwIcon == null) {
                pdwIcon = new FlatSVGIcon("icons/pdw_file.svg", iconSize, iconSize);
            }
            return pdwIcon;
        }

        if (!isImageFile(f)) {
            Icon fileIcon = UIManager.getIcon("FileView.fileIcon");
            Icon resized = createResizedIcon(fileIcon);
            cache.put(path, resized);
            return resized;
        }

        if (!loadingFlags.containsKey(path)) {
            loadingFlags.put(path, true);
            new ThumbnailLoader(f, path).execute();
        }
        return placeholderIcon;
    }

    private boolean isImageFile(File f) {
        String name = f.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp") || name.endsWith(".webp");
    }

    private Icon createThumbnailIcon(BufferedImage original) {
        int imgW = original.getWidth();
        int imgH = original.getHeight();
        double scale = Math.min((double) iconSize / imgW, (double) iconSize / imgH);
        int scaledW = (int) (imgW * scale);
        int scaledH = (int) (imgH * scale);
        Image scaledImage = original.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
        BufferedImage thumbnail = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = thumbnail.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, iconSize, iconSize);
        int x = (iconSize - scaledW) / 2;
        int y = (iconSize - scaledH) / 2;
        g.drawImage(scaledImage, x, y, scaledW, scaledH, null);
        g.dispose();
        return new ImageIcon(thumbnail);
    }


    private class ThumbnailLoader extends SwingWorker<Icon, Void> {
        private final File file;
        private final String path;

        ThumbnailLoader(File file, String path) {
            this.file = file;
            this.path = path;
        }

        @Override
        protected Icon doInBackground() {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img == null) return null;
                return createThumbnailIcon(img);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void done() {
            Icon icon;
            try {
                icon = get();
                if (icon == null) {
                    icon = createResizedIcon(UIManager.getIcon("FileView.fileIcon"));
                }
            } catch (Exception e) {
                icon = createResizedIcon(UIManager.getIcon("FileView.fileIcon"));
            }
            cache.put(path, icon);
            loadingFlags.remove(path);
            chooser.repaint();
        }
    }

    @Override
    public String getDescription(File f) { return null; }
    @Override
    public String getName(File f) { return f.getName(); }
    @Override
    public String getTypeDescription(File f) { return null; }
}