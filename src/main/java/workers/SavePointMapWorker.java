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
    private String mapTitle;

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

    public SavePointMapWorker(ImageCanvas canvas, File outputFile, double targetWidthCm, int targetDpi, String mapTitle) {
        this(canvas, outputFile);
        this.targetWidthCm = targetWidthCm;
        this.targetDpi = targetDpi;
        this.useTargetParams = true;
        this.mapTitle = mapTitle;
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
        
        // ==========================================
        // --- VẼ CALIBRATION MARKERS VÀ TITLE ---
        // ==========================================
        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 1. Vẽ Corner Markers (4 góc chữ L, nét 1cm, dài 4cm)
        int strokeWidthPx = (int) Math.round(1.0 * pixelsPerCm);
        int armLengthPx = (int) Math.round(4.0 * pixelsPerCm);
        
        // Top-Left
        g2d.fillRect(0, 0, armLengthPx, strokeWidthPx); // Horizontal
        g2d.fillRect(0, 0, strokeWidthPx, armLengthPx); // Vertical
        // Top-Right
        g2d.fillRect(exportWidth - armLengthPx, 0, armLengthPx, strokeWidthPx);
        g2d.fillRect(exportWidth - strokeWidthPx, 0, strokeWidthPx, armLengthPx);
        // Bottom-Left
        g2d.fillRect(0, exportHeight - strokeWidthPx, armLengthPx, strokeWidthPx);
        g2d.fillRect(0, exportHeight - armLengthPx, strokeWidthPx, armLengthPx);
        
        // Punch out text inside Bottom-Left arm to be truly transparent in PNG
        int fontSize12Px = (int) Math.round(12.0 * currentDpi / 72.0);
        int textFontSizePx = (int) Math.round(10.0 * currentDpi / 72.0);
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT));
        
        g2d.setFont(new Font("Arial", Font.PLAIN, textFontSizePx));
        String sizeText = Math.round(widthCm * 10) + " x " + Math.round(heightCm * 10) + " mm";
        
        FontMetrics fm = g2d.getFontMetrics();
        int textAscent = fm.getAscent();
        int textDescent = fm.getDescent();
        int textW = fm.stringWidth(sizeText);
        int armCenterY = exportHeight - strokeWidthPx / 2;
        int textY = armCenterY + (textAscent - textDescent) / 2;
        int textX = armLengthPx / 2 - textW / 2;
        
        g2d.drawString(sizeText, textX, textY);
        
        // Restore composite
        g2d.setComposite(originalComposite);
        
        // Bottom-Right
        g2d.fillRect(exportWidth - armLengthPx, exportHeight - strokeWidthPx, armLengthPx, strokeWidthPx);
        g2d.fillRect(exportWidth - strokeWidthPx, exportHeight - armLengthPx, strokeWidthPx, armLengthPx);

        // 2. Vẽ Calibration Square (2x2 cm ở góc dưới trái)
        int squareSizePx = (int) Math.round(2.0 * pixelsPerCm);
        int squareOffsetPx = (int) Math.round(2.0 * pixelsPerCm);
        int sqX = squareOffsetPx;
        int sqY = exportHeight - squareOffsetPx - squareSizePx;
        
        g2d.setStroke(new BasicStroke((float)(0.05 * pixelsPerCm))); // Nét rỗng, mảnh (khoảng 0.5mm)
        g2d.drawRect(sqX, sqY, squareSizePx, squareSizePx);
        
        // Vạch chia 1cm (ở giữa các cạnh)
        int midX = sqX + squareSizePx / 2;
        int midY = sqY + squareSizePx / 2;
        int tickLen = (int) Math.round(0.2 * pixelsPerCm);
        g2d.drawLine(midX, sqY - tickLen/2, midX, sqY + tickLen/2); // Top
        g2d.drawLine(midX, sqY + squareSizePx - tickLen/2, midX, sqY + squareSizePx + tickLen/2); // Bottom
        g2d.drawLine(sqX - tickLen/2, midY, sqX + tickLen/2, midY); // Left
        g2d.drawLine(sqX + squareSizePx - tickLen/2, midY, sqX + squareSizePx + tickLen/2, midY); // Right

        // Nhãn "2x2 cm" (bên phải hình vuông)
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize12Px));
        int labelX = sqX + squareSizePx + (int) Math.round(0.2 * pixelsPerCm);
        int labelY = sqY + squareSizePx; // Căn đáy nhãn bằng với đáy hình vuông
        g2d.drawString("2x2 cm", labelX, labelY);

        // 3. Vẽ Map Title (nếu có)
        if (mapTitle != null && !mapTitle.trim().isEmpty()) {
            g2d.setFont(new Font("Monospaced", Font.PLAIN, fontSize12Px));
            int titleX = armLengthPx + (int) Math.round(0.5 * pixelsPerCm); // Nằm bên phải góc chữ L dưới cùng bên trái 0.5cm
            int titleY = exportHeight - (int) Math.round(0.5 * pixelsPerCm); // Cách mép dưới 0.5cm
            g2d.drawString(mapTitle, titleX, titleY);
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
