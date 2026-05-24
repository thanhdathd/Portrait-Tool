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

if ! command -v appimagetool &> /dev/null; then
    echo "[ERROR] appimagetool not found."
    echo "Please install appimagetool via: sudo apt update && sudo apt install appimagetool"
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
RELEASE_DIR="$PROJECT_ROOT/dist/release/linux/AppImage"
RUNTIME_DIR="$PROJECT_ROOT/dist/runtime"

APP_NAME="PortraitTool"
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
cp "$RESOURCE_SRC/ic_p_540.png" "$RESOURCE_DST/"

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
# STEP 4: JPACKAGE APP-IMAGE
# =========================
echo ""
echo "========================================"
echo "STEP 4: Generating App Image Structure (jpackage)"
echo "========================================"
# Note: We pass --name "$APP_NAME" (PortraitTool) so the launcher is bin/PortraitTool
# which matches what AppRun and the desktop file execute.
jpackage \
  --type app-image \
  --name "$APP_NAME" \
  --input "$BUILD_DIR" \
  --dest "$OUTPUT_DIR" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --runtime-image "$RUNTIME_DIR" \
  --icon "$RESOURCE_DST/ic_p_540.png" \
  --vendor "Phi Thanh Dat" \
  --description "App for measuring and constructing outline sketch for portrait drawing" \
  --app-version "$VERSION"

if [ $? -ne 0 ]; then
    echo "[ERROR] JPACKAGE FAILED!"
    exit 1
fi

# =========================
# STEP 5: RESTRUCTURE APPDIR FOR APPIMAGE
# =========================
echo ""
echo "🏗️  Restructuring AppDir for AppImage..."
APP_DIR_ROOT="$OUTPUT_DIR/$APP_NAME"

if [ ! -d "$APP_DIR_ROOT" ]; then
    echo "[ERROR] jpackage did not generate the App directory at $APP_DIR_ROOT"
    exit 1
fi

# Remove default desktop file if any
rm -f "$APP_DIR_ROOT/$APP_NAME.desktop"

# Copy AppRun launcher script
cp "$RESOURCE_SRC/AppRun" "$APP_DIR_ROOT/"
chmod +x "$APP_DIR_ROOT/AppRun"

# Copy Icon to AppDir root
cp "$RESOURCE_SRC/PortraitTool.png" "$APP_DIR_ROOT/"

# Generate the .desktop file inside AppDir
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

# =========================
# STEP 6: BUILD APPIMAGE
# =========================
echo ""
echo "========================================"
echo "STEP 5: Packaging AppImage (appimagetool)"
echo "========================================"

cd "$OUTPUT_DIR"

APPIMAGE_FILENAME="$APP_NAME-$VERSION.AppImage"
appimagetool "$APP_NAME" "$APPIMAGE_FILENAME"

if [ ! -f "$APPIMAGE_FILENAME" ]; then
    echo "[ERROR] appimagetool failed to create the AppImage file."
    exit 1
fi

chmod +x "$APPIMAGE_FILENAME"

# =========================
# COPY RESULT & CLEANUP
# =========================
echo ""
echo "Copying files to release folder..."
cp "$APPIMAGE_FILENAME" "$RELEASE_DIR/"
cp "$RESOURCE_SRC/install.sh" "$RELEASE_DIR/"
cp "$RESOURCE_SRC/uninstall.sh" "$RELEASE_DIR/"
cp "$RESOURCE_SRC/FilePDW.png" "$RELEASE_DIR/"
cp "$RESOURCE_SRC/PortraitTool.png" "$RELEASE_DIR/"

# Create README.txt
cat <<EOF > "$RELEASE_DIR/README.txt"
DO NOT DELETE OR RENAME ANY FILE IN THIS FOLDER.

Run ./install.sh to install (add shortcut, mime associations).
Run ./uninstall.sh to uninstall.
EOF

# Clean up build folder
if [ -f "$RELEASE_DIR/$APPIMAGE_FILENAME" ]; then
    echo "Cleaning temporary build directory..."
    rm -rf "$BUILD_DIR"
    rm -rf "$OUTPUT_DIR"
    echo "Cleanup done."
fi

echo ""
echo "========================================"
echo "           ALL DONE SUCCESSFULLY!       "
echo "========================================"
echo "AppImage located at: $RELEASE_DIR"
echo "File:                $APPIMAGE_FILENAME"
echo "========================================"
echo ""
