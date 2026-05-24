# Hướng dẫn đóng gói ứng dụng (Packaging Guide)

Tài liệu này hướng dẫn cách đóng gói **PortraitTool** thành file cài đặt Windows (.msi) chuyên nghiệp bằng công cụ `jpackage` (đi kèm với JDK 14+).

## 0. Chuẩn bị môi trường & Tài nguyên

### A. Cài đặt WiX Toolset (Bắt buộc để tạo MSI)
`jpackage` phụ thuộc vào WiX Toolset để tạo bộ cài đặt Windows.
1.  Truy cập [github.com/wixtoolset/wix3/releases](https://github.com/wixtoolset/wix3/releases) và tải xuống bản **WiX Toolset v3.11** (file `wix311.exe` hoặc bản 3.x mới nhất).
2.  Chạy file `.exe` để cài đặt.
3.  **Quan trọng**: Sau khi cài đặt, bạn phải thêm thư mục `bin` của WiX (ví dụ: `C:\Program Files (x86)\WiX Toolset v3.11\bin`) vào biến môi trường **PATH** của hệ thống để `jpackage` có thể gọi được các lệnh `candle` và `light`.

### B. Chuẩn bị file biểu tượng (.ico)
Ứng dụng cần một file `.ico` chứa nhiều kích thước khác nhau để hiển thị đẹp trên mọi vùng của Windows (Taskbar, Desktop, Explorer).
1.  Chuẩn bị một file ảnh logo dạng `.png` hình vuông (khuyên dùng 512x512 hoặc 1024x1024).
2.  Truy cập trang [icoconverter.com](https://www.icoconverter.com/).
3.  Upload file PNG của bạn.
4.  Tại mục **Sizes**, hãy chọn **tất cả** các kích thước: `16, 32, 48, 64, 128, 256 pixels`.
5.  Tại mục **Bit depth**, chọn `32 bits`.
6.  Nhấn **Convert** và tải file về, đổi tên thành `pdw.ico` và đặt vào thư mục `resources/`.

---

## 1. Build JAR Artifact bằng IntelliJ IDEA

Trước khi đóng gói, bạn cần tạo file JAR "fat-jar" (chứa code và các thư viện liên quan) bằng IntelliJ:

1.  Mở **File > Project Structure** (Ctrl+Alt+Shift+S).
2.  Chọn mục **Artifacts** ở cột bên trái.
3.  Nhấn nút **+** > **JAR** > **From modules with dependencies...**
4.  Tại mục **Main Class**, nhấn biểu tượng thư mục và chọn `core.launcher.AppLauncher`.
5.  **Quan trọng (Library Handling)**:
    - Chọn tùy chọn: **"copy to the output directory and link via manifest"**. 
    - Việc này sẽ tạo ra một thư mục `libs` chứa các file `.jar` phụ thuộc, giúp file chính gọn nhẹ và dễ quản lý khi `jpackage` quét module.
6.  Nhấn **OK**. Trong màn hình Artifacts, hãy ghi nhớ đường dẫn tại **Output directory** (thường là `out/artifacts/PortraitTool_jar`).
7.  Nhấn **Apply** và **OK**.
8.  Để build: Vào menu **Build > Build Artifacts... > PortraitTool:jar > Build**.

---

## 2. Chuẩn bị tài nguyên đóng gói (Prerequisites)

Sau khi đã có file JAR từ bước trên, hãy chuẩn bị:
1. Copy file JAR và thư mục thư viện từ thư mục Output của IntelliJ vào một thư mục làm việc (ví dụ: `dist/`).
2. Đảm bảo file JAR chính nằm cùng cấp với các thư viện phụ thuộc trong thư mục đó.
3. Chuẩn bị các file tài nguyên trong thư mục `resources/`:
   - `pdw.ico`: File icon cho ứng dụng và định dạng file.
   - `LICENSE.rtf`: File nội dung bản quyền.
   - `file-associations.properties`: File cấu hình gắn kết định dạng file.

### Nội dung file `resources/file-associations.properties`:
```properties
extension=pdw
mime-type=application/x-portrait-data-work
description=Portrait Tool Project File
icon=resources/pdw.ico
```

---

## 3. Lệnh đóng gói (jpackage Command)

Dưới đây là lệnh mẫu để tạo file `.msi`. Hãy chạy lệnh này từ thư mục gốc của dự án:

```powershell
jpackage --type msi `
  --input dist/ `
  --dest output `
  --name "PortraitTool" `
  --main-jar portrai-tool.jar `
  --main-class core.launcher.AppLauncher `
  --add-modules ALL-MODULE-PATH `
  --app-version 2.0.0 `
  --vendor "Phi Thanh Dat" `
  --description "App for measuring and constructing outline sketch for portrait drawing" `
  --copyright "Copyright © 2026 Phi Thanh Dat. All rights reserved." `
  --license-file resources/LICENSE.rtf `
  --icon resources/pdw.ico `
  --file-associations resources/file-associations.properties `
  --win-shortcut `
  --win-menu `
  --win-dir-chooser `
  --verbose
```

## 4. Giải thích các tham số chính

| Tham số | Ý nghĩa |
|---------|---------|
| `--type msi` | Tạo bộ cài đặt định dạng MSI cho Windows. |
| `--input dist/` | Thư mục chứa file JAR và các thư viện phụ thuộc (`libs`). |
| `--dest output` | Thư mục đầu ra chứa file `.msi` sau khi đóng gói. |
| `--name "PortraitTool"` | Tên ứng dụng hiển thị trong Windows và thư mục cài đặt. |
| `--main-jar` | File JAR chính chứa hàm `main`. |
| `--main-class` | Đường dẫn đầy đủ của class chứa hàm `main` (`core.launcher.AppLauncher`). |
| `--add-modules ALL-MODULE-PATH` | Tự động quét và đóng gói các module JRE cần thiết (giúp app chạy không cần cài Java). |
| `--app-version 2.0.0` | Phiên bản của ứng dụng (Hiển thị trong Properties và Control Panel). |
| `--vendor "Phi Thanh Dat"` | Tên nhà phát triển hoặc tổ chức. |
| `--description` | Mô tả ngắn gọn về chức năng của ứng dụng. |
| `--copyright` | Thông tin bản quyền hiển thị trong chi tiết file. |
| `--license-file` | Đường dẫn tới file `.rtf` chứa điều khoản sử dụng (hiện ra khi cài đặt). |
| `--icon` | File biểu tượng (`.ico`) cho file thực thi (`.exe`) của ứng dụng. |
| `--file-associations` | Đăng ký định dạng file riêng (`.pdw`) với Windows. |
| `--win-shortcut` | Tự động tạo Shortcut trên màn hình Desktop sau khi cài. |
| `--win-menu` | Thêm ứng dụng vào danh sách Start Menu. |
| `--win-dir-chooser` | Cho phép người dùng chọn thư mục cài đặt khi chạy Installer. |
| `--verbose` | Hiển thị chi tiết quá trình đóng gói để dễ debug nếu có lỗi. |

## 5. Sau khi đóng gói
- File cài đặt sẽ xuất hiện trong thư mục `output/`.
- Sau khi cài đặt, các file `.pdw` trên máy tính sẽ tự động hiển thị icon `pdw.ico` và mở bằng PortraitTool khi được click đúp.
