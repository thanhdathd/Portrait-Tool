package ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

public class ImagePreviewPanel extends JPanel {
    private final JFileChooser chooser;
    private final ImagePreview previewLabel;
    private final JLabel nameLabel;
    private final JLabel typeLabel;
    private final JLabel sizeLabel;
    private final JLabel modifiedLabel;
    private final JLabel dimensionLabel;

    private static final int PREVIEW_MAX_SIZE = 220;
    private static final int NAME_MAX_CHARS_PER_LINE = 25;
    private static final int NAME_MAX_LINES = 3;

    public ImagePreviewPanel(JFileChooser chooser) {
        this.chooser = chooser;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Preview"));
        setPreferredSize(new Dimension(230, 500));

        // Thumbnail label
        previewLabel = new ImagePreview();
        previewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        previewLabel.setPreferredSize(new Dimension(230, 200));
        previewLabel.setMinimumSize(new Dimension(220, 150));

        // Metadata panel
        JPanel metaPanel = new JPanel();
        metaPanel.setLayout(new BoxLayout(metaPanel, BoxLayout.Y_AXIS));
        metaPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameLabel = new JLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setMaximumSize(new Dimension(180, Integer.MAX_VALUE));

        typeLabel = new JLabel();
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        typeLabel.setFont(typeLabel.getFont().deriveFont(11f));

        sizeLabel = new JLabel();
        sizeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sizeLabel.setFont(sizeLabel.getFont().deriveFont(11f));

        modifiedLabel = new JLabel();
        modifiedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        modifiedLabel.setFont(modifiedLabel.getFont().deriveFont(11f));

        dimensionLabel = new JLabel();
        dimensionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dimensionLabel.setFont(dimensionLabel.getFont().deriveFont(11f));

        metaPanel.add(nameLabel);
        metaPanel.add(Box.createVerticalStrut(5));
        metaPanel.add(typeLabel);
        metaPanel.add(sizeLabel);
        metaPanel.add(modifiedLabel);
        metaPanel.add(dimensionLabel);

        // Sắp xếp
        add(previewLabel);
        add(Box.createVerticalStrut(60));
        add(metaPanel);
        add(Box.createVerticalGlue());

        // Lắng nghe sự kiện chọn file
        chooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
                evt -> updatePreview(chooser.getSelectedFile()));
    }

    private void updatePreview(File file) {
        if (file == null || !isImageFile(file)) {
            clearPreview();
            return;
        }

        new SwingWorker<BufferedImage, Void>() {
            private int imgWidth, imgHeight;
            private long fileSize;
            private long lastModified;

            @Override
            protected BufferedImage doInBackground() {
                fileSize = file.length();
                lastModified = file.lastModified();
                try {
                    BufferedImage original = ImageIO.read(file);
                    if (original != null) {
                        imgWidth = original.getWidth();
                        imgHeight = original.getHeight();
                        // Chỉ scale xuống nếu ảnh thực sự quá lớn (để tiết kiệm RAM)
                        // KHÔNG cần phải scale chính xác bằng kích thước thumbnail
                        final int MAX_SAFE_RESOLUTION = 800;

                        if (imgWidth > MAX_SAFE_RESOLUTION || imgHeight > MAX_SAFE_RESOLUTION) {
                            double scale = Math.min((double) MAX_SAFE_RESOLUTION / imgWidth,
                                    (double) MAX_SAFE_RESOLUTION / imgHeight);
                            int w = (int) (imgWidth * scale);
                            int h = (int) (imgHeight * scale);

                            // Dùng hàm SCALE_SMOOTH mặc định của Java (chậm nhưng chất lượng hoàn hảo)
                            Image tempImg = original.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                            BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                            Graphics2D g2d = scaled.createGraphics();
                            g2d.drawImage(tempImg, 0, 0, null);
                            g2d.dispose();
                            return scaled;
                        }

                        return original; // Nếu ảnh nhỏ hơn 800px, giữ nguyên
                    }
                } catch (Exception e) {
                    // ignore
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    if (img != null) {
                        previewLabel.setImage(img);
                    } else {
                        previewLabel.clear();
                    }

                    String ext = "";
                    int dot = file.getName().lastIndexOf('.');
                    if (dot > 0) ext = file.getName().substring(dot + 1).toUpperCase();

                    nameLabel.setText( wrapFileName(file.getName(), NAME_MAX_CHARS_PER_LINE, NAME_MAX_LINES) );
                    typeLabel.setText("Type: " + ext + " Image");
                    sizeLabel.setText("Size: " + formatFileSize(fileSize));
                    modifiedLabel.setText("Modified: " + formatDate(lastModified));

                    if (imgWidth > 0 && imgHeight > 0) {
                        dimensionLabel.setText("Dimension: " + imgWidth + " x " + imgHeight);
                    } else {
                        dimensionLabel.setText("Dimension: N/A");
                    }
                } catch (Exception ex) {
                    clearPreview();
                }
            }
        }.execute();
    }

    private void clearPreview() {
        previewLabel.clear();
        nameLabel.setText("");
        typeLabel.setText("");
        sizeLabel.setText("");
        modifiedLabel.setText("");
        dimensionLabel.setText("");
    }

    private boolean isImageFile(File f) {
        String name = f.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp") || name.endsWith(".webp");
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "Unknown";
        String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private String formatDate(long timestamp) {
        if (timestamp <= 0) return "Unknown";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(timestamp));
    }

    private String wrapFileName(String fileName, int maxLineLength, int maxLines) {
        if (fileName.length() <= maxLineLength) return fileName;

        StringBuilder wrapped = new StringBuilder("<html><div style='text-align:left; width:180px;'>");
        int start = 0;
        int lines = 0;
        while (start < fileName.length() && lines < maxLines) {
            int end = Math.min(start + maxLineLength, fileName.length());
            if (end < fileName.length() && lines < maxLines - 1) {
                wrapped.append(fileName, start, end).append("<br>");
            } else {
                if (end < fileName.length()) {
                    wrapped.append(fileName.substring(start, Math.min(start + maxLineLength - 3, fileName.length())))
                            .append("...");
                } else {
                    wrapped.append(fileName.substring(start));
                }
            }
            start = end;
            lines++;
        }
        wrapped.append("</div></html>");
        return wrapped.toString();
    }
}
