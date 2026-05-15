#!/bin/bash

set -e

# ---  KIỂM TRA ĐIỀU KIỆN TIÊN QUYẾT (Pre-flight Checks) ---
echo "🔍 Đang kiểm tra môi trường hệ thống..."

# Kiểm tra Java/JDK
if ! command -v java &> /dev/null; then
    echo "❌ Lỗi: Không tìm thấy Java. Vui lòng cài đặt JDK (phiên bản 17 trở lên được khuyến nghị)."
    echo "👉 Lệnh cài đặt: sudo apt update && sudo apt install default-jdk"
    exit 1
fi

# Kiểm tra jpackage (Đi kèm JDK 14+)
if ! command -v jpackage &> /dev/null; then
    echo "❌ Lỗi: Không tìm thấy 'jpackage'."
    echo "Cần JDK 14+ để sử dụng jpackage. Hãy kiểm tra biến môi trường PATH hoặc cài đặt JDK mới nhất."
    exit 1
fi

# Kiểm tra appimagetool
if ! command -v appimagetool &> /dev/null; then
    echo "❌ Lỗi: Không tìm thấy 'appimagetool'."
    echo "👉 Vui lòng cài đặt bằng lệnh: sudo apt update && sudo apt install appimagetool"
    echo "(Hoặc tải AppImage của appimagetool, đổi tên thành 'appimagetool' và bỏ vào /usr/local/bin)"
    exit 1
fi

# ---  LẤY THÔNG TIN TỪ POM.XML (Dynamic Configuration) ---
echo "📖 Đang đọc cấu hình từ pom.xml..."

if [ ! -f "pom.xml" ]; then
    echo "❌ Lỗi: Không tìm thấy file pom.xml trong thư mục gốc."
    exit 1
fi

# Lấy Version (lấy dòng <version> đầu tiên sau <artifactId>)
VERSION=$(grep -m1 '<version>' pom.xml | sed -E 's/.*<version>(.*)<\/version>.*/\1/')
# Lấy ArtifactId
ARTIFACT_ID=$(grep -m1 '<artifactId>' pom.xml | sed -E 's/.*<artifactId>(.*)<\/artifactId>.*/\1/')

# Chuyển đổi dấu gạch ngang thành gạch dưới cho thư mục artifacts (theo cách IntelliJ thường đặt tên)
# Ví dụ: portrai-tool -> portrai_tool_jar
ARTIFACT_FOLDER_NAME="${ARTIFACT_ID//-/_}_jar"

# --- 3. ĐỊNH NGHĨA ĐƯỜNG DẪN ---
PROJECT_ROOT=$(pwd)
DIST_DIR="$PROJECT_ROOT/dist"
RESOURCE_PKG="$PROJECT_ROOT/resource-for-packaging"
ARTIFACTS_BASE="$PROJECT_ROOT/out/artifacts"
ARTIFACTS_DIR="$ARTIFACTS_BASE/$ARTIFACT_FOLDER_NAME"
RELEASE_DIR="$DIST_DIR/release/linux/AppImage"
MAIN_CLASS="core.launcher.AppLauncher"
APP_NAME="PortraitTool"

# --- 4. KIỂM TRA THƯ MỤC ARTIFACTS ---
echo "📂 Kiểm tra thư mục artifacts..."

if [ ! -d "$ARTIFACTS_BASE" ]; then
    echo "❌ Lỗi: Không tìm thấy thư mục 'out/artifacts'. Hãy Build Artifacts trong IDE trước."
    exit 1
fi

if [ ! -d "$ARTIFACTS_DIR" ]; then
    echo "❌ Lỗi: Không tìm thấy thư mục đích: $ARTIFACTS_DIR"
    echo "Hãy đảm bảo tên Artifact trong cấu hình Project Structure khớp với artifactId ($ARTIFACT_ID)."
    exit 1
fi

# --- 5. BẮT ĐẦU QUY TRÌNH BUILD ---
echo "🚀 Khởi động đóng gói $ARTIFACT_ID v$VERSION..."

# --- 2. Chuẩn bị thư mục ---
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR/resources"
mkdir -p "$DIST_DIR/output"
mkdir -p "$RELEASE_DIR"

# --- 3. Gom Artifacts (JAR files) ---
# Copy các file JAR
cp "$ARTIFACTS_DIR"/*.jar "$DIST_DIR/"

# --- 4. Chuẩn bị tài nguyên cho jpackage ---
echo "🎨 Đang chuẩn bị icon và tài nguyên đóng gói..."
cp "$RESOURCE_PKG/ic_p_540.png" "$DIST_DIR/resources/"
#cp "$RESOURCE_PKG"/*.ico "$DIST_DIR/resources/" 2>/dev/null # Nếu có file ico cho windows

# --- 5. Chạy jpackage để tạo Runtime Java nén ---
echo "☕ Đang chạy jpackage..."
# Lưu ý: jpackage sẽ tự đóng gói runtime JRE cần thiết vào trong
jpackage --type app-image --input "$DIST_DIR" --dest "$DIST_DIR/output" --name "$ARTIFACT_ID" --main-jar "$ARTIFACT_ID.jar" --main-class "$MAIN_CLASS" --add-modules ALL-MODULE-PATH --app-version "$VERSION" --vendor "Phi Thanh Dat" --description "App for measuring and constructing outline sketch for portrait drawing" --icon "$DIST_DIR/resources/ic_p_540.png" --verbose

# Kiểm tra xem jpackage có sinh ra thư mục PortraitTool không
APP_DIR_ROOT="$DIST_DIR/output/$ARTIFACT_ID"

if [ ! -d "$APP_DIR_ROOT" ]; then
    echo "❌ Lỗi: jpackage không sinh ra thư mục tại $APP_DIR_ROOT"
    exit 1
fi

# --- 6. Cấu trúc lại AppDir theo tiêu chuẩn AppImage ---
echo "🏗️  Đang cấu trúc lại AppDir..."
rm -f "$APP_DIR_ROOT/$ARTIFACT_ID.desktop"
# Copy AppRun
cp "$RESOURCE_PKG/AppRun" "$APP_DIR_ROOT/"
chmod +x "$APP_DIR_ROOT/AppRun"

# Copy Icon ra root của AppDir
cp "$APP_DIR_ROOT/lib/$APP_NAME.png" "$APP_DIR_ROOT/" 2>/dev/null || cp "$RESOURCE_PKG/PortraitTool.png" "$APP_DIR_ROOT/"

# Tạo file .desktop bên trong AppDir
cat <<EOF > "$APP_DIR_ROOT/$APP_NAME.desktop"
[Desktop Entry]
Type=Application
Name=$APP_NAME
Exec=bin/$APP_NAME %f
Icon=$APP_NAME
Terminal=false
Categories=Graphics;
MimeType=application/x-portrait-data-work;
Comment=App for measuring and constructing outline sketch for portrait drawing
StartupNotify=true
StartupWMClass=core-launcher-AppLauncher
EOF

# --- 7. Đóng gói thành AppImage bằng appimagetool ---
echo "💎 Đang tạo file AppImage..."
cd "$DIST_DIR/output" || exit

APPIMAGE_FILENAME="$APP_NAME-$VERSION.AppImage"
appimagetool "$ARTIFACT_ID" "$APPIMAGE_FILENAME"

# Kiểm tra nếu file AppImage chưa được tạo ra thì dừng lại
if [ ! -f "$APPIMAGE_FILENAME" ]; then
    echo "❌ Lỗi: appimagetool thất bại trong việc tạo file AppImage."
    exit 1
fi

# Cấp quyền thực thi
chmod +x "$APPIMAGE_FILENAME"

# --- 8. Phát hành (Release) ---
echo "🚚 Đang đưa file vào thư mục release..."
cp "$APPIMAGE_FILENAME" "$RELEASE_DIR/"
cp "$RESOURCE_PKG/install.sh" "$RELEASE_DIR/"
cp "$RESOURCE_PKG/uninstall.sh" "$RELEASE_DIR/"
# Copy luôn cả file icon PDW để script install sử dụng
cp "$RESOURCE_PKG/FilePDW.png" "$RELEASE_DIR/"
cp "$RESOURCE_PKG/PortraitTool.png" "$RELEASE_DIR/"

# --- 9. Tạo file README.txt ---
echo "📝 Đang tạo file README.txt..."
cat <<EOF > "$RELEASE_DIR/README.txt"
DO NOT DELETE OR RENAME ANY FILE

run install.sh for install
run uninstall.sh for uninstall
EOF

# --- 10. Dọn dẹp (Chỉ chạy nếu mọi thứ ở trên thành công) ---
echo "🧹 Đang dọn dẹp các file tạm..."

# Kiểm tra xem file AppImage đã thực sự nằm trong thư mục release chưa
if [ -f "$RELEASE_DIR/$APPIMAGE_FILENAME" ] && [ -f "$RELEASE_DIR/install.sh" ]; then
    rm -f "$DIST_DIR"/*.jar
    rm -rf "$DIST_DIR/output"
    rm -rf "$DIST_DIR/resources"
    echo "✨ Đã dọn dẹp xong thư mục dist."
else
    echo "⚠️ Cảnh báo: Phát hiện lỗi trong quá trình copy, bỏ qua bước dọn dẹp để bảo toàn dữ liệu."
    exit 1
fi

echo "------------------------------------------------"
echo "✅ ĐÓNG GÓI THÀNH CÔNG!"
echo "📍 Vị trí: $RELEASE_DIR"
echo "📦 File: $APPIMAGE_FILENAME"
echo "------------------------------------------------"