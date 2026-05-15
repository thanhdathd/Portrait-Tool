#!/bin/bash

# Dừng ngay nếu có lỗi
set -e

# --- 1. KIỂM TRA ĐIỀU KIỆN TIÊN QUYẾT ---
echo "🔍 Đang kiểm tra môi trường hệ thống..."

if ! command -v jpackage &> /dev/null; then
    echo "❌ Lỗi: Không tìm thấy 'jpackage'. Hãy đảm bảo bạn đã cài JDK 14+."
    exit 1
fi

if ! command -v fakeroot &> /dev/null || ! command -v dpkg-deb &> /dev/null; then
    echo "❌ Lỗi: Thiếu công cụ đóng gói Debian (fakeroot, dpkg-dev)."
    echo "👉 Cài đặt: sudo apt update && sudo apt install fakeroot dpkg-dev"
    exit 1
fi

# --- 2. LẤY THÔNG TIN TỪ POM.XML ---
echo "📖 Đang đọc cấu hình từ pom.xml..."
if [ ! -f "pom.xml" ]; then
    echo "❌ Lỗi: Không tìm thấy file pom.xml tại thư mục gốc."
    exit 1
fi

# Lấy Version và ArtifactId
VERSION=$(grep -m1 '<version>' pom.xml | sed -E 's/.*<version>(.*)<\/version>.*/\1/')
ARTIFACT_ID=$(grep -m1 '<artifactId>' pom.xml | sed -E 's/.*<artifactId>(.*)<\/artifactId>.*/\1/')

# Cấu hình định danh
TECH_NAME="portrait-tool"
WM_CLASS="core-launcher-AppLauncher"
DISPLAY_NAME="Portrait Tool"
ARTIFACT_FOLDER_NAME="${ARTIFACT_ID//-/_}_jar"

# --- 3. ĐỊNH NGHĨA ĐƯỜNG DẪN ---
PROJECT_ROOT="$(pwd)"
DIST_DIR="$PROJECT_ROOT/dist"
RESOURCE_PKG="$PROJECT_ROOT/resource-for-packaging"
ARTIFACTS_DIR="$PROJECT_ROOT/out/artifacts/$ARTIFACT_FOLDER_NAME"
RELEASE_DIR="$DIST_DIR/release/linux/DEB"
OVERRIDE_RES="$PROJECT_ROOT/temp_res_deb"

# --- 4. KIỂM TRA TÀI NGUYÊN ĐẦU VÀO ---
if [ ! -d "$ARTIFACTS_DIR" ]; then
    echo "❌ Lỗi: Không tìm thấy artifacts tại $ARTIFACTS_DIR"
    echo "👉 Hãy 'Build Artifacts' trong IntelliJ trước khi chạy script."
    exit 1
fi

if [ ! -f "$RESOURCE_PKG/PortraitTool.png" ] || [ ! -f "$RESOURCE_PKG/FilePDW.png" ]; then
    echo "❌ Lỗi: Thiếu file icon trong $RESOURCE_PKG"
    echo "App icon: PortraitTool.png, pdw file icon: FilePDW.png"
    exit 1
fi

# --- 5. CHUẨN BỊ MÔI TRƯỜNG BUILD ---
echo "🧹 Đang làm sạch thư mục build..."
rm -rf "$DIST_DIR"
rm -rf "$OVERRIDE_RES"
mkdir -p "$DIST_DIR/output_deb"
mkdir -p "$RELEASE_DIR"
mkdir -p "$OVERRIDE_RES"

# --- 6. TẠO SCRIPT HẬU CÀI ĐẶT (POSTINST) ---
# Đây là phần quan trọng nhất để xử lý icon trắng và Start Menu
cat <<EOF > "$OVERRIDE_RES/postinst"
#!/bin/bash
echo "🛠️  Đang cấu hình hệ thống cho $DISPLAY_NAME..."

# 1. Tạo file .desktop (Start Menu)
DESKTOP_FILE="/usr/share/applications/$TECH_NAME.desktop"
cat <<EOD > "\$DESKTOP_FILE"
[Desktop Entry]
Type=Application
Name=$DISPLAY_NAME
Exec=/opt/$TECH_NAME/bin/$TECH_NAME %f
Icon=$TECH_NAME
Comment=Measuring and constructing outline sketch for portrait drawing
Terminal=false
Categories=Graphics;
MimeType=application/x-portrait-data-work;
StartupWMClass=$WM_CLASS
EOD
chmod 644 "\$DESKTOP_FILE"

# 2. Tạo file MIME (Icon cho file .pdw)
MIME_FILE="/usr/share/mime/packages/$TECH_NAME.xml"
MIME_ICON_NAME="application-x-portrait-data-work"
cat <<EOM > "\$MIME_FILE"
<?xml version="1.0" encoding="UTF-8"?>
<mime-info xmlns="http://www.freedesktop.org/standards/shared-mime-info">
  <mime-type type="application/x-portrait-data-work">
    <comment>Portrait Tool Project File</comment>
    <glob pattern="*.pdw" weight="100"/>
    <icon name="\$MIME_ICON_NAME"/>
  </mime-type>
</mime-info>
EOM

# 3. Đăng ký Icon vào hệ thống (hicolor theme)
SIZES=(16 32 48 64 128 256 512)
for s in "\${SIZES[@]}"; do
    mkdir -p "/usr/share/icons/hicolor/\${s}x\${s}/apps"
    mkdir -p "/usr/share/icons/hicolor/\${s}x\${s}/mimetypes"
    cp "$RESOURCE_PKG/PortraitTool.png" "/usr/share/icons/hicolor/\${s}x\${s}/apps/$TECH_NAME.png" 2>/dev/null || true
    cp "$RESOURCE_PKG/FilePDW.png" "/usr/share/icons/hicolor/\${s}x\${s}/mimetypes/\$MIME_ICON_NAME.png" 2>/dev/null || true
done

# 4. Cập nhật database hệ thống
update-mime-database /usr/share/mime
update-desktop-database /usr/share/applications
gtk-update-icon-cache -f -t /usr/share/icons/hicolor
rm -rf /home/*/.cache/thumbnails/*
pkill nemo || true

echo "✅ Cấu hình hệ thống hoàn tất!"
EOF
chmod +x "$OVERRIDE_RES/postinst"

# --- 7. CHẠY JPACKAGE ---
echo "📄 Đang chuẩn bị cấu hình jpackage..."
cat <<EOF > "$DIST_DIR/pdw-association.properties"
mime-type=application/x-portrait-data-work
extension=pdw
description=Portrait Tool Project File
icon=$RESOURCE_PKG/FilePDW.png
EOF

cp "$ARTIFACTS_DIR"/*.jar "$DIST_DIR/"

echo "📦 Đang đóng gói DEB v$VERSION..."
jpackage \
    --type deb \
    --dest "$DIST_DIR/output_deb" \
    --name "$TECH_NAME" \
    --linux-package-name "$TECH_NAME" \
    --resource-dir "$OVERRIDE_RES" \
    --input "$DIST_DIR" \
    --main-jar "$ARTIFACT_ID.jar" \
    --vendor "ThanhDat" \
    --app-version "$VERSION" \
    --icon "$RESOURCE_PKG/PortraitTool.png" \
    --linux-deb-maintainer "thanhdathd@gmail.com" \
    --file-associations "$DIST_DIR/pdw-association.properties" \
    --install-dir "/opt"

# --- 8. PHÁT HÀNH & TẠO README ---
DEB_PATH=$(find "$DIST_DIR/output_deb" -name "*.deb" | head -n 1)
DEB_FILE=$(basename "$DEB_PATH")

if [ -n "$DEB_PATH" ]; then
    cp "$DEB_PATH" "$RELEASE_DIR/"

    echo "📝 Tạo file README.txt..."
    cat <<EOF > "$RELEASE_DIR/README.txt"
Portrait Tool v$VERSION - Debian Package
----------------------------------------
DO NOT DELETE OR RENAME ANY FILE IN THIS FOLDER.

To install:
sudo dpkg -i $DEB_FILE

To uninstall:
sudo apt remove $TECH_NAME
EOF

    # --- 9. DỌN DẸP SAU KHI THÀNH CÔNG ---
    echo "🧹 Đang dọn dẹp thư mục tạm..."
    rm -rf "$OVERRIDE_RES"
    rm -f "$DIST_DIR"/*.jar
    rm -f "$DIST_DIR/pdw-association.properties"
    rm -rf "$DIST_DIR/output_deb"

    echo "------------------------------------------------"
    echo "✅ ĐÓNG GÓI THÀNH CÔNG!"
    echo "📍 Vị trí: $RELEASE_DIR"
    echo "📍 Tên hiển thị: $DISPLAY_NAME"
    echo "📦 Tên gói hệ thống: $PACKAGE_NAME"
    echo "📦 File: $(basename "$DEB_PATH")"
    echo "------------------------------------------------"
else
    echo "❌ Lỗi: Build hoàn tất nhưng không tìm thấy file .deb đầu ra."
    exit 1
fi