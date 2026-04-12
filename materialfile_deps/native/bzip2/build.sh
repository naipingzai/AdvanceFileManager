#!/usr/bin/env bash
# build.sh - 编译 bzip2
# 用法: ./build.sh [--abi arm64-v8a] [--clean]
set -euo pipefail
ABI="arm64-v8a"; CLEAN=false
for a in "$@"; do case "$a" in --abi=*) ABI="${a#*=}" ;; --clean) CLEAN=true ;; esac; done

LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$LIB_DIR/../../.." && pwd)"
SRC_DIR="$LIB_DIR/src"
OUTPUT_DIR="$PROJECT_ROOT/prebuild/native/bzip2"

# Download source if not present
if [ ! -d "$SRC_DIR" ] || [ -z "$(ls -A "$SRC_DIR" 2>/dev/null)" ]; then
    echo "Downloading bzip2 1.0.8..."
    ARCHIVE="$(mktemp).tar.gz"
    curl -L --progress-bar -o "$ARCHIVE" "https://sourceware.org/pub/bzip2/bzip2-1.0.8.tar.gz"
    mkdir -p "$SRC_DIR"
    tar xf "$ARCHIVE" --strip-components=1 -C "$SRC_DIR"
    rm -f "$ARCHIVE"
    echo "Download complete."
fi
SDK_DIR="$PROJECT_ROOT/tools/android-sdk"
NDK_DIR="$(ls -d "$SDK_DIR/ndk/"* 2>/dev/null | sort -V | tail -1)"
NDK_BIN="$NDK_DIR/toolchains/llvm/prebuilt/linux-x86_64/bin"
CLANG="$NDK_BIN/clang"; AR="$NDK_BIN/llvm-ar"
MIN_API=21
case "$ABI" in arm64-v8a) TARGET="aarch64-linux-android$MIN_API" ;; armeabi-v7a) TARGET="armv7a-linux-androideabi$MIN_API" ;; x86_64) TARGET="x86_64-linux-android$MIN_API" ;; x86) TARGET="i686-linux-android$MIN_API" ;; esac
BUILD_DIR="$LIB_DIR/build/$ABI"

[ "$CLEAN" = true ] && rm -rf "$BUILD_DIR" "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR/lib" "$OUTPUT_DIR/include" "$BUILD_DIR"

CFLAGS="--target=$TARGET -DANDROID -O2 -fPIC -fdata-sections -ffunction-sections -I$SRC_DIR -DUSE_MMAP -Wno-unused-parameter"
OBJS=""
for f in blocksort huffman crctable randtable compress decompress bzlib; do
    "$CLANG" $CFLAGS -c "$SRC_DIR/$f.c" -o "$BUILD_DIR/$f.o"
    OBJS="$OBJS $BUILD_DIR/$f.o"
done
"$AR" rcs "$BUILD_DIR/libmaterialfile_bz2.a" $OBJS
cp "$BUILD_DIR/libmaterialfile_bz2.a" "$OUTPUT_DIR/lib/"
cp "$SRC_DIR/bzlib.h" "$OUTPUT_DIR/include/"
echo "bzip2 OK -> $OUTPUT_DIR"
