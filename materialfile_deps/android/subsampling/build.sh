#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../../.." && pwd)"
MODULE_NAME="$(basename "$SCRIPT_DIR")"

export JAVA_HOME="$ROOT_DIR/tools/jdk-17.0.12"
export ANDROID_HOME="$ROOT_DIR/tools/android-sdk"

GRADLEW_DIR="$SCRIPT_DIR/../gradle"

# ===== Download source =====
UPSTREAM_URL="https://github.com/davemorrissey/subsampling-scale-image-view.git"
UPSTREAM_TAG="v3.9.0"
MODULE_PATH="library"
SRC_BASE="$SCRIPT_DIR/src"
if [ ! -d "$SRC_BASE/java" ]; then
    echo "Downloading $MODULE_PATH source..."
    TMP_DIR=$(mktemp -d)
    git clone --depth 1 --branch "$UPSTREAM_TAG" "$UPSTREAM_URL" "$TMP_DIR"
    mkdir -p "$SRC_BASE"
    MAIN_SRC="$TMP_DIR/$MODULE_PATH/src/main"
    [ -d "$MAIN_SRC/java" ] && cp -r "$MAIN_SRC/java" "$SRC_BASE/java"
    if [ -d "$MAIN_SRC/kotlin" ]; then
        mkdir -p "$SRC_BASE/java"
        cp -r "$MAIN_SRC/kotlin/"* "$SRC_BASE/java/"
    fi
    [ -d "$MAIN_SRC/res" ] && cp -r "$MAIN_SRC/res" "$SRC_BASE/res"
    [ -d "$MAIN_SRC/aidl" ] && cp -r "$MAIN_SRC/aidl" "$SRC_BASE/aidl"
    [ -f "$MAIN_SRC/AndroidManifest.xml" ] && cp "$MAIN_SRC/AndroidManifest.xml" "$SRC_BASE/AndroidManifest.xml"
    rm -rf "$TMP_DIR"
    # Migrate from android.support to AndroidX
    find "$SRC_BASE/java" -name "*.java" -exec sed -i \
        -e 's/android\.support\.annotation\./androidx.annotation./g' \
        -e 's/android\.support\.media\.ExifInterface/androidx.exifinterface.media.ExifInterface/g' \
        {} +
    echo "Download complete (migrated to AndroidX)."
fi

echo "=== Building $MODULE_NAME ==="
"$GRADLEW_DIR/gradlew" --project-dir "$SCRIPT_DIR" assembleRelease --no-daemon

OUTPUT_DIR="$ROOT_DIR/prebuild/android/$MODULE_NAME"
mkdir -p "$OUTPUT_DIR"

AAR=$(find "$SCRIPT_DIR/build/outputs/aar" -name "*-release.aar" | head -1)
if [ -n "$AAR" ]; then
    cp "$AAR" "$OUTPUT_DIR/materialfile-$MODULE_NAME.aar"
    echo "Copied -> prebuild/android/$MODULE_NAME/materialfile-$MODULE_NAME.aar"
else
    echo "ERROR: No AAR output found for $MODULE_NAME"
    exit 1
fi