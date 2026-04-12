#!/usr/bin/env bash
# build.sh - 编译 mbedtls
set -euo pipefail

ABI="arm64-v8a"; CLEAN=false
for a in "$@"; do case "$a" in --abi=*) ABI="${a#*=}" ;; --clean) CLEAN=true ;; esac; done

LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$LIB_DIR/../../.." && pwd)"
SRC_DIR="$LIB_DIR/src"
OUTPUT_DIR="$PROJECT_ROOT/prebuild/native/mbedtls"

# Download source if not present (needs git clone for submodules)
if [ ! -f "$SRC_DIR/CMakeLists.txt" ]; then
    echo "Cloning mbedtls 3.6.5 (with submodules)..."
    rm -rf "$SRC_DIR"
    git clone --depth 1 --branch v3.6.5 --recurse-submodules https://github.com/Mbed-TLS/mbedtls.git "$SRC_DIR"
    echo "Clone complete."
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
    -DENABLE_PROGRAMS=OFF \
    -DENABLE_TESTING=OFF \
    -DUSE_SHARED_MBEDTLS_LIBRARY=OFF \
    -DUSE_STATIC_MBEDTLS_LIBRARY=ON \
    -DMBEDTLS_FATAL_WARNINGS=OFF \
    -B "$BUILD_DIR" -S "$SRC_DIR"

"$CMAKE" --build "$BUILD_DIR" --parallel

"$CMAKE" --install "$BUILD_DIR"

mv "$OUTPUT_DIR/lib/libmbedcrypto.a" "$OUTPUT_DIR/lib/libmaterialfile_mbedcrypto.a"

echo "mbedtls OK -> $OUTPUT_DIR"
