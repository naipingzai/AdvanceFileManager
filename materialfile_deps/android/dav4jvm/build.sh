#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../../.." && pwd)"
MODULE_NAME="dav4jvm"

export JAVA_HOME="$ROOT_DIR/tools/jdk-17.0.12"
export ANDROID_HOME="$ROOT_DIR/tools/android-sdk"

# ===== Download source =====
SRC_DIR="$SCRIPT_DIR/src"
if [ ! -d "$SRC_DIR/.git" ]; then
    echo "Cloning dav4jvm source..."
    git clone --depth 1 https://github.com/bitfireAT/dav4jvm.git "$SRC_DIR"
    echo "Clone complete."
    echo 'rootProject.name = "dav4jvm"' > "$SRC_DIR/settings.gradle.kts"
fi

echo "=== Building $MODULE_NAME ==="
cd "$SCRIPT_DIR/src"
./gradlew jar --no-daemon

OUTPUT_DIR="$ROOT_DIR/prebuild/android/$MODULE_NAME"
mkdir -p "$OUTPUT_DIR"

JAR=$(find "$SCRIPT_DIR/src/build/libs" -name "*.jar" ! -name "*-sources*" ! -name "*-javadoc*" | head -1)
if [ -n "$JAR" ]; then
    cp "$JAR" "$OUTPUT_DIR/materialfile-$MODULE_NAME.jar"
    echo "Copied -> prebuild/android/$MODULE_NAME/materialfile-$MODULE_NAME.jar"
else
    echo "ERROR: No JAR output found for $MODULE_NAME"
    exit 1
fi
