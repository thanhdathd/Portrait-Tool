# Hướng dẫn đóng gói ứng dụng Windows Installer (.msi)

Tài liệu này hướng dẫn chi tiết cách sử dụng script `build_msi.bat` để đóng gói ứng dụng **Portrait Tool** thành một trình cài đặt Windows `.msi` chuyên nghiệp.

Ứng dụng đóng gói sẽ đi kèm một môi trường Java Runtime Environment (JRE) thu nhỏ (được tối giản hóa qua `jlink`), giúp người dùng cuối có thể chạy ứng dụng trực tiếp mà không cần cài đặt sẵn Java trên máy tính.

---

## 1. Yêu cầu hệ thống & Công cụ cần thiết

Để chạy script thành công, máy tính của bạn cần cài đặt và cấu hình sẵn các công cụ sau trong biến môi trường `PATH`:

### 1.1. Java Development Kit (JDK 17 hoặc JDK 21)
* **Lý do**: Cần thiết để biên dịch mã nguồn và sử dụng công cụ đóng gói `jpackage` + `jlink`.
* **Tải về**: Khuyên dùng [Adoptium Temurin JDK 17 hoặc 21](https://adoptium.net/).
* **Kiểm tra**: Mở terminal (CMD hoặc PowerShell) và chạy lệnh:
  ```bash
  javac -version
  jpackage --version
  ```
* **Lưu ý**: Hãy chắc chắn thư mục `bin` của JDK (ví dụ: `C:\Program Files\Eclipse Adoptium\jdk-17.0.x.x\bin`) đã được thêm vào biến môi trường `PATH` của hệ thống.

### 1.2. WiX Toolset (Phiên bản v3.11 hoặc v3.14)
* **Lý do**: `jpackage` của JDK sử dụng bộ công cụ WiX Toolset bên dưới để tạo ra file `.msi` trên Windows. Script kiểm tra sự tồn tại của `candle.exe`.
* **Tải về**: Tải bản cài đặt WiX Toolset v3 tại [WiX Toolset Releases](https://github.com/wixtoolset/wix3/releases) (Khuyên dùng bản v3.14.1).
* **Kiểm tra**:
  ```bash
  candle -?
  ```
* **Lưu ý**: Sau khi cài đặt WiX, bạn phải thêm đường dẫn đến thư mục cài đặt WiX (mặc định là `C:\Program Files (x86)\WiX Toolset v3.14\bin`) vào biến môi trường `PATH` của Windows.

### 1.3. Apache Maven
* **Lý do**: Dùng để biên dịch và xây dựng file Fat JAR (`.jar`) chứa đầy đủ các thư viện phụ thuộc của ứng dụng.
* **Tải về**: Tải về từ [Apache Maven](https://maven.apache.org/download.cgi) và làm theo hướng dẫn cài đặt.
* **Kiểm tra**:
  ```bash
  mvn -version
  ```

---

## 2. Các bước đóng gói

Bạn có thể chạy đóng gói trực tiếp bằng giao diện hoặc thông qua dòng lệnh:

### Cách 1: Chạy trực tiếp (Mặc định)
1. Truy cập vào thư mục gốc của dự án `Portrait-Tool`.
2. Nhấp đúp chuột vào file `build_msi.bat`.
3. Script sẽ tự động chạy qua 4 bước đóng gói. Sau khi hoàn thành, nhấn phím bất kỳ để kết thúc.

### Cách 2: Chạy thông qua Dòng lệnh (CMD / PowerShell)
Mở cửa sổ CMD/PowerShell tại thư mục gốc của dự án và chạy:
```cmd
build_msi.bat
```

Script hỗ trợ các tham số tùy chọn (arguments) hữu ích để tăng tốc độ đóng gói trong quá trình thử nghiệm:
* **`--skip-maven`**: Bỏ qua bước chạy Maven biên dịch mã nguồn (`mvn clean package`). Script sẽ tái sử dụng file JAR hiện có trong thư mục `target/`.
* **`--skip-runtime`**: Bỏ qua bước phân tích và build lại môi trường JRE tùy chỉnh (`jlink`). Script sẽ tái sử dụng thư mục runtime cũ tại `dist/runtime/`.

**Ví dụ chạy bỏ qua Maven biên dịch (sử dụng lại file jar cũ):**
```cmd
build_msi.bat --skip-maven
```

---

## 3. Quy trình hoạt động của Script

Script thực thi các bước tuần tự như sau:
1. **Kiểm tra công cụ**: Đảm bảo `javac`, `jpackage`, `candle`, và `mvn` có sẵn trong biến môi trường.
2. **Biên dịch mã nguồn (Maven)**: Chạy `mvn clean package` để tạo file Fat JAR chứa tất cả các thư viện của dự án (ví dụ FlatLaf, fastexcel, twelvemonkeys).
3. **Phân tích module (jdeps)**: Chạy `jdeps` quét file JAR để tìm chính xác các module JDK cần thiết để chạy ứng dụng (ví dụ `java.desktop`, `java.logging`, v.v.).
4. **Tạo Runtime tùy chỉnh (jlink)**: Tạo ra thư mục chứa JRE rút gọn tại `dist/runtime`, chỉ gồm các module được tìm thấy ở bước 3 giúp tối ưu kích thước gói cài đặt.
5. **Đóng gói ứng dụng (jpackage)**: Kết hợp file JAR ứng dụng, JRE rút gọn, icon ứng dụng (`app2.ico`), file bản quyền (`LICENSE.rtf`), và file cấu hình liên kết định dạng `.pdw` để tạo ra file `.msi`.
6. **Lưu trữ & Dọn dẹp**: Di chuyển file `.msi` vào thư mục phát hành và xóa các thư mục tạm trung gian.

---

## 4. Kết quả đầu ra

Sau khi chạy script thành công:
* File cài đặt `.msi` sẽ được lưu trữ tại thư mục:
  `dist\release\windows\msi\PortraitTool-2.0.0.msi`
* Chạy file `.msi` này trên bất cứ máy tính Windows nào để cài đặt ứng dụng. Bộ cài đặt sẽ tự động tạo Shortcut trên Desktop, trong Menu Start và đăng ký liên kết mở file `.pdw` bằng Portrait Tool.

---

## 5. Xử lý sự cố thường gặp (Troubleshooting)

| Lỗi hiển thị | Nguyên nhân | Cách khắc phục |
| :--- | :--- | :--- |
| `[ERROR] JDK is not installed...` | Windows chưa cài JDK hoặc chưa cấu hình biến PATH. | Hãy cài đặt JDK và thêm thư mục `bin` vào biến hệ thống `PATH`. |
| `[ERROR] WiX Toolset not found...` | Chưa cài đặt WiX Toolset hoặc chưa cấu hình biến PATH cho WiX. | Hãy cài đặt WiX và thêm đường dẫn `C:\Program Files (x86)\WiX Toolset v3.14\bin` vào biến hệ thống `PATH`. |
| `[ERROR] Maven not found...` | Chưa cài Maven hoặc chưa cấu hình biến PATH cho Maven. | Hãy cài đặt Maven và cấu hình đường dẫn `bin` của Maven trong biến hệ thống `PATH`. |
| `[ERROR] JLINK FAILED!` | Gặp xung đột module hoặc module không tương thích. | Đảm bảo dự án không chứa các thư viện JAR không hợp lệ, chạy `mvn clean` trước khi build. |
| Phím tắt ứng dụng không xuất hiện trên Desktop sau khi cài đặt | Do Windows Explorer chưa làm mới (refresh). | Nhấp chuột phải ngoài Desktop chọn **Refresh** hoặc khởi động lại máy. |
