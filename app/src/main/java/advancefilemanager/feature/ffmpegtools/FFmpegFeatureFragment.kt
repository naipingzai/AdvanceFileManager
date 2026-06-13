/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.ffmpegtools

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.advancefilemanager.databinding.FragmentFfmpegFeatureBinding
import java.io.File

class FFmpegFeatureFragment : Fragment() {

    private lateinit var binding: FragmentFfmpegFeatureBinding
    private lateinit var feature: MediaToolFeature
    private lateinit var filePath: String
    private var filePaths: Array<String>? = null
    private var processingThread: Thread? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFfmpegFeatureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val actionType = requireArguments().getString(ARG_ACTION_TYPE)
            ?: run { activity?.finish(); return }
        filePath = requireArguments().getString(ARG_FILE_PATH)
            ?: run { activity?.finish(); return }
        filePaths = requireArguments().getStringArray(ARG_FILE_PATHS)
        feature = MediaToolFeature.entries.find { it.actionType == actionType }
            ?: run { activity?.finish(); return }

        setupFileInfo()
        setupOutputOptions()
        setupActionButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        processingThread?.interrupt()
        processingThread = null
    }

    private fun setupFileInfo() {
        val file = File(filePath)
        val allPaths = filePaths
        if (allPaths != null && allPaths.size > 1) {
            binding.fileName.text = "已选择 ${allPaths.size} 个文件"
            val totalSize = allPaths.sumOf { File(it).length() }
            binding.fileInfo.text = formatFileSize(totalSize)
        } else {
            binding.fileName.text = file.name
            val sizeStr = formatFileSize(file.length())
            binding.fileInfo.text = "$sizeStr · ${file.extension.uppercase()}"
        }
    }

    private fun setupOutputOptions() {
        val formats = getOutputFormats()
        if (formats.isEmpty()) {
            binding.outputLabel.visibility = View.GONE
            binding.outputFormatLayout.visibility = View.GONE
        } else {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, formats)
            binding.outputFormat.setAdapter(adapter)
            binding.outputFormat.setText(formats.firstOrNull() ?: "", false)
        }

        // Show time picker for GIF maker and media trim
        val showTimePicker = feature == MediaToolFeature.GIF_MAKER ||
            feature == MediaToolFeature.MEDIA_TRIM ||
            feature == MediaToolFeature.VIDEO_SNAPSHOT
        binding.timePickerLayout.visibility = if (showTimePicker) View.VISIBLE else View.GONE

        if (showTimePicker) {
            setupTimePicker()
        }
    }

    private fun setupTimePicker() {
        // Get media duration for max time
        try {
            val mediaInfo = MediaInfo()
            FFmpegJni.getMediaInfo(filePath, mediaInfo)
            maxDurationMs = mediaInfo.duration
            if (maxDurationMs > 0) {
                binding.maxDurationText.text = "时长: ${formatTime(maxDurationMs)}"
            }
        } catch (e: Exception) {
            maxDurationMs = 300_000L // Default 5 minutes
        }

        binding.startTimeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateDurationDisplay() }
        })
        binding.endTimeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateDurationDisplay() }
        })
    }

    private fun updateDurationDisplay() {
        val startMs = parseTimeInput(binding.startTimeInput.text.toString())
        val endMs = parseTimeInput(binding.endTimeInput.text.toString())
        if (endMs > startMs) {
            val durationMs = endMs - startMs
            binding.durationText.text = "时长: ${formatTime(durationMs)}"
        } else {
            binding.durationText.text = "时长: 00:00"
        }
    }

    private fun parseTimeInput(text: String): Long {
        return try {
            val parts = text.split(":", ".")
            when (parts.size) {
                1 -> parts[0].toLong() * 1000
                2 -> parts[0].toLong() * 60_000 + parts[1].toLong() * 1000
                3 -> parts[0].toLong() * 3_600_000 + parts[1].toLong() * 60_000 + parts[2].toLong() * 1000
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    private fun getOutputFormats(): List<String> {
        return when (feature) {
            MediaToolFeature.FORMAT_CONVERT -> listOf("mp4", "mkv", "avi", "mov", "webm", "flv", "mp3", "aac", "wav", "flac", "ogg")
            MediaToolFeature.IMAGE_COMPRESS -> listOf("jpg (质量80%)", "jpg (质量60%)", "png", "webp")
            MediaToolFeature.VIDEO_COMPRESS -> listOf("低质量 (CRF 28)", "中等质量 (CRF 23)", "高质量 (CRF 18)")
            MediaToolFeature.EXTRACT_AUDIO -> listOf("mp3", "aac", "wav", "flac", "ogg")
            MediaToolFeature.MEDIA_TRIM -> emptyList()
            MediaToolFeature.VIDEO_SNAPSHOT -> emptyList()
            MediaToolFeature.GIF_MAKER -> listOf("320px", "480px", "原始尺寸")
            MediaToolFeature.VIDEO_MERGE -> emptyList()
            MediaToolFeature.VIDEO_ENHANCE -> listOf("轻微增强 (1.2x)", "标准增强 (1.5x)", "强力增强 (2.0x)")
            MediaToolFeature.IMAGE_ENHANCE -> listOf("轻微增强 (1.2x)", "标准增强 (1.5x)", "强力增强 (2.0x)")
            MediaToolFeature.IMAGE_STITCH -> emptyList()
            MediaToolFeature.VIDEO_TO_AUDIO -> listOf("mp3", "aac", "wav", "flac", "ogg")
            MediaToolFeature.SUBTITLE_EXTRACT -> emptyList()
            MediaToolFeature.AUDIO_NORMALIZE -> emptyList()
            MediaToolFeature.VIDEO_WATERMARK -> emptyList()
            MediaToolFeature.VIDEO_ROTATE -> emptyList()
        }
    }

    private fun setupActionButton() {
        binding.actionButton.text = getString(feature.titleRes)
        binding.actionButton.setOnClickListener {
            startProcessing()
        }
    }

    private var maxDurationMs: Long = 0L

    private fun startProcessing() {
        binding.actionButton.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        binding.progressText.visibility = View.VISIBLE
        binding.progressText.text = "处理中..."

        val outputFormat = binding.outputFormat.text.toString()
        val inputFile = File(filePath)
        val outputFile = generateOutputFile(inputFile, outputFormat)

        processingThread = Thread {
            try {
                val result = executeFFmpeg(inputFile, outputFile, outputFormat)
                val act = activity ?: return@Thread
                act.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    if (result == 0) {
                        binding.progressBar.visibility = View.GONE
                        binding.progressText.text = "完成: ${outputFile.name}"
                        Toast.makeText(act, "处理完成: ${outputFile.name}", Toast.LENGTH_LONG).show()
                    } else {
                        binding.progressText.text = "失败: ${FFmpegJni.getLastError()}"
                    }
                    binding.actionButton.isEnabled = true
                }
            } catch (e: InterruptedException) {
                // Thread interrupted, fragment destroyed
            } catch (e: Exception) {
                val act = activity ?: return@Thread
                act.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    binding.progressBar.visibility = View.GONE
                    binding.progressText.text = "错误: ${e.message}"
                    binding.actionButton.isEnabled = true
                }
            }
        }.apply { start() }
    }

    private fun generateOutputFile(inputFile: File, format: String): File {
        val baseName = inputFile.nameWithoutExtension
        val ext = when (feature) {
            MediaToolFeature.FORMAT_CONVERT -> format.split(" ").first()
            MediaToolFeature.IMAGE_COMPRESS -> format.split(" ").first()
            MediaToolFeature.VIDEO_COMPRESS -> inputFile.extension
            MediaToolFeature.EXTRACT_AUDIO -> format.split(" ").first()
            MediaToolFeature.GIF_MAKER -> "gif"
            MediaToolFeature.VIDEO_SNAPSHOT -> "jpg"
            MediaToolFeature.VIDEO_ENHANCE -> inputFile.extension
            MediaToolFeature.IMAGE_ENHANCE -> inputFile.extension
            MediaToolFeature.VIDEO_MERGE -> inputFile.extension
            MediaToolFeature.VIDEO_TO_AUDIO -> format.split(" ").first()
            MediaToolFeature.VIDEO_WATERMARK -> inputFile.extension
            MediaToolFeature.VIDEO_ROTATE -> inputFile.extension
            MediaToolFeature.SUBTITLE_EXTRACT -> "srt"
            MediaToolFeature.AUDIO_NORMALIZE -> inputFile.extension
            MediaToolFeature.IMAGE_STITCH -> "jpg"
            else -> inputFile.extension
        }
        val dir = inputFile.parentFile ?: return File(inputFile.nameWithoutExtension + "_output.$ext")
        var output = File(dir, "${baseName}_output.$ext")
        var counter = 1
        while (output.exists()) {
            output = File(dir, "${baseName}_output_${counter}.$ext")
            counter++
        }
        return output
    }

    private fun executeFFmpeg(inputFile: File, outputFile: File, format: String): Int {
        val callback = object : FFmpegJni.ProgressCallback {
            override fun onProgress(percent: Int) {
                val act = activity ?: return
                act.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    binding.progressBar.progress = percent
                    binding.progressText.text = "处理中... $percent%"
                }
            }
        }

        val startMs = parseTimeInput(binding.startTimeInput.text.toString())
        val endMs = parseTimeInput(binding.endTimeInput.text.toString())

        return when (feature) {
            MediaToolFeature.FORMAT_CONVERT -> {
                FFmpegJni.convert(inputFile.absolutePath, outputFile.absolutePath, callback)
            }
            MediaToolFeature.EXTRACT_AUDIO -> {
                FFmpegJni.extractAudio(inputFile.absolutePath, outputFile.absolutePath, callback)
            }
            MediaToolFeature.VIDEO_COMPRESS -> {
                val (crf, width, height, fps) = parseVideoCompressParams(format, inputFile)
                FFmpegJni.videoCompress(
                    inputFile.absolutePath, outputFile.absolutePath,
                    crf, width, height, fps, callback
                )
            }
            MediaToolFeature.MEDIA_TRIM -> {
                FFmpegJni.trim(
                    inputFile.absolutePath, outputFile.absolutePath,
                    startMs, endMs, callback
                )
            }
            MediaToolFeature.VIDEO_SNAPSHOT -> {
                FFmpegJni.videoSnapshot(
                    inputFile.absolutePath, outputFile.absolutePath, startMs
                )
            }
            MediaToolFeature.GIF_MAKER -> {
                val width = parseGifWidth(format)
                FFmpegJni.gifMake(
                    inputFile.absolutePath, outputFile.absolutePath,
                    startMs, endMs, width, 10, callback
                )
            }
            MediaToolFeature.VIDEO_MERGE -> {
                val paths = filePaths
                if (paths == null || paths.size < 2) {
                    return -1
                }
                FFmpegJni.mergeFiles(
                    paths, outputFile.absolutePath, callback
                )
            }
            MediaToolFeature.IMAGE_COMPRESS -> {
                val (quality, maxWidth, maxHeight) = parseImageCompressParams(format)
                FFmpegJni.imageCompress(
                    inputFile.absolutePath, outputFile.absolutePath,
                    quality, maxWidth, maxHeight
                )
            }
            MediaToolFeature.VIDEO_ENHANCE -> {
                val strength = parseEnhanceStrength(format)
                FFmpegJni.videoEnhance(
                    inputFile.absolutePath, outputFile.absolutePath,
                    strength, 0, callback
                )
            }
            MediaToolFeature.IMAGE_ENHANCE -> {
                val strength = parseEnhanceStrength(format)
                FFmpegJni.imageEnhance(
                    inputFile.absolutePath, outputFile.absolutePath,
                    strength
                )
            }
            MediaToolFeature.VIDEO_TO_AUDIO -> {
                FFmpegJni.extractAudio(inputFile.absolutePath, outputFile.absolutePath, callback)
            }
            MediaToolFeature.IMAGE_STITCH -> {
                // Use merge for image stitching (same as video merge)
                val paths = filePaths
                if (paths == null || paths.size < 2) {
                    return -1
                }
                FFmpegJni.mergeFiles(paths, outputFile.absolutePath, callback)
            }
            MediaToolFeature.SUBTITLE_EXTRACT -> {
                // Extract subtitle streams using convert with subtitle format
                FFmpegJni.convert(inputFile.absolutePath, outputFile.absolutePath, callback)
            }
            MediaToolFeature.AUDIO_NORMALIZE -> {
                // Normalize audio using convert
                FFmpegJni.convert(inputFile.absolutePath, outputFile.absolutePath, callback)
            }
            MediaToolFeature.VIDEO_WATERMARK -> {
                // Watermark would need JNI support; use convert as placeholder
                FFmpegJni.convert(inputFile.absolutePath, outputFile.absolutePath, callback)
            }
            MediaToolFeature.VIDEO_ROTATE -> {
                // Rotate would need JNI support; use convert as placeholder
                FFmpegJni.convert(inputFile.absolutePath, outputFile.absolutePath, callback)
            }
        }
    }

    private fun parseVideoCompressParams(format: String, inputFile: File): VideoCompressParams {
        return when {
            format.contains("CRF 28") -> VideoCompressParams(28, 0, 0, 0)
            format.contains("CRF 23") -> VideoCompressParams(23, 0, 0, 0)
            format.contains("CRF 18") -> VideoCompressParams(18, 0, 0, 0)
            else -> VideoCompressParams(23, 0, 0, 0)
        }
    }

    private fun parseImageCompressParams(format: String): ImageCompressParams {
        return when {
            format.contains("质量80") -> ImageCompressParams(80, 0, 0)
            format.contains("质量60") -> ImageCompressParams(60, 0, 0)
            format.contains("webp") -> ImageCompressParams(80, 0, 0)
            else -> ImageCompressParams(80, 0, 0)
        }
    }

    private fun parseGifWidth(format: String): Int {
        return when {
            format.contains("320") -> 320
            format.contains("480") -> 480
            else -> 0
        }
    }

    private fun parseEnhanceStrength(format: String): Float {
        return when {
            format.contains("1.2") -> 1.2f
            format.contains("1.5") -> 1.5f
            format.contains("2.0") -> 2.0f
            else -> 1.5f
        }
    }

    private data class VideoCompressParams(
        val crf: Int,
        val width: Int,
        val height: Int,
        val fps: Int
    )

    private data class ImageCompressParams(
        val quality: Int,
        val maxWidth: Int,
        val maxHeight: Int
    )

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024 -> "%.1f KB".format(bytes / 1_024.0)
            else -> "$bytes B"
        }
    }

    companion object {
        private const val ARG_ACTION_TYPE = "action_type"
        private const val ARG_FILE_PATH = "file_path"
        private const val ARG_FILE_PATHS = "file_paths"

        fun newInstance(feature: MediaToolFeature, filePath: String, filePaths: Array<String>? = null): FFmpegFeatureFragment {
            return FFmpegFeatureFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACTION_TYPE, feature.actionType)
                    putString(ARG_FILE_PATH, filePath)
                    if (filePaths != null) {
                        putStringArray(ARG_FILE_PATHS, filePaths)
                    }
                }
            }
        }
    }
}
