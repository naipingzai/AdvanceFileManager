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
# Prebuilt native libraries (core file management dependencies)
# ===========================================================================

# Platform libz (NDK built-in, with SIMD optimizations)
find_library(Z_LIBRARY libz.a)

add_library(bz STATIC IMPORTED)
set_target_properties(bz PROPERTIES IMPORTED_LOCATION ${PREBUILD_DIR}/bzip2/lib/libadvancefilemanager_bz2.a)

add_library(lzma STATIC IMPORTED)
set_target_properties(lzma PROPERTIES IMPORTED_LOCATION ${PREBUILD_DIR}/xz/lib/libadvancefilemanager_lzma.a)

add_library(lz4 STATIC IMPORTED)
set_target_properties(lz4 PROPERTIES IMPORTED_LOCATION ${PREBUILD_DIR}/lz4/lib/libadvancefilemanager_lz4.a)

add_library(zstd STATIC IMPORTED)
set_target_properties(zstd PROPERTIES IMPORTED_LOCATION ${PREBUILD_DIR}/zstd/lib/libadvancefilemanager_zstd.a)

add_library(mbedcrypto STATIC IMPORTED)
set_target_properties(mbedcrypto PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/mbedtls/lib/libadvancefilemanager_mbedcrypto.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/mbedtls/include")

add_library(archive STATIC IMPORTED)
set_target_properties(archive PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/libarchive/lib/libadvancefilemanager_archive.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/libarchive/include")

add_library(pcre2 STATIC IMPORTED)
set_target_properties(pcre2 PROPERTIES
        IMPORTED_LOCATION ${PREBUILD_DIR}/pcre2/lib/libadvancefilemanager_pcre2.a)

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
        IMPORTED_LOCATION ${PREBUILD_DIR}/selinux/lib/libadvancefilemanager_selinux.a
        INTERFACE_INCLUDE_DIRECTORIES "${PREBUILD_DIR}/selinux/include")

# --- selinux-jni (shared library) ---
add_library(selinux-jni SHARED
        src/main/jni/libselinux-jni.c)
target_link_libraries(selinux-jni selinux pcre2 ${LOG_LIBRARY})

# ===========================================================================
# FFmpeg JNI (media tools)
# ===========================================================================

add_subdirectory(src/main/cpp/ffmpeg)

