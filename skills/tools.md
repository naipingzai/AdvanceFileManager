# Skill: tools — 工具模块

## 概述
`naipingzai.materialfile.tools` 包含 11 个子模块。

## 子模块

| 模块 | 主要类 | 功能 |
|------|--------|------|
| appmanager | AppManagerFragment, AppItemAdapter | 应用管理 |
| duplicatefinder | DuplicateFinderFragment, DuplicateAdapter | 重复文件查找 |
| emptysearch | EmptySearchFragment | 空文件夹查找 |
| encryption | EncryptionFragment, FileEncryptionHelper | AES 文件加密/解密 |
| filecompare | FileCompareFragment | 文件比较 |
| filesearch | FileSearchFragment, FileSearchAdapter | 文件搜索 |
| formatconvert | FFmpegJni, MediaInfo, ProgressCallback | 格式转换(见 formatconvert.md) |
| imagecompress | ImageCompressFragment | 图片压缩(批量) |
| mediatools | MediaToolsFragment, FFmpegOperationHelper | 媒体工具(见 mediatools.md) |
| recentfiles | RecentFilesFragment | 最近文件 |
| storageanalysis | StorageAnalysisFragment | 存储空间分析 |

## 通用组件

| 类 | 方法 |
|----|------|
| OperationLogBottomSheet | `appendLog()`, `setStep()`, `setProgress()`, `finish()` |
| OutputPaths | `converted(subDir)`, `resolve(relativePath)` |
| FileTypeUtils | `getUniqueFile()`, `isImageFile()`, `matchesFileType()` |

## 测试策略
- FileTypeUtils/OutputPaths: Unit Test
- EncryptionHelper: Instrumented Test (需要文件 IO)
- Fragment 启动: Instrumented Test
