# Skill: file — 文件类型系统

## 概述
`naipingzai.materialfile.file` 包含 MIME 类型、文件大小、文件项、文件图标等核心文件类型定义。

## 可测试接口

| 文件 | 接口 | 测试类型 |
|------|------|----------|
| MimeType.kt | `type`, `subtype`, `suffix`, `parameters`, `match()`, `of()`, `asMimeType()`, `asMimeTypeOrNull()` | Robolectric |
| MimeTypeTypeExtensions.kt | `isApk`, `isSupportedArchive`, `isImage`, `isAudio`, `isVideo`, `isMedia`, `isPdf`, `isMobi`, `isEpub`, `isEbook`, `isCsv` | Robolectric |
| MimeTypeIcon.kt | `MimeType.icon`, `MimeType.iconRes` | Robolectric |
| MimeTypeConversionExtensions.kt | `guessFromPath()`, `guessFromExtension()`, `extension`, `intentType` | Robolectric |
| FileSize.kt | `isHumanReadableInBytes`, `formatInBytes()`, `formatHumanReadable()`, `asFileSize()` | Robolectric |
| DurationExtensions.kt | `Duration.format()` | Unit |
| JavaFile.kt | `isDirectory()`, `getFreeSpace()`, `getTotalSpace()` | Instrumented |
| FileItem.kt | `attributes`, `isSymbolicLinkBroken`, `loadFileItem()` | Instrumented |
| DocumentUri.kt | `treeDocumentId`, `documentId`, `asDocumentUri()`, `asDocumentUriOrNull()` | Robolectric |
| ExternalStorageUri.kt | `rootId`, `path`, `asExternalStorageUri()` | Robolectric |

## 测试策略
- MimeType 系列: Robolectric (依赖 Android Parcelable + DocumentsContract)
- FileSize: Robolectric (依赖 Context 格式化)
- JavaFile: Instrumented (依赖真实文件系统)
