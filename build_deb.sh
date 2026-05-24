#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# =========================
# PARSE ARGUMENTS
# =========================
SKIP_MAVEN=0
SKIP_RUNTIME=0

show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  -m, --skip-maven     Skip Maven build and use existing Fat JAR in target/"
    echo "  -r, --skip-runtime   Skip custom JRE runtime creation and use existing runtime in dist/runtime"
    echo "  -h, --help           Show this help message"
    exit 0
}

while [[ "$#" -gt 0 ]]; do
    case "$1" in
        -m|--skip-maven)   SKIP_MAVEN=1; shift ;;
        -r|--skip-runtime) SKIP_RUNTIME=1; shift ;;
        -h|--help)         show_help ;;
        *)                 echo "Unknown option: $1"; show_help; exit 1 ;;
    esac
done

echo "[INFO] Build Configuration:"
echo " - Skip Maven Build: $SKIP_MAVEN"
echo " - Skip Custom Runtime: $SKIP_RUNTIME"
echo ""

# =========================
# CHECK TOOLS
# =========================
echo "Checking Required Tools..."

if ! command -v java &> /dev/null; then
    echo "[ERROR] Java is not installed or not in PATH."
    exit 1
fi

if ! command -v javac &> /dev/null; then
    echo "[ERROR] JDK is not installed or not in PATH (javac missing)."
    echo "Please install JDK 17+ or JDK 21 (recommended)."
    exit 1
fi

if ! command -v jpackage &> /dev/null; then
    echo "[ERROR] jpackage not found."
    echo "Please install JDK 14+ (recommended JDK 17+ or 21+)."
    echo "Make sure JAVA_HOME/bin is added to your PATH."
    exit 1
fi

if ! command -v fakeroot &> /dev/null || ! command -v dpkg-deb &> /dev/null; then
    echo "[ERROR] Debian packaging tools (fakeroot, dpkg-deb) are missing."
    echo "Please install them via: sudo apt update && sudo apt install fakeroot dpkg-dev"
    exit 1
fi

if [ "$SKIP_MAVEN" = "0" ]; then
    if ! command -v mvn &> /dev/null; then
        echo "[ERROR] Maven not found (mvn missing)."
        echo "Please install Apache Maven and add it to your PATH."
        exit 1
    fi
fi

if [ "$SKIP_RUNTIME" = "0" ]; then
    if ! command -v jdeps &> /dev/null; then
        echo "[ERROR] jdeps not found."
        exit 1
    fi
    if ! command -v jlink &> /dev/null; then
        echo "[ERROR] jlink not found."
        exit 1
    fi
fi

echo "All tools OK!"
echo ""

# =========================
# CONFIG
# =========================
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Read Version and ArtifactId from pom.xml
if [ ! -f "$PROJECT_ROOT/pom.xml" ]; then
    echo "[ERROR] pom.xml not found at $PROJECT_ROOT/pom.xml"
    exit 1
fi

VERSION=$(grep -m1 '<version>' "$PROJECT_ROOT/pom.xml" | sed -E 's/.*<version>(.*)<\/version>.*/\1/')
ARTIFACT_ID=$(grep -m1 '<artifactId>' "$PROJECT_ROOT/pom.xml" | sed -E 's/.*<artifactId>(.*)<\/artifactId>.*/\1/')

ARTIFACT_DIR="$PROJECT_ROOT/target"
MAIN_JAR="${ARTIFACT_ID}-${VERSION}.jar"

BUILD_DIR="$PROJECT_ROOT/dist/build"
RESOURCE_SRC="$PROJECT_ROOT/resource-for-packaging"
RESOURCE_DST="$BUILD_DIR/resources"

OUTPUT_DIR="$PROJECT_ROOT/dist/build/output"
RELEASE_DIR="$PROJECT_ROOT/dist/release/linux/deb"
RUNTIME_DIR="$PROJECT_ROOT/dist/runtime"

TECH_NAME="portrait-tool"
DISPLAY_NAME="Portrait Tool"
WM_CLASS="core-launcher-AppLauncher"
MAIN_CLASS="core.launcher.AppLauncher"

# =========================
# BUILD FAT JAR WITH MAVEN
# =========================
echo "========================================"

if [ "$SKIP_MAVEN" = "1" ]; then
    echo "STEP 1: Skipping Maven build... Using existing Fat JAR."
    if [ ! -f "$ARTIFACT_DIR/$MAIN_JAR" ]; then
        echo "[ERROR] Fat JAR not found at $ARTIFACT_DIR/$MAIN_JAR"
        echo "Please build it first or run without --skip-maven"
        exit 1
    fi
else
    echo "STEP 1: Building Fat JAR with Maven..."
    echo "========================================"
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "[ERROR] MAVEN BUILD FAILED! Please check your code."
        exit 1
    fi
fi

# =========================
# CLEAN OLD BUILD FOLDERS
# =========================
echo ""
echo "Cleaning old packaging folders..."
rm -rf "$BUILD_DIR"
rm -rf "$OUTPUT_DIR"
rm -rf "$RELEASE_DIR"

if [ "$SKIP_RUNTIME" = "0" ]; then
    rm -rf "$RUNTIME_DIR"
fi

# =========================
# CREATE FOLDERS
# =========================
mkdir -p "$BUILD_DIR"
mkdir -p "$RESOURCE_DST"
mkdir -p "$OUTPUT_DIR"
mkdir -p "$RELEASE_DIR"

# =========================
# COPY JARS & RESOURCES
# =========================
echo "Copying jar and resources..."
cp "$ARTIFACT_DIR/$MAIN_JAR" "$BUILD_DIR/"
cp "$RESOURCE_SRC/LICENSE.rtf" "$RESOURCE_DST/"
cp "$RESOURCE_SRC/PortraitTool.png" "$RESOURCE_DST/"
cp "$RESOURCE_SRC/FilePDW.png" "$RESOURCE_DST/"

# Generate file-associations.properties dynamically
cat <<EOF > "$RESOURCE_DST/file-associations.properties"
mime-type=application/x-portrait-data-work
extension=pdw
description=Portrait Tool Project File
icon=$RESOURCE_DST/FilePDW.png
EOF

# =========================
# RUN JDEPS (DYNAMIC MODULES)
# =========================
echo ""
echo "========================================"

if [ "$SKIP_RUNTIME" = "1" ]; then
    echo "STEP 2 & 3: Skipping custom runtime build..."
    if [ ! -f "$RUNTIME_DIR/bin/java" ]; then
        echo "[ERROR] Custom runtime not found at $RUNTIME_DIR"
        echo "Please build it first or run without --skip-runtime"
        exit 1
    fi
else
    echo "STEP 2: Analyzing dependencies (jdeps)"
    echo "========================================"

    # Get active JDK major version dynamically
    JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
    echo "Active Java Major Version: $JAVA_VER"

    MODULE_DEPS=$(jdeps --ignore-missing-deps -q --recursive --multi-release "$JAVA_VER" --print-module-deps "$BUILD_DIR/$MAIN_JAR")

    if [ -z "$MODULE_DEPS" ]; then
        echo "[ERROR] jdeps failed to find any modules. Check your jar file!"
        exit 1
    fi
    echo "Found required modules: $MODULE_DEPS"

    # =========================
    # RUN JLINK (CUSTOM RUNTIME)
    # =========================
    echo ""
    echo "========================================"
    echo "STEP 3: Building Custom Runtime (jlink)"
    echo "========================================"
    jlink \
      --add-modules "$MODULE_DEPS" \
      --strip-debug \
      --compress=zip-9 \
      --no-header-files \
      --no-man-pages \
      --output "$RUNTIME_DIR"

    if [ $? -ne 0 ]; then
        echo "[ERROR] JLINK FAILED!"
        exit 1
    fi
    echo "Custom runtime built successfully."
fi

# =========================
# CREATE DEBIAN RESOURCE OVERRIDES
# =========================
OVERRIDE_RES="$BUILD_DIR/resources-override"
mkdir -p "$OVERRIDE_RES"

# postinst script
cat <<EOF > "$OVERRIDE_RES/postinst"
#!/bin/bash
echo "🛠️  Configuring system for $DISPLAY_NAME..."

# 1. Create .desktop file (Start Menu)
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

# 2. Create MIME file (Icon for .pdw files)
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

# 3. Register icons in system (hicolor theme)
SIZES=(16 22 24 32 36 48 64 72 96 128 192 256 512)
for s in "\${SIZES[@]}"; do
    mkdir -p "/usr/share/icons/hicolor/\${s}x\${s}/apps"
    mkdir -p "/usr/share/icons/hicolor/\${s}x\${s}/mimetypes"
    # Copy icons from the self-contained installed path /opt/$TECH_NAME/lib/app/resources/
    cp "/opt/$TECH_NAME/lib/app/resources/PortraitTool.png" "/usr/share/icons/hicolor/\${s}x\${s}/apps/$TECH_NAME.png" 2>/dev/null || true
    cp "/opt/$TECH_NAME/lib/app/resources/FilePDW.png" "/usr/share/icons/hicolor/\${s}x\${s}/mimetypes/\$MIME_ICON_NAME.png" 2>/dev/null || true
done

# 3.5 Register icon in other custom system and user icon themes
if [ -d "/usr/share/icons" ]; then
    for theme_dir in /usr/share/icons/*; do
        if [ -d "\$theme_dir" ] && [ -f "\$theme_dir/index.theme" ]; then
            theme_name=\$(basename "\$theme_dir")
            if [ "\$theme_name" != "hicolor" ]; then
                find "\$theme_dir" -type d -name "mimetypes" 2>/dev/null | while read -r mime_dir; do
                    cp "/opt/$TECH_NAME/lib/app/resources/FilePDW.png" "\$mime_dir/\$MIME_ICON_NAME.png" 2>/dev/null || true
                done
                gtk-update-icon-cache -f "\$theme_dir" 2>/dev/null || true
            fi
        fi
    done
fi

for user_home in /home/*; do
    if [ -d "\$user_home/.local/share/icons" ]; then
        for theme_dir in "\$user_home/.local/share/icons"/*; do
            if [ -d "\$theme_dir" ] && [ -f "\$theme_dir/index.theme" ]; then
                find "\$theme_dir" -type d -name "mimetypes" 2>/dev/null | while read -r mime_dir; do
                    cp "/opt/$TECH_NAME/lib/app/resources/FilePDW.png" "\$mime_dir/\$MIME_ICON_NAME.png" 2>/dev/null || true
                    user_owner=\$(stat -c '%u:%g' "\$user_home")
                    chown "\$user_owner" "\$mime_dir/\$MIME_ICON_NAME.png" 2>/dev/null || true
                done
                user_name=\$(basename "\$user_home")
                su - "\$user_name" -c "gtk-update-icon-cache -f \$theme_dir" 2>/dev/null || true
            fi
        done
    fi
done

# 4. Update system databases & caches
update-mime-database /usr/share/mime
update-desktop-database /usr/share/applications
gtk-update-icon-cache -f -t /usr/share/icons/hicolor
rm -rf /home/*/.cache/thumbnails/*

echo "✅ System configuration completed!"
EOF
chmod +x "$OVERRIDE_RES/postinst"

# prerm script (called before removal)
cat <<EOF > "$OVERRIDE_RES/prerm"
#!/bin/bash
set +e

echo "🧹 Cleaning up system configuration before removing $DISPLAY_NAME..."

# 1. Remove launcher symlinks if any
rm -f /usr/bin/$TECH_NAME

# 2. Remove desktop shortcut
rm -f /usr/share/applications/$TECH_NAME.desktop

# 3. Remove MIME registration
rm -f /usr/share/mime/packages/$TECH_NAME.xml

# 4. Remove MIME icon registration
MIME_ICON_NAME="application-x-portrait-data-work"
SIZES=(16 22 24 32 36 48 64 72 96 128 192 256 512)
for s in "\${SIZES[@]}"; do
    rm -f "/usr/share/icons/hicolor/\${s}x\${s}/apps/$TECH_NAME.png"
    rm -f "/usr/share/icons/hicolor/\${s}x\${s}/mimetypes/\$MIME_ICON_NAME.png"
done

# 4.5 Remove icon from other custom system and user icon themes
if [ -d "/usr/share/icons" ]; then
    for theme_dir in /usr/share/icons/*; do
        if [ -d "\$theme_dir" ] && [ -f "\$theme_dir/index.theme" ]; then
            theme_name=\$(basename "\$theme_dir")
            if [ "\$theme_name" != "hicolor" ]; then
                find "\$theme_dir" -type f -name "\$MIME_ICON_NAME.png" -delete 2>/dev/null || true
                gtk-update-icon-cache -f "\$theme_dir" 2>/dev/null || true
            fi
        fi
    done
fi

for user_home in /home/*; do
    if [ -d "\$user_home/.local/share/icons" ]; then
        for theme_dir in "\$user_home/.local/share/icons"/*; do
            if [ -d "\$theme_dir" ] && [ -f "\$theme_dir/index.theme" ]; then
                find "\$theme_dir" -type f -name "\$MIME_ICON_NAME.png" -delete 2>/dev/null || true
                user_name=\$(basename "\$user_home")
                su - "\$user_name" -c "gtk-update-icon-cache -f \$theme_dir" 2>/dev/null || true
            fi
        done
    fi
done

# 5. Update databases
command -v update-desktop-database &> /dev/null && update-desktop-database /usr/share/applications || true
command -v update-mime-database &> /dev/null && update-mime-database /usr/share/mime || true
command -v gtk-update-icon-cache &> /dev/null && gtk-update-icon-cache -f -t /usr/share/icons/hicolor || true

exit 0
EOF
chmod +x "$OVERRIDE_RES/prerm"

# =========================
# STEP 4: JPACKAGE DEB
# =========================
echo ""
echo "========================================"
echo "STEP 4: Packaging DEB Installer (jpackage)"
echo "========================================"
jpackage \
  --type deb \
  --name "$TECH_NAME" \
  --linux-package-name "$TECH_NAME" \
  --input "$BUILD_DIR" \
  --dest "$OUTPUT_DIR" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --runtime-image "$RUNTIME_DIR" \
  --icon "$RESOURCE_DST/PortraitTool.png" \
  --license-file "$RESOURCE_DST/LICENSE.rtf" \
  --file-associations "$RESOURCE_DST/file-associations.properties" \
  --vendor "Phi Thanh Dat" \
  --description "Measuring and constructing outline sketch for portrait drawing" \
  --copyright "Copyright (c) 2026 Phi Thanh Dat. All rights reserved." \
  --app-version "$VERSION" \
  --linux-deb-maintainer "thanhdathd@gmail.com" \
  --install-dir "/opt" \
  --resource-dir "$OVERRIDE_RES"

if [ $? -ne 0 ]; then
    echo "[ERROR] JPACKAGE FAILED!"
    exit 1
fi

# =========================
# COPY DEB RESULT & CREATE README
# =========================
DEB_PATH=$(find "$OUTPUT_DIR" -name "*.deb" | head -n 1)
DEB_FILE=$(basename "$DEB_PATH")

if [ -n "$DEB_PATH" ]; then
    echo ""
    echo "Copying DEB to release folder..."
    cp "$DEB_PATH" "$RELEASE_DIR/"

    echo "📝 Creating README.txt..."
    cat <<EOF > "$RELEASE_DIR/README.txt"
Portrait Tool v$VERSION - Debian Package
----------------------------------------
DO NOT DELETE OR RENAME ANY FILE IN THIS FOLDER.

To install:
sudo dpkg -i $DEB_FILE

To uninstall:
sudo apt remove $TECH_NAME
EOF

    # =========================
    # CLEANUP BUILD FOLDER
    # =========================
    if [ -f "$RELEASE_DIR/$DEB_FILE" ]; then
        echo "Cleaning temporary build directory..."
        rm -rf "$BUILD_DIR"
        echo "Cleanup done."
    fi

    echo ""
    echo "========================================"
    echo "           ALL DONE SUCCESSFULLY!       "
    echo "========================================"
    echo "DEB located at: $RELEASE_DIR"
    echo "Display Name:   $DISPLAY_NAME"
    echo "Package Name:   $TECH_NAME"
    echo "File:           $DEB_FILE"
    echo "========================================"
    echo ""
else
    echo "[ERROR] Build completed but output .deb file was not found!"
    exit 1
fi
