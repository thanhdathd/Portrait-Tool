# Hướng dẫn Đóng gói Ứng dụng (MSI Installer)

Tài liệu này hướng dẫn chi tiết cách chạy script `build_msi.bat` để đóng gói ứng dụng **Portrait-Tool** thành bộ cài đặt dạng `.msi` chạy trên hệ điều hành Windows.

---

## 1. Yêu cầu Hệ thống & Công cụ

Để chạy thành công script đóng gói, máy tính của bạn cần được cài đặt sẵn các công cụ sau và thêm chúng vào biến môi trường `PATH`:

### 1.1. Java Development Kit (JDK 17 hoặc cao hơn)
*   Khuyên dùng **JDK 17** hoặc **JDK 21** từ [Adoptium (Temurin)](https://adoptium.net/).
*   Đảm bảo lệnh `javac` và `jpackage` hoạt động trong terminal:
    ```powershell
    javac --version
    jpackage --version
    ```
*   Biến môi trường `JAVA_HOME` phải được cấu hình chính xác và `JAVA_HOME\bin` phải nằm trong `PATH`.

### 1.2. WiX Toolset (v3.14.1 hoặc cao hơn)
*   `jpackage` của JDK yêu cầu WiX Toolset để tạo file cài đặt `.msi` trên Windows.
*   Tải bản cài đặt từ [WiX Toolset Releases](https://github.com/wixtoolset/wix3/releases) (Khuyên dùng bản v3.14.1).
*   Sau khi cài đặt, hãy thêm thư mục cài đặt của WiX (ví dụ: `C:\Program Files (x86)\WiX Toolset v3.11\bin`) vào biến môi trường `PATH` của hệ thống để lệnh `candle` có thể chạy được:
    ```powershell
    candle --version
    ```

### 1.3. Apache Maven
*   Dùng để build và đóng gói mã nguồn Java thành file fat JAR.
*   Tải và cài đặt từ [Apache Maven](https://maven.apache.org/download.cgi).
*   Kiểm tra với lệnh:
    ```powershell
    mvn --version
    ```

---

## 2. Các tham số hỗ trợ của script

Script `build_msi.bat` hỗ trợ một số tham số hữu ích để tăng tốc độ đóng gói khi phát triển:

*   Không truyền tham số: Thực hiện toàn bộ các bước từ clean, build Maven, tạo JRE runtime tùy chỉnh, tới build MSI installer.
*   `--skip-maven`: Bỏ qua bước build project bằng Maven. Hữu ích nếu bạn vừa chạy lệnh build thủ công hoặc chỉ thay đổi tài nguyên đóng gói (như icon, license) mà không đổi code Java. File JAR cũ trong thư mục `target/` sẽ được sử dụng lại.
*   `--skip-runtime`: Bỏ qua bước phân tích `jdeps` và đóng gói JRE bằng `jlink`. Script sẽ tái sử dụng thư mục JRE runtime đã tạo trước đó tại `dist/runtime`. Tiết kiệm rất nhiều thời gian chạy.

Ví dụ chạy bỏ qua cả build code và build runtime:
```powershell
.\build_msi.bat --skip-maven --skip-runtime
```

---

## 3. Quy trình Đóng gói của Script

Script hoạt động tự động qua 4 bước chính:

1.  **Bước 1: Build Fat JAR**
    Chạy lệnh `mvn clean package` để biên dịch toàn bộ dự án và tạo ra file JAR chứa đầy đủ các thư viện phụ thuộc (`target/portrai-tool-2.0.0.jar`).
2.  **Bước 2: Phân tích Dependencies (`jdeps`)**
    Sử dụng công cụ `jdeps` quét file JAR để tìm chính xác các module JDK (như `java.desktop`, `java.logging`,...) mà ứng dụng thực sự sử dụng.
3.  **Bước 3: Tạo JRE Runtime Thu nhỏ (`jlink`)**
    Tạo ra một bộ JRE siêu gọn nhẹ chỉ chứa các module được phát hiện ở Bước 2 tại thư mục `dist/runtime`. Việc này giúp giảm dung lượng bộ cài đặt MSI (chỉ khoảng 30-40 MB thay vì hơn 150 MB của một JRE đầy đủ).
4.  **Bước 4: Đóng gói Installer (`jpackage`)**
    Sử dụng công cụ `jpackage` để gói file JAR và JRE runtime thu nhỏ kèm theo:
    *   **Icon ứng dụng**: `resource-for-packaging/app2.ico`
    *   **Thỏa thuận bản quyền**: `resource-for-packaging/LICENSE.rtf`
    *   **Liên kết file**: Tự động đăng ký định dạng file `.pdw` với hệ điều hành thông qua cấu hình `resource-for-packaging/file-associations.properties`.

---

## 4. Kết quả đầu ra

Sau khi chạy xong, bộ cài đặt MSI sẽ được copy vào thư mục:
```text
dist/release/windows/msi/PortraitTool-2.0.0.msi
```

Bạn có thể kích đúp vào file `.msi` này để cài đặt ứng dụng vào máy tính như một phần mềm Windows thông thường. Khi cài đặt xong, ứng dụng sẽ có:
*   Phím tắt trên Desktop và trong Menu Start.
*   Hỗ trợ chọn thư mục cài đặt tùy ý.
*   Tính năng gỡ cài đặt (Uninstall) tích hợp trong Control Panel của Windows.
*   Kích đúp vào file `.pdw` để tự động mở bằng Portrait-Tool.
