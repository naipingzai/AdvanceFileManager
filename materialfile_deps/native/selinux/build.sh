#!/usr/bin/env bash
# build.sh - ?? selinux (libselinux)
# ?? NDK clang ???????? CMake
set -euo pipefail

ABI="arm64-v8a"; CLEAN=false
for a in "$@"; do case "$a" in --abi=*) ABI="${a#*=}" ;; --clean) CLEAN=true ;; esac; done

LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$LIB_DIR/../../.." && pwd)"
SRC_DIR="$LIB_DIR/src/libselinux/src"
SRC_BASE="$LIB_DIR/src"
INCLUDE_DIR="$LIB_DIR/src/libselinux/include"
OUTPUT_DIR="$PROJECT_ROOT/prebuild/native/selinux"
PREBUILD_NATIVE="$PROJECT_ROOT/prebuild/native"

# Download source if not present
if [ ! -d "$SRC_BASE" ] || [ -z "$(ls -A "$SRC_BASE" 2>/dev/null)" ]; then
    echo "Downloading selinux 3.7..."
    ARCHIVE="$(mktemp).tar.gz"
    curl -L --progress-bar -o "$ARCHIVE" "https://github.com/SELinuxProject/selinux/archive/refs/tags/3.7.tar.gz"
    mkdir -p "$SRC_BASE"
    tar xf "$ARCHIVE" --strip-components=1 -C "$SRC_BASE"
    rm -f "$ARCHIVE"
    echo "Download complete."
fi

SDK_DIR="$PROJECT_ROOT/tools/android-sdk"
NDK_DIR="$(ls -d "$SDK_DIR/ndk/"* 2>/dev/null | sort -V | tail -1)"

MIN_API=21
BUILD_DIR="$LIB_DIR/build/$ABI"

# ?? ABI ?? target triple
case "$ABI" in
    arm64-v8a)   TARGET="aarch64-linux-android$MIN_API" ;;
    armeabi-v7a) TARGET="armv7a-linux-androideabi$MIN_API" ;;
    x86)         TARGET="i686-linux-android$MIN_API" ;;
    x86_64)      TARGET="x86_64-linux-android$MIN_API" ;;
    *) echo "Unsupported ABI: $ABI" >&2; exit 1 ;;
esac

# ?? toolchain bin ??
HOST_TAG="linux-x86_64"
if [[ "$(uname -s)" == "Darwin" ]]; then HOST_TAG="darwin-x86_64"; fi
TOOLCHAIN_BIN="$NDK_DIR/toolchains/llvm/prebuilt/$HOST_TAG/bin"
CC="$TOOLCHAIN_BIN/clang"
AR="$TOOLCHAIN_BIN/llvm-ar"

[ "$CLEAN" = true ] && rm -rf "$BUILD_DIR" "$OUTPUT_DIR"
mkdir -p "$BUILD_DIR"
mkdir -p "$OUTPUT_DIR/lib" "$OUTPUT_DIR/include/selinux"

# ????�?SOURCES=(
    avc.c avc_internal.c avc_sidtab.c booleans.c callbacks.c
    canonicalize_context.c checkAccess.c check_context.c
    compute_av.c compute_create.c compute_member.c context.c
    deny_unknown.c disable.c enabled.c
    fgetfilecon.c freecon.c fsetfilecon.c get_initial_context.c
    getenforce.c getfilecon.c getpeercon.c hashtab.c init.c
    label.c label_backends_android.c label_file.c label_support.c
    lgetfilecon.c load_policy.c lsetfilecon.c mapping.c
    matchpathcon.c policyvers.c procattr.c regex.c
    reject_unknown.c selinux_internal.c sestatus.c
    setenforce.c setfilecon.c setrans_client.c sha1.c stringrep.c
)

# ????
DEFINES=(
    -DNO_PERSISTENTLY_STORED_PATTERNS
    -DDISABLE_SETRANS
    -DDISABLE_BOOL
    -D_GNU_SOURCE
    -DNO_MEDIA_BACKEND
    -DNO_X_BACKEND
    -DNO_DB_BACKEND
    -DUSE_PCRE2
    -DPCRE2_CODE_UNIT_WIDTH=8
    -DAUDITD_LOG_TAG=1003
)

INCLUDES=(
    -I"$INCLUDE_DIR"
    -I"$SRC_DIR"
    -I"$PREBUILD_NATIVE/pcre2/include"
)

EXTRA_FLAGS=(
    -Wno-error=missing-noreturn
    -Wno-error=unused-function
    -Wno-error=unused-variable
    -Wno-error=unused-but-set-variable
    '-D__fsetlocking(fp,type)=0'
)

COMMON_FLAGS=(--target="$TARGET" -DANDROID -fPIC -O2 "${DEFINES[@]}" "${INCLUDES[@]}" "${EXTRA_FLAGS[@]}")

# ???? .c ??
OBJECTS=()
for src in "${SOURCES[@]}"; do
    obj="${src%.c}.o"
    echo "CC $src"
    "$CC" -c "$SRC_DIR/$src" -o "$BUILD_DIR/$obj" "${COMMON_FLAGS[@]}"
    OBJECTS+=("$BUILD_DIR/$obj")
done

# ?????
echo "AR libmaterialfile_selinux.a"
"$AR" rcs "$BUILD_DIR/libmaterialfile_selinux.a" "${OBJECTS[@]}"

# ????
cp "$BUILD_DIR/libmaterialfile_selinux.a" "$OUTPUT_DIR/lib/libmaterialfile_selinux.a"
cp "$INCLUDE_DIR/selinux/selinux.h" "$OUTPUT_DIR/include/selinux/selinux.h"

echo "selinux OK -> $OUTPUT_DIR"
