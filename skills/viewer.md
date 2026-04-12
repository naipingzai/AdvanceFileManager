# Skill: viewer — 内置文件查看器

## 概述
9 个查看器子包，每个包含 Activity + Fragment。

## 查看器列表

| 查看器 | Activity | Fragment | 关键功能 |
|--------|----------|----------|----------|
| 图片 | ImageViewerActivity | ImageViewerFragment | Coil + SubsamplingScaleImageView |
| 视频 | VideoViewerActivity | VideoViewerFragment | ExoPlayer + ±15s 手势 |
| 音频 | AudioPlayerActivity | AudioPlayerFragment | ExoPlayer + 专辑封面 |
| PDF | PdfViewerActivity | PdfViewerFragment | PdfRenderer |
| CSV | CsvViewerActivity | CsvViewerFragment | WebView HTML 表格 |
| 电子书 | EbookViewerActivity | EbookViewerFragment | WebView + EpubParser/MobiParser |
| 文本 | TextEditorActivity | TextEditorFragment | 编码检测 + 编辑 |
| 十六进制 | HexViewerActivity | HexViewerFragment | RecyclerView 分页 |
| 保存为 | SaveAsActivity | - | 文件另存 |

## CSV 查看器特有可测试逻辑

| 函数 | 说明 |
|------|------|
| `parseCsvLine(line)` | CSV 行解析(引号/逗号/转义) |
| `escapeHtml(text)` | HTML 特殊字符转义 |
| `buildHtmlTable(reader, topPadDp)` | HTML 表格生成 |

## 电子书解析器

| 类 | 方法 |
|----|------|
| EpubParser | `parse(input, imgDir): EpubBook` |
| MobiParser | `parse(input, imgDir): MobiBook` |

## 测试策略
- Activity 启动: Instrumented Test (不提供 path 时应 finish)
- CSV 解析逻辑: 纯 Unit Test (已有 CsvParserTest)
- 电子书解析: Instrumented Test (需要文件 IO)
