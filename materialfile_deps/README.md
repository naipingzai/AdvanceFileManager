# MaterialFile 依赖库

三方依赖库的源码和编译管理。所有库直接下载完整源码，通过本地编译脚本交叉编译。
编译产物统一使用 `materialfile` 前缀（`.a` / `.aar` / `.jar`），标识为本工程自编译产物。

## 目录结构

```
materialfile_deps/
├── build-all.ps1          # 全量编译脚本 (PowerShell/Windows)
├── build-all.sh           # 全量编译脚本 (Shell/Linux)
├── native/                # C/C++ 静态库源码 + 编译脚本
│   ├── bzip2/
│   ├── ffmpeg/
│   ├── libarchive/
│   ├── lz4/
│   ├── mbedtls/
│   ├── pcre2/
│   ├── selinux/
│   ├── xz/
│   └── zstd/
└── android/               # Java/Kotlin 库源码 + 编译脚本
    ├── gradle/            # 共享 Gradle wrapper
    └── ...                # 25 个模块
```

## 命名规范

| 类型 | 产物前缀 | 示例 |
|------|----------|------|
| Native 静态库 | `libmaterialfile_` | `libmaterialfile_bz2.a` |
| Android AAR | `materialfile-` | `materialfile-coil.aar` |
| Android JAR | `materialfile-` | `materialfile-okhttp.jar` |

头文件保持原始名称（第三方标准 API 头，如 `archive.h`、`bzlib.h`），通过 prebuild 目录结构区分归属。

---

## Native 库（C/C++）— 9 个

通过 Android NDK 交叉编译为 ARM64 静态库（`.a`），输出到 `prebuild/native/<name>/`。

### 来源与下载

| 库名 | 版本 | 来源 | 下载方式 | 产物 |
|------|------|------|----------|------|
| bzip2 | 1.0.8 | https://sourceware.org/bzip2/ | `wget https://sourceware.org/pub/bzip2/bzip2-1.0.8.tar.gz` | `libmaterialfile_bz2.a` |
| xz | 5.6.4 | https://tukaani.org/xz/ | `wget https://github.com/tukaani-project/xz/releases/download/v5.6.4/xz-5.6.4.tar.xz` | `libmaterialfile_lzma.a` |
| lz4 | 1.10.0 | https://github.com/lz4/lz4 | `git clone --branch v1.10.0 --depth 1 https://github.com/lz4/lz4.git` | `libmaterialfile_lz4.a` |
| zstd | 1.5.6 | https://github.com/facebook/zstd | `git clone --branch v1.5.6 --depth 1 https://github.com/facebook/zstd.git` | `libmaterialfile_zstd.a` |
| mbedtls | 3.6.5 | https://github.com/Mbed-TLS/mbedtls | `git clone --branch v3.6.5 --depth 1 https://github.com/Mbed-TLS/mbedtls.git` | `libmaterialfile_mbedcrypto.a` |
| pcre2 | 10.44 | https://github.com/PCRE2Project/pcre2 | `git clone --branch pcre2-10.44 --depth 1 https://github.com/PCRE2Project/pcre2.git` | `libmaterialfile_pcre2.a` |
| libarchive | 3.7.7 | https://github.com/libarchive/libarchive | `git clone --branch v3.7.7 --depth 1 https://github.com/libarchive/libarchive.git` | `libmaterialfile_archive.a` |
| selinux | AOSP | https://android.googlesource.com/platform/external/selinux | `git clone` (AOSP tag) | `libmaterialfile_selinux.a` |
| ffmpeg | 7.1 | https://ffmpeg.org/ | `git clone --branch n7.1 --depth 1 https://github.com/FFmpeg/FFmpeg.git` | `libmaterialfile_av*.a` `libmaterialfile_sw*.a` |

### 依赖关系

```
libarchive → bzip2, xz, lz4, zstd, mbedtls, libz(NDK 内置)
selinux    → pcre2
ffmpeg     → 无（独立编译）
其他       → 无（独立编译）
```

### 编译方式

每个库目录包含：
- `src/` — 完整源码（从上游直接下载，不做裁剪）
- `build.ps1` — PowerShell 编译脚本（Windows）
- `build.sh` — Shell 编译脚本（Linux/macOS）

编译流程：使用 NDK toolchain 的 clang 交叉编译，产出 ARM64 静态库，安装头文件和 `.a` 到 `prebuild/native/<name>/`。

```bash
# 单个库编译示例
bash materialfile_deps/native/bzip2/build.sh
# 或 Windows
powershell -File materialfile_deps/native/bzip2/build.ps1
```

---

## Android 库（Java/Kotlin）— 25 个模块

通过 Gradle 编译为 AAR/JAR，输出到 `prebuild/android/<name>/`。

### JAR 模块（3 个）

| 模块 | 版本 | 来源 | 下载方式 | 产物 |
|------|------|------|----------|------|
| okio | 3.9.0 | https://github.com/square/okio | `git clone --branch 3.9.0 --depth 1` | `materialfile-okio.jar` |
| okhttp | 4.12.0 | https://github.com/square/okhttp | `git clone --branch parent-4.12.0 --depth 1` | `materialfile-okhttp.jar` |
| dav4jvm | ec6264d | https://github.com/nicikess/dav4jvm | `git clone` + checkout commit | `materialfile-dav4jvm.jar` |

### AAR 模块（22 个）

| 模块 | 版本 | 来源 | 产物 |
|------|------|------|------|
| advrecyclerview | 1.0.0 | https://github.com/nicikess/android-advancedrecyclerview | `materialfile-advrecyclerview.aar` |
| androidsvg | 1.4 | https://github.com/nicikess/androidsvg | `materialfile-androidsvg.aar` |
| coil-base | 2.7.0 | https://github.com/coil-kt/coil (tag 2.7.0) | `materialfile-coil-base.aar` |
| coil | 2.7.0 | 同上 | `materialfile-coil.aar` |
| coil-gif | 2.7.0 | 同上 | `materialfile-coil-gif.aar` |
| coil-svg | 2.7.0 | 同上 | `materialfile-coil-svg.aar` |
| coil-video | 2.7.0 | 同上 | `materialfile-coil-video.aar` |
| drawer | master | https://github.com/nicikess/Drawer | `materialfile-drawer.aar` |
| insetter | 0.3.1 | https://github.com/nicikess/insetter | `materialfile-insetter.aar` |
| insetter-ktx | 0.3.1 | 同上 | `materialfile-insetter-ktx.aar` |
| libsu-core | 5.2.2 | https://github.com/topjohnwu/libsu (tag 5.2.2) | `materialfile-libsu-core.aar` |
| libsu-service | 5.2.2 | 同上 | `materialfile-libsu-service.aar` |
| licensesdialog | 2.2.0 | https://github.com/nicikess/LicensesDialog | `materialfile-licensesdialog.aar` |
| materialshadownp | 1.0.0 | 自建 | `materialfile-materialshadownp.aar` |
| photoview | 2.3.0 | https://github.com/Baseflow/PhotoView (tag 2.3.0) | `materialfile-photoview.aar` |
| preferencex | 1.1.0 | https://github.com/nicikess/preferencex-android | `materialfile-preferencex.aar` |
| shizuku-aidl | 12.1.0 | https://github.com/nicikess/Shizuku (tag 12.1.0) | `materialfile-shizuku-aidl.aar` |
| shizuku-api | 12.1.0 | 同上 | `materialfile-shizuku-api.aar` |
| shizuku-shared | 12.1.0 | 同上 | `materialfile-shizuku-shared.aar` |
| simplemenu | 自建 | 自建模块 | `materialfile-simplemenu.aar` |
| speed-dial | 3.3.0 | https://github.com/nicikess/floating-action-button-speed-dial | `materialfile-speed-dial.aar` |
| subsampling | 3.10.0 | https://github.com/nicikess/subsampling-scale-image-view | `materialfile-subsampling.aar` |

### 依赖关系

```
okhttp       → okio
dav4jvm      → okhttp
coil         → coil-base
coil-gif     → coil-base
coil-svg     → coil-base, androidsvg
coil-video   → coil-base
insetter-ktx → insetter
libsu-service → libsu-core
shizuku-api  → shizuku-shared, shizuku-aidl
```

### 编译方式

每个模块目录包含：
- `src/` — 完整源码
- `build.ps1` — PowerShell 编译脚本（Windows）
- `build.sh` — Shell 编译脚本（Linux/macOS）
- `build.gradle` — Gradle 构建文件

编译流程：通过 Gradle wrapper 执行 `assembleRelease`，将产物重命名为 `materialfile-<name>.aar/jar` 后复制到 `prebuild/android/<name>/`。

```bash
# 单个模块编译示例
bash materialfile_deps/android/photoview/build.sh
# 或 Windows
powershell -File materialfile_deps/android/photoview/build.ps1
```

---

## 全量编译

### 环境要求

- JDK 17: `tools/jdk-17.0.12`
- Android SDK: `tools/android-sdk`（含 NDK 28.1.13356709）
- Gradle 8.13, AGP 8.11.1, Kotlin 2.1.21

### 编译命令

```powershell
# PowerShell (Windows)
powershell -NoProfile -ExecutionPolicy Bypass -File materialfile_deps/build-all.ps1

# Shell (Linux/macOS)
bash materialfile_deps/build-all.sh
```

### 编译顺序

1. **Phase 1** — Native 独立库：bzip2, xz, lz4, zstd, mbedtls, pcre2, ffmpeg
2. **Phase 2** — Native 依赖库：libarchive（依赖 Phase 1）, selinux（依赖 pcre2）
3. **Phase 3** — Android JAR：okio → okhttp → dav4jvm
4. **Phase 4** — Android AAR 独立模块（无互依赖）
5. **Phase 5** — Android AAR 依赖模块（依赖 Phase 4 产物）
