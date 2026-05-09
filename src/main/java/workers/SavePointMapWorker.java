package workers;

import core.state.AppState;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import ui.canvas.ImageCanvas;
import ui.canvas.RenderUtils;
import userpackage.SPoint;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Saves the point map (SPoints with markers on a transparent background) to a file.
 */
public class SavePointMapWorker extends SwingWorker<Void, Void> {
    private final ImageCanvas canvas;
    private final File outputFile;
    private final int width;
    private final int height;

    private double targetWidthCm;
    private int targetDpi = 300;
    private boolean useTargetParams = false;

    public SavePointMapWorker(ImageCanvas canvas, File outputFile) {
        this.canvas = canvas;
        this.outputFile = outputFile;
        BufferedImage originalImage = canvas.getBackgroundImage();
        if (originalImage != null) {
            this.width = originalImage.getWidth();
            this.height = originalImage.getHeight();
        } else {
            this.width = canvas.getWidth();
            this.height = canvas.getHeight();
        }
    }

    public SavePointMapWorker(ImageCanvas canvas, File outputFile, double targetWidthCm, int targetDpi) {
        this(canvas, outputFile);
        this.targetWidthCm = targetWidthCm;
        this.targetDpi = targetDpi;
        this.useTargetParams = true;
    }

    private static final double CM_PER_INCH = 2.54;

    @Override
    protected Void doInBackground() throws Exception {
        AppState appState = canvas.getAppState();
        List<SPoint> sPoints = appState.getCanvasState().getStickyPoints();
        float originalScale = appState.getScale(); // cm/px

        if (originalScale <= 0) originalScale = 0.01f; // Minimal fallback

        // 1. Calculate physical size and target DPI
        double widthCm;
        double currentDpi;
        
        if (useTargetParams) {
            widthCm = targetWidthCm;
            currentDpi = targetDpi;
        } else {
            widthCm = width * originalScale;
            currentDpi = 300; // Default
        }
        System.out.println("widthCm: " + widthCm);
        double heightCm = widthCm * ((double) height / width);
        System.out.println("heightCm: " + heightCm);
        // 2. Calculate target pixel dimensions
        double pixelsPerCm = currentDpi / CM_PER_INCH;
        int exportWidth = (int) Math.round(widthCm * pixelsPerCm);
        int exportHeight = (int) Math.round(heightCm * pixelsPerCm);
        
        // Scale for marker rendering (cm/px)
        float exportScale = (float) (1.0 / pixelsPerCm);
        
        // Coordinate transformation factor (original px to export px)
        // Original px to cm: * originalScale
        // Cm to export px: * pixelsPerCm
        double coordFactor = pixelsPerCm * originalScale;
        
        // If we used custom width, the coord factor needs adjustment if the originalScale 
        // doesn't match the widthCm/width ratio. 
        // Actually, if widthCm is what the user wants, and width is the original pixel width,
        // then the "effective original scale" for this export is widthCm / width.
        double effectiveOriginalScale = widthCm / width;
        coordFactor = pixelsPerCm * effectiveOriginalScale;

        // Create transparent image (TYPE_INT_ARGB)
        BufferedImage result = new BufferedImage(exportWidth, exportHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        
        // Use high quality rendering for smooth markers
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        for (SPoint p : sPoints) {
            // Translate original coordinates to the 300 DPI space
            int scaledX = (int) Math.round(p.X * coordFactor);
            int scaledY = (int) Math.round(p.Y * coordFactor);
            
            // Create a temporary SPoint for rendering at the new resolution
            SPoint sp = new SPoint(p.id, scaledX, scaledY, p.c);
            RenderUtils.drawPointMarker(g2d, sp, exportScale);
        }
        
        g2d.dispose();

        // ==========================================
        // 4. KIỂM TRA ĐỊNH DẠNG VÀ XUẤT FILE (LOGIC MỚI)
        // ==========================================
        String fileName = outputFile.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            // XUẤT RA PDF
            exportAsPDF(result, widthCm, heightCm, outputFile);
        } else {
            // MẶC ĐỊNH XUẤT RA PNG
            ImageIO.write(result, "png", outputFile);
        }
        return null;
    }

    /**
     * Hàm phụ trợ để đóng gói ảnh vào file PDF với kích thước trang chuẩn xác
     */
    private void exportAsPDF(BufferedImage image, double widthCm, double heightCm, File outputFile) throws Exception {
        // Chuyển đổi từ Cm sang Points (1 inch = 2.54 cm = 72 points)
        float pdfWidthPts = (float) ((widthCm / CM_PER_INCH) * 72);
        float pdfHeightPts = (float) ((heightCm / CM_PER_INCH) * 72);

        try (PDDocument document = new PDDocument()) {
            // Tạo trang PDF với kích thước tùy chỉnh do user chọn
            PDPage page = new PDPage(new PDRectangle(pdfWidthPts, pdfHeightPts));
            document.addPage(page);

            // Chuyển BufferedImage (có Alpha/Transparent) sang object của PDFBox
            // LosslessFactory sẽ giữ nguyên độ trong suốt (Alpha channel)
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);

            // Vẽ ảnh lên trang PDF sao cho phủ kín toàn bộ tờ giấy
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, 0, 0, pdfWidthPts, pdfHeightPts);
            }

            document.save(outputFile);
        }
    }

    @Override
    protected void done() {
        try {
            get();
            JOptionPane.showMessageDialog(null, "Point map successfully saved to:\n" + outputFile.getAbsolutePath(), "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to save point map: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
