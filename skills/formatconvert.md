# Skill: formatconvert — FFmpeg JNI 音视频处理

## 概述
`naipingzai.materialfile.tools.formatconvert` 封装了 FFmpeg 7.1 的 JNI 接口，提供音视频转换、压缩、截帧、GIF 生成等功能。

## JNI 接口 (FFmpegJni.kt)

| 方法 | 参数 | 返回值 | 测试要点 |
|------|------|--------|----------|
| `getVersion()` | 无 | String | 返回非空 FFmpeg 版本号 |
| `getLastError()` | 无 | String | 返回错误描述，初始非 null |
| `cancel()` | 无 | Unit | 多次调用不崩溃 |
| `getMediaInfo(path, info)` | 文件路径 + MediaInfo | Unit | 填充 MediaInfo 字段 |
| `convert(input, output, callback)` | 路径 + 回调 | Int | 0=成功, 非0=失败 |
| `extractAudio(input, output, callback)` | 路径 + 回调 | Int | 音频提取 |
| `trim(input, output, startMs, endMs, callback)` | 路径 + 时间范围 + 回调 | Int | 裁剪 |
| `videoCompress(input, output, bitrate, w, h, fps, callback)` | 路径 + 压缩参数 + 回调 | Int | 压缩 |
| `videoSnapshot(input, output, timeMs)` | 路径 + 时间点 | Int | 截帧 |
| `gifMake(input, output, startMs, endMs, w, fps, callback)` | 路径 + GIF 参数 + 回调 | Int | GIF 生成 |
| `mergeFiles(inputs, output, callback)` | 路径数组 + 回调 | Int | 合并 |
| `normalizeVideo(input, output, w, h, bitrate, callback)` | 路径 + 目标参数 + 回调 | Int | 格式统一 |
| `mergeFilesTranscode(inputs, output, w, h, bitrate, callback)` | 路径数组 + 目标参数 + 回调 | Int | 转码合并 |
| `imageCompress(input, output, quality, maxW, maxH)` | 路径 + 质量 + 尺寸 | Int | 图片压缩 |
| `imageEnhance(input, output, strength)` | 路径 + 强度 | Int | 图片增强 |
| `videoEnhance(input, output, strength, bitrate, callback)` | 路径 + 强度 + 码率 + 回调 | Int | 视频增强 |

## 辅助类 (MediaInfo.kt)

| 属性/方法 | 说明 |
|-----------|------|
| `durationMs` | 时长(毫秒) |
| `formatName` | 容器格式 |
| `audioCodec`, `sampleRate`, `channels`, `audioBitrate` | 音频信息 |
| `videoCodec`, `width`, `height`, `videoBitrate` | 视频信息 |
| `hasAudio` | 是否有音频流 |
| `hasVideo` | 是否有视频流 |
| `formatDuration()` | 格式化时长 "H:MM:SS" |
| `summary()` | 完整摘要字符串 |

## 测试策略
- 全部为 Instrumented Test (需要 JNI 加载原生库)
- 创建最小有效 WAV 文件进行正向测试
- 无效路径测试返回非零错误码
- MediaInfo 纯 Kotlin 方法可 Unit Test
