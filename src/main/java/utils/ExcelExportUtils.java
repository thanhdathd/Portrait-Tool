package utils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.dhatim.fastexcel.ConditionalFormattingExpressionRule;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import userpackage.SPoint;

import javax.swing.*;

public class ExcelExportUtils {

    public static void exportToXlsx(List<SPoint> points, float scale, OutputStream os) {

        List<SPoint> sorted = points.stream().sorted((p1,p2) -> {
            if(p1.id == 1) return -1;
            if(p2.id == 1) return 1;

            return Integer.compare(p1.X, p2.X);
        }).toList();

        int size = sorted.size();


        // 1. Khởi tạo Workbook
        try (Workbook wb = new Workbook(os, "PointApp", "1.0")) {
            // 2. Tạo Worksheet
            Worksheet ws = wb.newWorksheet("Sticky Points Data");

            // khu vực thông tin kích thước
            ws.value(2,0, "rộng tranh");
            ws.value(2,1, "rộng ảnh");
            ws.value(2,2, "Project name");
            ws.width(2, 20);
            ws.style(2,2).bold().set();
            ws.value(3,0, 42);
            ws.value(3,1, 1250);
            ws.value(8,0, "Kích thước giấy");
            ws.value(9,0, "A2");

            // 3. Tạo định dạng cho Header (In đậm)
            ws.value(6,4, "toa do");
            ws.range(6,4,6,5).merge();
            ws.range(6,4,6,5).style().horizontalAlignment("center").fillColor("00A5E8").set();

            ws.value(6, 6, "so do quy doi");
            ws.range(6,6,6,7).merge();
            ws.range(6,6,6,7).style().horizontalAlignment("center").fillColor("00A5E8").set();

            ws.value(6, 9, "Lam tron");
            ws.range(6,9,6,11).merge();
            ws.range(6,9,6,11).style()
                    .horizontalAlignment("center")
                    .fillColor("00A5E8")
                    .borderStyle("thin")
                    .set();

            ws.range(6,3,7,17).style().borderStyle("thin").set();

            ws.value(6, 12, "Progress");
            ws.style(6,12).bold().fillColor("00A5E8").set();

            ws.value(6,13, "toa do" );
            ws.range(6,13,6,15).merge();
            ws.range(6,13,6,15).style().horizontalAlignment("center").fillColor("00A5E8").set();

            // Áp dụng style cho dòng đầu tiên
            ws.style(7, 3).bold().fillColor("00A5E8").set();
            ws.style(7, 4).bold().fillColor("00A5E8").set();
            ws.style(7, 5).bold().fillColor("00A5E8").set();
            ws.style(7, 6).bold().fillColor("00A5E8").set();
            ws.style(7, 7).bold().fillColor("00A5E8").set();
            ws.style(7, 8).bold().fillColor("00A5E8").set();
            ws.style(7, 9).bold().fillColor("00A5E8").set();
            ws.style(7, 10).bold().fillColor("00A5E8").set();
            ws.style(7, 11).bold().fillColor("00A5E8").set();

            ws.style(7, 13).bold().fillColor("99FFCC").set();
            ws.style(7, 14).bold().fillColor("99FFCC").set();
            ws.style(7, 15).bold().fillColor("99FFCC").set();
            ws.style(7, 16).bold().fillColor("99FFCC").set();

            // 4. Ghi Tiêu đề cột
            ws.value(7, 3, "Point");
            ws.value(7, 4, "X");
            ws.value(7, 5, "Y");
            ws.value(7, 6, "Ngang"); // Cột tính toán
            ws.value(7, 7, "Doc");
            ws.value(7, 8, "BS line");
            ws.value(7, 9, "ngang");
            ws.value(7, 10, "up - down");
            ws.value(7, 11, "doc");

            ws.value(7, 13, "point");
            ws.value(7, 14, "X");
            ws.value(7, 15, "Y");
            ws.value(7, 16, "progress percent");

            // chỉnh độ rộng cột
            ws.width(6, 15);
            ws.width(7, 15);
            ws.width(8, 15);


            // Ghi tỷ lệ
            ws.value(4, 3, "Ty le");
            ws.value(5, 3, scale);

            // format %
            ws.range(8,16,size+8,16).style().format("0.0%").set();

            // conditional format
            ws.range(8,10, size + 8, 10).style().fillColor("99FFCC")
                    .set(new ConditionalFormattingExpressionRule("K9>0", true));



            // 5. Đổ dữ liệu từ danh sách SPoint
            int row = 8;
            int index = 1;
            for (SPoint p : sorted) {
                ws.value(row, 3, p.id);
                ws.value(row, 4, p.X);
                ws.value(row, 5, p.Y);

                // Ghi CÔNG THỨC: Excel sẽ tự tính X * Y cho từng dòng
                // Ví dụ: formula = "E8*$D$5"
                String formula1 = String.format("E%d*$D$6", row +1);
                ws.formula(row, 6, formula1);
                String formula2 = String.format("F%d*$D$6", row+1);
                ws.formula(row, 7, formula2);

                String bsLineFomular = String.format("$H$9-H%d", row+1);
                ws.formula(row, 8, bsLineFomular);

                String ngangLamTron = String.format("MROUND(G%1$d, 0.05)", row + 1);
                ws.formula(row, 9, ngangLamTron);

                String updownCeil = String.format("MROUND(I%1$d, 0.05 * SIGN(I%1$d))", row+1);
                ws.formula(row, 10, updownCeil);

                String docCeil = String.format("MROUND(H%1$d, 0.05)", row+1);
                ws.formula(row, 11, docCeil);

                ws.value(row, 13, p.id);
                ws.value(row, 14, p.X);
                ws.value(row, 15, p.Y);

                ws.value(row, 17, index);

                String progressPercent = String.format("MROUND((R%2$d/%1$d), 0.0005)",size, row+1);
                ws.formula(row, 16, progressPercent);

                row++;
                index++;
            }

            // Lưu ý: Với FastExcel, Workbook sẽ tự 'finish' khi thoát khối try-with-resources
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Trong ExcelExportUtils.java
    public static void exportToCsv(List<SPoint> points, PrintWriter pw) {
        pw.println("ID\tX\tY");
        for (SPoint p : points) {
            pw.printf("%d\t%d\t%d\n", p.id, p.X, p.Y);
        }
    }
}
