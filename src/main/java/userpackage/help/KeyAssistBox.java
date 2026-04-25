package userpackage.help;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class KeyAssistBox extends Dialog {
    private static final long serialVersionUID = 1L;
    public Dialog box;
    private String[][] data = new String[][]{{"Ctrl+Z", "Undo"}, {"Alt+Z", "Redo"}, {"Alt+S", "Save file"}, {"Alt+O", "Open file"}, {"Shift+Z", "Open/Close Zoom Dialog"}, {"Z", "Zoom tool"}, {"C, T", "Tick point tool"}, {"V, G", "Grid tool"}, {"B, P", "Point 2 Point tool"}, {"H, Space", "Hand tool"}, {"Shift", "Change zoom mode"}, {"Alt+C", "Show/Hide cross line in Zoom Dialog"}, {"Alt+X", "Export Data to *.xls file"}, {"Q", "Open Filter Box"}, {"R", "Show/Hide ruler line in Zoom Dialog"}, {"A", "Show/Hide measurement line in Zoom Dialog"}, {"N", "Show unit on ruler line"}, {"L", "Lock/Unlock mouse move in Zoom Dialog"}, {">", "Increase ruler's amount"}, {"<", "Decrease ruler's amount"}, {"]", "Block increase ruler's amount"}, {"[", "Block decrease ruler's amount"}, {"/", "Reset ruler's amount"}, {"Up/Down Arrow", "Move Up/Down horizontal measurement line "}, {"Left/Right Arrow", "Move Left/Right vertical measurement line"}, {"Page Up", "Move Up horizontal measurement line faster"}, {"Page Down", "Move Down horizontal measurement line faster"}, {"Home", "Move Left vertical measurement line faster"}, {"End", "Move Right vertical measurement line faster"}, {"Numpad 1-9", "Set ruler Amount to 1-9 in centimet"}, {"Left/Right Arrow", "Increase/Decrease Grid size (in grid mode)"}, {"Right Arrow", "Change number position to right (in tick point mode)"}, {"Left Arrow", "Change number position to left (in tick point mode)"}, {"Up Arrow", "Change number position to top (in tick point mode)"}, {"Down Arrow", "Change number position to bottom (in tick point mode)"}, {"1", "Preview image in Default RGB Gray Filter"}, {"2", "Preview image in Gray Filter with Red Chanel off"}, {"3", "Preview image in Gray Filter with Blue Chanel off"}, {"4", "Preview image in Gray Filter with Green Chanel off"}, {"5", "Preview image in Gray Filter with Green Chanel out"}, {"6", "Preview image in Gray Filter with Red Chanel out"}, {"7", "Preview image in Gray Filter with Blue Chanel out"}, {"8", "Preview image in Gray Filter with Red Chanel off, Gray level 255"}, {"9", "Preview image in Gray Filter with Red Chanel off, Gray level 16"}, {"0", "Reset layout"}, {"W", "See programmer"}};
    private String[][] data2 = new String[][]{{"Ctrl+Z", "Hoàn tác"}, {"Alt+Z", "Làm lại"}, {"Alt+S", "Lưu tệp"}, {"Alt+O", "Mở tệp"}, {"Shift+Z", "Đóng/Mở khung thu phóng"}, {"Z", "Phóng to/Thu nhỏ"}, {"C, T", "Công cụ đánh dấu tọa độ điểm"}, {"V, G", "Công cụ kẻ khung lưới"}, {"B, P", "Công cụ đo điểm tới điểm"}, {"H, Space", "Kéo ảnh"}, {"Shift", "Chuyển chế độ zoom"}, {"Alt+C", "Ẩn/Hiện tâm soi trong cửa sổ Zoom"}, {"Alt+X", "Xuất dữ liệu ra file excel"}, {"Q", "Mở hộp thoại Filter"}, {"R", "Ẩn/Hiện thanh thước trong khung Zoom"}, {"A", "Ẩn/Hiện thanh đo ngang-dọc trong khung Zoom"}, {"N", "Hiện đơn vị trên thanh thước"}, {"L", "Khóa/Mở khóa Chuột trong khung Zoom"}, {">", "Tăng đơn vị thanh thước"}, {"<", "Giảm đơn vị thanh thước"}, {"]", "Tăng đơn vị thanh thước (nhiều)"}, {"[", "Giảm đơn vị thanh thước (nhiều)"}, {"/", "Đặt lại đơn vị thanh thước về mặc định"}, {"Up/Down Arrow", "Di chuyển thanh đo ngang lên - xuống"}, {"Left/Right Arrow", "Di chuyển thanh đo dọc sang trái - phải"}, {"Page Up", "Di chuyển thanh đo ngang lên trên (nhanh)"}, {"Page Down", "Di chuyển thanh đo ngang xuống dưới (nhanh)"}, {"Home", "Di chuyển thanh đo dọc sang trái (nhanh)"}, {"End", "Di chuyển thanh đo dọc sang phải (nhanh)"}, {"Numpad 1-9", "Đặt đơn vị thanh thước thành 1-9 cemtimet"}, {"Left/Right Arrow", "Tăng/Giảm kích thước ô lưới(trong chế độ vẽ ô lưới)"}, {"Right Arrow", "Thay đổi vị trí vẽ số sang bên phải (trong chế độ đánh dấu điểm)"}, {"Left Arrow", "Thay đổi vị trí vẽ số sang bên trái (trong chế độ đánh dấu điểm)"}, {"Up Arrow", "Thay đổi vị trí vẽ số lên bên trên (trong chế độ đánh dấu điểm)"}, {"Down Arrow", "Thay đổi vị trí vẽ số xuống bên dưới (trong chế độ đánh dấu điểm)"}, {"1", "Xem trước hình khi qua bộ lọc thang xám mặc định"}, {"2", "Xem trước hình với bộ lọc xám khử màu đỏ (red)"}, {"3", "Xem trước hình với bộ lọc xám khử màu lục (blue)"}, {"4", "Xem trước hình với bộ lọc xám khử màu xanh (green)"}, {"5", "Xem trước hình với bộ lọc xám lấy màu xanh (green)"}, {"6", "Xem trước hình với bộ lọc xám lấy màu đỏ (red)"}, {"7", "Xem trước hình với bộ lọc xám lấy màu lục (blue)"}, {"8", "Xem trước hình với bộ lọc xám khử màu đỏ (red), thang xám 255"}, {"9", "Xem trước hình với bộ lọc xám khử màu đỏ (red), thang xám 16"}, {"0", "Làm mới bộ trình bày"}};
    private String[] columNames = new String[]{"Key", "Function"};
    private String[] columNames2 = new String[]{"Phím tắt", "Chức năng"};
    private boolean viLang;
    private JTable table;

    public KeyAssistBox(Frame f, String title, boolean isModal, boolean viLang) {
        super(f);
        this.box = new Dialog(f, title, isModal);
        this.viLang = viLang;
        this.creatGUI();
        this.processEvent();
    }

    private void processEvent() {
        this.box.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                KeyAssistBox.this.box.dispose();
            }
        });
    }

    private void creatGUI() {
        if (this.viLang) {
            this.table = new JTable(this.data2, this.columNames2);
            this.table.getColumn(this.columNames2[1]).setPreferredWidth(350);
            JScrollPane pane = new JScrollPane(this.table);
            this.box.add(pane);
            this.box.setTitle("Phím tắt");
        } else {
            this.table = new JTable(this.data, this.columNames);
            this.table.getColumn(this.columNames[1]).setPreferredWidth(350);
            JScrollPane pane = new JScrollPane(this.table);
            this.box.add(pane);
        }

        this.box.setBounds(600, 150, 550, 280);
        this.box.setFocusableWindowState(false);
    }
}
