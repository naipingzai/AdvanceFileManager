/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature

import com.advancefilemanager.R
import com.advancefilemanager.app.application
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
                title = application.getString(R.string.feature_file_tools_title),
                description = application.getString(R.string.feature_file_tools_desc),
                mimeTypes = listOf("*/*"),
                category = FeatureCategory.TOOL,
                subFeatures = listOf(
                    FeatureSubItem(
                        id = "file-tools.file_search",
                        title = application.getString(R.string.file_tool_file_search),
                        description = application.getString(R.string.file_tool_file_search_desc),
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "file_search"
                    ),
                    FeatureSubItem(
                        id = "file-tools.duplicate_finder",
                        title = application.getString(R.string.file_tool_duplicate_finder),
                        description = application.getString(R.string.file_tool_duplicate_finder_desc),
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "duplicate_finder"
                    ),
                    FeatureSubItem(
                        id = "file-tools.empty_search",
                        title = application.getString(R.string.file_tool_empty_search),
                        description = application.getString(R.string.file_tool_empty_search_desc),
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "empty_search"
                    ),
                    FeatureSubItem(
                        id = "file-tools.recent_files",
                        title = application.getString(R.string.file_tool_recent_files),
                        description = application.getString(R.string.file_tool_recent_files_desc),
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "recent_files"
                    ),
                    FeatureSubItem(
                        id = "file-tools.hex_viewer",
                        title = application.getString(R.string.file_tool_hex_viewer),
                        description = application.getString(R.string.file_tool_hex_viewer_desc),
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "hex_viewer"
                    ),
                    FeatureSubItem(
                        id = "file-tools.encryption",
                        title = application.getString(R.string.file_tool_encryption),
                        description = application.getString(R.string.file_tool_encryption_desc),
                        mimeTypes = listOf("*/*"),
                        featureId = "file-tools",
                        actionType = "encryption"
                    ),
                    FeatureSubItem(
                        id = "file-tools.file_compare",
                        title = application.getString(R.string.file_tool_file_compare),
                        description = application.getString(R.string.file_tool_file_compare_desc),
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
                title = application.getString(R.string.feature_media_tools_title),
                description = application.getString(R.string.feature_media_tools_desc),
                mimeTypes = listOf("video/*", "audio/*", "image/*"),
                category = FeatureCategory.TOOL,
                subFeatures = listOf(
                    FeatureSubItem(
                        id = "ffmpeg-tools.format_convert",
                        title = application.getString(R.string.media_tool_format_convert),
                        description = application.getString(R.string.media_tool_format_convert_desc),
                        mimeTypes = listOf("video/*", "audio/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "format_convert"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.image_compress",
                        title = application.getString(R.string.media_tool_image_compress),
                        description = application.getString(R.string.media_tool_image_compress_desc),
                        mimeTypes = listOf("image/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "image_compress"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.video_compress",
                        title = application.getString(R.string.media_tool_video_compress),
                        description = application.getString(R.string.media_tool_video_compress_desc),
                        mimeTypes = listOf("video/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "video_compress"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.extract_audio",
                        title = application.getString(R.string.media_tool_extract_audio),
                        description = application.getString(R.string.media_tool_extract_audio_desc),
                        mimeTypes = listOf("video/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "extract_audio"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.media_trim",
                        title = application.getString(R.string.media_tool_media_trim),
                        description = application.getString(R.string.media_tool_media_trim_desc),
                        mimeTypes = listOf("video/*", "audio/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "media_trim"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.video_snapshot",
                        title = application.getString(R.string.media_tool_video_snapshot),
                        description = application.getString(R.string.media_tool_video_snapshot_desc),
                        mimeTypes = listOf("video/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "video_snapshot"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.gif_maker",
                        title = application.getString(R.string.media_tool_gif_maker),
                        description = application.getString(R.string.media_tool_gif_maker_desc),
                        mimeTypes = listOf("video/*"),
                        featureId = "ffmpeg-tools",
                        actionType = "gif_maker"
                    ),
                    FeatureSubItem(
                        id = "ffmpeg-tools.video_merge",
                        title = application.getString(R.string.media_tool_video_merge),
                        description = application.getString(R.string.media_tool_video_merge_desc),
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
                title = application.getString(R.string.feature_ebook_viewer_title),
                description = application.getString(R.string.feature_ebook_viewer_desc),
                mimeTypes = listOf(
                    "application/epub+zip",
                    "application/x-mobipocket-ebook"
                ),
                category = FeatureCategory.VIEWER
            )
        )
    }
}