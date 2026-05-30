cmake_minimum_required(VERSION 3.22.1)

project(FFmpegPlugin C)

add_compile_options(
        -fdata-sections
        -ffunction-sections
        -flto)
add_link_options(
        LINKER:--gc-sections)

find_library(LOG_LIBRARY log)
find_library(Z_LIBRARY libz.a)

set(PREBUILD_DIR ${CMAKE_SOURCE_DIR}/../../../../prebuild/native)

# ===========================================================================
# Prebuilt FFmpeg libraries
# ===========================================================================

add_library(avcodec STATIC IMPORTED)
set_target_properties(avcodec PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libadvancefilemanager_avcodec.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

add_library(avformat STATIC IMPORTED)
set_target_properties(avformat PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libadvancefilemanager_avformat.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

add_library(avutil STATIC IMPORTED)
set_target_properties(avutil PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libadvancefilemanager_avutil.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

add_library(avfilter STATIC IMPORTED)
set_target_properties(avfilter PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libadvancefilemanager_avfilter.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

add_library(swresample STATIC IMPORTED)
set_target_properties(swresample PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libadvancefilemanager_swresample.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

add_library(swscale STATIC IMPORTED)
set_target_properties(swscale PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/ffmpeg/lib/libadvancefilemanager_swscale.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/ffmpeg/include")

# --- ffmpeg-jni (shared library) ---
add_library(ffmpeg-jni SHARED
        ffmpeg-jni.c)
target_compile_options(ffmpeg-jni
        PRIVATE
        -Wall)
target_link_libraries(ffmpeg-jni
        -Wl,--whole-archive
        avformat avcodec avfilter swresample swscale avutil
        -Wl,--no-whole-archive
        "${Z_LIBRARY}" ${LOG_LIBRARY} m mediandk android)
