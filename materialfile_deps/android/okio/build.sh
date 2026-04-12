#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../../.." && pwd)"
MODULE_NAME="okio"

export JAVA_HOME="$ROOT_DIR/tools/jdk-17.0.12"
export ANDROID_HOME="$ROOT_DIR/tools/android-sdk"

# ===== Download source =====
SRC_DIR="$SCRIPT_DIR/src"
if [ ! -d "$SRC_DIR/.git" ]; then
    echo "Cloning okio source (tag: parent-3.9.0)..."
    git clone --depth 1 --branch parent-3.9.0 https://github.com/square/okio.git "$SRC_DIR"
    echo "Clone complete."
fi

echo "=== Building $MODULE_NAME ==="
cd "$SCRIPT_DIR/src"
./gradlew :okio:jvmJar --no-daemon

OUTPUT_DIR="$ROOT_DIR/prebuild/android/$MODULE_NAME"
mkdir -p "$OUTPUT_DIR"

JAR=$(find "$SCRIPT_DIR/src/okio/build/libs" -name "*.jar" ! -name "*-sources*" ! -name "*-javadoc*" | head -1)
if [ -n "$JAR" ]; then
    cp "$JAR" "$OUTPUT_DIR/materialfile-$MODULE_NAME.jar"
    echo "Copied -> prebuild/android/$MODULE_NAME/materialfile-$MODULE_NAME.jar"
else
    echo "ERROR: No JAR output found for $MODULE_NAME"
    exit 1
fi
