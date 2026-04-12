/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.mediatools

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import naipingzai.materialfile.R

enum class MediaToolFeature(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    FORMAT_CONVERT(
        R.drawable.convert_icon_white_24dp,
        R.string.media_tool_format_convert,
        R.string.media_tool_format_convert_desc
    ),
    IMAGE_COMPRESS(
        R.drawable.image_icon_white_24dp,
        R.string.media_tool_image_compress,
        R.string.media_tool_image_compress_desc
    ),
    IMAGE_ENHANCE(
        R.drawable.image_icon_white_24dp,
        R.string.media_tool_image_enhance,
        R.string.media_tool_image_enhance_desc
    ),
    VIDEO_COMPRESS(
        R.drawable.video_icon_white_24dp,
        R.string.media_tool_video_compress,
        R.string.media_tool_video_compress_desc
    ),
    VIDEO_ENHANCE(
        R.drawable.video_icon_white_24dp,
        R.string.media_tool_video_enhance,
        R.string.media_tool_video_enhance_desc
    ),
    EXTRACT_AUDIO(
        R.drawable.extract_icon_control_normal_24dp,
        R.string.media_tool_extract_audio,
        R.string.media_tool_extract_audio_desc
    ),
    MEDIA_TRIM(
        R.drawable.cut_icon_control_normal_24dp,
        R.string.media_tool_media_trim,
        R.string.media_tool_media_trim_desc
    ),
    VIDEO_SNAPSHOT(
        R.drawable.camera_icon_white_24dp,
        R.string.media_tool_video_snapshot,
        R.string.media_tool_video_snapshot_desc
    ),
    GIF_MAKER(
        R.drawable.image_icon_white_24dp,
        R.string.media_tool_gif_maker,
        R.string.media_tool_gif_maker_desc
    ),
    MEDIA_MERGE(
        R.drawable.add_icon_white_24dp,
        R.string.media_tool_media_merge,
        R.string.media_tool_media_merge_desc
    ),
    MEDIA_INFO(
        R.drawable.information_icon_white_24dp,
        R.string.media_tool_media_info,
        R.string.media_tool_media_info_desc
    )
}
