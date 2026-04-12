# 预编译依赖清单

## 目录结构

```
prebuild/
├── native/            # C/C++ 静态库（NDK 交叉编译产物）
└── android/           # Java/Kotlin JAR/AAR
```

所有库从 `materialfile_deps/` 目录的源码编译而来。
详细的库来源、下载方式和构建说明见 [materialfile_deps/README.md](../materialfile_deps/README.md)。

## Native 静态库（9 个）

输出目录：`prebuild/native/<name>/`

| 库名 | 版本 | 产物 |
|------|------|------|
| bzip2 | 1.0.8 | libmaterialfile_bz2.a |
| xz | 5.6.4 | libmaterialfile_lzma.a |
| lz4 | 1.10.0 | libmaterialfile_lz4.a |
| zstd | 1.5.6 | libmaterialfile_zstd.a |
| mbedtls | 3.6.5 | libmaterialfile_mbedcrypto.a |
| pcre2 | 10.44 | libmaterialfile_pcre2.a |
| libarchive | 3.7.7 | libmaterialfile_archive.a |
| selinux | AOSP | libmaterialfile_selinux.a |
| ffmpeg | 7.1 | libmaterialfile_avcodec.a libmaterialfile_avformat.a libmaterialfile_avutil.a libmaterialfile_avfilter.a libmaterialfile_swresample.a libmaterialfile_swscale.a |

## Android 库（25 个）

输出目录：`prebuild/android/<name>/`

| 模块 | 版本 | 格式 | 文件名 |
|------|------|------|--------|
| okio | 3.9.0 | JAR | materialfile-okio.jar |
| okhttp | 4.12.0 | JAR | materialfile-okhttp.jar |
| dav4jvm | ec6264d | JAR | materialfile-dav4jvm.jar |
| advrecyclerview | 1.0.0 | AAR | materialfile-advrecyclerview.aar |
| androidsvg | 1.4 | AAR | materialfile-androidsvg.aar |
| coil-base / coil / coil-gif / coil-svg / coil-video | 2.7.0 | AAR | materialfile-coil-*.aar |
| drawer | master | AAR | materialfile-drawer.aar |
| insetter / insetter-ktx | 0.3.1 | AAR | materialfile-insetter*.aar |
| libsu-core / libsu-service | 5.2.2 | AAR | materialfile-libsu-*.aar |
| licensesdialog | 2.2.0 | AAR | materialfile-licensesdialog.aar |
| materialshadownp | 1.0.0 | AAR | materialfile-materialshadownp.aar |
| photoview | 2.3.0 | AAR | materialfile-photoview.aar |
| preferencex | 1.1.0 | AAR | materialfile-preferencex.aar |
| shizuku-aidl / shizuku-api / shizuku-shared | 12.1.0 | AAR | materialfile-shizuku-*.aar |
| simplemenu | 自建 | AAR | materialfile-simplemenu.aar |
| speed-dial | 3.3.0 | AAR | materialfile-speed-dial.aar |
| subsampling | 3.10.0 | AAR | materialfile-subsampling.aar |
