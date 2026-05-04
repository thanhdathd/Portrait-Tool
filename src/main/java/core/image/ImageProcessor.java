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
}