/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.ffmpegtools

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class MediaToolFeature(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val actionType: String
) {
    FORMAT_CONVERT(
        R.drawable.ic_convert,
        R.string.media_tool_format_convert,
        R.string.media_tool_format_convert_desc,
        "format_convert"
    ),
    IMAGE_COMPRESS(
        R.drawable.ic_image,
        R.string.media_tool_image_compress,
        R.string.media_tool_image_compress_desc,
        "image_compress"
    ),
    VIDEO_COMPRESS(
        R.drawable.ic_video,
        R.string.media_tool_video_compress,
        R.string.media_tool_video_compress_desc,
        "video_compress"
    ),
    EXTRACT_AUDIO(
        R.drawable.ic_extract,
        R.string.media_tool_extract_audio,
        R.string.media_tool_extract_audio_desc,
        "extract_audio"
    ),
    MEDIA_TRIM(
        R.drawable.ic_cut,
        R.string.media_tool_media_trim,
        R.string.media_tool_media_trim_desc,
        "media_trim"
    ),
    VIDEO_SNAPSHOT(
        R.drawable.ic_camera,
        R.string.media_tool_video_snapshot,
        R.string.media_tool_video_snapshot_desc,
        "video_snapshot"
    ),
    GIF_MAKER(
        R.drawable.ic_image,
        R.string.media_tool_gif_maker,
        R.string.media_tool_gif_maker_desc,
        "gif_maker"
    ),
    VIDEO_MERGE(
        R.drawable.ic_merge,
        R.string.media_tool_video_merge,
        R.string.media_tool_video_merge_desc,
        "video_merge"
    )
}
