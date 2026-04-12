# Skill: mediatools — 媒体工具 UI

## 概述
`naipingzai.materialfile.tools.mediatools` 提供 FFmpeg 操作的 UI 封装，包含参数对话框和进度显示。

## 接口

### FFmpegOperationHelper.kt
| 方法 | 说明 |
|------|------|
| `extractAudio(fragment, filePath)` | 音频提取 |
| `trimMedia(fragment, filePath)` | 媒体裁剪 |
| `compressVideo(fragment, filePath)` | 视频压缩(弹出参数对话框) |
| `snapshotVideo(fragment, filePath)` | 视频截帧 |
| `makeGif(fragment, filePath)` | GIF 制作 |
| `compressImage(fragment, filePath)` | 图片压缩 |
| `enhanceImage(fragment, filePath)` | 图片增强 |
| `enhanceVideo(fragment, filePath)` | 视频增强(弹出强度选择) |

### MediaToolsFragment.kt
| 功能 | 说明 |
|------|------|
| 视频合并 | 多文件合并 |
| 各 FFmpeg 功能入口 | 卡片式导航 |

## 测试策略
- Activity/Fragment 启动测试: Instrumented Test
- 对话框 UI 逻辑: Espresso UI Test (可选)
