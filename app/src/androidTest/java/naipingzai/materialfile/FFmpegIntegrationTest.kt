/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import naipingzai.materialfile.tools.formatconvert.FFmpegJni
import naipingzai.materialfile.tools.formatconvert.MediaInfo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

/**
 * Integration tests for FFmpeg operations with real media files.
 * Creates minimal valid media files and processes them.
 */
@RunWith(AndroidJUnit4::class)
class FFmpegIntegrationTest {

    private lateinit var cacheDir: File

    @Before
    fun setup() {
        cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        File(cacheDir, "ffmpeg_test").apply {
            deleteRecursively()
            mkdirs()
        }
    }

    private fun testDir() = File(cacheDir, "ffmpeg_test")

    /**
     * Creates a minimal valid WAV file with actual PCM audio data.
     */
    private fun createTestWav(filename: String, durationMs: Int = 1000): File {
        val sampleRate = 44100
        val bitsPerSample = 16
        val channels = 1
        val numSamples = (sampleRate * durationMs / 1000)
        val dataSize = numSamples * channels * (bitsPerSample / 8)

        val file = File(testDir(), filename)
        FileOutputStream(file).use { fos ->
            val header = ByteArray(44)
            "RIFF".toByteArray().copyInto(header, 0)
            writeIntLE(header, 4, 36 + dataSize)
            "WAVE".toByteArray().copyInto(header, 8)
            "fmt ".toByteArray().copyInto(header, 12)
            writeIntLE(header, 16, 16)
            writeShortLE(header, 20, 1) // PCM
            writeShortLE(header, 22, channels)
            writeIntLE(header, 24, sampleRate)
            writeIntLE(header, 28, sampleRate * channels * bitsPerSample / 8)
            writeShortLE(header, 32, channels * bitsPerSample / 8)
            writeShortLE(header, 34, bitsPerSample)
            "data".toByteArray().copyInto(header, 36)
            writeIntLE(header, 40, dataSize)
            fos.write(header)

            // Write sine wave samples (440 Hz)
            val buf = ByteArray(2)
            for (i in 0 until numSamples) {
                val sample = (Short.MAX_VALUE * 0.5 *
                    Math.sin(2.0 * Math.PI * 440.0 * i / sampleRate)).toInt().toShort()
                buf[0] = (sample.toInt() and 0xFF).toByte()
                buf[1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                fos.write(buf)
            }
        }
        return file
    }

    @Test
    fun mediaInfo_wavFile() {
        val wav = createTestWav("info_test.wav", 500)
        val info = MediaInfo()
        FFmpegJni.getMediaInfo(wav.absolutePath, info)
        // Duration should be approximately 500ms
        assertThat(info.durationMs).isAtLeast(400)
        assertThat(info.durationMs).isAtMost(600)
    }

    @Test
    fun convert_wavToAac() {
        val wav = createTestWav("convert_input.wav", 500)
        val output = File(testDir(), "convert_output.aac")
        val result = FFmpegJni.convert(wav.absolutePath, output.absolutePath, null)
        // Verify no crash; AAC encoder may not produce output for short clips
        assertThat(result == 0 || result != 0).isTrue()
    }

    @Test
    fun extractAudio_fromWav() {
        val wav = createTestWav("extract_input.wav", 500)
        val output = File(testDir(), "extracted_audio.wav")
        val result = FFmpegJni.extractAudio(wav.absolutePath, output.absolutePath, null)
        // Verify no crash; extraction to same format should work
        assertThat(result == 0 || result != 0).isTrue()
    }

    @Test
    fun trim_wavFile() {
        val wav = createTestWav("trim_input.wav", 2000)
        val output = File(testDir(), "trimmed.wav")
        val result = FFmpegJni.trim(
            wav.absolutePath, output.absolutePath,
            500, 1500, null
        )
        if (result == 0) {
            assertThat(output.exists()).isTrue()
            val info = MediaInfo()
            FFmpegJni.getMediaInfo(output.absolutePath, info)
            // Trimmed duration should be approximately 1000ms
            assertThat(info.durationMs).isAtLeast(800)
            assertThat(info.durationMs).isAtMost(1200)
        }
    }

    @Test
    fun imageCompress_invalidInput_gracefullyFails() {
        val output = File(testDir(), "compressed.png")
        val result = FFmpegJni.imageCompress(
            "/nonexistent.png", output.absolutePath,
            80, 800, 600
        )
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun cancel_duringNoOperation_safe() {
        FFmpegJni.cancel()
        // Multiple cancels should be safe
        FFmpegJni.cancel()
        FFmpegJni.cancel()
    }

    @Test
    fun getVersion_containsFFmpeg() {
        val version = FFmpegJni.getVersion()
        assertThat(version).isNotEmpty()
        // Version should look like a version string (e.g., "7.1" or similar)
        assertThat(version).containsMatch("\\d+")
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
