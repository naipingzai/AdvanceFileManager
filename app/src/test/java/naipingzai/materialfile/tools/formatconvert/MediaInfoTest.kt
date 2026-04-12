/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.formatconvert

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MediaInfoTest {

    @Test
    fun defaults() {
        val info = MediaInfo()
        assertThat(info.durationMs).isEqualTo(0)
        assertThat(info.formatName).isNull()
        assertThat(info.audioCodec).isNull()
        assertThat(info.videoCodec).isNull()
        assertThat(info.width).isEqualTo(0)
        assertThat(info.height).isEqualTo(0)
    }

    @Test
    fun hasAudio_whenCodecSet() {
        val info = MediaInfo().apply { audioCodec = "aac" }
        assertThat(info.hasAudio).isTrue()
    }

    @Test
    fun hasAudio_whenCodecNull() {
        assertThat(MediaInfo().hasAudio).isFalse()
    }

    @Test
    fun hasVideo_whenCodecSet() {
        val info = MediaInfo().apply { videoCodec = "h264" }
        assertThat(info.hasVideo).isTrue()
    }

    @Test
    fun hasVideo_whenCodecNull() {
        assertThat(MediaInfo().hasVideo).isFalse()
    }

    @Test
    fun formatDuration_zeroMs() {
        val info = MediaInfo().apply { durationMs = 0 }
        assertThat(info.formatDuration()).isEqualTo("0:00")
    }

    @Test
    fun formatDuration_seconds() {
        val info = MediaInfo().apply { durationMs = 45_000 }
        assertThat(info.formatDuration()).isEqualTo("0:45")
    }

    @Test
    fun formatDuration_minutes() {
        val info = MediaInfo().apply { durationMs = 125_000 }
        assertThat(info.formatDuration()).isEqualTo("2:05")
    }

    @Test
    fun formatDuration_hours() {
        val info = MediaInfo().apply { durationMs = 3723_000 } // 1h 2m 3s
        assertThat(info.formatDuration()).isEqualTo("1:02:03")
    }

    @Test
    fun summary_audioOnly() {
        val info = MediaInfo().apply {
            formatName = "mp3"
            audioCodec = "mp3"
            sampleRate = 44100
            audioBitrate = 320
            durationMs = 60_000
        }
        val s = info.summary()
        assertThat(s).contains("mp3")
        assertThat(s).contains("44100Hz")
        assertThat(s).contains("320kbps")
        assertThat(s).contains("1:00")
    }

    @Test
    fun summary_videoOnly() {
        val info = MediaInfo().apply {
            formatName = "mp4"
            videoCodec = "h264"
            width = 1920
            height = 1080
            videoBitrate = 3000
        }
        val s = info.summary()
        assertThat(s).contains("h264")
        assertThat(s).contains("1920x1080")
        assertThat(s).contains("3000kbps")
    }

    @Test
    fun summary_audioAndVideo() {
        val info = MediaInfo().apply {
            formatName = "mp4"
            videoCodec = "h264"
            width = 1280
            height = 720
            videoBitrate = 2000
            audioCodec = "aac"
            sampleRate = 48000
            audioBitrate = 128
            durationMs = 120_000
        }
        val s = info.summary()
        assertThat(s).contains("mp4")
        assertThat(s).contains("h264")
        assertThat(s).contains("1280x720")
        assertThat(s).contains("aac")
        assertThat(s).contains("48000Hz")
        assertThat(s).contains("2:00")
    }

    @Test
    fun summary_empty() {
        val info = MediaInfo()
        val s = info.summary()
        assertThat(s).isEmpty()
    }

    @Test
    fun summary_zeroBitrate_notShown() {
        val info = MediaInfo().apply {
            formatName = "mp4"
            videoCodec = "mpeg4"
            width = 640
            height = 480
            videoBitrate = 0
        }
        val s = info.summary()
        assertThat(s).doesNotContain("0kbps")
    }
}
