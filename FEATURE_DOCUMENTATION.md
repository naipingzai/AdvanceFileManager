# AdvanceFileManager 功能文档

## 一、核心功能

### 1.1 文件浏览与管理
| 功能 | 说明 | 状态 |
|------|------|------|
| 文件列表浏览 | 列表/网格视图，支持排序、过滤 | ✅ 正常 |
| 面包屑导航 | 路径导航栏，快速跳转 | ✅ 正常 |
| 文件操作 | 复制、剪切、粘贴、删除、重命名 | ✅ 正常 |
| 批量重命名 | 支持正则表达式批量重命名 | ✅ 正常 |
| 创建文件/文件夹 | 新建文件或目录 | ✅ 正常 |
| 文件搜索 | 按名称、类型、大小搜索 | ✅ 正常 |
| 隐藏文件显示 | 显示/隐藏系统文件 | ✅ 正常 |
| 书签管理 | 添加常用目录书签 | ✅ 正常 |

### 1.2 文件查看器
| 格式 | 查看器 | 状态 |
|------|--------|------|
| 图片 | ImageViewer（支持手势缩放） | ✅ 正常 |
| 视频 | VideoViewer（ExoPlayer） | ✅ 正常 |
| 音频 | AudioPlayer（后台播放） | ✅ 正常 |
| PDF | PdfViewer（页面渲染） | ✅ 正常 |
| 文本 | TextEditor（语法高亮） | ✅ 正常 |
| 大文本 | LargeTextViewer（懒加载） | ✅ 正常 |
| CSV | CsvViewer（WebView） | ✅ 正常 |
| 电子书 | EbookViewer（EPUB/MOBI） | ✅ 正常 |

### 1.3 文件属性
| 属性类型 | 支持内容 | 状态 |
|----------|----------|------|
| 基本属性 | 名称、大小、日期、路径 | ✅ 正常 |
| 权限管理 | 所有者、组、模式、SELinux | ✅ 正常 |
| 校验和 | MD5、SHA-1、SHA-256、SHA-512、CRC32 | ✅ 正常 |
| APK 信息 | 包名、版本、权限列表 | ✅ 正常 |
| 图片 EXIF | 尺寸、GPS、相机信息 | ✅ 正常 |
| 音频元数据 | 标题、艺术家、专辑、时长 | ✅ 正常 |
| 视频元数据 | 编码、分辨率、帧率 | ✅ 正常 |

---

## 二、文件工具

### 2.1 文件工具集
| 工具 | 功能 | 状态 |
|------|------|------|
| 文件搜索 | 按名称、扩展名、大小搜索文件 | ✅ 正常 |
| 重复文件查找 | 基于哈希算法查找重复文件 | ✅ 正常 |
| 空文件夹搜索 | 查找空文件和空目录 | ✅ 正常 |
| 最近文件 | 查看最近修改的文件 | ✅ 正常 |
| 十六进制查看 | Hex + ASCII 并排显示 | ✅ 正常 |
| 文件加密/解密 | AES-256-GCM 加密 | ⚠️ 需修复 |
| 文件对比 | 二进制/文本差异对比 | ✅ 正常 |
| 回收站 | 删除文件临时存储 | ✅ 正常 |

### 2.2 FFmpeg 多媒体工具
| 工具 | 功能 | 状态 |
|------|------|------|
| 格式转换 | 音视频格式互转（硬件加速） | ✅ 正常 |
| 音频提取 | 从视频提取音轨 | ✅ 正常 |
| 视频裁剪 | 按时间范围裁剪（无重编码） | ✅ 正常 |
| 视频压缩 | 调整码率/分辨率/帧率 | ✅ 正常 |
| 视频截图 | 提取指定时间点帧 | ✅ 正常 |
| GIF 制作 | 视频片段转 GIF | ✅ 正常 |
| 视频合并 | 多视频合并为一 | ⚠️ 需修复 |
| 图片压缩 | 降低图片文件大小 | ❌ 未实现 |
| 视频增强 | 画质增强 | ❌ 未实现 |
| 图片增强 | 图片增强处理 | ❌ 未实现 |

---

## 三、归档支持

### 3.1 支持的归档格式
| 格式 | 读取 | 写入 | 加密 |
|------|------|------|------|
| ZIP | ✅ | ✅ | ✅ |
| 7Z | ✅ | ✅ | ❌ |
| TAR | ✅ | ✅ | ❌ |
| TAR.GZ | ✅ | ✅ | ❌ |
| TAR.XZ | ✅ | ✅ | ❌ |
| RAR | ✅ | ❌ | ❌ |
| RAR5 | ✅ | ❌ | ❌ |
| CPIO | ✅ | ❌ | ❌ |
| ISO | ✅ | ❌ | ❌ |

### 3.2 归档操作
| 操作 | 说明 | 状态 |
|------|------|------|
| 浏览归档 | 虚拟文件系统浏览归档内容 | ✅ 正常 |
| 解压文件 | 从归档中提取文件 | ✅ 正常 |
| 创建归档 | 压缩文件/文件夹 | ✅ 正常 |
| 密码保护 | ZIP 格式加密 | ✅ 正常 |
| 流式读取 | 无需解压即可读取内容 | ✅ 正常 |

---

## 四、存储支持

### 4.1 文件系统类型
| 类型 | 说明 | 状态 |
|------|------|------|
| Linux 本地 | 本地文件系统（需权限） | ✅ 正常 |
| Document Provider | Android SAF 框架 | ✅ 正常 |
| Content Provider | Content URI 访问 | ✅ 正常 |
| Root 文件系统 | Root 权限访问 | ✅ 正常 |
| 归档文件系统 | ZIP/7Z/TAR 虚拟浏览 | ✅ 正常 |

### 4.2 存储管理
| 功能 | 说明 | 状态 |
|------|------|------|
| 设备存储 | 内部/外部存储管理 | ✅ 正常 |
| Document Tree | 外部存储授权 | ✅ 正常 |
| 存储快捷方式 | 快速访问常用位置 | ✅ 正常 |

---

## 五、设置与自定义

### 5.1 通用设置
| 设置项 | 说明 | 状态 |
|--------|------|------|
| 语言 | 多语言支持 | ✅ 正常 |
| 文件名显示 | 省略方式（开头/中间/结尾/滚动） | ✅ 正常 |
| 默认目录 | 启动时打开的目录 | ✅ 正常 |
| Root 模式 | 普通/自动/仅 Root | ✅ 正常 |
| 归档编码 | 文件名字符编码 | ✅ 正常 |
| APK 默认操作 | 安装/查看/询问 | ✅ 正常 |

### 5.2 显示设置
| 设置项 | 说明 | 状态 |
|--------|------|------|
| 字体大小 | 全局字体缩放（50%~200%） | ✅ 正常 |
| 界面间距 | 元素间距缩放 | ✅ 正常 |
| 列表项高度 | 列表行高调整 | ✅ 正常 |
| 图标大小 | 图标尺寸调整 | ✅ 正常 |
| 页面边距 | 屏幕边缘留白 | ✅ 正常 |
| 对话框内边距 | 对话框内容边距 | ✅ 正常 |
| 按钮间距 | 按钮之间间距 | ✅ 正常 |

### 5.3 功能开关
| 功能模块 | 说明 | 状态 |
|----------|------|------|
| 文件工具 | 搜索、重复查找等 | ✅ 正常 |
| 媒体工具 | FFmpeg 相关工具 | ✅ 正常 |
| 电子书查看 | EPUB/MOBI 阅读 | ✅ 正常 |

---

## 六、JNI 功能支持

### 6.1 FFmpeg JNI
**库文件：** `libffmpeg-jni.so`

| 方法 | 功能 | 说明 |
|------|------|------|
| `getVersion()` | 获取 FFmpeg 版本 | 显示版本信息 |
| `convert()` | 格式转换 | 智能 remux/transcode |
| `cancel()` | 取消操作 | 异步取消支持 |
| `getMediaInfo()` | 媒体信息 | 时长、编码、分辨率等 |
| `extractAudio()` | 提取音频 | remux 优先 |
| `trim()` | 裁剪 | 无重编码 |
| `videoCompress()` | 压缩 | 调整码率/分辨率 |
| `normalizeVideo()` | 标准化 | H.264+AAC MP4 |
| `videoSnapshot()` | 截图 | 单帧提取 |
| `gifMake()` | GIF 制作 | 视频转 GIF |
| `mergeFiles()` | 合并视频 | 多文件合并 |

**硬件加速支持：**
- H.264 (MediaCodec)
- HEVC (MediaCodec)
- VP8/VP9 (MediaCodec)
- AV1 (MediaCodec)

### 6.2 libarchive JNI
**库文件：** `libarchive-jni.so`

| 功能 | 说明 |
|------|------|
| 归档读取 | ZIP/7Z/TAR/RAR/CPIO/ISO 等 |
| 归档写入 | ZIP/7Z/TAR 格式 |
| 压缩过滤器 | GZIP/BZIP2/LZMA/XZ/LZ4/ZSTD 等 |
| 加密支持 | ZIP 加密 (ZipCrypt) |
| 密码管理 | 读取/写入密码保护 |

### 6.3 Syscall JNI
**库文件：** `libsyscall.so`

| 功能分类 | 支持的系统调用 |
|----------|----------------|
| 文件操作 | open, read, write, close, stat, lstat, rename, remove, mkdir, symlink, link |
| 权限管理 | chmod, chown, lchown |
| 扩展属性 | lgetxattr, llistxattr, lsetxattr |
| SELinux | getfilecon, setfilecon, lgetfilecon, lsetfilecon |
| 目录操作 | opendir, readdir, closedir |
| 用户/组 | getpwnam, getpwuid, getgrnam, getgrgid |
| 挂载点 | setmntent, getmntent, endmntent |
| inotify | inotify_init1, inotify_add_watch, inotify_rm_watch |
| 其他 | sendfile, statvfs, mount, ioctl, fcntl, realpath, readlink |

---

## 七、已知问题与限制

### 7.1 功能限制
| 问题 | 说明 | 影响 |
|------|------|------|
| 归档写入格式有限 | 仅支持 ZIP/7Z/TAR 写入 | 低 |
| RAR 只读 | RAR 格式仅支持读取 | 低 |
| 密码仅 ZIP | 归档加密仅支持 ZIP 格式 | 中 |
| 电子书格式有限 | 仅支持 EPUB/MOBI | 低 |

### 7.2 需修复问题
| 问题 | 严重程度 | 说明 |
|------|----------|------|
| FFmpeg 未实现方法 | 🔴 高 | videoEnhance/imageCompress/imageEnhance 调用会崩溃 |
| 文件加密 salt/iv 读取 | 🟡 中 | 解密时未检查读取完整性 |
| 归档密码格式限制 | 🟡 中 | 非 ZIP 格式设置密码会崩溃 |

---

## 八、技术架构

### 8.1 文件系统抽象层
```
FileSystemProvider (接口)
├── LinuxFileSystemProvider (本地文件)
├── DocumentFileSystemProvider (SAF)
├── ContentFileSystemProvider (Content URI)
├── RootFileSystemProvider (Root 权限)
├── ArchiveFileSystemProvider (归档文件)
└── RemoteFileSystemProvider (远程文件)
```

### 8.2 核心组件
- **FileListFragment** — 文件列表 UI
- **FileListViewModel** — 文件列表逻辑
- **FileJobService** — 后台文件操作服务
- **FileSystemProviders** — 文件系统提供者注册中心

### 8.3 依赖库
- **libarchive** — 归档格式支持
- **FFmpeg** — 多媒体处理
- **ExoPlayer** — 音视频播放
- **Coil** — 图片加载
- **Material Components** — UI 组件
