/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.ffmpegtools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.advancefilemanager.R
import com.advancefilemanager.databinding.FragmentFfmpegFeatureBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class FFmpegFeatureFragment : Fragment() {

    private lateinit var binding: FragmentFfmpegFeatureBinding
    private lateinit var feature: MediaToolFeature
    private lateinit var filePath: String
    private var filePaths: Array<String>? = null
    private var maxDurationMs: Long = 300_000L
    private var progressReceiver: BroadcastReceiver? = null
    private var outputFilePath: String = ""
    private var isImageFile: Boolean = false
    private var startTimeMs: Long = 0L
    private var endTimeMs: Long = 5000L
    private var skipChipListeners = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFfmpegFeatureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val actionType = requireArguments().getString(ARG_ACTION_TYPE) ?: run { activity?.finish(); return }
        filePath = requireArguments().getString(ARG_FILE_PATH) ?: run { activity?.finish(); return }
        filePaths = requireArguments().getStringArray(ARG_FILE_PATHS)
        feature = MediaToolFeature.entries.find { it.actionType == actionType } ?: run { activity?.finish(); return }
        setupFileInfo()
        setupOutputOptions()
        setupTimeRange()
        setupActionButton()
    }

    override fun onDestroyView() { super.onDestroyView(); unregisterProgressReceiver() }
    override fun onResume() { super.onResume(); registerProgressReceiver() }
    override fun onPause() { super.onPause(); unregisterProgressReceiver() }

    private fun registerProgressReceiver() {
        if (progressReceiver != null) return
        progressReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (!isAdded) return
                when (intent?.action) {
                    ACTION_PROGRESS_UPDATE -> {
                        binding.progressBar.progress = intent.getIntExtra(EXTRA_PERCENT, 0)
                        binding.progressText.text = getString(R.string.processing_percent, intent.getIntExtra(EXTRA_PERCENT, 0))
                    }
                    ACTION_PROCESSING_COMPLETE -> {
                        onProcessingComplete(intent.getBooleanExtra(EXTRA_SUCCESS, false), intent.getStringExtra(EXTRA_ERROR) ?: "")
                    }
                }
            }
        }
        val filter = IntentFilter(ACTION_PROGRESS_UPDATE).apply { addAction(ACTION_PROCESSING_COMPLETE) }
        requireContext().registerReceiver(progressReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    private fun unregisterProgressReceiver() {
        progressReceiver?.let { try { requireContext().unregisterReceiver(it) } catch (_: Exception) {} }
        progressReceiver = null
    }

    private fun onProcessingComplete(success: Boolean, error: String) {
        if (!isAdded) return
        activity?.runOnUiThread {
            if (!isAdded) return@runOnUiThread
            binding.actionButton.isEnabled = true
            if (success) {
                binding.progressBar.visibility = View.GONE
                val name = if (outputFilePath.isNotEmpty()) File(outputFilePath).name else getString(R.string.completed)
                binding.progressText.text = getString(R.string.completed_format, name)
                Toast.makeText(requireContext(), getString(R.string.processing_completed_format, name), Toast.LENGTH_LONG).show()
            } else {
                val rawError = error.ifEmpty { FFmpegJni.getLastError() }
                val localizedError = if (rawError == "Operation cancelled") getString(R.string.ffmpeg_processing_cancelled) else rawError
                binding.progressText.text = getString(R.string.error_format, localizedError)
            }
        }
    }

    private fun setupFileInfo() {
        val file = File(filePath)
        isImageFile = isImageFileType(file)
        val allPaths = filePaths
        if (allPaths != null && allPaths.size > 1) {
            binding.fileName.text = getString(R.string.selected_files_count, allPaths.size)
            binding.fileInfo.text = formatFileSize(allPaths.sumOf { File(it).length() })
        } else {
            binding.fileName.text = file.name
            binding.fileInfo.text = "${formatFileSize(file.length())} · ${file.extension.uppercase()}"
        }
    }

    private fun setupOutputOptions() {
        val formats = getOutputFormats()
        if (formats.isEmpty()) { binding.outputLabel.visibility = View.GONE; binding.outputFormatLayout.visibility = View.GONE }
        else {
            binding.outputFormat.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, formats))
            binding.outputFormat.setText(formats.firstOrNull() ?: "", false)
        }
        val showTimeRange = when (feature) { MediaToolFeature.GIF_MAKER -> !isImageFile; MediaToolFeature.MEDIA_TRIM, MediaToolFeature.VIDEO_SNAPSHOT -> true; else -> false }
        binding.timeRangeCard.visibility = if (showTimeRange) View.VISIBLE else View.GONE
    }

    private fun setupTimeRange() {
        try {
            val mi = MediaInfo()
            FFmpegJni.getMediaInfo(filePath, mi)
            if (mi.durationMs > 0) maxDurationMs = mi.durationMs else if (mi.duration > 0) maxDurationMs = mi.duration
        } catch (_: Exception) {}

        // Add listeners
        binding.preset5s.setOnCheckedChangeListener { _, c -> if (c) selectPreset(5) }
        binding.preset10s.setOnCheckedChangeListener { _, c -> if (c) selectPreset(10) }
        binding.preset30s.setOnCheckedChangeListener { _, c -> if (c) selectPreset(30) }
        binding.presetAll.setOnCheckedChangeListener { _, c -> if (c) selectPresetAll() }
        binding.presetCustom.setOnCheckedChangeListener { _, c -> if (c) showCustomTimeDialog() }

        // Set default to 5s
        selectPreset(5)
    }

    private fun selectPreset(seconds: Int) { startTimeMs = 0L; endTimeMs = minOf(seconds * 1000L, maxDurationMs); updateRangeDisplay() }
    private fun selectPresetAll() { startTimeMs = 0L; endTimeMs = maxDurationMs; updateRangeDisplay() }
    private fun updateRangeDisplay() {
        binding.timeRangeLabel.text = "${formatTimeFull(startTimeMs)} — ${formatTimeFull(endTimeMs)}"
        val d = ((endTimeMs - startTimeMs) / 1000).toInt()
        binding.durationLabel.text = if (d < 60) getString(R.string.duration_seconds, d) else getString(R.string.duration_minutes_seconds, d / 60, d % 60)
    }

    private fun showCustomTimeDialog() {
        val totalSec = (maxDurationMs / 1000).toInt().coerceAtLeast(1)
        val maxH = (totalSec / 3600).coerceAtMost(23)
        val startSec = (startTimeMs / 1000).toInt(); val endSec = (endTimeMs / 1000).toInt()
        val dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_time_range, null)
        val shp = dv.findViewById<NumberPicker>(R.id.startHourPicker); val smp = dv.findViewById<NumberPicker>(R.id.startMinPicker); val ssp = dv.findViewById<NumberPicker>(R.id.startSecPicker)
        val ehp = dv.findViewById<NumberPicker>(R.id.endHourPicker); val emp = dv.findViewById<NumberPicker>(R.id.endMinPicker); val esp = dv.findViewById<NumberPicker>(R.id.endSecPicker)
        val dl = dv.findViewById<android.widget.TextView>(R.id.dialogDurationLabel)
        dv.findViewById<android.widget.TextView>(R.id.dialogMaxDuration).text = getString(R.string.total_duration, formatTimeFull(maxDurationMs))

        setupPicker(shp, 0, maxH, startSec / 3600); setupPicker(smp, 0, 59, (startSec % 3600) / 60); setupPicker(ssp, 0, 59, startSec % 60)
        setupPicker(ehp, 0, maxH, endSec / 3600); setupPicker(emp, 0, 59, (endSec % 3600) / 60); setupPicker(esp, 0, 59, endSec % 60)

        val upd = {
            val s = shp.value * 3600 + smp.value * 60 + ssp.value; val e = ehp.value * 3600 + emp.value * 60 + esp.value; val d2 = (e - s).coerceAtLeast(0)
            dl.text = getString(R.string.selected_duration, if (d2 < 60) getString(R.string.duration_seconds, d2) else getString(R.string.duration_minutes_seconds, d2 / 60, d2 % 60))
        }
        shp.setOnValueChangedListener { _, _, _ -> upd() }; smp.setOnValueChangedListener { _, _, _ -> upd() }; ssp.setOnValueChangedListener { _, _, _ -> upd() }
        ehp.setOnValueChangedListener { _, _, _ -> upd() }; emp.setOnValueChangedListener { _, _, _ -> upd() }; esp.setOnValueChangedListener { _, _, _ -> upd() }
        upd()

        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.custom_time_range).setView(dv)
            .setPositiveButton(R.string.ok) { _, _ ->
                startTimeMs = (shp.value * 3600L + smp.value * 60L + ssp.value) * 1000
                endTimeMs = (ehp.value * 3600L + emp.value * 60L + esp.value) * 1000
                if (endTimeMs <= startTimeMs) endTimeMs = minOf(startTimeMs + 1000, maxDurationMs)
                skipChipListeners = true; binding.presetCustom.isChecked = true; skipChipListeners = false
                updateRangeDisplay()
            }.setNegativeButton(R.string.cancel, null).show()
    }

    private fun setupPicker(p: NumberPicker, min: Int, max: Int, v: Int) { p.minValue = min; p.maxValue = max; p.value = v; p.wrapSelectorWheel = false }

    private fun getOutputFormats(): List<String> = when (feature) {
        MediaToolFeature.FORMAT_CONVERT -> listOf("mp4", "mkv", "avi", "mov", "webm", "flv", "mp3", "aac", "wav", "flac", "ogg")
        MediaToolFeature.IMAGE_COMPRESS -> listOf(getString(R.string.ffmpeg_output_format_jpg_quality_80), getString(R.string.ffmpeg_output_format_jpg_quality_60), "png", "webp")
        MediaToolFeature.VIDEO_COMPRESS -> listOf(getString(R.string.ffmpeg_output_format_low_quality), getString(R.string.ffmpeg_output_format_medium_quality), getString(R.string.ffmpeg_output_format_high_quality))
        MediaToolFeature.EXTRACT_AUDIO -> listOf("mp3", "aac", "wav", "flac", "ogg")
        MediaToolFeature.GIF_MAKER -> listOf("320px", "480px", getString(R.string.ffmpeg_output_format_original_size))
        MediaToolFeature.VIDEO_TO_AUDIO -> listOf("mp3", "aac", "wav", "flac", "ogg")
        MediaToolFeature.VIDEO_ENHANCE -> listOf(getString(R.string.ffmpeg_enhance_light), getString(R.string.ffmpeg_enhance_standard), getString(R.string.ffmpeg_enhance_strong))
        MediaToolFeature.IMAGE_ENHANCE -> listOf(getString(R.string.ffmpeg_enhance_light), getString(R.string.ffmpeg_enhance_standard), getString(R.string.ffmpeg_enhance_strong))
        else -> emptyList()
    }

    private fun setupActionButton() { binding.actionButton.text = getString(feature.titleRes); binding.actionButton.setOnClickListener { startProcessing() } }

    private fun startProcessing() {
        binding.actionButton.isEnabled = false; binding.progressBar.visibility = View.VISIBLE; binding.progressText.visibility = View.VISIBLE; binding.progressBar.progress = 0; binding.progressText.text = getString(R.string.processing)
        val of = binding.outputFormat.text.toString(); val ip = File(filePath); val op = generateOutputFile(ip, of); outputFilePath = op.absolutePath
        FFmpegProcessingService.startProcessing(requireContext(), ip.absolutePath, op.absolutePath, feature.actionType, if (isImageFile) 0L else startTimeMs, if (isImageFile) 0L else endTimeMs, parseGifWidth(of), 10)
    }

    private fun generateOutputFile(inputFile: File, format: String): File {
        val bn = inputFile.nameWithoutExtension; val ext = when (feature) {
            MediaToolFeature.FORMAT_CONVERT, MediaToolFeature.IMAGE_COMPRESS, MediaToolFeature.VIDEO_TO_AUDIO -> format.split(" ").first()
            MediaToolFeature.VIDEO_COMPRESS, MediaToolFeature.VIDEO_ENHANCE, MediaToolFeature.IMAGE_ENHANCE, MediaToolFeature.VIDEO_MERGE, MediaToolFeature.VIDEO_WATERMARK, MediaToolFeature.VIDEO_ROTATE -> inputFile.extension
            MediaToolFeature.EXTRACT_AUDIO -> format.split(" ").first()
            MediaToolFeature.GIF_MAKER -> "gif"; MediaToolFeature.VIDEO_SNAPSHOT -> "jpg"
            MediaToolFeature.SUBTITLE_EXTRACT -> "srt"; MediaToolFeature.AUDIO_NORMALIZE -> inputFile.extension; MediaToolFeature.IMAGE_STITCH -> "jpg"; else -> inputFile.extension
        }
        val dir = inputFile.parentFile ?: return File("${inputFile.nameWithoutExtension}_output.$ext")
        var o = File(dir, "${bn}_output.$ext"); var c = 1; while (o.exists()) { o = File(dir, "${bn}_output_${c}.$ext"); c++ }; return o
    }

    private fun parseGifWidth(f: String): Int = when { f.contains("320") -> 320; f.contains("480") -> 480; else -> 0 }
    private fun formatFileSize(b: Long): String = when { b >= 1_073_741_824 -> "%.1f GB".format(b / 1_073_741_824.0); b >= 1_048_576 -> "%.1f MB".format(b / 1_048_576.0); b >= 1_024 -> "%.1f KB".format(b / 1_024.0); else -> "$b B" }
    private fun formatTimeFull(ms: Long): String { val t = (ms / 1000).toInt(); return "%d:%02d:%02d".format(t / 3600, (t % 3600) / 60, t % 60) }
    private fun isImageFileType(f: File): Boolean = f.extension.lowercase() in setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff", "tif", "heic", "heif")

    companion object {
        private const val ARG_ACTION_TYPE = "action_type"; private const val ARG_FILE_PATH = "file_path"; private const val ARG_FILE_PATHS = "file_paths"
        const val ACTION_PROGRESS_UPDATE = "com.advancefilemanager.FFMPEG_PROGRESS"
        const val ACTION_PROCESSING_COMPLETE = "com.advancefilemanager.FFMPEG_COMPLETE"
        const val EXTRA_PERCENT = "percent"; const val EXTRA_SUCCESS = "success"; const val EXTRA_ERROR = "error"
        fun newInstance(feature: MediaToolFeature, filePath: String, filePaths: Array<String>? = null) = FFmpegFeatureFragment().apply { arguments = Bundle().apply { putString(ARG_ACTION_TYPE, feature.actionType); putString(ARG_FILE_PATH, filePath); if (filePaths != null) putStringArray(ARG_FILE_PATHS, filePaths) } }
    }
}