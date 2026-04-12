# Material Files

一个开源的 Material Design 3 文件管理器，适用于 Android 5.0+，内置媒体查看器和实用工具。

## 功能特性

### 文件管理

- **Material Design 3**：全面遵循最新的 Material Design 3 设计规范，在布局、颜色、字体和动效上精益求精。
- **面包屑导航栏**：点击导航栏所显示路径中的任一文件夹即可快速访问。
- **Root 支持**：使用 root 权限查看和管理文件。
- **压缩文件支持**：查看、提取和创建常见的压缩文件（ZIP、TAR、GZIP、BZIP2、XZ、7Z、RAR 等）。
- **主题**：可自定义的界面颜色，支持 Material You 动态取色，以及可选纯黑的夜间模式。
- **Linux 友好**：类似 [Nautilus](https://apps.gnome.org/Nautilus/)，支持符号链接、文件权限和 SELinux 上下文。

### 内置查看器

- **图片查看器**：支持缩放、拖拽和幻灯片浏览。
- **视频播放器**：基于 ExoPlayer 的内置视频播放器，支持滑动快进快退手势、长按倍速播放和进度条控制。
- **音频播放器**：内置音频播放器，支持专辑封面显示、播放控制和进度拖动。
- **PDF 阅读器**：无需离开应用即可逐页渲染和浏览 PDF 文档。
- **文本编辑器**：支持编码检测、编码切换和保存的文本文件编辑。
- **十六进制查看器**：以 Hex + ASCII 并排显示的方式检查二进制文件。

### 实用工具

- **回收站**：将文件移入回收站而非永久删除，支持恢复操作。
- **文件搜索**：在文件系统中按名称搜索文件。
- **重复文件查找器**：查找重复文件以释放存储空间。
- **空文件搜索**：扫描空文件和空文件夹以清理存储。
- **存储分析**：可视化磁盘使用情况，按类别分类展示。
- **另存为**：接收其他应用分享的内容并保存到任意位置。

### FFmpeg 媒体工具

- **格式转换**：在常见格式之间转换音频/视频（MP3、AAC、FLAC、MP4、WebM 等）。
- **音频提取**：从视频文件中提取音轨。
- **视频/图片增强**：使用 FFmpeg unsharp 滤镜锐化视频和图片。
- **媒体信息**：显示编解码器、比特率、分辨率和时长等详细信息。
- **文件比较**：逐字节比较两个文件的差异。

### 技术亮点

- **健壮性**：使用 Linux 系统调用实现，而不是另一个 [`ls` 解析器](https://news.ycombinator.com/item?id=7994720)。
- **实现良好**：基于 [Java NIO2 文件 API](https://docs.oracle.com/javase/8/docs/api/java/nio/file/package-summary.html) 和 [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) 构建，采用现代 `ViewModel` 架构。
- **正确的编码处理**：能正确处理含有无效 UTF-8 编码的文件名，因为应用中的路径没有简单地使用 Java `String` 存储。

## 构建

### 环境要求

所有编译工具都包含在 `tools/` 目录中：

- JDK 17（`tools/jdk-17.0.12`）
- Android SDK（`tools/android-sdk`，含 NDK 28.1.13356709）
- Gradle 8.13、AGP 8.11.1、Kotlin 2.1.21

### 编译三方依赖库

所有第三方依赖均从源码编译。编译 APK 前需先编译三方库：

```powershell
# PowerShell (Windows)
powershell -NoProfile -ExecutionPolicy Bypass -File materialfile_deps/build-all.ps1

# Shell (Linux/macOS)
bash materialfile_deps/build-all.sh
```

单个库编译：

```powershell
# 示例：编译 bzip2（Native 库）
powershell -NoProfile -ExecutionPolicy Bypass -File materialfile_deps/native/bzip2/build.ps1

# 示例：编译 advrecyclerview（Android 库）
powershell -NoProfile -ExecutionPolicy Bypass -File materialfile_deps/android/advrecyclerview/build.ps1
```

### 编译 APK

```bash
./gradlew assembleDebug    # 调试版
./gradlew assembleRelease  # 发布版（需配置签名）
```

## 第三方依赖

所有第三方库均从源码编译。源码和构建脚本在 `materialfile_deps/` 目录下（按 `native/` 和 `android/` 分类），编译产物输出到 `prebuild/`。编译产物使用 `materialfile` 前缀标识。

各库的来源、下载方式和构建说明详见 [materialfile_deps/README.md](materialfile_deps/README.md)。

### Native（C/C++）— 9 个库

| 库名 | 版本 | 说明 |
|------|------|------|
| [FFmpeg](https://github.com/FFmpeg/FFmpeg) | 7.1 | 音视频编解码与处理 |
| [bzip2](https://sourceware.org/bzip2/) | 1.0.8 | bzip2 压缩 |
| [xz](https://github.com/tukaani-project/xz) | 5.6.4 | XZ/LZMA 压缩 |
| [lz4](https://github.com/lz4/lz4) | 1.10.0 | 极快无损压缩 |
| [zstd](https://github.com/facebook/zstd) | 1.5.6 | Zstandard 压缩 |
| [mbedtls](https://github.com/Mbed-TLS/mbedtls) | 3.6.5 | TLS 密码学库 |
| [pcre2](https://github.com/PCRE2Project/pcre2) | 10.44 | Perl 兼容正则表达式 |
| [libarchive](https://github.com/libarchive/libarchive) | 3.7.7 | 多格式归档压缩库 |
| [selinux](https://android.googlesource.com/platform/external/selinux) | AOSP | SELinux 用户空间库 |

### Android（Java/Kotlin）— 25 个模块

| 库名 | 版本 | 说明 |
|------|------|------|
| [coil](https://github.com/coil-kt/coil) | 2.7.0 | Kotlin 协程图片加载（5 个模块） |
| [okhttp](https://github.com/square/okhttp) | 4.12.0 | HTTP 客户端 |
| [okio](https://github.com/square/okio) | 3.9.0 | I/O 库 |
| [dav4jvm](https://github.com/niclas-nicoring/dav4jvm) | ec6264d | WebDAV 客户端 |
| [libsu](https://github.com/topjohnwu/libsu) | 5.2.2 | Root 访问库（2 个模块） |
| [Shizuku](https://github.com/niclas-nicoring/Shizuku-API) | 13.1.5 | 无 Root 特权 API 框架（3 个模块） |
| [PhotoView](https://github.com/Baseflow/PhotoView) | 2.3.0 | 手势缩放 ImageView |
| [AndroidSVG](https://github.com/niclas-nicoring/androidsvg) | 1.4 | SVG 渲染 |
| [MaterialDrawer](https://github.com/niclas-nicoring/MaterialDrawer) | 1.0.3 | 抽屉导航组件 |
| [SpeedDial](https://github.com/leinardi/FloatingActionButtonSpeedDial) | 3.3.0 | FAB 快速拨号 |
| [Subsampling](https://github.com/niclas-nicoring/subsampling-scale-image-view) | 3.10.0 | 大图分块浏览 |
| [AdvRecyclerView](https://github.com/niclas-nicoring/android-advancedrecyclerview) | 1.0.0 | RecyclerView 高级特性（2 个模块） |
| [Insetter](https://github.com/niclas-nicoring/insetter) | 0.3.1 | WindowInsets 辅助（2 个模块） |
| [PreferenceX](https://github.com/niclas-nicoring/MaterialPreference) | 1.1.0 | 增强版 Preference |
| [SimpleMenu](https://github.com/niclas-nicoring/SimpleMenuPreference) | 1.0.3 | SimpleMenu Preference |
| [LicensesDialog](https://github.com/niclas-nicoring/LicensesDialog) | 2.1.0 | 开源许可证对话框 |

## 许可证

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

## 源码架构

### 整体架构

```
AppProvider (应用启动入口)
    ├── FileSystemProviders.install()     ── 安装 NIO.2 虚拟文件系统
    │       ├── LinuxFileSystemProvider         (本地 Linux + Root)
    │       ├── DocumentFileSystemProvider      (SAF 文档树)
    │       ├── ContentFileSystemProvider       (content:// URI)
    │       └── ArchiveFileSystemProvider       (压缩包虚拟文件系统)
    ├── Coil 图片加载初始化
    ├── Settings / Theme / NightMode
    └── AppUpgrader (版本升级迁移)

FileListActivity → FileListFragment (核心文件列表)
    ├── NavigationFragment              (侧边抽屉导航)
    ├── FileListViewModel               (路径/搜索/排序/选择状态)
    │       ├── FileListLiveData        (目录内容加载)
    │       ├── SearchFileListLiveData  (文件搜索)
    │       └── TrailLiveData           (导航历史栈)
    ├── FileJobService → FileJob        (后台文件操作)
    ├── FilePropertiesDialogFragment    (文件属性)
    └── Viewer Activities               (文本/图片/视频/PDF/Hex/电子书)
```

### JNI 本地层 (`app/src/main/jni/`)

| 文件 | 说明 |
|------|------|
| `syscall.c` | Linux 系统调用封装（stat/chmod/chown/opendir/readdir/inotify 等），约 1590 行 |
| `archive-jni.c` | libarchive JNI 封装，提供压缩包读写（zip/tar/7z/rar 等），约 3800 行 |
| `libselinux-jni.c` | SELinux 操作封装（getcon/setfilecon/lgetfilecon 等） |
| `hiddenapi.c` | 通过 `VMRuntime.setHiddenApiExemptions` 绕过 Android P+ 隐藏 API 限制 |

### Kotlin/Java 源码包 (`app/src/main/java/naipingzai/materialfile/`)

#### `about/` — 关于页面

| 文件 | 说明 |
|------|------|
| `AboutActivity.kt` | 关于页面宿主 Activity |
| `AboutFragment.kt` | 显示版本号、GitHub 链接、开源许可证等信息 |

#### `app/` — 应用全局初始化

| 文件 | 说明 | 关键 API |
|------|------|----------|
| `AppProvider.kt` | ContentProvider 启动入口，执行所有初始化器 | `application` 全局变量 |
| `AppActivity.kt` | 所有 Activity 基类，统一应用夜间模式/自定义主题 | `onCreate()` |
| `AppInitializers.kt` | 启动初始化列表（隐藏API/Coil/文件系统等） | `appInitializers` |
| `AppUpgrader.kt` / `AppUpgraders.kt` | 版本升级管理，跟踪 version code 执行数据迁移 | `upgradeApp()` |
| `BackgroundActivityStarter.kt` | 后台安全启动 Activity（前台直接启动/后台通过通知） | `startActivity()` |
| `SystemServices.kt` | 全局惰性系统服务 | `clipboardManager`, `notificationManager` 等 |
| `NotificationIds.kt` | 通知 ID 常量定义 | `FILE_JOB = 1` |

#### `coil/` — Coil 图片加载扩展

| 文件 | 说明 | 关键 API |
|------|------|----------|
| `CoilInitializer.kt` | 初始化 Coil（注册 Keyer/Fetcher/Decoder） | `initializeCoil()` |
| `AppIconFetcher.kt` | 应用图标加载基类 | `fetch()` |
| `AppIconApplicationInfoFetcherFactory.kt` | 根据 ApplicationInfo 加载应用图标 | `Factory` |
| `AppIconPackageNameFetcherFactory.kt` | 根据包名加载应用图标 | `Factory` |
| `PathAttributesFetcher.kt` | 根据 Path+Attributes 加载文件缩略图 | `fetch()` |
| `PdfPageFetcher.kt` | PDF 页面渲染为 Bitmap | `fetch()` |
| `VideoFrameFetcher.kt` | 视频帧提取 | `fetch()` |
| `CoilUtils.kt` | Bitmap/Size/Path 辅助扩展 | `isHardware`, `dataSource` |
| `CoilExtensions.kt` | `fadeIn()` 淡入动画扩展 | `fadeIn()` |
| `LoadRequestBuilderExtensions.kt` | 忽略错误回调扩展 | `ignoreError()` |

#### `colorpicker/` — 颜色选择器

| 文件 | 说明 |
|------|------|
| `BaseColorPreference.kt` | 颜色偏好项基类（`value`, `entryValues`, `defaultValue`） |
| `ColorPreferenceDialogFragment.kt` | 颜色选择对话框 |
| `ColorPaletteAdapter.kt` | 颜色调色板 GridView 适配器 |
| `ColorSwatchView.kt` | 单个颜色色块 View，支持选中状态 |

#### `compat/` — Android API 兼容层（37 个文件）

为不同 Android API Level 提供统一接口。代表性文件：

| 文件 | 说明 | 关键 API |
|------|------|----------|
| `ContextCompat.kt` | Context 扩展 | `checkSelfPermissionCompat()`, `mainExecutorCompat` |
| `DocumentsContractCompat.kt` | DocumentsContract 兼容 | `isTreeUri()` |
| `SELinuxCompat.kt` | SELinux 操作 | `isSELinuxEnabled()`, `getFileContext()` |
| `StorageManagerCompat.kt` | StorageManager 兼容 | `openProxyFileDescriptorCompat()` |
| `StorageVolumeCompat.kt` | StorageVolume 兼容 | `isPrimaryCompat`, `pathCompat` |
| `MediaMetadataRetrieverCompat.kt` | 媒体元数据兼容 | `getFrameAtTimeCompat()` |
| 其余文件 | Activity/Dialog/Drawable/Intent/Parcel/View 等兼容封装 | — |

#### `file/` — 文件模型与 MIME 类型

| 文件 | 说明 | 关键 API |
|------|------|----------|
| `FileItem.kt` | 文件列表项数据模型 | `path`, `attributes`, `mimeType`, `isHidden` |
| `MimeType.kt` | MIME 类型封装（inline value class） | `type`, `subtype`, `match()` |
| `MimeTypeIcon.kt` | MIME 类型到图标映射（22 种类型） | `MimeType.icon` |
| `FileSize.kt` | 文件大小格式化 | `formatHumanReadable()`, `Long.asFileSize()` |
| `FileProvider.kt` | ContentProvider，通过 content:// URI 暴露文件 | `getUriForPath()`, `openFile()` |
| `DocumentTreeUri.kt` | SAF 文档树 URI 封装 | `takePersistablePermission()` |
| `DocumentUri.kt` | SAF 文档 URI 封装 | `documentId`, `displayName` |
| `ExternalStorageUri.kt` | 外部存储 URI 封装 | `rootId`, `path` |
| `JavaFile.kt` | java.io.File 简单封装 | `isDirectory()`, `getFreeSpace()` |
| `MimeType*Extensions.kt` | MIME 类型扩展函数 | `isApk`, `isImage`, `isVideo`, `isPdf` |

#### `fileaction/` — 文件操作对话框

| 文件 | 说明 |
|------|------|
| `ArchivePasswordDialogActivity.kt` | 密码对话框宿主 Activity |
| `ArchivePasswordDialogFragment.kt` | 压缩包密码输入对话框，通过 `RemoteCallback` 回调 |

#### `filejob/` — 后台文件作业系统

| 文件 | 说明 | 关键 API |
|------|------|----------|
| `FileJob.kt` | 文件作业抽象基类 | `run()`, `runOn(service)` |
| `FileJobs.kt` | 所有具体作业实现（约 2370 行） | `CopyFileJob`, `MoveFileJob`, `DeleteFileJob`, `CreateArchiveFileJob`, `RenameFileJob` 等 |
| `FileJobService.kt` | 前台 Service，管理作业执行/取消/通知 | `copy()`, `move()`, `delete()`, `cancelJob()` |
| `FileJobConflictDialogFragment.kt` | 文件冲突解决对话框 | — |
| `FileJobConflictAction.kt` | 冲突动作枚举 | `MERGE_OR_REPLACE`, `RENAME`, `SKIP` |
| `FileJobErrorDialogFragment.kt` | 文件操作错误对话框 | — |
| `FileJobErrorAction.kt` | 错误动作枚举 | `POSITIVE`, `NEGATIVE`, `NEUTRAL`, `CANCELED` |
| `FileJobNotificationTemplate.kt` | 通知模板 | — |
| `FileJobReceiver.kt` | 通知操作的 BroadcastReceiver | — |

#### `filelist/` — 文件列表核心

| 文件 | 说明 | 关键 API |
|------|------|----------|
| `FileListActivity.kt` | 文件列表主 Activity | `createViewIntent()`, `OpenFileContract` |
| `FileListFragment.kt` | 核心列表 Fragment（约 1760 行） | `navigateTo()`, `navigateUp()` |
| `FileListViewModel.kt` | 列表 ViewModel | `navigateTo()`, `search()`, `currentPath` |
| `FileListAdapter.kt` | RecyclerView 适配器（LIST/GRID 双模式） | `viewType`, `sortOptions` |
| `FileListLiveData.kt` | 目录内容加载 + 文件变更观察 | `loadValue()` |
| `SearchFileListLiveData.kt` | 文件搜索结果 LiveData | `loadValue()` |
| `FileSortOptions.kt` | 排序选项（名称/类型/大小/时间 × 升降序） | `createComparator()` |
| `FileViewType.kt` | 视图类型枚举 | `LIST`, `GRID` |
| `FileItemSet.kt` | 多选文件集合（LinkedMapSet） | `fileItemSetOf()` |
| `BreadcrumbLayout.kt` | 面包屑导航栏自定义 View | `setData()`, `Listener` |
| `TrailLiveData.kt` | 导航路径历史栈 LiveData | `navigateTo()`, `navigateUp()` |
| `PathObserver.kt` | 文件系统变更观察（inotify 封装） | `close()` |
| 对话框类 | `CreateDirectoryDialogFragment`, `RenameFileDialogFragment`, `ConfirmDeleteFilesDialogFragment`, `CreateArchiveDialogFragment`, `BatchRenameDialogFragment`, `NavigateToPathDialogFragment` 等 | — |

#### `fileproperties/` — 文件属性

文件属性对话框，包含多个选项卡：

| 子包 | 说明 | 关键类 |
|------|------|--------|
| （根） | 属性主对话框和基础设施 | `FilePropertiesDialogFragment`, `FileLiveData`, `PathObserverLiveData<T>` |
| `basic/` | 基本属性（大小/类型/路径/时间） | `FilePropertiesBasicTabFragment` |
| `permission/` | 权限管理（owner/group/mode/SELinux） | `SetOwnerDialogFragment`, `SetModeDialogFragment` |
| `image/` | 图片 EXIF 信息 | `ImageInfoLiveData`, `FilePropertiesImageTabFragment` |
| `audio/` | 音频元数据 | `AudioInfoLiveData`, `FilePropertiesAudioTabFragment` |
| `video/` | 视频元数据 | `VideoInfoLiveData`, `FilePropertiesVideoTabFragment` |
| `apk/` | APK 信息（包名/版本/权限/签名） | `ApkInfoLiveData`, `FilePropertiesApkTabFragment` |
| `checksum/` | 文件校验和（MD5/SHA-1/SHA-256/CRC32） | `ChecksumInfoLiveData` |

#### `hiddenapi/` — 隐藏 API 绕过

| 文件 | 说明 |
|------|------|
| `HiddenApi.kt` | 加载 hiddenapi native 库，调用 `disableHiddenApiChecks()` |
| `RestrictedHiddenApi.kt` | `@RestrictedHiddenApi` 注解，标记使用了受限隐藏 API 的位置 |

#### `lib/` — 内嵌第三方库

| 子包 | 说明 | 关键类 |
|------|------|--------|
| `appiconloader/` | 应用图标加载器 | `AppIconLoader` — `loadIcon()` |
| `fastscroll/` | 快速滚动 RecyclerView | `FastScroller`, `FastScrollerBuilder` |
| `foregroundcompat/` | 前景兼容 View | `ForegroundLinearLayout` |
| `libarchive/` | libarchive JNI Java 封装 | `Archive`, `ArchiveEntry` |
| `libselinux/` | SELinux JNI Java 封装 | `SeLinux` |
| `systemuihelper/` | 全屏/沉浸模式辅助 | `SystemUiHelper` |

#### `navigation/` — 侧边栏导航

| 文件 | 说明 | 关键 API |
|------|------|----------|
| `NavigationFragment.kt` | 导航抽屉 Fragment | `Listener` 接口 |
| `NavigationItem.kt` | 导航项抽象基类 | `getIcon()`, `getTitle()`, `onClick()` |
| `NavigationItems.kt` | 构建所有导航项（存储/目录/书签/工具） | `navigationItems` |
| `NavigationListAdapter.kt` | 导航列表适配器 | — |
| `BookmarkDirectory.kt` | 书签目录数据类 | `id`, `name`, `path` |
| `BookmarkDirectories.kt` | 书签 CRUD 操作 | `add()`, `replace()`, `remove()` |
| `NavigationRoot.kt` | 导航根项接口 | `path`, `getName()` |
| `StandardDirectory.kt` | 标准目录（Download/Documents/Pictures 等） | `relativePath`, `isEnabled` |
| `NavigationItemListLiveData.kt` | 导航项变化观察 LiveData | — |

#### `provider/` — 文件系统 Provider 层（核心架构）

基于 Java NIO.2 `FileSystemProvider` 抽象，统一 Linux/SAF/Content/Archive/Root 文件系统访问。

| 子包 | 说明 | 关键类 |
|------|------|--------|
| （根） | Provider 安装入口 | `FileSystemProviders` — `install()`, `get(scheme)` |
| `common/` | 公共基础：Path 抽象、ByteString、文件属性、观察/搜索接口（~70 个文件） | `AbstractPath<T>`, `ByteString`, `PathObservable`, `Searchable`, `PosixFileAttributes` |
| `linux/` | 本地 Linux 文件系统 | `LinuxFileSystemProvider`, `LinuxPath` |
| `linux/syscall/` | JNI 系统调用 Kotlin 封装 | `Syscall` — `stat()`, `chmod()`, `chown()`, `opendir()`, `readdir()`, `inotify_*()` |
| `linux/media/` | 媒体库扫描 | `MediaScanner` |
| `document/` | SAF DocumentProvider 文件系统 | `DocumentFileSystemProvider`, `DocumentPath` |
| `document/resolver/` | SAF 底层查询 | `DocumentResolver` |
| `content/` | content:// URI 文件系统 | `ContentFileSystemProvider`, `ContentPath` |
| `content/resolver/` | ContentResolver 封装 | `Resolver` |
| `archive/` | 压缩包虚拟文件系统（只读） | `ArchiveFileSystemProvider`, `ArchivePath` |
| `archive/archiver/` | 压缩包读写器 | `ArchiveReader`, `ArchiveWriter` |
| `remote/` | Binder IPC 远程文件系统 | `RemoteFileSystemProvider`, `RemoteInterface<T>` |
| `root/` | Root 权限文件系统（自动降级） | `RootableFileSystemProvider`, `RootStrategy` |

#### `settings/` — 设置系统

| 文件 | 说明 | 关键 API |
|------|------|----------|
| `Settings.kt` | 所有设置项集中定义 | `STORAGES`, `FILE_LIST_SHOW_HIDDEN_FILES`, `NIGHT_MODE`, `ROOT_STRATEGY` 等 |
| `SettingLiveData.kt` | 响应式设置 LiveData 基类 | `putValue()`, `getValue()` |
| `SettingLiveDatas.kt` | 具体实现 | `BooleanSettingLiveData`, `EnumSettingLiveData` 等 |
| `PathSettings.kt` | 路径特定的视图/排序设置 | `getFileListViewType(path)` |
| `SettingsActivity.kt` | 设置主 Activity | `restart()` |
| `SettingsPreferenceFragment.kt` | PreferenceFragment 实现 | — |
| 偏好项类 | `BookmarkDirectoriesPreference`, `DefaultDirectoryPreference`, `CharsetPreference`, `RootStrategyPreference` 等 | — |

#### `storage/` — 存储管理

| 文件 | 说明 | 关键 API |
|------|------|----------|
| `Storage.kt` | 存储抽象基类 | `id`, `getName()`, `path`, `linuxPath` |
| `DeviceStorage.kt` | 设备存储（`FileSystemRoot`/`PrimaryStorageVolume`） | `path` |
| `DocumentTree.kt` | SAF 文档树存储 | `uri: DocumentTreeUri` |
| `ExternalStorageShortcut.kt` | 外部存储快捷方式 | — |
| `Storages.kt` | 存储 CRUD 操作 | `addOrReplace()`, `remove()` |
| `StorageVolumeListLiveData.kt` | 存储卷列表 LiveData | — |
| 对话框类 | `AddStorageDialogFragment`, `EditDeviceStorageDialogFragment` 等 | — |

#### `terminal/` — 终端集成

| 文件 | 说明 |
|------|------|
| `Terminal.kt` | 调用外部终端应用在指定路径打开 — `open(path, context)` |

#### `theme/` — 主题系统

| 文件 | 说明 | 关键 API |
|------|------|----------|
| `custom/CustomThemeHelper.kt` | 自定义主题颜色管理 | `initialize(app)`, `apply(activity)` |
| `night/NightMode.kt` | 夜间模式枚举 | `FOLLOW_SYSTEM`, `OFF`, `ON` |
| `night/NightModeHelper.kt` | 夜间模式切换，联动所有 Activity | `initialize(app)`, `apply(activity)` |

#### `tools/` — 工具模块

| 子包 | 说明 | 关键类 |
|------|------|--------|
| `duplicatefinder/` | 重复文件查找 | `DuplicateFinderActivity`, `DuplicateFinderFragment` |
| `emptysearch/` | 空文件/空文件夹搜索 | `EmptySearchActivity`, `EmptySearchFragment` |
| `filesearch/` | 按名称/类型搜索文件 | `FileSearchActivity`, `FileSearchFragment` |
| `trash/` | 回收站 | `TrashHelper` — `moveToTrash()`, `restoreFromTrash()`, `emptyTrash()` |

#### `ui/` — 自定义 UI 组件（51 个文件）

| 文件 | 说明 |
|------|------|
| `AnimatedListAdapter.kt` | 带 DiffUtil 动画的 RecyclerView 适配器基类 |
| `CheckableItemBackground.kt` | 可选中项背景（涟漪 + 高亮） |
| `OverlayToolbar.kt` | 覆盖式工具栏 |
| `ToolbarActionMode.kt` | 基于 Toolbar 的 ActionMode |
| `PersistentBarLayout.kt` | 持久底部栏布局 |
| `PersistentDrawerLayout.kt` | 持久抽屉布局 |
| `TabFragmentPagerAdapter.kt` | Tab+ViewPager Fragment 适配器 |
| `ThemedFastScroller.kt` | 主题化快速滚动器 |
| `ThemedSpeedDialView.kt` | 主题化 SpeedDial FAB |
| `PreferenceFragmentCompat.kt` | 增强的偏好 Fragment 基类 |
| 其余 | `AspectRatioFrameLayout`, `AutoGoneTextView`, `CoordinatorAppBarLayout`, `NavigationFrameLayout` 等自定义 View |

#### `util/` — 工具类（71 个文件）

| 文件 | 说明 | 关键 API |
|------|------|----------|
| `Stateful.kt` | 通用加载状态容器 | `Loading`, `Failure`, `Success` |
| `CloseableLiveData.kt` | 可关闭的 LiveData 基类 | `close()` |
| `ParcelableArgs.kt` | Fragment/Intent 类型安全参数传递 | `putArgs()`, `args<T>()` |
| `ForegroundNotificationManager.kt` | 前台 Service 通知管理 | — |
| `RemoteCallback.kt` | Binder 回调封装 | — |
| `SelectionLiveData.kt` | 多选状态管理 | — |
| `DebouncedRunnable.kt` | 防抖 Runnable | — |
| `ThrottledRunnable.kt` | 节流 Runnable | — |
| `MapSet.kt` | 键值映射 + Set 混合集合 | `LinkedMapSet<K, V>` |
| 扩展函数文件 | `ContextExtensions`, `ViewExtensions`, `FragmentExtensions`, `IntentExtensions` 等 | — |

#### `viewer/` — 内置文件查看器

| 子包 | 说明 | 关键类 |
|------|------|--------|
| `text/` | 文本编辑器/查看器（支持编码检测切换、大文件分行） | `TextEditorFragment`, `LargeTextViewerFragment` |
| `image/` | 图片查看器（缩放、多图滑动） | `ImageViewerFragment`, `ImageViewerAdapter` |
| `video/` | 视频播放器（ExoPlayer、手势快进、倍速） | `VideoViewerFragment` |
| `audio/` | 音频播放器（专辑封面、播放控制） | `AudioPlayerFragment` |
| `pdf/` | PDF 查看器（逐页渲染） | `PdfViewerFragment`, `PdfPageAdapter` |
| `hex/` | 十六进制查看器（Hex + ASCII 并排） | `HexViewerFragment`, `HexAdapter` |
| `ebook/` | 电子书查看器 | `EbookViewerFragment`, `EpubParser`, `MobiParser` |
| `saveas/` | 另存为功能 | `SaveAsActivity` |
