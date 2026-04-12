/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.formatconvert

/**
 * Data class filled by native code via JNI field access.
 * Field names must match exactly what ffmpeg-jni.c expects.
 */
class MediaInfo {
    @JvmField var durationMs: Long = 0
    @JvmField var formatName: String? = null
    @JvmField var audioCodec: String? = null
    @JvmField var sampleRate: Int = 0
    @JvmField var channels: Int = 0
    @JvmField var audioBitrate: Int = 0
    @JvmField var videoCodec: String? = null
    @JvmField var width: Int = 0
    @JvmField var height: Int = 0
    @JvmField var videoBitrate: Int = 0

    val hasAudio: Boolean get() = audioCodec != null
    val hasVideo: Boolean get() = videoCodec != null

    fun formatDuration(): String {
        val totalSec = durationMs / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
        else String.format("%d:%02d", m, s)
    }

    fun summary(): String = buildString {
        formatName?.let { append(it) }
        if (hasVideo) {
            append(" | ${videoCodec} ${width}x${height}")
            if (videoBitrate > 0) append(" ${videoBitrate}kbps")
        }
        if (hasAudio) {
            append(" | ${audioCodec} ${sampleRate}Hz")
            if (audioBitrate > 0) append(" ${audioBitrate}kbps")
        }
        if (durationMs > 0) append(" | ${formatDuration()}")
    }
}
