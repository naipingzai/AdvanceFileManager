#!/usr/bin/env bash
# build.sh - 编译 libarchive
set -euo pipefail

ABI="arm64-v8a"; CLEAN=false
for a in "$@"; do case "$a" in --abi=*) ABI="${a#*=}" ;; --clean) CLEAN=true ;; esac; done

LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$LIB_DIR/../../.." && pwd)"
SRC_DIR="$LIB_DIR/src"
OUTPUT_DIR="$PROJECT_ROOT/prebuild/native/libarchive"
PREBUILD_NATIVE="$PROJECT_ROOT/prebuild/native"

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
    echo "Downloading libarchive 3.7.7..."
    ARCHIVE="$(mktemp).tar.gz"
    curl -L --progress-bar -o "$ARCHIVE" "https://github.com/libarchive/libarchive/archive/refs/tags/v3.7.7.tar.gz"
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
    -DBUILD_SHARED_LIBS=OFF \
    -DENABLE_TEST=OFF \
    -DENABLE_TAR=OFF \
    -DENABLE_CPIO=OFF \
    -DENABLE_CAT=OFF \
    -DENABLE_UNZIP=OFF \
    -DENABLE_XATTR=OFF \
    -DENABLE_ACL=OFF \
    -DENABLE_ICONV=OFF \
    -DENABLE_EXPAT=OFF \
    -DENABLE_LIBXML2=OFF \
    -DENABLE_OPENSSL=OFF \
    -DENABLE_LIBB2=OFF \
    -DENABLE_LZ4=ON \
    -DENABLE_LZMA=ON \
    -DENABLE_ZSTD=ON \
    -DENABLE_ZLIB=ON \
    -DENABLE_BZip2=ON \
    -DENABLE_MBEDTLS=ON \
    -DBZIP2_INCLUDE_DIR="$PREBUILD_NATIVE/bzip2/include" \
    -DBZIP2_LIBRARIES="$PREBUILD_NATIVE/bzip2/lib/libmaterialfile_bz2.a" \
    -DHAVE_LIBBZ2:BOOL=TRUE \
    -DLIBLZMA_INCLUDE_DIR="$PREBUILD_NATIVE/xz/include" \
    -DLIBLZMA_LIBRARY="$PREBUILD_NATIVE/xz/lib/libmaterialfile_lzma.a" \
    -DLIBLZMA_HAS_AUTO_DECODER:BOOL=TRUE \
    -DLIBLZMA_HAS_EASY_ENCODER:BOOL=TRUE \
    -DLIBLZMA_HAS_LZMA_PRESET:BOOL=TRUE \
    -DHAVE_LIBLZMA:BOOL=TRUE \
    -DLZ4_INCLUDE_DIR="$PREBUILD_NATIVE/lz4/include" \
    -DLZ4_LIBRARY="$PREBUILD_NATIVE/lz4/lib/libmaterialfile_lz4.a" \
    -DHAVE_LIBLZ4:BOOL=TRUE \
    -DZSTD_INCLUDE_DIR="$PREBUILD_NATIVE/zstd/include" \
    -DZSTD_LIBRARY="$PREBUILD_NATIVE/zstd/lib/libmaterialfile_zstd.a" \
    -DHAVE_ZSTD:BOOL=TRUE \
    -DMBEDTLS_INCLUDE_DIRS="$PREBUILD_NATIVE/mbedtls/include" \
    -DMBEDTLS_LIBRARY="$PREBUILD_NATIVE/mbedtls/lib/libmbedtls.a" \
    -DMBEDX509_LIBRARY="$PREBUILD_NATIVE/mbedtls/lib/libmbedx509.a" \
    -DMBEDCRYPTO_LIBRARY="$PREBUILD_NATIVE/mbedtls/lib/libmaterialfile_mbedcrypto.a" \
    -B "$BUILD_DIR" -S "$SRC_DIR"

"$CMAKE" --build "$BUILD_DIR" --parallel

"$CMAKE" --install "$BUILD_DIR"

mv "$OUTPUT_DIR/lib/libarchive.a" "$OUTPUT_DIR/lib/libmaterialfile_archive.a"

echo "libarchive OK -> $OUTPUT_DIR"
