#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../../.." && pwd)"
MODULE_NAME="$(basename "$SCRIPT_DIR")"

export JAVA_HOME="$ROOT_DIR/tools/jdk-17.0.12"
export ANDROID_HOME="$ROOT_DIR/tools/android-sdk"

GRADLEW_DIR="$SCRIPT_DIR/../gradle"

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