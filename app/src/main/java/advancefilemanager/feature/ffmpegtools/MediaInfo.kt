/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.ffmpegtools

data class MediaInfo(
    var duration: Long = 0L,
    var bitrate: Long = 0L,
    var width: Int = 0,
    var height: Int = 0,
    var videoCodec: String? = null,
    var audioCodec: String? = null,
    var frameRate: Float = 0f,
    var sampleRate: Int = 0,
    var channels: Int = 0,
    // Fields referenced by native FFmpeg JNI code
    var durationMs: Long = 0L,
    var formatName: String? = null,
    var audioBitrate: Int = 0,
    var videoBitrate: Int = 0
)
