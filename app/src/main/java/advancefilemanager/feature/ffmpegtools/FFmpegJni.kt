/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.ffmpegtools

/**
 * FFmpeg JNI bridge — moved from main app.
 */
object FFmpegJni {
    init {
        System.loadLibrary("ffmpeg-jni")
    }

    interface ProgressCallback {
        fun onProgress(percent: Int)
    }

    @JvmStatic
    external fun getVersion(): String

    @JvmStatic
    external fun convert(inputPath: String, outputPath: String, callback: ProgressCallback?): Int

    @JvmStatic
    external fun cancel()

    @JvmStatic
    external fun getLastError(): String

    @JvmStatic
    external fun getMediaInfo(path: String, info: MediaInfo)

    @JvmStatic
    external fun extractAudio(inputPath: String, outputPath: String, callback: ProgressCallback?): Int

    @JvmStatic
    external fun trim(inputPath: String, outputPath: String, startMs: Long, endMs: Long, callback: ProgressCallback?): Int

    @JvmStatic
    external fun videoCompress(inputPath: String, outputPath: String, bitrateKbps: Int, width: Int, height: Int, fps: Int, callback: ProgressCallback?): Int

    @JvmStatic
    external fun videoSnapshot(inputPath: String, outputPath: String, timeMs: Long): Int

    @JvmStatic
    external fun gifMake(inputPath: String, outputPath: String, startMs: Long, durationMs: Long, width: Int, fps: Int, callback: ProgressCallback?): Int

    @JvmStatic
    external fun videoEnhance(inputPath: String, outputPath: String, strength: Float, callback: ProgressCallback?): Int

    @JvmStatic
    external fun imageCompress(inputPath: String, outputPath: String, quality: Int, maxWidth: Int, maxHeight: Int): Int

    @JvmStatic
    external fun imageEnhance(inputPath: String, outputPath: String, strength: Float): Int

    @JvmStatic
    external fun mergeFiles(inputListPath: String, outputPath: String, callback: ProgressCallback?): Int

    @JvmStatic
    external fun normalizeVideo(inputPath: String, outputPath: String, targetWidth: Int, targetHeight: Int, targetBitrateKbps: Int, callback: ProgressCallback?): Int
}
