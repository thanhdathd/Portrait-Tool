package ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePreview extends JPanel {
    private BufferedImage image;
    private Icon icon;

    public void setImage(BufferedImage img) {
        this.image = img;
        this.icon = null;
        repaint(); // Yêu cầu vẽ lại UI khi có ảnh mới
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
        this.image = null;
        repaint();
    }

    public void clear() {
        this.image = null;
        this.icon = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Vẽ background

        if (image != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            // BẬT TOÀN BỘ TÍNH NĂNG KHỬ RĂNG CƯA VÀ LÀM MỊN CỦA JAVA 2D
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = image.getWidth();
            int imgH = image.getHeight();

            // Tính toán tỷ lệ để giữ nguyên Aspect Ratio (không bị méo hình)
            double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);

            // Căn giữa ảnh
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;

            // DrawImage lúc này sẽ tận dụng Hardware Acceleration và nội suy Bicubic
            g2d.drawImage(image, x, y, drawW, drawH, null);
            g2d.dispose();
        } else if (icon != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int panelW = getWidth();
            int panelH = getHeight();
            Icon renderIcon = icon;
            if (renderIcon instanceof com.formdev.flatlaf.extras.FlatSVGIcon) {
                renderIcon = ((com.formdev.flatlaf.extras.FlatSVGIcon) renderIcon).derive(128, 128);
            }
            int iconW = renderIcon.getIconWidth();
            int iconH = renderIcon.getIconHeight();

            int x = (panelW - iconW) / 2;
            int y = (panelH - iconH) / 2;

            renderIcon.paintIcon(this, g2d, x, y);
            g2d.dispose();
        }
    }
}
