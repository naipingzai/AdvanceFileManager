#!/usr/bin/env bash
# build.sh - 编译 pcre2
set -euo pipefail

ABI="arm64-v8a"; CLEAN=false
for a in "$@"; do case "$a" in --abi=*) ABI="${a#*=}" ;; --clean) CLEAN=true ;; esac; done

LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$LIB_DIR/../../.." && pwd)"
SRC_DIR="$LIB_DIR/src"
OUTPUT_DIR="$PROJECT_ROOT/prebuild/native/pcre2"

SDK_DIR="$PROJECT_ROOT/tools/android-sdk"
NDK_DIR="$(ls -d "$SDK_DIR/ndk/"* 2>/dev/null | sort -V | tail -1)"
CMAKE_DIR="$(ls -d "$SDK_DIR/cmake/"* 2>/dev/null | sort -V | tail -1)"
CMAKE="$CMAKE_DIR/bin/cmake"
NINJA="$CMAKE_DIR/bin/ninja"
TOOLCHAIN="$NDK_DIR/build/cmake/android.toolchain.cmake"

MIN_API=21
BUILD_DIR="$LIB_DIR/build/$ABI"

# Download source if not present
if [ ! -d "$SRC_DIR" ] || [ -z "$(ls -A "$SRC_DIR" 2>/dev/null)" ]; then
    echo "Downloading pcre2 10.44..."
    ARCHIVE="$(mktemp).tar.gz"
    curl -L --progress-bar -o "$ARCHIVE" "https://github.com/PCRE2Project/pcre2/archive/refs/tags/pcre2-10.44.tar.gz"
    mkdir -p "$SRC_DIR"
    tar xf "$ARCHIVE" --strip-components=1 -C "$SRC_DIR"
    rm -f "$ARCHIVE"
    echo "Download complete."
fi

[ "$CLEAN" = true ] && rm -rf "$BUILD_DIR" "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR/lib" "$OUTPUT_DIR/include"

"$CMAKE" -G Ninja -DCMAKE_MAKE_PROGRAM="$NINJA" -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN" \
    -DANDROID_ABI="$ABI" -DANDROID_PLATFORM="android-$MIN_API" -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_INSTALL_PREFIX="$OUTPUT_DIR" \
    -DPCRE2_BUILD_TESTS=OFF \
    -DPCRE2_BUILD_PCRE2GREP=OFF \
    -DBUILD_SHARED_LIBS=OFF \
    -DBUILD_STATIC_LIBS=ON \
    -B "$BUILD_DIR" -S "$SRC_DIR"

"$CMAKE" --build "$BUILD_DIR" --parallel

"$CMAKE" --install "$BUILD_DIR"

mv "$OUTPUT_DIR/lib/libpcre2-8.a" "$OUTPUT_DIR/lib/libmaterialfile_pcre2.a"

echo "pcre2 OK -> $OUTPUT_DIR"
