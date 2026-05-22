package core.image;

import filter.*;

import java.awt.image.BufferedImage;
import java.awt.image.RGBImageFilter;
import java.util.function.BooleanSupplier;

public class ImageProcessor {

    /**
     * Applies the given FilterProperties to the source BufferedImage.
     * @param isCancelled A supplier to check if the process should be aborted early.
     */
    public static BufferedImage applyFilter(BufferedImage source, FilterProperties props, BooleanSupplier isCancelled) {
        if (source == null || props == null) {
            return source;
        }

        String preset = props.getPresetName();

        if ("Default RGB".equals(preset)) {
            return source;
        }

        RGBImageFilter filter = null;
        
        // Mode 0 = RGB, -1 = Black & White (Grayscale)
        if ("Custom".equals(preset)) {
            if (props.getMode() == 0) {
                filter = new RGBFilter(props.red, props.gre, props.blu, props.alp);
            } else {
                if (props.gra < 128) {
                    filter = new RGBGrayFilter(props.red, props.gre, props.blu, props.alp, props.gra);
                } else {
                    filter = new RGBGrayFilter(props.red, props.gre, props.blu, props.alp);
                }
            }
        } else {
            // LOGIC KHI CHỌN PRESET (MAP VÀO CLASS CỤ THỂ)
            switch (preset) {
                case "RGB Black & White":
                    filter = new RGBGrayFilter(100, 100, 100, 100); // Hoặc tham số khởi tạo mặc định của class này
                    break;
                case "Gray with Blue filter":
                    filter = new BlueOutGrayFilter();
                    break;
                case "Gray with Red filter":
                    filter = new RedOutGrayFilter();
                    break;
                case "Gray with Green filter":
                    filter = new GreenOutGrayFilter();
                    break;
                case "Gray with Orange filter":
                    filter = new OrangeOutGrayFilter();
                    break;
                case "Gray with Yelow filter":
                    filter = new YelowOutGrayFilter();
                    break;
                case "Red off":
                    filter = new RedGrayFilter();
                    break;
                case "Green off":
                    filter = new GreenGrayFilter();
                    break;
                case "Blue off":
                    filter = new BlueGrayFilter();
                    break;
            }
        }

        if (filter == null) return source; // Safety check

        int width = source.getWidth();
        int height = source.getHeight();
        
        // Tạo ảnh đích
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // VÒNG LẶP XỬ LÝ PIXEL TRỰC TIẾP
        for (int y = 0; y < height; y++) {

            // KIỂM TRA HỦY: Kiểm tra ở đầu mỗi dòng (row)
            // Không nên kiểm tra trên từng pixel để tránh overhead gọi hàm quá nhiều
            if (isCancelled != null && isCancelled.getAsBoolean()) {
                return null; // Thoát ngay lập tức, giải phóng CPU!
            }

            for (int x = 0; x < width; x++) {
                // Lấy màu gốc
                int rgb = source.getRGB(x, y);

                // Đưa qua bộ lọc của bạn
                int newRgb = filter.filterRGB(x, y, rgb);

                // Gán màu mới vào ảnh đích
                result.setRGB(x, y, newRgb);
            }
        }

        return result;
    }

    // Giữ lại hàm cũ (Overload) cho các trường hợp không cần cancel (ví dụ: Save file)
    public static BufferedImage applyFilter(BufferedImage source, FilterProperties props) {
        return applyFilter(source, props, () -> false);
    }

    public static BufferedImage blur(BufferedImage source, int radius) {
        if (source == null) return null;
        
        int w = source.getWidth();
        int h = source.getHeight();
        
        // Scale down factor: 1/16th of the original size, keeping minimum at 16x16 where possible
        int dw = w < 16 ? w : Math.max(16, w / 16);
        int dh = h < 16 ? h : Math.max(16, h / 16);
        
        int type = source.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : source.getType();
        BufferedImage small = new BufferedImage(dw, dh, type);
        java.awt.Graphics2D g = small.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, dw, dh, null);
        g.dispose();
        
        // Adjust kernel size to fit within downscaled dimensions
        int size = Math.min(5, Math.min(dw, dh));
        if (size >= 3) {
            float weight = 1.0f / (size * size);
            float[] data = new float[size * size];
            java.util.Arrays.fill(data, weight);
            
            java.awt.image.Kernel kernel = new java.awt.image.Kernel(size, size, data);
            java.awt.image.ConvolveOp op = new java.awt.image.ConvolveOp(kernel, java.awt.image.ConvolveOp.EDGE_NO_OP, null);
            
            BufferedImage blurred = op.filter(small, null);
            // Second pass for smoother blur
            blurred = op.filter(blurred, null);
            return blurred;
        } else {
            // Image is too small to convolve, returning the downscaled image is fine since upscaling it back will naturally blur it
            return small;
        }
    }
}