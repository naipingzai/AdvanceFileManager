/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.ffmpegtools

import com.advancefilemanager.plugin.protocol.PluginFeature
import com.advancefilemanager.plugin.protocol.PluginFeatureProvider

object FFmpegFeatureProvider : PluginFeatureProvider {
    override val pluginId: String = "ffmpeg-tools"

    override fun getFeatures(): List<PluginFeature> = listOf(
        PluginFeature(
            id = "ffmpeg-tools.format_convert",
            title = "格式转换",
            description = "音视频格式互转",
            mimeTypes = listOf("video/*", "audio/*"),
            pluginId = pluginId,
            actionType = "format_convert"
        ),
        PluginFeature(
            id = "ffmpeg-tools.image_compress",
            title = "图片压缩",
            description = "降低图片文件大小",
            mimeTypes = listOf("image/*"),
            pluginId = pluginId,
            actionType = "image_compress"
        ),
        PluginFeature(
            id = "ffmpeg-tools.video_compress",
            title = "视频压缩",
            description = "降低比特率重编码",
            mimeTypes = listOf("video/*"),
            pluginId = pluginId,
            actionType = "video_compress"
        ),
        PluginFeature(
            id = "ffmpeg-tools.extract_audio",
            title = "音频提取",
            description = "从视频中提取音轨",
            mimeTypes = listOf("video/*"),
            pluginId = pluginId,
            actionType = "extract_audio"
        ),
        PluginFeature(
            id = "ffmpeg-tools.media_trim",
            title = "媒体剪切",
            description = "按时间范围裁剪",
            mimeTypes = listOf("video/*", "audio/*"),
            pluginId = pluginId,
            actionType = "media_trim"
        ),
        PluginFeature(
            id = "ffmpeg-tools.video_snapshot",
            title = "视频截帧",
            description = "提取指定时间点帧",
            mimeTypes = listOf("video/*"),
            pluginId = pluginId,
            actionType = "video_snapshot"
        ),
        PluginFeature(
            id = "ffmpeg-tools.gif_maker",
            title = "GIF 制作",
            description = "视频片段转 GIF",
            mimeTypes = listOf("video/*"),
            pluginId = pluginId,
            actionType = "gif_maker"
        ),
        PluginFeature(
            id = "ffmpeg-tools.video_merge",
            title = "视频合并",
            description = "多视频合并为一",
            mimeTypes = listOf("video/*"),
            pluginId = pluginId,
            actionType = "video_merge"
        )
    )
}
