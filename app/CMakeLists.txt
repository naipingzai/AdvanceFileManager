cmake_minimum_required(VERSION 3.22.1)

project(MaterialFiles C CXX ASM)

# Common optimization options (shared by all native libraries)
add_compile_options(
        -fdata-sections
        -ffunction-sections
        -flto)
add_link_options(
        LINKER:--gc-sections)

find_library(LOG_LIBRARY log)

# Convenience path variables
set(JNI_DIR ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jni)
set(PREBUILD_DIR ${CMAKE_SOURCE_DIR}/../prebuild/native)

# ===========================================================================
# App native libraries
# ===========================================================================

add_library(hiddenapi SHARED
        src/main/jni/hiddenapi.c)
target_compile_options(hiddenapi
        PRIVATE
        -Wall
        -Werror)

add_library(syscall SHARED
        src/main/jni/syscall.c)
target_compile_options(syscall
        PRIVATE
        -Wall
        -Werror)
target_link_libraries(syscall
        PRIVATE
        ${LOG_LIBRARY})

# ===========================================================================
# Prebuilt libraries (compiled from materialfile_deps)
# ===========================================================================

# Platform libz (NDK built-in, with SIMD optimizations)
find_library(Z_LIBRARY libz.a)

add_library(bz STATIC IMPORTED)
set_target_properties(bz PROPERTIES IMPORTED_LOCATION ${PREBUILD_DIR}/bzip2/lib/libmaterialfile_bz2.a)

add_library(lzma STATIC IMPORTED)
set_target_properties(lzma PROPERTIES IMPORTED_LOCATION ${PREBUILD_DIR}/xz/lib/libmaterialfile_lzma.a)

add_library(lz4 STATIC IMPORTED)
set_target_properties(lz4 PROPERTIES IMPORTED_LOCATION ${PREBUILD_DIR}/lz4/lib/libmaterialfile_lz4.a)

add_library(zstd STATIC IMPORTED)
set_target_properties(zstd PROPERTIES IMPORTED_LOCATION ${PREBUILD_DIR}/zstd/lib/libmaterialfile_zstd.a)

add_library(mbedcrypto STATIC IMPORTED)
set_target_properties(mbedcrypto PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/mbedtls/lib/libmaterialfile_mbedcrypto.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/mbedtls/include")

add_library(archive STATIC IMPORTED)
set_target_properties(archive PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/libarchive/lib/libmaterialfile_archive.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/libarchive/include")

add_library(pcre2 STATIC IMPORTED)
set_target_properties(pcre2 PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/pcre2/lib/libmaterialfile_pcre2.a)

# ===========================================================================
# archive-jni (shared library)
# ===========================================================================

add_library(archive-jni SHARED
        src/main/jni/archive-jni.c)
target_compile_options(archive-jni
        PRIVATE
        -Wall
        -Werror)
target_compile_definitions(archive-jni
        PRIVATE
        HAVE_MBEDTLS)
target_link_libraries(archive-jni
        archive bz lzma lz4 zstd mbedcrypto "${Z_LIBRARY}" ${LOG_LIBRARY})

# ===========================================================================
# Prebuilt libselinux
# ===========================================================================

add_library(selinux STATIC IMPORTED)
set_target_properties(selinux PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/selinux/lib/libmaterialfile_selinux.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/selinux/include")

# --- selinux-jni (shared library) ---
add_library(selinux-jni SHARED
        src/main/jni/libselinux-jni.c)
target_link_libraries(selinux-jni selinux pcre2 ${LOG_LIBRARY})

# ===========================================================================
# Prebuilt FFmpeg libraries
# ===========================================================================

add_library(avcodec STATIC IMPORTED)
set_target_properties(avcodec PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libmaterialfile_avcodec.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

add_library(avformat STATIC IMPORTED)
set_target_properties(avformat PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libmaterialfile_avformat.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

add_library(avutil STATIC IMPORTED)
set_target_properties(avutil PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libmaterialfile_avutil.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

add_library(avfilter STATIC IMPORTED)
set_target_properties(avfilter PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libmaterialfile_avfilter.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

add_library(swresample STATIC IMPORTED)
set_target_properties(swresample PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libmaterialfile_swresample.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

add_library(swscale STATIC IMPORTED)
set_target_properties(swscale PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libmaterialfile_swscale.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

# --- ffmpeg-jni (shared library) ---
add_library(ffmpeg-jni SHARED
        src/main/jni/ffmpeg-jni.c)
target_compile_options(ffmpeg-jni
        PRIVATE
        -Wall)
# Use --whole-archive for FFmpeg static libs to ensure all codecs,
# bitstream filters, muxers/demuxers are linked in (required for
# MediaCodec encoders which depend on BSFs like h264_mp4toannexb).
target_link_libraries(ffmpeg-jni
        -Wl,--whole-archive
        avformat avcodec avfilter swresample swscale avutil
        -Wl,--no-whole-archive
        "${Z_LIBRARY}" ${LOG_LIBRARY} m mediandk android)


