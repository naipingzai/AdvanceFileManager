/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature

import com.advancefilemanager.R
import com.advancefilemanager.feature.protocol.FeatureCategory
import com.advancefilemanager.feature.protocol.FeatureInfo
import com.advancefilemanager.feature.protocol.FeatureSubItem

/**
 * Registers all built-in features with FeatureManager.
 * Called once during app initialization.
 */
object FeatureInitializer {

    fun initialize() {
        registerFileTools()
        registerFFmpegTools()
        registerEbookViewer()
    }

    private fun registerFileTools() {
        FeatureManager.registerFeature(
            FeatureInfo(
                id = "file-tools",
                title = "文件工具",
                description = "文件搜索、重复查找、空文件夹搜索等",
                mimeTypes = listOf("*/*"),
                category = FeatureCategory.TOOL,
                subFeatures = listOf(
                    FeatureSubItem(
                        id = "file-tools.file_search",
                        title = "文件搜索",
                        description = "按名称搜索文件",
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "file_search"
                    ),
                    FeatureSubItem(
                        id = "file-tools.duplicate_finder",
                        title = "重复文件查找",
                        description = "查找重复文件",
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "duplicate_finder"
                    ),
                    FeatureSubItem(
                        id = "file-tools.empty_search",
                        title = "空文件夹搜索",
                        description = "查找空文件和文件夹",
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "empty_search"
                    ),
                    FeatureSubItem(
                        id = "file-tools.recent_files",
                        title = "最近文件",
                        description = "查看最近修改的文件",
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "recent_files"
                    ),
                    FeatureSubItem(
                        id = "file-tools.hex_viewer",
                        title = "十六进制查看",
                        description = "以Hex+ASCII并排查看文件",
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "hex_viewer"
                    ),
                    FeatureSubItem(
                        id = "file-tools.encryption",
                        title = "文件加密",
                        description = "加密或解密文件",
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "encryption"
                    ),
                    FeatureSubItem(
                        id = "file-tools.file_compare",
                        title = "文件对比",
                        description = "对比两个文件的内容",
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "file_compare"
                    )
                )
            )
        )
    }

    private fun registerFFmpegTools() {
        FeatureManager.registerFeature(
            FeatureInfo(
                id = "ffmpeg-tools",
                title = "媒体工具",
                description = "格式转换、视频压缩、音频提取等",
                mimeTypes = listOf("video/*", "audio/*", "image/*"),
                category = FeatureCategory.TOOL,
                subFeatures = listOf(
                    FeatureSubItem(
                        id = "ffmpeg-tools.format_convert",
                        title = "格式转换",
                        description = "音视频格式互转",
                        mimeTypes = listOf("video/*", "audio/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "format_convert"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.image_compress",
                        title = "图片压缩",
                        description = "降低图片文件大小",
                        mimeTypes = listOf("image/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "image_compress"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.video_compress",
                        title = "视频压缩",
                        description = "降低比特率重编码",
                        mimeTypes = listOf("video/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "video_compress"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.extract_audio",
                        title = "音频提取",
                        description = "从视频中提取音轨",
                        mimeTypes = listOf("video/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "extract_audio"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.media_trim",
                        title = "媒体剪切",
                        description = "按时间范围裁剪",
                        mimeTypes = listOf("video/*", "audio/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "media_trim"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.video_snapshot",
                        title = "视频截帧",
                        description = "提取指定时间点帧",
                        mimeTypes = listOf("video/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "video_snapshot"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.gif_maker",
                        title = "GIF 制作",
                        description = "视频片段转 GIF",
                        mimeTypes = listOf("video/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "gif_maker"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.video_merge",
                        title = "视频合并",
                        description = "多视频合并为一",
                        mimeTypes = listOf("video/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "video_merge"
                    )
                )
            )
        )
    }

    private fun registerEbookViewer() {
        FeatureManager.registerFeature(
            FeatureInfo(
                id = "ebook-viewer",
                title = "电子书查看",
                description = "EPUB/MOBI 电子书阅读",
                mimeTypes = listOf(
                    "application/epub+zip",
                    "application/x-mobipocket-ebook"
                ),
                category = FeatureCategory.VIEWER
            )
        )
    }
}