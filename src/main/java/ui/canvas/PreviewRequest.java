package ui.canvas;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class PreviewRequest {
    public BufferedImage imageToProcess; // Ảnh con đã được trích xuất
    public Rectangle originalBounds;     // Tọa độ của ảnh con này so với ảnh gốc (để dán lại cho đúng)

    public PreviewRequest(BufferedImage imageToProcess, Rectangle originalBounds) {
        this.imageToProcess = imageToProcess;
        this.originalBounds = originalBounds;
    }
}
