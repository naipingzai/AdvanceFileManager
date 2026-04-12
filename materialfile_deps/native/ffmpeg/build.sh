#!/usr/bin/env bash
# build.sh - 编译 FFmpeg (精简版，仅保留格式转换所需组件)
# 用法: ./build.sh [--abi=arm64-v8a] [--clean]
set -euo pipefail
ABI="arm64-v8a"; CLEAN=false
for a in "$@"; do case "$a" in --abi=*) ABI="${a#*=}" ;; --clean) CLEAN=true ;; esac; done

LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$LIB_DIR/../../.." && pwd)"
SRC_DIR="$LIB_DIR/src"
OUTPUT_DIR="$PROJECT_ROOT/prebuild/native/ffmpeg"
SDK_DIR="$PROJECT_ROOT/tools/android-sdk"
NDK_DIR="$(ls -d "$SDK_DIR/ndk/"* 2>/dev/null | sort -V | tail -1)"
TOOLCHAIN="$NDK_DIR/toolchains/llvm/prebuilt/linux-x86_64"
TOOLCHAIN_BIN="$TOOLCHAIN/bin"
SYSROOT="$TOOLCHAIN/sysroot"
MIN_API=21
NPROC=$(nproc 2>/dev/null || echo 4)

case "$ABI" in
    arm64-v8a)
        ARCH="aarch64"; CPU="armv8-a"
        CROSS_PREFIX="aarch64-linux-android" ;;
    armeabi-v7a)
        ARCH="arm"; CPU="armv7-a"
        CROSS_PREFIX="armv7a-linux-androideabi" ;;
    x86_64)
        ARCH="x86_64"; CPU="x86-64"
        CROSS_PREFIX="x86_64-linux-android" ;;
    x86)
        ARCH="x86"; CPU="i686"
        CROSS_PREFIX="i686-linux-android" ;;
    *)
        echo "Unsupported ABI: $ABI"; exit 1 ;;
esac

CC="$TOOLCHAIN_BIN/${CROSS_PREFIX}${MIN_API}-clang"
CXX="$TOOLCHAIN_BIN/${CROSS_PREFIX}${MIN_API}-clang++"
BUILD_DIR="$LIB_DIR/build/$ABI"

# Download source if not present
if [ ! -d "$SRC_DIR" ] || [ -z "$(ls -A "$SRC_DIR" 2>/dev/null)" ]; then
    echo "Downloading ffmpeg 7.1..."
    ARCHIVE="$(mktemp).tar.gz"
    curl -L --progress-bar -o "$ARCHIVE" "https://github.com/FFmpeg/FFmpeg/archive/refs/tags/n7.1.tar.gz"
    mkdir -p "$SRC_DIR"
    tar xf "$ARCHIVE" --strip-components=1 -C "$SRC_DIR"
    rm -f "$ARCHIVE"
    echo "Download complete."
fi

if [ "$CLEAN" = true ]; then
    rm -rf "$BUILD_DIR" "$OUTPUT_DIR"
fi

mkdir -p "$BUILD_DIR" "$OUTPUT_DIR/lib" "$OUTPUT_DIR/include"

echo "=== FFmpeg Configure ($ABI) ==="
cd "$BUILD_DIR"
"$SRC_DIR/configure" \
    --prefix="$OUTPUT_DIR" \
    --target-os=android \
    --arch=$ARCH \
    --cpu=$CPU \
    --cc="$CC" \
    --cxx="$CXX" \
    --sysroot="$SYSROOT" \
    --cross-prefix="$TOOLCHAIN_BIN/${CROSS_PREFIX}-" \
    --nm="$TOOLCHAIN_BIN/llvm-nm" \
    --ar="$TOOLCHAIN_BIN/llvm-ar" \
    --ranlib="$TOOLCHAIN_BIN/llvm-ranlib" \
    --strip="$TOOLCHAIN_BIN/llvm-strip" \
    --enable-cross-compile \
    --enable-pic \
    --enable-small \
    --enable-static \
    --disable-shared \
    --disable-programs \
    --disable-doc \
    --disable-htmlpages \
    --disable-manpages \
    --disable-podpages \
    --disable-txtpages \
    --disable-avdevice \
    --disable-postproc \
    --disable-network \
    --disable-debug \
    --disable-symver \
    --disable-vulkan \
    --disable-v4l2-m2m \
    --disable-everything \
    --enable-jni \
    --enable-mediacodec \
    --enable-decoder=aac,mp3,flac,opus,vorbis,pcm_s16le,pcm_s24le,pcm_s32le,pcm_f32le \
    --enable-decoder=h264,hevc,vp8,vp9,av1,mpeg4,mjpeg \
    --enable-decoder=h264_mediacodec,hevc_mediacodec,vp8_mediacodec,vp9_mediacodec,av1_mediacodec \
    --enable-decoder=png,mjpeg,bmp,webp,gif,tiff \
    --enable-decoder=flv,vp6,vp6f,vp6a \
    --enable-decoder=wmv1,wmv2,wmv3,wmav1,wmav2 \
    --enable-decoder=mpeg1video,mpeg2video,mpegvideo \
    --enable-decoder=rv10,rv20,rv30,rv40,cook,ra_288 \
    --enable-encoder=aac,opus,pcm_s16le,flac \
    --enable-encoder=png,mjpeg,gif \
    --enable-encoder=h264_mediacodec,hevc_mediacodec \
    --enable-encoder=vp8_mediacodec,vp9_mediacodec \
    --enable-encoder=mpeg4 \
    --enable-demuxer=aac,mp3,flac,ogg,wav,mov,matroska,avi,mpegts,concat \
    --enable-demuxer=flv,asf,mpegps,mpegvideo,rm \
    --enable-demuxer=image2,image_png_pipe,image_jpeg_pipe,image_webp_pipe,image_bmp_pipe,gif \
    --enable-muxer=mp4,ipod,adts,mp3,flac,ogg,wav,matroska,webm,avi,mpegts,mov,image2,webp,png,mjpeg,gif \
    --enable-parser=aac,h264,hevc,vp8,vp9,av1,mpegaudio,opus,flac,png,mjpeg,bmp,webp \
    --enable-parser=mpeg4video,mpegvideo \
    --enable-protocol=file \
    --enable-filter=aresample,scale,format,aformat,anull,null,unsharp \
    --enable-bsf=aac_adtstoasc,h264_mp4toannexb,hevc_mp4toannexb \
    --enable-swresample \
    --enable-swscale \
    --extra-cflags="-Os -fPIC -DANDROID" \
    --extra-ldflags="-lm -llog -landroid -lmediandk"

echo "=== FFmpeg Build ($ABI) ==="
make -j"$NPROC"

echo "=== FFmpeg Install ($ABI) ==="
make install

mv "$OUTPUT_DIR/lib/libavcodec.a" "$OUTPUT_DIR/lib/libmaterialfile_avcodec.a"
mv "$OUTPUT_DIR/lib/libavformat.a" "$OUTPUT_DIR/lib/libmaterialfile_avformat.a"
mv "$OUTPUT_DIR/lib/libavutil.a" "$OUTPUT_DIR/lib/libmaterialfile_avutil.a"
mv "$OUTPUT_DIR/lib/libavfilter.a" "$OUTPUT_DIR/lib/libmaterialfile_avfilter.a"
mv "$OUTPUT_DIR/lib/libswresample.a" "$OUTPUT_DIR/lib/libmaterialfile_swresample.a"
mv "$OUTPUT_DIR/lib/libswscale.a" "$OUTPUT_DIR/lib/libmaterialfile_swscale.a"

echo "FFmpeg OK -> $OUTPUT_DIR"
