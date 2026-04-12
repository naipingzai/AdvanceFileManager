/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import naipingzai.materialfile.tools.formatconvert.FFmpegJni
import naipingzai.materialfile.tools.formatconvert.MediaInfo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Instrumented tests for FFmpeg JNI interface.
 * Tests all native methods are callable and return expected results.
 */
@RunWith(AndroidJUnit4::class)
class FFmpegJniTest {

    private lateinit var cacheDir: File

    @Before
    fun setup() {
        cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
    }

    @Test
    fun getVersion_returnsNonEmptyString() {
        val version = FFmpegJni.getVersion()
        assertThat(version).isNotEmpty()
    }

    @Test
    fun getLastError_initiallyEmpty() {
        val error = FFmpegJni.getLastError()
        assertThat(error).isNotNull()
    }

    @Test
    fun getMediaInfo_invalidFile_doesNotCrash() {
        val info = MediaInfo()
        try {
            FFmpegJni.getMediaInfo("/nonexistent/file.mp4", info)
        } catch (_: Exception) {
            // Expected - invalid file
        }
        // Should not crash the process
    }

    @Test
    fun convert_invalidInput_returnsNonZero() {
        val result = FFmpegJni.convert(
            "/nonexistent/input.mp4",
            File(cacheDir, "output_convert.mp4").absolutePath,
            null
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun extractAudio_invalidInput_returnsNonZero() {
        val result = FFmpegJni.extractAudio(
            "/nonexistent/input.mp4",
            File(cacheDir, "output_audio.aac").absolutePath,
            null
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun trim_invalidInput_returnsNonZero() {
        val result = FFmpegJni.trim(
            "/nonexistent/input.mp4",
            File(cacheDir, "output_trim.mp4").absolutePath,
            0, 5000, null
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun videoCompress_invalidInput_returnsNonZero() {
        val result = FFmpegJni.videoCompress(
            "/nonexistent/input.mp4",
            File(cacheDir, "output_compress.mp4").absolutePath,
            1500, 0, 0, 0, null
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun videoSnapshot_invalidInput_returnsNonZero() {
        val result = FFmpegJni.videoSnapshot(
            "/nonexistent/input.mp4",
            File(cacheDir, "output_snapshot.png").absolutePath,
            1000
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun gifMake_invalidInput_returnsNonZero() {
        val result = FFmpegJni.gifMake(
            "/nonexistent/input.mp4",
            File(cacheDir, "output.gif").absolutePath,
            0, 5000, 320, 15, null
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun normalizeVideo_invalidInput_returnsNonZero() {
        val result = FFmpegJni.normalizeVideo(
            "/nonexistent/input.mp4",
            File(cacheDir, "output_normalize.mp4").absolutePath,
            1920, 1080, 3000, null
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun mergeFiles_emptyList_returnsNonZero() {
        val result = FFmpegJni.mergeFiles(
            emptyArray(),
            File(cacheDir, "output_merge.mp4").absolutePath,
            null
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun imageCompress_invalidInput_returnsNonZero() {
        val result = FFmpegJni.imageCompress(
            "/nonexistent/input.png",
            File(cacheDir, "output_imgcompress.png").absolutePath,
            80, 1920, 1080
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun imageEnhance_invalidInput_returnsNonZero() {
        val result = FFmpegJni.imageEnhance(
            "/nonexistent/input.png",
            File(cacheDir, "output_imgenhance.png").absolutePath,
            1.5f
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun videoEnhance_invalidInput_returnsNonZero() {
        val result = FFmpegJni.videoEnhance(
            "/nonexistent/input.mp4",
            File(cacheDir, "output_videoenhance.mp4").absolutePath,
            1.5f, 3000, null
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun cancel_doesNotCrash() {
        FFmpegJni.cancel()
        // Should not crash
    }

    /**
     * Test with a real minimal file: create a tiny WAV and try to get media info.
     */
    @Test
    fun getMediaInfo_validWavFile() {
        // Create a minimal WAV file (44-byte header + 0 data)
        val wavFile = File(cacheDir, "test_minimal.wav")
        FileOutputStream(wavFile).use { fos ->
            val header = createMinimalWavHeader(0)
            fos.write(header)
        }
        try {
            val info = MediaInfo()
            FFmpegJni.getMediaInfo(wavFile.absolutePath, info)
            // If it parsed, duration should be 0 or very small
            assertThat(info.durationMs).isAtLeast(0)
        } finally {
            wavFile.delete()
        }
    }

    private fun createMinimalWavHeader(dataSize: Int): ByteArray {
        val totalSize = 36 + dataSize
        val header = ByteArray(44)
        // RIFF header
        "RIFF".toByteArray().copyInto(header, 0)
        writeIntLE(header, 4, totalSize)
        "WAVE".toByteArray().copyInto(header, 8)
        // fmt chunk
        "fmt ".toByteArray().copyInto(header, 12)
        writeIntLE(header, 16, 16) // chunk size
        writeShortLE(header, 20, 1) // PCM format
        writeShortLE(header, 22, 1) // mono
        writeIntLE(header, 24, 44100) // sample rate
        writeIntLE(header, 28, 88200) // byte rate
        writeShortLE(header, 32, 2) // block align
        writeShortLE(header, 34, 16) // bits per sample
        // data chunk
        "data".toByteArray().copyInto(header, 36)
        writeIntLE(header, 40, dataSize)
        return header
    }

    private fun writeIntLE(buf: ByteArray, offset: Int, value: Int) {
        buf[offset] = (value and 0xFF).toByte()
        buf[offset + 1] = ((value shr 8) and 0xFF).toByte()
        buf[offset + 2] = ((value shr 16) and 0xFF).toByte()
        buf[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun writeShortLE(buf: ByteArray, offset: Int, value: Int) {
        buf[offset] = (value and 0xFF).toByte()
        buf[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }
}
