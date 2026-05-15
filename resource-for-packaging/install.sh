#!/bin/bash

# 1. ĐỊNH DANH ĐỘC BẢN (Để tránh trùng lặp với Theme hệ thống)
PT_ID="portrait-tool"
MIME_TYPE="application/x-portrait-data-work"
# Tên icon này là duy nhất, không tuân theo quy tắc application-x...
UNIQUE_ICON_NAME="pdw-file-unique-icon"

APP_DIR="$HOME/Applications"
MENU_DIR="$HOME/.local/share/applications"
MIME_DIR="$HOME/.local/share/mime/packages"
ICON_BASE="$HOME/.local/share/icons/hicolor"
CURRENT_DIR=$(dirname "$(readlink -f "$0")")

# Tìm file
APP_IMAGE=$(ls "$CURRENT_DIR"/*.AppImage 2>/dev/null | head -n 1)
APP_ICON_FILE="$CURRENT_DIR/PortraitTool.png"
FILE_ICON_FILE="$CURRENT_DIR/FilePDW.png"

if [ ! -f "$APP_ICON_FILE" ] || [ ! -f "$FILE_ICON_FILE" ]; then
    echo "❌ Lỗi: Thiếu file PortraitTool.png hoặc FilePDW.png"
    exit 1
fi

echo "🚀 Đang thiết lập liên kết icon độc bản..."

# 2. Đăng ký MIME với Priority cực cao và Icon độc bản
mkdir -p "$MIME_DIR"
cat <<EOF > "$MIME_DIR/portraittool-mime.xml"
<?xml version="1.0" encoding="UTF-8"?>
<mime-info xmlns="http://www.freedesktop.org/standards/shared-mime-info">
  <mime-type type="$MIME_TYPE">
    <comment>Portrait Tool Project File</comment>
    <glob pattern="*.pdw" weight="100"/>
    <icon name="$UNIQUE_ICON_NAME"/>
    <generic-icon name="$UNIQUE_ICON_NAME"/>
  </mime-type>
</mime-info>
EOF

# 3. Cài đặt AppImage
mkdir -p "$APP_DIR"
cp "$APP_IMAGE" "$APP_DIR/PortraitTool.AppImage"
chmod +x "$APP_DIR/PortraitTool.AppImage"

# 4. Tạo và cài đặt Icon chuẩn kích thước
# Chúng ta cài vào cả 'apps' và 'mimetypes' để đảm bảo Nemo tìm thấy
SIZES=(16 32 48 64 128 256 512)
for s in "${SIZES[@]}"; do
    # Icon cho App
    mkdir -p "$ICON_BASE/${s}x${s}/apps"
    convert "$APP_ICON_FILE" -resize ${s}x${s} "$ICON_BASE/${s}x${s}/apps/$PT_ID.png"

    # Icon cho File (Dùng Unique Name)
    mkdir -p "$ICON_BASE/${s}x${s}/mimetypes"
    convert "$FILE_ICON_FILE" -resize ${s}x${s} "$ICON_BASE/${s}x${s}/mimetypes/$UNIQUE_ICON_NAME.png"
done

# Bản scalable để "trị" các theme như Tela-dark
mkdir -p "$ICON_BASE/scalable/mimetypes"
convert "$FILE_ICON_FILE" "$ICON_BASE/scalable/mimetypes/$UNIQUE_ICON_NAME.svg" 2>/dev/null || cp "$FILE_ICON_FILE" "$ICON_BASE/scalable/mimetypes/$UNIQUE_ICON_NAME.png"

# 5. Tạo file .desktop
DESKTOP_CONTENT="[Desktop Entry]
Type=Application
Version=2.0.0
Name=Portrait Tool
Comment=App for measuring and constructing outline sketch for portrait drawing
Exec=$APP_DIR/PortraitTool.AppImage %f
Icon=$PT_ID
Terminal=false
Categories=Graphics;
MimeType=$MIME_TYPE;
StartupWMClass=core-launcher-AppLauncher"

echo "$DESKTOP_CONTENT" > "$MENU_DIR/$PT_ID.desktop"
chmod +x "$MENU_DIR/$PT_ID.desktop"
DESKTOP_PATH="$(xdg-user-dir DESKTOP)/$PT_ID.desktop"
echo "$DESKTOP_CONTENT" > "$DESKTOP_PATH"
chmod +x "$DESKTOP_PATH"
gio set "$DESKTOP_PATH" metadata::trusted true 2>/dev/null

# 6. Cập nhật hệ thống và LÀM MỚI TELA THEME
echo "🔄 Đang cập nhật database..."
update-mime-database "$HOME/.local/share/mime"
update-desktop-database "$MENU_DIR"
gtk-update-icon-cache -f -t "$ICON_BASE"

# Nếu theme Tela-dark nằm trong thư mục local, cập nhật luôn cache của nó
TELA_DIR="$HOME/.local/share/icons/Tela-dark"
if [ -d "$TELA_DIR" ]; then
    gtk-update-icon-cache -f -t "$TELA_DIR"
fi

# Xóa cache thumbnail
rm -rf ~/.cache/thumbnails/*

# Khởi động lại Nemo
nemo -q && nohup nemo > /dev/null 2>&1 &

echo "✅ Đã cài đặt xong. Vui lòng kiểm tra icon file .pdw!"