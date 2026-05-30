# 预编译依赖清单

## 目录结构

```
prebuild/
└── native/            # C/C++ 静态库（NDK 交叉编译产物）— 仅保留核心文件管理所需
```

Android (Java/Kotlin) 依赖已全部迁移到 Maven/JitPack 官方源，详见 `app/build.gradle`。
FFmpeg 已迁移至插件模块 `plugin-ffmpeg-tools/`。

## Native 静态库（8 个）

输出目录：`prebuild/native/<name>/`

| 库名 | 版本 | 产物 | 用途 |
|------|------|------|------|
| bzip2 | 1.0.8 | libadvancefilemanager_bz2.a | libarchive 依赖 |
| xz | 5.6.4 | libadvancefilemanager_lzma.a | libarchive 依赖 |
| lz4 | 1.10.0 | libadvancefilemanager_lz4.a | libarchive 依赖 |
| zstd | 1.5.6 | libadvancefilemanager_zstd.a | libarchive 依赖 |
| mbedtls | 3.6.5 | libadvancefilemanager_mbedcrypto.a | libarchive 加密支持 |
| pcre2 | 10.44 | libadvancefilemanager_pcre2.a | libselinux 依赖 |
| libarchive | 3.7.7 | libadvancefilemanager_archive.a | 压缩包浏览/解压 |
| selinux | AOSP | libadvancefilemanager_selinux.a | SELinux 上下文显示 |

### 依赖关系

```
libarchive → bzip2, xz, lz4, zstd, mbedtls, libz(NDK 内置)
selinux    → pcre2
```

### 编译方式

这些静态库需要通过 NDK 交叉编译产生。编译脚本已移除，
如需重新编译，请参考各库官方文档使用 NDK toolchain 交叉编译为 ARM64。
