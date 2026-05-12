package utils;

public class Utils {
    public static String bytesToHumanReadable(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("Số byte không thể âm");
        }

        // Các đơn vị từ nhỏ đến lớn
        String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};

        if (bytes == 0) {
            return "0 B";
        }

        int unitIndex = 0;
        double size = bytes;

        // Chia cho 1024 cho đến khi size < 1024 hoặc hết đơn vị
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        // Format số với 2 chữ số thập phân nếu cần
        if (size == Math.floor(size)) {
            // Nếu là số nguyên thì không hiển thị phần thập phân
            return String.format("%.0f %s", size, units[unitIndex]);
        } else {
            // Nếu có phần thập phân, hiển thị 2 chữ số
            return String.format("%.2f %s", size, units[unitIndex]);
        }
    }

    public static String formatFileSize(long size) {
        if (size <= 0) return "Unknown";
        String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public static String formatDate(long timestamp) {
        if (timestamp <= 0) return "Unknown";
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(timestamp));
    }

    public static String wrapFileName(String fileName, int maxLineLength, int maxLines) {
        if (fileName.length() <= maxLineLength) return fileName;

        StringBuilder wrapped = new StringBuilder("<html><div style='text-align:left; width:180px;'>");
        int start = 0;
        int lines = 0;
        while (start < fileName.length() && lines < maxLines) {
            int end = Math.min(start + maxLineLength, fileName.length());
            if (end < fileName.length() && lines < maxLines - 1) {
                wrapped.append(fileName.substring(start, end)).append("<br>");
            } else {
                if (end < fileName.length()) {
                    // dòng cuối cùng còn dài -> cắt và thêm ...
                    wrapped.append(fileName.substring(start, Math.min(start + maxLineLength - 3, fileName.length())))
                            .append("...");
                } else {
                    wrapped.append(fileName.substring(start));
                }
            }
            start = end;
            lines++;
            if (lines >= maxLines && start < fileName.length()) break;
        }
        wrapped.append("</div></html>");
        return wrapped.toString();
    }
}
