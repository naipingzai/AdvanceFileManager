#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "========================================"
echo "  Third-party Library Build-All Script"
echo "========================================"
echo ""

FAILED=()
SUCCEEDED=()

run_build() {
    local category="$1"
    local name="$2"
    local script="$SCRIPT_DIR/$category/$name/build.sh"
    if [ ! -f "$script" ]; then
        echo "SKIP: $category/$name (no build.sh)"
        return
    fi
    echo ""
    echo ">>> Building $category/$name <<<"
    if bash "$script"; then
        SUCCEEDED+=("$category/$name")
        echo "<<< $category/$name SUCCESS >>>"
    else
        FAILED+=("$category/$name")
        echo "<<< $category/$name FAILED >>>"
    fi
}

# Phase 1: Native libraries (independent)
echo "Phase 1: Native libraries (independent)"
for lib in bzip2 xz lz4 zstd mbedtls pcre2 ffmpeg; do
    run_build "native" "$lib"
done

# Phase 2: Native libraries (dependent)
echo ""
echo "Phase 2: Native libraries (dependent)"
run_build "native" "libarchive"   # depends on bzip2, xz, lz4, zstd, mbedtls
run_build "native" "selinux"      # depends on pcre2

# Phase 3: Android JAR modules
echo ""
echo "Phase 3: Android JAR modules"
for lib in okio okhttp dav4jvm; do
    run_build "android" "$lib"
done

# Phase 4: Android AAR modules (independent)
echo ""
echo "Phase 4: Android AAR modules (independent)"
for lib in advrecyclerview androidsvg coil-base drawer insetter \
           libsu-core licensesdialog materialshadownp photoview \
           preferencex shizuku-aidl shizuku-shared simplemenu \
           speed-dial subsampling; do
    run_build "android" "$lib"
done

# Phase 5: Android AAR modules (dependent)
echo ""
echo "Phase 5: Android AAR modules (dependent)"
for lib in coil coil-gif coil-svg coil-video insetter-ktx libsu-service shizuku-api; do
    run_build "android" "$lib"
done

# Summary
echo ""
echo "========================================"
echo "  Build Summary"
echo "========================================"
echo "Succeeded: ${#SUCCEEDED[@]}"
for s in "${SUCCEEDED[@]}"; do echo "  OK: $s"; done
if [ ${#FAILED[@]} -gt 0 ]; then
    echo "Failed: ${#FAILED[@]}"
    for f in "${FAILED[@]}"; do echo "  FAIL: $f"; done
    exit 1
else
    echo "All builds succeeded!"
fi
