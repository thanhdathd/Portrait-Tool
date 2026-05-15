#!/bin/bash

PT_ID="portrait-tool"
MIME_ICON="application-x-portrait-data-work"

APP_DIR="$HOME/Applications"
MENU_DIR="$HOME/.local/share/applications"
MIME_DIR="$HOME/.local/share/mime/packages"
DESKTOP_DIR=$(xdg-user-dir DESKTOP)

pause_and_exit() {
    echo ""
    read -p "Nhấn [Enter] để đóng..."
    exit $1
}

echo "🗑️  Đang gỡ bỏ Portrait Tool hoàn toàn..."

# 1. Gỡ bỏ icon khỏi database hệ thống
for s in 16 32 48 64 128 256 512; do
    xdg-icon-resource uninstall --context apps --size $s "$PT_ID" 2>/dev/null
    xdg-icon-resource uninstall --context mimetypes --size $s "$MIME_ICON" 2>/dev/null
done

# 2. Xóa các file .desktop (Đây là lý do bản cũ của bạn bị sót icon)
# Xóa trong Start Menu
rm -f "$MENU_DIR/$PT_ID.desktop"

# Xóa ngoài Desktop (Dùng lệnh rm cho mọi khả năng tên file)
rm -f "$DESKTOP_DIR/$PT_ID.desktop"
rm -f "$DESKTOP_DIR/PortraitTool"*.desktop # Xóa luôn các file cũ bị sót

# 3. Xóa file MIME XML và AppImage
rm -f "$MIME_DIR/portraittool-mime.xml"
rm -f "$APP_DIR/PortraitTool"*.AppImage

# 4. Cập nhật lại toàn bộ hệ thống
update-mime-database "$HOME/.local/share/mime"
update-desktop-database "$MENU_DIR"
gtk-update-icon-cache -f -t "$HOME/.local/share/icons/hicolor"
rm -rf ~/.cache/thumbnails/*
nemo -q && nohup nemo > /dev/null 2>&1 &

echo "✅ Đã gỡ bỏ sạch sẽ mọi dấu vết!"
pause_and_exit 0