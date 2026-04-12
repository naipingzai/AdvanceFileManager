# Material Files

[本文中文版](README_zh-CN.md)

An open source Material Design 3 file manager for Android 5.0+, with built-in media viewers and practical tools.

## Features

### File Management

- **Material Design 3**: Fully compliant with the latest Material Design 3 guidelines, with meticulous attention to detail in layout, color, typography and motion.
- **Breadcrumb navigation**: Navigate in the filesystem with ease by tapping any folder in the path bar.
- **Root support**: View and manage files with root access.
- **Archive support**: View, extract and create common compressed files (ZIP, TAR, GZIP, BZIP2, XZ, 7Z, RAR, etc.).
- **Themes**: Customizable UI colors with Material You dynamic color support, plus night mode with optional true black.
- **Linux-aware**: Like [Nautilus](https://apps.gnome.org/Nautilus/), knows symbolic links, file permissions and SELinux context.

### Built-in Viewers

- **Image viewer**: View images with zoom, pan and slideshow support.
- **Video player**: Built-in video player powered by ExoPlayer with swipe-to-seek gesture, long-press speed control and progress bar.
- **Audio player**: Built-in audio player with album art display, playback controls and progress seeking.
- **PDF viewer**: Render and browse PDF documents page by page without leaving the app.
- **Text editor**: Edit text files with encoding detection, encoding switching and save support.
- **Hex viewer**: Inspect binary files with hex + ASCII side-by-side display.

### Tools

- **Trash (Recycle Bin)**: Move files to trash instead of permanent deletion, with restore support.
- **File search**: Search files by name across the filesystem.
- **Duplicate finder**: Find duplicate files to free up storage space.
- **Empty file search**: Scan for empty files and folders to clean up.
- **Storage analysis**: Visualize disk usage with categorized breakdown.
- **Save As**: Receive shared content from other apps and save to any location.

### FFmpeg Media Tools

- **Format conversion**: Convert audio/video between common formats (MP3, AAC, FLAC, MP4, WebM, etc.).
- **Audio extraction**: Extract audio tracks from video files.
- **Video/Image enhance**: Sharpen video and images using FFmpeg unsharp filter.
- **Media info**: Display detailed codec, bitrate, resolution and duration information.
- **File compare**: Compare two files byte-by-byte for differences.

### Technical Highlights

- **Robust**: Uses Linux system calls under the hood, not yet another [`ls` parser](https://news.ycombinator.com/item?id=7994720).
- **Well-implemented**: Built upon [Java NIO2 File API](https://docs.oracle.com/javase/8/docs/api/java/nio/file/package-summary.html) and [LiveData](https://developer.android.com/topic/libraries/architecture/livedata), with modern `ViewModel` architecture.
- **Correct encoding**: Handles file names with invalid UTF-8 encoding properly, as paths are not naively stored as Java `String`s.

## Build

### Prerequisites

All build tools are included in the `tools/` directory:

- JDK 17 (`tools/jdk-17.0.12`)
- Android SDK (`tools/android-sdk`, includes NDK 28.1.13356709)
- Gradle 8.13, AGP 8.11.1, Kotlin 2.1.21

### Build Third-Party Libraries

All third-party dependencies are compiled from source. Build them first before building the APK:

```powershell
# PowerShell (Windows)
powershell -NoProfile -ExecutionPolicy Bypass -File materialfile_deps/build-all.ps1

# Shell (Linux/macOS)
bash materialfile_deps/build-all.sh
```

To build a single library:

```powershell
# Example: build bzip2 (native)
powershell -NoProfile -ExecutionPolicy Bypass -File materialfile_deps/native/bzip2/build.ps1

# Example: build advrecyclerview (android)
powershell -NoProfile -ExecutionPolicy Bypass -File materialfile_deps/android/advrecyclerview/build.ps1
```

### Build APK

```bash
./gradlew assembleDebug    # Debug build
./gradlew assembleRelease  # Release build (requires signing config)
```

## Third-Party Dependencies

All third-party libraries are compiled from source. Sources and build scripts are in `materialfile_deps/` (organized by `native/` and `android/`), build outputs go to `prebuild/`. Compiled artifacts use `materialfile` prefix to indicate project-local builds.

See [materialfile_deps/README.md](materialfile_deps/README.md) for download sources, build instructions, and dependency details.

### Native (C/C++) — 9 libraries

| Library | Version | Description |
|---------|---------|-------------|
| [FFmpeg](https://github.com/FFmpeg/FFmpeg) | 7.1 | Audio/video codec and processing |
| [bzip2](https://sourceware.org/bzip2/) | 1.0.8 | bzip2 compression |
| [xz](https://github.com/tukaani-project/xz) | 5.6.4 | XZ/LZMA compression |
| [lz4](https://github.com/lz4/lz4) | 1.10.0 | Fast lossless compression |
| [zstd](https://github.com/facebook/zstd) | 1.5.6 | Zstandard compression |
| [mbedtls](https://github.com/Mbed-TLS/mbedtls) | 3.6.5 | TLS crypto library |
| [pcre2](https://github.com/PCRE2Project/pcre2) | 10.44 | Perl-compatible regex |
| [libarchive](https://github.com/libarchive/libarchive) | 3.7.7 | Multi-format archive library |
| [selinux](https://android.googlesource.com/platform/external/selinux) | AOSP | SELinux userspace library |

### Android (Java/Kotlin) — 25 modules

| Library | Version | Description |
|---------|---------|-------------|
| [coil](https://github.com/coil-kt/coil) | 2.7.0 | Kotlin coroutine image loader (5 modules) |
| [okhttp](https://github.com/square/okhttp) | 4.12.0 | HTTP client |
| [okio](https://github.com/square/okio) | 3.9.0 | I/O library |
| [dav4jvm](https://github.com/niclas-nicoring/dav4jvm) | ec6264d | WebDAV client |
| [libsu](https://github.com/topjohnwu/libsu) | 5.2.2 | Root access library (2 modules) |
| [Shizuku](https://github.com/niclas-nicoring/Shizuku-API) | 13.1.5 | Privileged API framework (3 modules) |
| [PhotoView](https://github.com/Baseflow/PhotoView) | 2.3.0 | Gesture zoom ImageView |
| [AndroidSVG](https://github.com/niclas-nicoring/androidsvg) | 1.4 | SVG rendering |
| [MaterialDrawer](https://github.com/niclas-nicoring/MaterialDrawer) | 1.0.3 | Drawer navigation |
| [SpeedDial](https://github.com/leinardi/FloatingActionButtonSpeedDial) | 3.3.0 | FAB speed dial |
| [Subsampling](https://github.com/niclas-nicoring/subsampling-scale-image-view) | 3.10.0 | Large image viewer |
| [AdvRecyclerView](https://github.com/niclas-nicoring/android-advancedrecyclerview) | 1.0.0 | Advanced RecyclerView (2 modules) |
| [Insetter](https://github.com/niclas-nicoring/insetter) | 0.3.1 | WindowInsets helper (2 modules) |
| [PreferenceX](https://github.com/niclas-nicoring/MaterialPreference) | 1.1.0 | Enhanced Preference |
| [SimpleMenu](https://github.com/niclas-nicoring/SimpleMenuPreference) | 1.0.3 | SimpleMenu Preference |
| [LicensesDialog](https://github.com/niclas-nicoring/LicensesDialog) | 2.1.0 | License dialog |

## License

    Copyright (C) 2026 naipingzai <npznnz@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
