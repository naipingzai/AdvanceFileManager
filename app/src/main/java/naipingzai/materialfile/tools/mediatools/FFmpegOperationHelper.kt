/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.mediatools

import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naipingzai.materialfile.R
import naipingzai.materialfile.provider.linux.media.MediaScanner
import naipingzai.materialfile.tools.FileTypeUtils
import naipingzai.materialfile.tools.OutputPaths
import naipingzai.materialfile.tools.formatconvert.FFmpegJni
import naipingzai.materialfile.tools.formatconvert.MediaInfo
import naipingzai.materialfile.util.FormatUtils
import java.io.File

private const val VIDEO_COMPRESS_BITRATE_KBPS = 1500
private const val IMAGE_COMPRESS_QUALITY = 80
private const val IMAGE_COMPRESS_MAX_DIM = 1920
private const val IMAGE_ENHANCE_STRENGTH = 1.5f
private const val VIDEO_ENHANCE_STRENGTH = 1.5f
private const val GIF_DEFAULT_WIDTH = 320
private const val GIF_DEFAULT_FPS = 15

/**
 * Helper to run FFmpeg operations directly from any Fragment using a progress dialog.
 * No extra page/Activity needed.
 */
object FFmpegOperationHelper {

    private var currentJob: Job? = null
    @Volatile private var lastProgressTime = 0L

    fun extractAudio(fragment: Fragment, filePath: String) {
        val file = File(filePath)
        runWithValidation(fragment, listOf(file), requireAudio = true) { validFiles ->
            doExtractAudio(fragment, validFiles)
        }
    }

    fun trimMedia(fragment: Fragment, filePath: String) {
        val file = File(filePath)
        runWithValidation(fragment, listOf(file)) { validFiles ->
            showTrimDialog(fragment, validFiles.first())
        }
    }

    fun compressVideo(fragment: Fragment, filePath: String) {
        val file = File(filePath)
        runWithValidation(fragment, listOf(file), requireVideo = true) { validFiles ->
            showCompressDialog(fragment, validFiles)
        }
    }

    fun snapshotVideo(fragment: Fragment, filePath: String) {
        val file = File(filePath)
        runWithValidation(fragment, listOf(file), requireVideo = true) { validFiles ->
            showSnapshotDialog(fragment, validFiles.first())
        }
    }

    fun makeGif(fragment: Fragment, filePath: String) {
        val file = File(filePath)
        runWithValidation(fragment, listOf(file), requireVideo = true) { validFiles ->
            showGifDialog(fragment, validFiles.first())
        }
    }

    fun compressImage(fragment: Fragment, filePath: String) {
        doImageCompress(fragment, listOf(File(filePath)))
    }

    fun enhanceImage(fragment: Fragment, filePath: String) {
        doImageEnhance(fragment, listOf(File(filePath)))
    }

    fun enhanceVideo(fragment: Fragment, filePath: String) {
        val file = File(filePath)
        runWithValidation(fragment, listOf(file), requireVideo = true) { validFiles ->
            showEnhanceDialog(fragment, validFiles)
        }
    }

    // ======== Progress dialog ========

    private fun showProgressDialog(fragment: Fragment, title: String): Pair<Dialog, (String, Int) -> Unit> {
        val context = fragment.requireContext()
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_ffmpeg_progress, null)
        val progressText = view.findViewById<TextView>(R.id.progressText)
        val progressBar = view.findViewById<LinearProgressIndicator>(R.id.progressBar)
        progressText.text = title

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(false)
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                currentJob?.cancel()
                try { FFmpegJni.cancel() } catch (_: Exception) {}
            }
            .create()
        dialog.show()

        val handler = Handler(Looper.getMainLooper())
        val updater: (String, Int) -> Unit = { text, percent ->
            handler.post {
                progressText.text = text
                if (percent < 0) {
                    progressBar.isIndeterminate = true
                } else {
                    if (progressBar.isIndeterminate) {
                        progressBar.isIndeterminate = false
                    }
                    progressBar.setProgressCompat(percent, true)
                }
            }
        }
        return dialog to updater
    }

    private fun makeProgressCallback(updater: (String, Int) -> Unit, label: String): FFmpegJni.ProgressCallback {
        lastProgressTime = 0L
        return object : FFmpegJni.ProgressCallback {
            override fun onProgress(percent: Int) {
                val now = System.currentTimeMillis()
                if (percent != 100 && now - lastProgressTime < 200) return
                lastProgressTime = now
                updater("$label $percent%", percent)
            }
        }
    }

    // ======== Validation ========

    private fun runWithValidation(
        fragment: Fragment,
        files: List<File>,
        requireVideo: Boolean = false,
        requireAudio: Boolean = false,
        onValid: (List<File>) -> Unit
    ) {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            val validFiles = mutableListOf<File>()
            for (file in files) {
                val info = withContext(Dispatchers.IO) {
                    try {
                        val mi = MediaInfo()
                        FFmpegJni.getMediaInfo(file.absolutePath, mi)
                        mi
                    } catch (_: Exception) { null }
                }
                if (info == null) continue
                if (requireVideo && !info.hasVideo) continue
                if (requireAudio && !info.hasAudio) continue
                validFiles.add(file)
            }
            if (validFiles.isEmpty()) {
                val msg = when {
                    requireVideo -> fragment.getString(R.string.media_tool_video_requires)
                    requireAudio -> fragment.getString(R.string.media_tool_no_audio_track)
                    else -> fragment.getString(R.string.media_tool_probe_failed)
                }
                MaterialAlertDialogBuilder(fragment.requireContext())
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                return@launch
            }
            onValid(validFiles)
        }
    }

    // ======== Operations ========

    private fun doExtractAudio(fragment: Fragment, files: List<File>) {
        val outputDir = OutputPaths.resolve(OutputPaths.EXTRACTED_AUDIO).also { it.mkdirs() }
        val (dialog, updater) = showProgressDialog(fragment,
            fragment.getString(R.string.media_tool_extract_audio))

        currentJob = fragment.viewLifecycleOwner.lifecycleScope.launch {
            var success = 0; var failed = 0
            for ((i, file) in files.withIndex()) {
                val label = "${fragment.getString(R.string.media_tool_extract_audio)} (${i + 1}/${files.size})"
                val outputFile = FileTypeUtils.getUniqueFile(outputDir, file.nameWithoutExtension, "m4a")
                val result = withContext(Dispatchers.IO) {
                    try { FFmpegJni.extractAudio(file.absolutePath, outputFile.absolutePath,
                        makeProgressCallback(updater, label)) } catch (_: Exception) { -1 }
                }
                if (result == 0) { success++; MediaScanner.scan(outputFile) }
                else { failed++; outputFile.delete() }
            }
            dialog.dismiss()
            if (failed > 0) {
                val err = getNativeError()
                val msg = fragment.getString(R.string.media_tool_extract_audio_result, success, failed) +
                    if (err.isNotEmpty()) "\n$err" else ""
                showError(fragment, msg)
            } else {
                showResult(fragment, fragment.getString(R.string.media_tool_extract_audio_result, success, failed))
            }
        }
    }

    private fun doVideoCompress(
        fragment: Fragment, files: List<File>,
        bitrateKbps: Int, targetWidth: Int, targetHeight: Int, targetFps: Int
    ) {
        val outputDir = OutputPaths.resolve(OutputPaths.COMPRESSED).also { it.mkdirs() }
        val (dialog, updater) = showProgressDialog(fragment,
            fragment.getString(R.string.media_tool_video_compress))

        currentJob = fragment.viewLifecycleOwner.lifecycleScope.launch {
            var success = 0; var failed = 0
            for ((i, file) in files.withIndex()) {
                val label = "${fragment.getString(R.string.media_tool_video_compress)} (${i + 1}/${files.size})"
                val outputFile = FileTypeUtils.getUniqueFile(outputDir, "${file.nameWithoutExtension}_compressed", "mp4")
                val result = withContext(Dispatchers.IO) {
                    try { FFmpegJni.videoCompress(file.absolutePath, outputFile.absolutePath,
                        bitrateKbps, targetWidth, targetHeight, targetFps,
                        makeProgressCallback(updater, label)) } catch (_: Exception) { -1 }
                }
                if (result == 0) { success++; MediaScanner.scan(outputFile) }
                else {
                    failed++; outputFile.delete()
                }
            }
            dialog.dismiss()
            if (failed > 0) {
                val err = getNativeError()
                val msg = fragment.getString(R.string.media_tool_compress_result, success, failed) +
                    if (err.isNotEmpty()) "\n$err" else ""
                showError(fragment, msg)
            } else {
                showResult(fragment, fragment.getString(R.string.media_tool_compress_result, success, failed))
            }
        }
    }

    private fun doVideoEnhance(fragment: Fragment, files: List<File>, strength: Float) {
        val outputDir = OutputPaths.resolve(OutputPaths.ENHANCED).also { it.mkdirs() }
        val (dialog, updater) = showProgressDialog(fragment,
            fragment.getString(R.string.media_tool_video_enhance))

        currentJob = fragment.viewLifecycleOwner.lifecycleScope.launch {
            var success = 0; var failed = 0
            for ((i, file) in files.withIndex()) {
                val label = "${fragment.getString(R.string.media_tool_video_enhance)} (${i + 1}/${files.size})"
                val outputFile = FileTypeUtils.getUniqueFile(outputDir, "${file.nameWithoutExtension}_enhanced", "mp4")
                val result = withContext(Dispatchers.IO) {
                    try { FFmpegJni.videoEnhance(file.absolutePath, outputFile.absolutePath,
                        strength, 0, makeProgressCallback(updater, label)) } catch (_: Exception) { -1 }
                }
                if (result == 0 && outputFile.exists()) { success++; MediaScanner.scan(outputFile) }
                else {
                    failed++; outputFile.delete()
                }
            }
            dialog.dismiss()
            if (failed > 0) {
                val err = getNativeError()
                val msg = fragment.getString(R.string.media_tool_enhance_result, success, failed) +
                    if (err.isNotEmpty()) "\n$err" else ""
                showError(fragment, msg)
            } else {
                showResult(fragment, fragment.getString(R.string.media_tool_enhance_result, success, failed))
            }
        }
    }

    private fun doImageCompress(fragment: Fragment, files: List<File>) {
        val outputDir = OutputPaths.resolve(OutputPaths.COMPRESSED).also { it.mkdirs() }
        val (dialog, updater) = showProgressDialog(fragment,
            fragment.getString(R.string.media_tool_image_compress))

        currentJob = fragment.viewLifecycleOwner.lifecycleScope.launch {
            var success = 0; var failed = 0; var totalSaved = 0L
            for ((i, file) in files.withIndex()) {
                updater("${fragment.getString(R.string.media_tool_image_compress)} (${i + 1}/${files.size})",
                    (i * 100) / files.size)
                val ext = file.extension.lowercase().let {
                    if (it in setOf("jpg", "jpeg", "png", "webp", "bmp")) it else "jpg"
                }
                val outputExt = if (ext == "bmp") "jpg" else ext
                val outputFile = FileTypeUtils.getUniqueFile(outputDir, "${file.nameWithoutExtension}_compressed", outputExt)
                val result = withContext(Dispatchers.IO) {
                    try { FFmpegJni.imageCompress(file.absolutePath, outputFile.absolutePath,
                        IMAGE_COMPRESS_QUALITY, IMAGE_COMPRESS_MAX_DIM, IMAGE_COMPRESS_MAX_DIM) } catch (_: Exception) { -1 }
                }
                if (result == 0 && outputFile.exists()) {
                    success++; totalSaved += maxOf(0L, file.length() - outputFile.length())
                    MediaScanner.scan(outputFile)
                } else { failed++; outputFile.delete() }
            }
            dialog.dismiss()
            if (failed > 0) {
                val err = getNativeError()
                val msg = fragment.getString(R.string.image_compress_result, success, FormatUtils.formatSize(totalSaved)) +
                    if (err.isNotEmpty()) "\n$err" else ""
                showError(fragment, msg)
            } else {
                showResult(fragment, fragment.getString(R.string.image_compress_result, success, FormatUtils.formatSize(totalSaved)))
            }
        }
    }

    private fun doImageEnhance(fragment: Fragment, files: List<File>) {
        val outputDir = OutputPaths.resolve(OutputPaths.ENHANCED).also { it.mkdirs() }
        val (dialog, updater) = showProgressDialog(fragment,
            fragment.getString(R.string.media_tool_image_enhance))

        currentJob = fragment.viewLifecycleOwner.lifecycleScope.launch {
            var success = 0; var failed = 0
            for ((i, file) in files.withIndex()) {
                updater("${fragment.getString(R.string.media_tool_image_enhance)} (${i + 1}/${files.size})",
                    (i * 100) / files.size)
                val ext = file.extension.lowercase().let {
                    if (it in setOf("jpg", "jpeg", "png", "webp")) it else "jpg"
                }
                val outputFile = FileTypeUtils.getUniqueFile(outputDir, "${file.nameWithoutExtension}_enhanced", ext)
                val result = withContext(Dispatchers.IO) {
                    try { FFmpegJni.imageEnhance(file.absolutePath, outputFile.absolutePath,
                        IMAGE_ENHANCE_STRENGTH) } catch (_: Exception) { -1 }
                }
                if (result == 0 && outputFile.exists()) { success++; MediaScanner.scan(outputFile) }
                else { failed++; outputFile.delete() }
            }
            dialog.dismiss()
            if (failed > 0) {
                val err = getNativeError()
                val msg = fragment.getString(R.string.media_tool_enhance_result, success, failed) +
                    if (err.isNotEmpty()) "\n$err" else ""
                showError(fragment, msg)
            } else {
                showResult(fragment, fragment.getString(R.string.media_tool_enhance_result, success, failed))
            }
        }
    }

    // ======== Dialogs for operations needing user input ========

    private fun showTrimDialog(fragment: Fragment, file: File) {
        val view = LayoutInflater.from(fragment.requireContext())
            .inflate(R.layout.dialog_media_trim, null)
        val startInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.startTimeInput)
        val endInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.endTimeInput)

        // Probe duration to fill end time
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            val info = withContext(Dispatchers.IO) {
                try {
                    val mi = MediaInfo()
                    FFmpegJni.getMediaInfo(file.absolutePath, mi)
                    mi
                } catch (_: Exception) { null }
            }
            if (info != null && info.durationMs > 0) {
                endInput.setText(String.format("%.1f", info.durationMs / 1000.0))
            }
        }

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(R.string.media_tool_media_trim)
            .setView(view)
            .setPositiveButton(R.string.media_tool_start) { _, _ ->
                val startMs = ((startInput.text.toString().toDoubleOrNull() ?: 0.0) * 1000).toLong()
                val endMs = ((endInput.text.toString().toDoubleOrNull() ?: return@setPositiveButton) * 1000).toLong()
                if (endMs <= startMs) return@setPositiveButton
                doTrim(fragment, file, startMs, endMs)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun doTrim(fragment: Fragment, file: File, startMs: Long, endMs: Long) {
        val outputDir = OutputPaths.resolve(OutputPaths.TRIMMED).also { it.mkdirs() }
        val ext = file.extension.ifEmpty { "mp4" }
        val outputFile = FileTypeUtils.getUniqueFile(outputDir, "${file.nameWithoutExtension}_trimmed", ext)
        val (dialog, updater) = showProgressDialog(fragment,
            fragment.getString(R.string.media_tool_media_trim))

        currentJob = fragment.viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try { FFmpegJni.trim(file.absolutePath, outputFile.absolutePath, startMs, endMs,
                    makeProgressCallback(updater, fragment.getString(R.string.media_tool_media_trim))) } catch (_: Exception) { -1 }
            }
            dialog.dismiss()
            if (result == 0) {
                MediaScanner.scan(outputFile)
                showResult(fragment, fragment.getString(R.string.media_tool_trim_success, outputFile.name))
            } else {
                outputFile.delete()
                val err = getNativeError()
                val msg = fragment.getString(R.string.media_tool_trim_failed) +
                    if (err.isNotEmpty()) "\n$err" else ""
                showError(fragment, msg)
            }
        }
    }

    private fun showSnapshotDialog(fragment: Fragment, file: File) {
        val view = LayoutInflater.from(fragment.requireContext())
            .inflate(R.layout.dialog_video_snapshot, null)
        val timeInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.snapshotTimeInput)

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(R.string.media_tool_video_snapshot)
            .setView(view)
            .setPositiveButton(R.string.media_tool_start) { _, _ ->
                val timeMs = ((timeInput.text.toString().toDoubleOrNull() ?: 0.0) * 1000).toLong()
                doSnapshot(fragment, file, timeMs)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun doSnapshot(fragment: Fragment, file: File, timeMs: Long) {
        val outputDir = OutputPaths.resolve(OutputPaths.SNAPSHOT).also { it.mkdirs() }
        val outputFile = FileTypeUtils.getUniqueFile(outputDir, "${file.nameWithoutExtension}_${timeMs}ms", "jpg")
        val (dialog, _) = showProgressDialog(fragment,
            fragment.getString(R.string.media_tool_video_snapshot))

        currentJob = fragment.viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try { FFmpegJni.videoSnapshot(file.absolutePath, outputFile.absolutePath, timeMs) } catch (_: Exception) { -1 }
            }
            dialog.dismiss()
            if (result == 0) {
                MediaScanner.scan(outputFile)
                showResult(fragment, fragment.getString(R.string.media_tool_snapshot_success, outputFile.name))
            } else {
                outputFile.delete()
                val err = getNativeError()
                val msg = fragment.getString(R.string.media_tool_snapshot_failed) +
                    if (err.isNotEmpty()) "\n$err" else ""
                showError(fragment, msg)
            }
        }
    }

    private fun showGifDialog(fragment: Fragment, file: File) {
        val view = LayoutInflater.from(fragment.requireContext())
            .inflate(R.layout.dialog_media_trim, null)
        val startInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.startTimeInput)
        val endInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.endTimeInput)

        // Probe duration to fill end time
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            val info = withContext(Dispatchers.IO) {
                try {
                    val mi = MediaInfo()
                    FFmpegJni.getMediaInfo(file.absolutePath, mi)
                    mi
                } catch (_: Exception) { null }
            }
            if (info != null && info.durationMs > 0) {
                endInput.setText(String.format("%.1f", info.durationMs / 1000.0))
            }
        }

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(R.string.media_tool_gif_maker)
            .setView(view)
            .setPositiveButton(R.string.media_tool_start) { _, _ ->
                val startMs = ((startInput.text.toString().toDoubleOrNull() ?: 0.0) * 1000).toLong()
                val endMs = ((endInput.text.toString().toDoubleOrNull() ?: return@setPositiveButton) * 1000).toLong()
                if (endMs <= startMs) return@setPositiveButton
                doGif(fragment, file, startMs, endMs)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun doGif(fragment: Fragment, file: File, startMs: Long, endMs: Long) {
        val outputDir = OutputPaths.resolve(OutputPaths.GIF).also { it.mkdirs() }
        val outputFile = FileTypeUtils.getUniqueFile(outputDir, file.nameWithoutExtension, "gif")
        val (dialog, updater) = showProgressDialog(fragment,
            fragment.getString(R.string.media_tool_gif_maker))

        currentJob = fragment.viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try { FFmpegJni.gifMake(file.absolutePath, outputFile.absolutePath,
                    startMs, endMs, GIF_DEFAULT_WIDTH, GIF_DEFAULT_FPS,
                    makeProgressCallback(updater, fragment.getString(R.string.media_tool_gif_maker))) } catch (_: Exception) { -1 }
            }
            dialog.dismiss()
            if (result == 0) {
                MediaScanner.scan(outputFile)
                showResult(fragment, fragment.getString(R.string.media_tool_gif_success, outputFile.name))
            } else {
                outputFile.delete()
                val err = getNativeError()
                val msg = fragment.getString(R.string.media_tool_gif_failed) +
                    if (err.isNotEmpty()) "\n$err" else ""
                showError(fragment, msg)
            }
        }
    }

    // ======== Dialogs for compress/enhance options ========

    private fun showCompressDialog(fragment: Fragment, files: List<File>) {
        val view = LayoutInflater.from(fragment.requireContext())
            .inflate(R.layout.dialog_video_compress, null)
        val bitrateGroup = view.findViewById<RadioGroup>(R.id.bitrateGroup)
        val resolutionGroup = view.findViewById<RadioGroup>(R.id.resolutionGroup)
        val fpsGroup = view.findViewById<RadioGroup>(R.id.fpsGroup)

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(R.string.media_tool_video_compress)
            .setView(view)
            .setPositiveButton(R.string.media_tool_start) { _, _ ->
                val bitrateKbps = when (bitrateGroup.checkedRadioButtonId) {
                    R.id.bitrate_low -> 500
                    R.id.bitrate_high -> 3000
                    else -> 1500
                }
                val (targetWidth, targetHeight) = when (resolutionGroup.checkedRadioButtonId) {
                    R.id.res_720p -> 0 to 720
                    R.id.res_480p -> 0 to 480
                    R.id.res_360p -> 0 to 360
                    else -> 0 to 0
                }
                val targetFps = when (fpsGroup.checkedRadioButtonId) {
                    R.id.fps_30 -> 30
                    R.id.fps_24 -> 24
                    R.id.fps_15 -> 15
                    else -> 0
                }
                doVideoCompress(fragment, files, bitrateKbps, targetWidth, targetHeight, targetFps)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showEnhanceDialog(fragment: Fragment, files: List<File>) {
        val items = arrayOf(
            fragment.getString(R.string.enhance_strength_light),
            fragment.getString(R.string.enhance_strength_medium),
            fragment.getString(R.string.enhance_strength_strong)
        )
        val strengths = floatArrayOf(0.5f, 1.5f, 3.0f)
        var selected = 1 // default medium

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(R.string.enhance_option_strength)
            .setSingleChoiceItems(items, selected) { _, which -> selected = which }
            .setPositiveButton(R.string.media_tool_start) { _, _ ->
                doVideoEnhance(fragment, files, strengths[selected])
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    // ======== Result display ========

    private fun getNativeError(): String {
        return try { FFmpegJni.getLastError() } catch (_: Exception) { "" }
    }

    private fun showResult(fragment: Fragment, message: String) {
        try {
            val view = fragment.view ?: return
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
        } catch (_: Exception) {}
    }

    private fun showError(fragment: Fragment, message: String) {
        try {
            MaterialAlertDialogBuilder(fragment.requireContext())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        } catch (_: Exception) {}
    }
}
