package ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import core.fileio.ImageFormatHelper;

public class ImagePreviewPanel extends JPanel {
    private final JFileChooser chooser;
    private final ImagePreview previewLabel;
    private final JLabel nameLabel;
    private final JLabel typeLabel;
    private final JLabel sizeLabel;
    private final JLabel modifiedLabel;
    private final JLabel dimensionLabel;
    private final JLabel pointsLabel;
    private final JLabel gridsLabel;
    private final JLabel scaleLabel;
    
    private final JPanel pointsRow;
    private final JPanel gridsRow;
    private final JPanel scaleRow;

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
        metaPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        nameLabel = new JLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));

        typeLabel = new JLabel();
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.ITALIC, 11f));
        typeLabel.setForeground(Color.GRAY);

        sizeLabel = new JLabel();
        modifiedLabel = new JLabel();
        dimensionLabel = new JLabel();
        pointsLabel = new JLabel();
        gridsLabel = new JLabel();
        scaleLabel = new JLabel();

        // Helper to add rows
        metaPanel.add(nameLabel);
        metaPanel.add(Box.createVerticalStrut(5));
        metaPanel.add(typeLabel);
        metaPanel.add(Box.createVerticalStrut(15));
        
        metaPanel.add(createRow("Size", sizeLabel));
        metaPanel.add(createRow("Modified", modifiedLabel));
        metaPanel.add(createRow("Dimension", dimensionLabel));
        metaPanel.add(Box.createVerticalStrut(10));
        
        pointsRow = createRow("Points", pointsLabel);
        gridsRow = createRow("Grids", gridsLabel);
        scaleRow = createRow("Scale", scaleLabel);
        
        metaPanel.add(pointsRow);
        metaPanel.add(gridsRow);
        metaPanel.add(scaleRow);

        // Hide project rows by default
        pointsRow.setVisible(false);
        gridsRow.setVisible(false);
        scaleRow.setVisible(false);

        // Sắp xếp
        add(previewLabel);
        add(metaPanel);
        add(Box.createVerticalGlue());

        // Lắng nghe sự kiện chọn file
        chooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
                evt -> updatePreview(chooser.getSelectedFile()));
    }

    private JPanel createRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(210, 20));
        row.setOpaque(false);
        
        JLabel keyLabel = new JLabel(label + ":");
        keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD, 11f));
        keyLabel.setForeground(new Color(100, 100, 100));
        
        valueLabel.setFont(valueLabel.getFont().deriveFont(11f));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        row.add(keyLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
    }

    private void updatePreview(File file) {
        if (file == null || !isSupportedFile(file)) {
            clearPreview();
            return;
        }

        new SwingWorker<BufferedImage, Void>() {
            private int imgWidth, imgHeight;
            private long fileSize;
            private long lastModified;
            private int pointsCount = -1;
            private int gridsCount = -1;
            private double scaleVal = -1;
            private boolean isPdw = false;

            @Override
            protected BufferedImage doInBackground() {
                fileSize = file.length();
                lastModified = file.lastModified();
                try {
                    isPdw = file.getName().toLowerCase().endsWith(".pdw");
                    BufferedImage original = null;
                    
                    if (isPdw) {
                        try {
                            core.state.ProjectFileManager.LoadedProject project = core.state.ProjectFileManager.loadProject(file);
                            original = project.image;
                            if (project.data != null) {
                                pointsCount = project.data.stickyPoints != null ? project.data.stickyPoints.size() : 0;
                                gridsCount = project.data.grids != null ? project.data.grids.size() : 0;
                                scaleVal = project.data.scale;
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to parse PDW for preview: " + e.getMessage());
                        }
                    } else {
                        original = ImageIO.read(file);
                    }

                    if (original != null) {
                        imgWidth = original.getWidth();
                        imgHeight = original.getHeight();
                        // Chỉ scale xuống nếu ảnh thực sự quá lớn (để tiết kiệm RAM)
                        final int MAX_SAFE_RESOLUTION = 800;

                        if (imgWidth > MAX_SAFE_RESOLUTION || imgHeight > MAX_SAFE_RESOLUTION) {
                            double scale = Math.min((double) MAX_SAFE_RESOLUTION / imgWidth,
                                    (double) MAX_SAFE_RESOLUTION / imgHeight);
                            int w = (int) (imgWidth * scale);
                            int h = (int) (imgHeight * scale);

                            Image tempImg = original.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                            BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                            Graphics2D g2d = scaled.createGraphics();
                            g2d.drawImage(tempImg, 0, 0, null);
                            g2d.dispose();
                            return scaled;
                        }

                        return original; 
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
                    if (isPdw) {
                        typeLabel.setText("<html><div style=\"color:#10acd3; width:180px; text-align:center;\"><b>PDW Portrait Data Work File</b></div></html>");
                    } else {
                        typeLabel.setText(ext + " Image");
                    }
                    sizeLabel.setText(formatFileSize(fileSize));
                    modifiedLabel.setText(formatDate(lastModified));

                    if (isPdw && pointsCount >= 0) {
                        pointsLabel.setText(String.valueOf(pointsCount));
                        gridsLabel.setText(String.valueOf(gridsCount));
                        scaleLabel.setText(String.format("%.2f", scaleVal));
                        
                        pointsRow.setVisible(true);
                        gridsRow.setVisible(true);
                        scaleRow.setVisible(true);
                    } else {
                        pointsRow.setVisible(false);
                        gridsRow.setVisible(false);
                        scaleRow.setVisible(false);
                    }

                    if (imgWidth > 0 && imgHeight > 0) {
                        dimensionLabel.setText(imgWidth + " x " + imgHeight);
                    } else {
                        dimensionLabel.setText("N/A");
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
        
        pointsRow.setVisible(false);
        gridsRow.setVisible(false);
        scaleRow.setVisible(false);
    }

    private boolean isSupportedFile(File f) {
        String name = f.getName().toLowerCase();
        int lastDot = name.lastIndexOf('.');
        if (lastDot == -1) return false;
        String ext = name.substring(lastDot + 1);
        return ImageFormatHelper.getSupportedExtensions().contains(ext);
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
