#!/usr/bin/env bash
# build.sh - 编译 xz (liblzma)
set -euo pipefail

ABI="arm64-v8a"; CLEAN=false
for a in "$@"; do case "$a" in --abi=*) ABI="${a#*=}" ;; --clean) CLEAN=true ;; esac; done

LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$LIB_DIR/../../.." && pwd)"
SRC_DIR="$LIB_DIR/src"
OUTPUT_DIR="$PROJECT_ROOT/prebuild/native/xz"

# Download source if not present
if [ ! -d "$SRC_DIR" ] || [ -z "$(ls -A "$SRC_DIR" 2>/dev/null)" ]; then
    echo "Downloading xz 5.6.4..."
    ARCHIVE="$(mktemp).tar.gz"
    curl -L --progress-bar -o "$ARCHIVE" "https://github.com/tukaani-project/xz/archive/refs/tags/v5.6.4.tar.gz"
    mkdir -p "$SRC_DIR"
    tar xf "$ARCHIVE" --strip-components=1 -C "$SRC_DIR"
    rm -f "$ARCHIVE"
    echo "Download complete."
fi

SDK_DIR="$PROJECT_ROOT/tools/android-sdk"
NDK_DIR="$(ls -d "$SDK_DIR/ndk/"* 2>/dev/null | sort -V | tail -1)"
CMAKE_DIR="$(ls -d "$SDK_DIR/cmake/"* 2>/dev/null | sort -V | tail -1)"
CMAKE="$CMAKE_DIR/bin/cmake"
NINJA="$CMAKE_DIR/bin/ninja"
TOOLCHAIN="$NDK_DIR/build/cmake/android.toolchain.cmake"

MIN_API=21
BUILD_DIR="$LIB_DIR/build/$ABI"

[ "$CLEAN" = true ] && rm -rf "$BUILD_DIR" "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR/lib" "$OUTPUT_DIR/include"

"$CMAKE" -G Ninja -DCMAKE_MAKE_PROGRAM="$NINJA" -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN" \
    -DANDROID_ABI="$ABI" -DANDROID_PLATFORM="android-$MIN_API" -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_INSTALL_PREFIX="$OUTPUT_DIR" \
    -DBUILD_SHARED_LIBS=OFF \
    -DCREATE_XZ_SYMLINKS=OFF \
    -DCREATE_LZMA_SYMLINKS=OFF \
    -B "$BUILD_DIR" -S "$SRC_DIR"

"$CMAKE" --build "$BUILD_DIR" --parallel

"$CMAKE" --install "$BUILD_DIR" --component liblzma_Runtime
"$CMAKE" --install "$BUILD_DIR" --component liblzma_Development

mv "$OUTPUT_DIR/lib/liblzma.a" "$OUTPUT_DIR/lib/libmaterialfile_lzma.a"

echo "xz (liblzma) OK -> $OUTPUT_DIR"
