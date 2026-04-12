/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.formatconvert

object FFmpegJni {
    init {
        System.loadLibrary("ffmpeg-jni")
    }

    interface ProgressCallback {
        fun onProgress(percent: Int)
    }

    /** Get FFmpeg version string */
    @JvmStatic
    external fun getVersion(): String

    /**
     * Convert a media file.
     * @param inputPath  absolute path to input file
     * @param outputPath absolute path to output file (extension determines format)
     * @param callback   optional progress callback (0-100)
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun convert(inputPath: String, outputPath: String, callback: ProgressCallback?): Int

    /** Cancel ongoing conversion */
    @JvmStatic
    external fun cancel()

    /** Get the last error message from native FFmpeg operations */
    @JvmStatic
    external fun getLastError(): String

    /** Fill MediaInfo fields from a file */
    @JvmStatic
    external fun getMediaInfo(path: String, info: MediaInfo)

    /**
     * Extract audio track from a media file.
     * @param inputPath  absolute path to input video/media file
     * @param outputPath absolute path to output audio file (extension determines codec)
     * @param callback   optional progress callback (0-100)
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun extractAudio(inputPath: String, outputPath: String, callback: ProgressCallback?): Int

    /**
     * Trim (cut) a media file by time range.
     * @param inputPath  absolute path to input file
     * @param outputPath absolute path to output file
     * @param startMs    start time in milliseconds
     * @param endMs      end time in milliseconds
     * @param callback   optional progress callback (0-100)
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun trim(inputPath: String, outputPath: String, startMs: Long, endMs: Long, callback: ProgressCallback?): Int

    /**
     * Compress a video file by re-encoding at lower bitrate/resolution/framerate.
     * @param inputPath         absolute path to input video
     * @param outputPath        absolute path to output video
     * @param targetBitrateKbps target video bitrate in kbps
     * @param targetWidth       target width (0 = keep original)
     * @param targetHeight      target height (0 = keep original)
     * @param targetFps         target framerate (0 = keep original)
     * @param callback          optional progress callback (0-100)
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun videoCompress(
        inputPath: String, outputPath: String,
        targetBitrateKbps: Int,
        targetWidth: Int, targetHeight: Int, targetFps: Int,
        callback: ProgressCallback?
    ): Int

    /**
     * Extract a single frame (snapshot) from a video.
     * @param inputPath  absolute path to input video
     * @param outputPath absolute path to output image (jpg/png)
     * @param timeMs     timestamp in milliseconds
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun videoSnapshot(inputPath: String, outputPath: String, timeMs: Long): Int

    /**
     * Create a GIF from a video segment.
     * @param inputPath  absolute path to input video
     * @param outputPath absolute path to output GIF
     * @param startMs    start time in milliseconds
     * @param endMs      end time in milliseconds
     * @param width      output GIF width (height auto-scaled)
     * @param fps        output GIF frame rate
     * @param callback   optional progress callback (0-100)
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun gifMake(inputPath: String, outputPath: String, startMs: Long, endMs: Long, width: Int, fps: Int, callback: ProgressCallback?): Int

    /**
     * Merge (concatenate) multiple media files.
     * @param inputPaths array of absolute paths to input files
     * @param outputPath absolute path to output file
     * @param callback   optional progress callback (0-100)
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun mergeFiles(inputPaths: Array<String>, outputPath: String, callback: ProgressCallback?): Int

    /**
     * Normalize a video to H.264+AAC at target resolution and bitrate.
     * Used to prepare files with different codecs/resolutions for merging.
     * @param inputPath          absolute path to input file
     * @param outputPath         absolute path to output MP4 file
     * @param targetWidth        target width (0 = keep original)
     * @param targetHeight       target height (0 = keep original)
     * @param targetBitrateKbps  target video bitrate in kbps (0 = auto)
     * @param callback           optional progress callback (0-100)
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun normalizeVideo(
        inputPath: String, outputPath: String,
        targetWidth: Int, targetHeight: Int, targetBitrateKbps: Int,
        callback: ProgressCallback?
    ): Int

    /**
     * Merge files via single-pass transcode: decode all inputs and re-encode
     * into one H.264+AAC MP4. Handles different codecs, resolutions, sample rates.
     * @param inputPaths       array of absolute paths to input files
     * @param outputPath       absolute path to output MP4 file
     * @param targetWidth      target output width
     * @param targetHeight     target output height
     * @param targetBitrateKbps target video bitrate in kbps (0 = auto)
     * @param callback         optional progress callback (0-100)
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun mergeFilesTranscode(
        inputPaths: Array<String>, outputPath: String,
        targetWidth: Int, targetHeight: Int, targetBitrateKbps: Int,
        callback: ProgressCallback?
    ): Int

    /**
     * Compress an image via FFmpeg (decode → scale → encode).
     * @param inputPath  absolute path to input image
     * @param outputPath absolute path to output image (extension determines format: jpg/png/webp)
     * @param quality    JPEG/WebP quality (1-100), ignored for PNG
     * @param maxWidth   max output width (0 = keep original)
     * @param maxHeight  max output height (0 = keep original)
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun imageCompress(
        inputPath: String, outputPath: String,
        quality: Int, maxWidth: Int, maxHeight: Int
    ): Int

    /**
     * Enhance (sharpen) an image using FFmpeg unsharp mask filter.
     * @param inputPath  absolute path to input image
     * @param outputPath absolute path to output image
     * @param strength   sharpening strength (0.5 = light, 1.5 = medium, 3.0 = strong)
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun imageEnhance(inputPath: String, outputPath: String, strength: Float): Int

    /**
     * Enhance (sharpen) a video using FFmpeg unsharp mask filter.
     * @param inputPath         absolute path to input video
     * @param outputPath        absolute path to output video
     * @param strength          sharpening strength (0.5 = light, 1.5 = medium, 3.0 = strong)
     * @param targetBitrateKbps target video bitrate in kbps (0 = auto/same as source)
     * @param callback          optional progress callback (0-100)
     * @return 0 on success, negative on error
     */
    @JvmStatic
    external fun videoEnhance(
        inputPath: String, outputPath: String,
        strength: Float, targetBitrateKbps: Int,
        callback: ProgressCallback?
    ): Int
}
