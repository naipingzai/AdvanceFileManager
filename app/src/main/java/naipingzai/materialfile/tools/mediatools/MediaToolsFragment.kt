/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.mediatools

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.MediaToolsFragmentBinding
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.filelist.FileListActivity
import naipingzai.materialfile.provider.linux.media.MediaScanner
import naipingzai.materialfile.tools.FileTypeUtils
import naipingzai.materialfile.tools.OutputPaths
import naipingzai.materialfile.tools.formatconvert.FFmpegJni
import naipingzai.materialfile.tools.formatconvert.FormatConvertFragment
import naipingzai.materialfile.tools.formatconvert.MediaInfo
import naipingzai.materialfile.app.ToolHostActivity
import naipingzai.materialfile.util.FormatUtils
import java.io.File

private const val TAG = "MediaToolsMerge"
private const val GIF_DEFAULT_WIDTH = 320
private const val GIF_DEFAULT_FPS = 15
private const val VIDEO_COMPRESS_BITRATE_KBPS = 1500
private const val IMAGE_COMPRESS_QUALITY = 80
private const val IMAGE_COMPRESS_MAX_DIM = 1920
private const val IMAGE_ENHANCE_STRENGTH = 1.5f
private const val VIDEO_ENHANCE_STRENGTH = 1.5f

class MediaToolsFragment : Fragment() {
    private lateinit var binding: MediaToolsFragmentBinding
    private lateinit var adapter: MediaToolCardAdapter
    private val features = MediaToolFeature.entries.toList()
    private var operationJob: Job? = null
    private var ffmpegAvailable = false
    private var operationBackCallback: OnBackPressedCallback? = null

    companion object {
        const val EXTRA_FILE_PATH = "naipingzai.materialfile.extra.MEDIA_FILE_PATH"
        const val EXTRA_FILE_PATHS = "naipingzai.materialfile.extra.MEDIA_FILE_PATHS"
        const val EXTRA_ACTION = "naipingzai.materialfile.extra.MEDIA_ACTION"
        const val ACTION_EXTRACT_AUDIO = "extract_audio"
        const val ACTION_TRIM = "trim"
        const val ACTION_VIDEO_COMPRESS = "video_compress"
        const val ACTION_VIDEO_SNAPSHOT = "video_snapshot"
        const val ACTION_GIF_MAKE = "gif_make"
        const val ACTION_IMAGE_COMPRESS = "image_compress"
        const val ACTION_IMAGE_ENHANCE = "image_enhance"
        const val ACTION_VIDEO_ENHANCE = "video_enhance"
    }

    private var preselectedFiles: List<File> = emptyList()

    private val extractAudioLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths ->
        if (paths.isEmpty()) return@registerForActivityResult
        val files = paths.map { it.toFile() }
        validateFilesAndRun(files, requireAudio = true) { validFiles ->
            doExtractAudio(validFiles)
        }
    }

    private val mediaTrimLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths ->
        if (paths.isEmpty()) return@registerForActivityResult
        val file = paths.first().toFile()
        validateFilesAndRun(listOf(file)) { validFiles ->
            ensureCompatible(validFiles.first(), MediaToolFeature.MEDIA_TRIM) { readyFile, tempDir ->
                showTrimDialog(readyFile, tempDir)
            }
        }
    }

    private val videoCompressLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths ->
        if (paths.isEmpty()) return@registerForActivityResult
        val files = paths.map { it.toFile() }
        validateFilesAndRun(files, requireVideo = true) { validFiles ->
            ensureAllCompatible(validFiles, MediaToolFeature.VIDEO_COMPRESS) { readyFiles, tempDir ->
                doVideoCompress(readyFiles, tempDir)
            }
        }
    }

    private val videoSnapshotLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths ->
        if (paths.isEmpty()) return@registerForActivityResult
        val file = paths.first().toFile()
        validateFilesAndRun(listOf(file), requireVideo = true) { validFiles ->
            ensureCompatible(validFiles.first(), MediaToolFeature.VIDEO_SNAPSHOT) { readyFile, tempDir ->
                showSnapshotDialog(readyFile, tempDir)
            }
        }
    }

    private val gifMakerLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths ->
        if (paths.isEmpty()) return@registerForActivityResult
        val file = paths.first().toFile()
        validateFilesAndRun(listOf(file), requireVideo = true) { validFiles ->
            ensureCompatible(validFiles.first(), MediaToolFeature.GIF_MAKER) { readyFile, tempDir ->
                showGifDialog(readyFile, tempDir)
            }
        }
    }

    private val mediaMergeLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths ->
        if (paths.size < 2) {
            Snackbar.make(binding.root, R.string.media_tool_merge_need_multiple, Snackbar.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        val files = paths.map { it.toFile() }
        validateMergeAndRun(files)
    }

    private val mediaInfoLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths ->
        if (paths.isEmpty()) return@registerForActivityResult
        showMediaInfo(paths.first().toFile())
    }

    private val imageCompressLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths ->
        if (paths.isEmpty()) return@registerForActivityResult
        val files = paths.map { it.toFile() }.filter { FileTypeUtils.isImageFile(it.name) }
        if (files.isEmpty()) {
            Snackbar.make(binding.root, R.string.image_compress_not_image, Snackbar.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        doImageCompress(files)
    }

    private val imageEnhanceLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths ->
        if (paths.isEmpty()) return@registerForActivityResult
        val files = paths.map { it.toFile() }.filter { FileTypeUtils.isImageFile(it.name) }
        if (files.isEmpty()) {
            Snackbar.make(binding.root, R.string.image_compress_not_image, Snackbar.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        doImageEnhance(files)
    }

    private val videoEnhanceLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths ->
        if (paths.isEmpty()) return@registerForActivityResult
        val files = paths.map { it.toFile() }
        validateFilesAndRun(files, requireVideo = true) { validFiles ->
            doVideoEnhance(validFiles)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = MediaToolsFragmentBinding.inflate(inflater, container, false)
        .also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        try {
            val version = FFmpegJni.getVersion()
            binding.ffmpegVersionText.text = version
            ffmpegAvailable = true
        } catch (e: Exception) {
            binding.ffmpegVersionText.text = getString(R.string.media_tool_ffmpeg_unavailable)
            binding.ffmpegVersionText.setTextColor(
                com.google.android.material.color.MaterialColors.getColor(
                    binding.root, com.google.android.material.R.attr.colorError
                )
            )
            binding.ffmpegStatusDot.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    com.google.android.material.color.MaterialColors.getColor(
                        binding.root, com.google.android.material.R.attr.colorError
                    )
                )
            ffmpegAvailable = false
        }

        adapter = MediaToolCardAdapter(features) { feature -> onFeatureClick(feature) }
        binding.recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        // Read preselected files from arguments / intent extras
        val preselectedPathsArr = arguments?.getStringArray(EXTRA_FILE_PATHS)
            ?: activity?.intent?.getStringArrayExtra(EXTRA_FILE_PATHS)
        if (!preselectedPathsArr.isNullOrEmpty()) {
            preselectedFiles = preselectedPathsArr.map { File(it) }.filter { it.exists() }
            // Show banner with preselected files count
            binding.ffmpegVersionText.append(
                "  \u00b7  " + getString(R.string.media_tool_preselected_files, preselectedFiles.size)
            )
        }

        // Back press confirms exit during operation
        operationBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.media_tools_title)
                    .setMessage(R.string.media_tool_exit_confirm)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        operationJob?.cancel()
                        g_cancel = true
                        FFmpegJni.cancel()
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, operationBackCallback!!
        )

        // If opened from file context menu with specific action
        val actionFilePath = arguments?.getString(EXTRA_FILE_PATH)
        val action = arguments?.getString(EXTRA_ACTION)
        if (actionFilePath != null && action != null && ffmpegAvailable) {
            // Hide the tools list, only show operation UI
            binding.recyclerView.visibility = View.GONE
            binding.ffmpegVersionText.visibility = View.GONE
            binding.ffmpegStatusDot.visibility = View.GONE
            val file = File(actionFilePath)
            when (action) {
                ACTION_EXTRACT_AUDIO -> {
                    validateFilesAndRun(listOf(file), requireAudio = true) { validFiles ->
                        doExtractAudio(validFiles)
                    }
                }
                ACTION_TRIM -> {
                    validateFilesAndRun(listOf(file)) { validFiles ->
                        ensureCompatible(validFiles.first(), MediaToolFeature.MEDIA_TRIM) { readyFile, tempDir ->
                            showTrimDialog(readyFile, tempDir)
                        }
                    }
                }
                ACTION_VIDEO_COMPRESS -> {
                    validateFilesAndRun(listOf(file), requireVideo = true) { validFiles ->
                        ensureAllCompatible(validFiles, MediaToolFeature.VIDEO_COMPRESS) { readyFiles, tempDir ->
                            doVideoCompress(readyFiles, tempDir)
                        }
                    }
                }
                ACTION_VIDEO_SNAPSHOT -> {
                    validateFilesAndRun(listOf(file), requireVideo = true) { validFiles ->
                        ensureCompatible(validFiles.first(), MediaToolFeature.VIDEO_SNAPSHOT) { readyFile, tempDir ->
                            showSnapshotDialog(readyFile, tempDir)
                        }
                    }
                }
                ACTION_GIF_MAKE -> {
                    validateFilesAndRun(listOf(file), requireVideo = true) { validFiles ->
                        ensureCompatible(validFiles.first(), MediaToolFeature.GIF_MAKER) { readyFile, tempDir ->
                            showGifDialog(readyFile, tempDir)
                        }
                    }
                }
                ACTION_IMAGE_COMPRESS -> doImageCompress(listOf(file))
                ACTION_IMAGE_ENHANCE -> doImageEnhance(listOf(file))
                ACTION_VIDEO_ENHANCE -> {
                    validateFilesAndRun(listOf(file), requireVideo = true) { validFiles ->
                        doVideoEnhance(validFiles)
                    }
                }
            }
        }
    }

    private fun onFeatureClick(feature: MediaToolFeature) {
        if (!ffmpegAvailable) {
            showFFmpegRequiredDialog()
            return
        }
        // If we have preselected files, run the action directly without launcher
        if (preselectedFiles.isNotEmpty()) {
            runFeatureWithFiles(feature, preselectedFiles)
            return
        }
        when (feature) {
            MediaToolFeature.FORMAT_CONVERT -> {
                startActivity(ToolHostActivity.createIntent<FormatConvertFragment>(R.string.format_convert_title))
            }
            MediaToolFeature.IMAGE_COMPRESS -> {
                imageCompressLauncher.launch(listOf(MimeType.IMAGE_ANY))
            }
            MediaToolFeature.IMAGE_ENHANCE -> {
                imageEnhanceLauncher.launch(listOf(MimeType.IMAGE_ANY))
            }
            MediaToolFeature.VIDEO_COMPRESS -> {
                videoCompressLauncher.launch(listOf(MimeType.VIDEO_ANY))
            }
            MediaToolFeature.VIDEO_ENHANCE -> {
                videoEnhanceLauncher.launch(listOf(MimeType.VIDEO_ANY))
            }
            MediaToolFeature.EXTRACT_AUDIO -> {
                extractAudioLauncher.launch(listOf(MimeType.VIDEO_ANY))
            }
            MediaToolFeature.MEDIA_TRIM -> {
                mediaTrimLauncher.launch(listOf(MimeType.VIDEO_ANY, MimeType.AUDIO_ANY))
            }
            MediaToolFeature.VIDEO_SNAPSHOT -> {
                videoSnapshotLauncher.launch(listOf(MimeType.VIDEO_ANY))
            }
            MediaToolFeature.GIF_MAKER -> {
                gifMakerLauncher.launch(listOf(MimeType.VIDEO_ANY))
            }
            MediaToolFeature.MEDIA_MERGE -> {
                mediaMergeLauncher.launch(listOf(MimeType.VIDEO_ANY, MimeType.AUDIO_ANY))
            }
            MediaToolFeature.MEDIA_INFO -> {
                mediaInfoLauncher.launch(listOf(
                    MimeType.VIDEO_ANY, MimeType.AUDIO_ANY, MimeType.IMAGE_ANY
                ))
            }
        }
    }

    private fun runFeatureWithFiles(feature: MediaToolFeature, files: List<File>) {
        when (feature) {
            MediaToolFeature.FORMAT_CONVERT -> {
                val intent = ToolHostActivity.createIntent<FormatConvertFragment>(R.string.format_convert_title).apply {
                    putExtra(FormatConvertFragment.EXTRA_FILE_PATHS, files.map { it.absolutePath }.toTypedArray())
                }
                startActivity(intent)
            }
            MediaToolFeature.IMAGE_COMPRESS -> {
                val imgs = files.filter { FileTypeUtils.isImageFile(it.name) }
                if (imgs.isEmpty()) {
                    Snackbar.make(binding.root, R.string.image_compress_not_image, Snackbar.LENGTH_SHORT).show(); return
                }
                doImageCompress(imgs)
            }
            MediaToolFeature.IMAGE_ENHANCE -> {
                val imgs = files.filter { FileTypeUtils.isImageFile(it.name) }
                if (imgs.isEmpty()) {
                    Snackbar.make(binding.root, R.string.image_compress_not_image, Snackbar.LENGTH_SHORT).show(); return
                }
                doImageEnhance(imgs)
            }
            MediaToolFeature.VIDEO_COMPRESS -> {
                validateFilesAndRun(files, requireVideo = true) { validFiles ->
                    ensureAllCompatible(validFiles, MediaToolFeature.VIDEO_COMPRESS) { readyFiles, tempDir ->
                        doVideoCompress(readyFiles, tempDir)
                    }
                }
            }
            MediaToolFeature.VIDEO_ENHANCE -> {
                validateFilesAndRun(files, requireVideo = true) { validFiles ->
                    doVideoEnhance(validFiles)
                }
            }
            MediaToolFeature.EXTRACT_AUDIO -> {
                validateFilesAndRun(files, requireAudio = true) { validFiles ->
                    doExtractAudio(validFiles)
                }
            }
            MediaToolFeature.MEDIA_TRIM -> {
                validateFilesAndRun(listOf(files.first())) { validFiles ->
                    ensureCompatible(validFiles.first(), MediaToolFeature.MEDIA_TRIM) { readyFile, tempDir ->
                        showTrimDialog(readyFile, tempDir)
                    }
                }
            }
            MediaToolFeature.VIDEO_SNAPSHOT -> {
                validateFilesAndRun(listOf(files.first()), requireVideo = true) { validFiles ->
                    ensureCompatible(validFiles.first(), MediaToolFeature.VIDEO_SNAPSHOT) { readyFile, tempDir ->
                        showSnapshotDialog(readyFile, tempDir)
                    }
                }
            }
            MediaToolFeature.GIF_MAKER -> {
                validateFilesAndRun(listOf(files.first()), requireVideo = true) { validFiles ->
                    ensureCompatible(validFiles.first(), MediaToolFeature.GIF_MAKER) { readyFile, tempDir ->
                        showGifDialog(readyFile, tempDir)
                    }
                }
            }
            MediaToolFeature.MEDIA_MERGE -> {
                if (files.size < 2) {
                    Snackbar.make(binding.root, R.string.media_tool_merge_need_multiple, Snackbar.LENGTH_SHORT).show()
                    return
                }
                validateMergeAndRun(files)
            }
            MediaToolFeature.MEDIA_INFO -> showMediaInfo(files.first())
        }
    }

    // ======== Exclusive operation guard ========
    private fun launchExclusiveOperation(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) {
        if (operationJob?.isActive == true) {
            Snackbar.make(binding.root, R.string.media_tool_operation_running, Snackbar.LENGTH_SHORT).show()
            return
        }
        g_cancel = false
        operationBackCallback?.isEnabled = true
        operationJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                block()
            } finally {
                operationBackCallback?.isEnabled = false
            }
        }
    }

    // ======== FFmpeg availability dialog ========
    private fun showFFmpegRequiredDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.media_tool_ffmpeg_required_title)
            .setMessage(R.string.media_tool_ffmpeg_required_message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    // ======== Prerequisite dialog ========
    private fun showPrerequisiteDialog(messageRes: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.media_tool_prerequisite_title)
            .setMessage(messageRes)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showPrerequisiteDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.media_tool_prerequisite_title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    // ======== Probe file and get MediaInfo ========
    private suspend fun probeFile(file: File): MediaInfo? = withContext(Dispatchers.IO) {
        try {
            val mi = MediaInfo()
            FFmpegJni.getMediaInfo(file.absolutePath, mi)
            mi
        } catch (e: Exception) { null }
    }

    // ======== Validate files then run operation ========
    private fun validateFilesAndRun(
        files: List<File>,
        requireVideo: Boolean = false,
        requireAudio: Boolean = false,
        onValid: (List<File>) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val validFiles = mutableListOf<File>()
            val issues = mutableListOf<String>()

            for (file in files) {
                val info = probeFile(file)
                if (info == null) {
                    issues.add("${file.name}: ${getString(R.string.media_tool_probe_failed)}")
                    continue
                }
                if (requireVideo && !info.hasVideo) {
                    issues.add("${file.name}: ${getString(R.string.media_tool_no_video_track)}")
                    continue
                }
                if (requireAudio && !info.hasAudio) {
                    issues.add("${file.name}: ${getString(R.string.media_tool_extract_audio_requires)}")
                    continue
                }
                validFiles.add(file)
            }

            if (validFiles.isEmpty()) {
                val msg = if (issues.size == 1) issues.first()
                else if (requireVideo) getString(R.string.media_tool_video_requires)
                else if (requireAudio) getString(R.string.media_tool_no_audio_track)
                else getString(R.string.media_tool_probe_failed)
                showPrerequisiteDialog(msg)
                return@launch
            }

            if (issues.isNotEmpty() && validFiles.size < files.size) {
                val skippedMsg = getString(
                    R.string.media_tool_extract_audio_result,
                    validFiles.size, issues.size
                )
                Snackbar.make(binding.root, skippedMsg, Snackbar.LENGTH_LONG).show()
            }

            onValid(validFiles)
        }
    }

    // ======== Validate merge compatibility ========
    private fun validateMergeAndRun(files: List<File>) {
        viewLifecycleOwner.lifecycleScope.launch {
            val infos = mutableListOf<Pair<File, MediaInfo>>()
            val failedFiles = mutableListOf<String>()

            for (file in files) {
                val info = probeFile(file)
                if (info == null) {
                    failedFiles.add(file.name)
                } else {
                    infos.add(file to info)
                }
            }

            if (infos.size < 2) {
                val msg = if (failedFiles.isNotEmpty()) {
                    getString(R.string.media_tool_probe_failed) + "\n" +
                        failedFiles.joinToString(", ")
                } else getString(R.string.media_tool_merge_need_multiple)
                showPrerequisiteDialog(msg)
                return@launch
            }

            val hasAnyVideo = infos.any { it.second.hasVideo }
            val mixedVideoAudio = hasAnyVideo && infos.any { !it.second.hasVideo }

            // Check codec / resolution compatibility
            val first = infos.first().second
            val codecMismatch = infos.drop(1).any { (_, info) ->
                (first.hasVideo && info.hasVideo &&
                    (first.videoCodec != info.videoCodec ||
                     first.width != info.width ||
                     first.height != info.height)) ||
                (first.hasAudio && info.hasAudio &&
                    first.audioCodec != info.audioCodec)
            }

            // Also detect stream-type mismatch (video vs audio-only)
            val hasStructureMismatch = mixedVideoAudio

            val validFiles = infos.map { it.first }
            if (codecMismatch || hasStructureMismatch) {
                val analysisMsg = buildMergeAnalysisMessage(infos)
                val issueDesc = buildString {
                    append(analysisMsg)
                    appendLine("\n")
                    if (hasStructureMismatch) {
                        appendLine(getString(R.string.media_tool_merge_structure_mismatch))
                    }
                    if (codecMismatch) {
                        appendLine(getString(R.string.media_tool_merge_codec_mismatch))
                    }
                    appendLine(getString(R.string.media_tool_merge_incompatible_hint))
                }

                val dialogView = layoutInflater.inflate(R.layout.dialog_merge_analysis, null)
                dialogView.findViewById<android.widget.TextView>(R.id.analysisContent).text = issueDesc

                val dialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.media_tool_merge_analysis_title)
                    .setView(dialogView)
                    .create()

                dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClose)
                    .setOnClickListener { dialog.dismiss() }

                dialog.show()
            } else {
                doMediaMerge(validFiles)
            }
        }
    }

    /** Build human-readable analysis of all files' media properties */
    private fun buildMergeAnalysisMessage(infos: List<Pair<File, MediaInfo>>): String =
        buildString {
            for ((i, pair) in infos.withIndex()) {
                val (file, info) = pair
                appendLine("${i + 1}. ${file.name}")
                if (info.hasVideo) {
                    append("   ${getString(R.string.media_tool_info_video)}: ${info.videoCodec}")
                    append(" ${info.width}×${info.height}")
                    if (info.videoBitrate > 0) append(" ${info.videoBitrate}kbps")
                    appendLine()
                }
                if (info.hasAudio) {
                    append("   ${getString(R.string.media_tool_info_audio)}: ${info.audioCodec}")
                    append(" ${info.sampleRate}Hz")
                    if (info.channels > 0) append(" ${info.channels}ch")
                    appendLine()
                }
                if (!info.hasVideo && !info.hasAudio) {
                    appendLine("   ⚠ ${getString(R.string.media_tool_merge_no_av_stream)}")
                }
                if (info.durationMs > 0) {
                    appendLine("   ${getString(R.string.media_tool_info_duration)}: ${info.formatDuration()}")
                }
            }
        }.trimEnd()

    @Volatile
    private var g_cancel = false
    @Volatile
    private var lastOpProgressTime = 0L

    private fun showOperationProgress(text: String, percent: Int = -1) {
        try {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                binding.operationStatusText.text = text
                binding.operationStatusText.isVisible = true
                binding.operationProgressBar.isVisible = true
                if (percent < 0) {
                    binding.operationProgressBar.isIndeterminate = true
                } else {
                    binding.operationProgressBar.isIndeterminate = false
                    binding.operationProgressBar.progress = percent
                }
            }
        } catch (_: IllegalStateException) {}
    }

    private fun hideOperationProgress() {
        try {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                binding.operationProgressBar.isVisible = false
                binding.operationStatusText.isVisible = false
            }
        } catch (_: IllegalStateException) {}
    }

    private fun makeProgressCallback(label: String): FFmpegJni.ProgressCallback {
        lastOpProgressTime = 0L
        return object : FFmpegJni.ProgressCallback {
            override fun onProgress(percent: Int) {
                val now = System.currentTimeMillis()
                if (percent != 100 && now - lastOpProgressTime < 200) return
                lastOpProgressTime = now
                showOperationProgress("$label $percent%", percent)
            }
        }
    }

    // ======== Format compatibility helpers ========
    /** Common video codecs that FFmpeg can decode reliably */
    private fun isCommonVideoCodec(codec: String): Boolean {
        val c = codec.lowercase()
        return c.contains("h264") || c.contains("avc") ||
               c.contains("hevc") || c.contains("h265") ||
               c.contains("mpeg4") || c.contains("mpeg2") || c.contains("mpeg1") ||
               c.contains("vp8") || c.contains("vp9") || c.contains("av1") ||
               c.contains("wmv") || c.contains("theora") || c.contains("mjpeg") ||
               c.contains("msmpeg4") || c.contains("flv1") || c.contains("rv")
    }

    /** Audio codecs that can be stream-copied into MP4 container */
    private fun isMp4CompatibleAudio(codec: String): Boolean {
        val c = codec.lowercase()
        return c.contains("aac") || c.contains("mp3") || c.contains("mp2") ||
               c.contains("ac3") || c.contains("eac3") || c.contains("alac") ||
               c.contains("pcm")
    }

    /** Common audio codecs (broader, for trim which keeps same container) */
    private fun isCommonAudioCodec(codec: String): Boolean =
        isMp4CompatibleAudio(codec) || codec.lowercase().let {
            it.contains("opus") || it.contains("vorbis") || it.contains("flac") ||
            it.contains("wmav") || it.contains("amr") || it.contains("dts") ||
            it.contains("cook") || it.contains("speex")
        }

    /** Check if file format is directly supported for the given tool */
    private fun isFormatDirectlySupported(info: MediaInfo, feature: MediaToolFeature): Boolean =
        when (feature) {
            MediaToolFeature.GIF_MAKER, MediaToolFeature.VIDEO_SNAPSHOT -> {
                !info.hasVideo || isCommonVideoCodec(info.videoCodec ?: "")
            }
            MediaToolFeature.VIDEO_COMPRESS -> {
                val videoOk = !info.hasVideo || isCommonVideoCodec(info.videoCodec ?: "")
                val audioOk = !info.hasAudio || isMp4CompatibleAudio(info.audioCodec ?: "")
                videoOk && audioOk
            }
            MediaToolFeature.MEDIA_TRIM -> {
                val videoOk = !info.hasVideo || isCommonVideoCodec(info.videoCodec ?: "")
                val audioOk = !info.hasAudio || isCommonAudioCodec(info.audioCodec ?: "")
                videoOk && audioOk
            }
            else -> true
        }

    /** Build description of format issues for user display */
    private fun buildFormatIssueDetails(info: MediaInfo, feature: MediaToolFeature): String =
        buildString {
            if (info.hasVideo && !isCommonVideoCodec(info.videoCodec ?: "")) {
                appendLine(getString(R.string.media_tool_format_video_issue, info.videoCodec ?: "unknown"))
            }
            if (feature == MediaToolFeature.VIDEO_COMPRESS && info.hasAudio &&
                !isMp4CompatibleAudio(info.audioCodec ?: "")) {
                appendLine(getString(R.string.media_tool_format_audio_mp4_issue, info.audioCodec ?: "unknown"))
            }
            if (feature == MediaToolFeature.MEDIA_TRIM && info.hasAudio &&
                !isCommonAudioCodec(info.audioCodec ?: "")) {
                appendLine(getString(R.string.media_tool_format_audio_issue, info.audioCodec ?: "unknown"))
            }
        }.trimEnd()

    /**
     * Ensure file format is compatible with the given tool.
     * If not, show dialog and offer to auto-convert.
     * @param onReady called with (readyFile, tempDirToCleanup)
     */
    private fun ensureCompatible(
        file: File,
        feature: MediaToolFeature,
        onReady: (readyFile: File, tempDir: File?) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val info = probeFile(file)
            if (info == null) {
                showPrerequisiteDialog(R.string.media_tool_probe_failed)
                return@launch
            }

            if (isFormatDirectlySupported(info, feature)) {
                onReady(file, null)
            } else {
                val featureName = getString(feature.titleRes)
                val issues = buildFormatIssueDetails(info, feature)
                val message = getString(R.string.media_tool_format_incompatible_desc,
                    file.name, featureName, issues)

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.media_tool_format_incompatible_title)
                    .setMessage(message)
                    .setPositiveButton(R.string.media_tool_auto_convert_and_continue) { _, _ ->
                        doPreConvert(file, info, onReady)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
    }

    /**
     * Ensure all files are format-compatible with the given tool.
     * If some are not, show dialog and offer to auto-convert.
     */
    private fun ensureAllCompatible(
        files: List<File>,
        feature: MediaToolFeature,
        onReady: (readyFiles: List<File>, tempDir: File?) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val probed = files.mapNotNull { f -> probeFile(f)?.let { f to it } }
            if (probed.isEmpty()) {
                showPrerequisiteDialog(R.string.media_tool_probe_failed)
                return@launch
            }
            val incompatible = probed.filter { !isFormatDirectlySupported(it.second, feature) }

            if (incompatible.isEmpty()) {
                onReady(probed.map { it.first }, null)
            } else {
                val msg = buildString {
                    appendLine(getString(R.string.media_tool_format_multi_incompatible,
                        incompatible.size, probed.size))
                    for ((f, info) in incompatible) {
                        val codecs = listOfNotNull(info.videoCodec, info.audioCodec).joinToString(" / ")
                        appendLine("• ${f.name}: $codecs")
                    }
                    appendLine()
                    append(getString(R.string.media_tool_format_auto_convert_hint))
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.media_tool_format_incompatible_title)
                    .setMessage(msg)
                    .setPositiveButton(R.string.media_tool_auto_convert_and_continue) { _, _ ->
                        doPreConvertMultiple(probed, feature, onReady)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
    }

    /** Pre-convert a single file to H.264+AAC MP4 */
    private fun doPreConvert(
        file: File, info: MediaInfo,
        onComplete: (readyFile: File, tempDir: File?) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val tempDir = File(requireContext().cacheDir, "preconvert_${System.currentTimeMillis()}")
                .also { it.mkdirs() }
            val tempFile = File(tempDir, "${file.nameWithoutExtension}.mp4")

            Snackbar.make(binding.root, R.string.media_tool_format_converting, Snackbar.LENGTH_SHORT).show()

            val w = if (info.width > 0) info.width else 0
            val h = if (info.height > 0) info.height else 0
            val br = if (info.videoBitrate > 0) info.videoBitrate else 0

            val result = withContext(Dispatchers.IO) {
                try {
                    FFmpegJni.normalizeVideo(file.absolutePath, tempFile.absolutePath, w, h, br, null)
                } catch (e: Exception) { -1 }
            }

            if (result == 0) {
                onComplete(tempFile, tempDir)
            } else {
                tempDir.deleteRecursively()
                Snackbar.make(binding.root, R.string.media_tool_format_convert_failed, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    /** Pre-convert multiple files, only those that are incompatible */
    private fun doPreConvertMultiple(
        probed: List<Pair<File, MediaInfo>>,
        feature: MediaToolFeature,
        onComplete: (readyFiles: List<File>, tempDir: File?) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val tempDir = File(requireContext().cacheDir, "preconvert_${System.currentTimeMillis()}")
                .also { it.mkdirs() }
            val readyFiles = mutableListOf<File>()
            var allOk = true

            for ((index, pair) in probed.withIndex()) {
                val (f, info) = pair
                if (isFormatDirectlySupported(info, feature)) {
                    readyFiles.add(f)
                } else {
                    Snackbar.make(binding.root,
                        getString(R.string.media_tool_format_converting_file, f.name, index + 1, probed.size),
                        Snackbar.LENGTH_SHORT).show()

                    val tempFile = File(tempDir, "${index}_${f.nameWithoutExtension}.mp4")
                    val w = if (info.width > 0) info.width else 0
                    val h = if (info.height > 0) info.height else 0
                    val br = if (info.videoBitrate > 0) info.videoBitrate else 0

                    val result = withContext(Dispatchers.IO) {
                        try {
                            FFmpegJni.normalizeVideo(f.absolutePath, tempFile.absolutePath, w, h, br, null)
                        } catch (e: Exception) { -1 }
                    }

                    if (result == 0) {
                        readyFiles.add(tempFile)
                    } else {
                        allOk = false
                        Snackbar.make(binding.root,
                            getString(R.string.media_tool_merge_normalize_failed, f.name),
                            Snackbar.LENGTH_LONG).show()
                        break
                    }
                }
            }

            if (allOk && readyFiles.isNotEmpty()) {
                onComplete(readyFiles, tempDir)
            } else {
                tempDir.deleteRecursively()
            }
        }
    }

    // ======== Extract Audio ========
    private fun doExtractAudio(files: List<File>) {
        val outputDir = OutputPaths.resolve(OutputPaths.EXTRACTED_AUDIO).also { it.mkdirs() }

        launchExclusiveOperation {
            var success = 0
            var failed = 0
            showOperationProgress(getString(R.string.media_tool_extract_audio) + " (0/${files.size})")
            for ((i, file) in files.withIndex()) {
                val outputFile = FileTypeUtils.getUniqueFile(outputDir, file.nameWithoutExtension, "m4a")
                val label = "${getString(R.string.media_tool_extract_audio)} (${i + 1}/${files.size})"
                val result = withContext(Dispatchers.IO) {
                    try {
                        FFmpegJni.extractAudio(file.absolutePath, outputFile.absolutePath,
                            makeProgressCallback(label))
                    } catch (e: Exception) { -1 }
                }
                if (result == 0) success++ else {
                    failed++
                    outputFile.delete()
                }
                if (result == 0) MediaScanner.scan(outputFile)
            }
            hideOperationProgress()
            Snackbar.make(
                binding.root,
                getString(R.string.media_tool_extract_audio_result, success, failed),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    // ======== Probe duration helper ========
    private fun probeDurationAndFillEnd(
        file: File,
        endInput: com.google.android.material.textfield.TextInputEditText
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val info = withContext(Dispatchers.IO) {
                try {
                    val mi = MediaInfo()
                    FFmpegJni.getMediaInfo(file.absolutePath, mi)
                    mi
                } catch (e: Exception) { null }
            }
            if (info != null && info.durationMs > 0) {
                val durationStr = String.format("%.1f", info.durationMs / 1000.0)
                if (endInput.text.isNullOrEmpty()) {
                    endInput.setText(durationStr)
                }
            }
        }
    }

    // ======== Media Trim ========
    private fun showTrimDialog(file: File, tempDir: File? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_media_trim, null)
        val startInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.startTimeInput)
        val endInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.endTimeInput)

        probeDurationAndFillEnd(file, endInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.media_tool_media_trim)
            .setView(dialogView)
            .setPositiveButton(R.string.media_tool_start) { _, _ ->
                val startMs = ((startInput.text.toString().toDoubleOrNull() ?: 0.0) * 1000).toLong()
                val endMs = ((endInput.text.toString().toDoubleOrNull()
                    ?: return@setPositiveButton) * 1000).toLong()
                if (endMs <= startMs) {
                    Snackbar.make(binding.root, R.string.media_tool_invalid_time_range, Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                doTrim(file, startMs, endMs, tempDir)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> tempDir?.deleteRecursively() }
            .setOnCancelListener { tempDir?.deleteRecursively() }
            .show()
    }

    private fun doTrim(file: File, startMs: Long, endMs: Long, tempDir: File? = null) {
        val outputDir = OutputPaths.resolve(OutputPaths.TRIMMED).also { it.mkdirs() }
        val ext = file.extension.ifEmpty { "mp4" }
        val outputFile = FileTypeUtils.getUniqueFile(outputDir, "${file.nameWithoutExtension}_trimmed", ext)

        launchExclusiveOperation {
            try {
                showOperationProgress(getString(R.string.media_tool_media_trim))
                val result = withContext(Dispatchers.IO) {
                    try {
                        FFmpegJni.trim(file.absolutePath, outputFile.absolutePath, startMs, endMs,
                            makeProgressCallback(getString(R.string.media_tool_media_trim)))
                    } catch (e: Exception) { -1 }
                }
                hideOperationProgress()
                val msg = if (result == 0) {
                    MediaScanner.scan(outputFile)
                    getString(R.string.media_tool_trim_success, outputFile.name)
                } else {
                    outputFile.delete()
                    val nativeError = try { FFmpegJni.getLastError() } catch (_: Exception) { "" }
                    getString(R.string.media_tool_trim_failed) +
                        if (nativeError.isNotEmpty()) "\n$nativeError" else ""
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            } finally {
                tempDir?.deleteRecursively()
            }
        }
    }

    // ======== Video Compress ========
    private fun doVideoCompress(files: List<File>, tempDir: File? = null) {
        val outputDir = OutputPaths.resolve(OutputPaths.COMPRESSED).also { it.mkdirs() }

        launchExclusiveOperation {
            try {
                var success = 0
                var failed = 0
                showOperationProgress(getString(R.string.media_tool_video_compress) + " (0/${files.size})")
                for ((i, file) in files.withIndex()) {
                    val outputFile = FileTypeUtils.getUniqueFile(outputDir, "${file.nameWithoutExtension}_compressed", "mp4")
                    val label = "${getString(R.string.media_tool_video_compress)} (${i + 1}/${files.size})"
                    val result = withContext(Dispatchers.IO) {
                        try {
                            FFmpegJni.videoCompress(
                                file.absolutePath, outputFile.absolutePath,
                                VIDEO_COMPRESS_BITRATE_KBPS, 0, 0, 0, makeProgressCallback(label)
                            )
                        } catch (e: Exception) { -1 }
                    }
                    if (result == 0) success++ else {
                        failed++
                        outputFile.delete()
                    }
                    if (result == 0) MediaScanner.scan(outputFile)
                }
                hideOperationProgress()
                Snackbar.make(
                    binding.root,
                    getString(R.string.media_tool_compress_result, success, failed),
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                tempDir?.deleteRecursively()
            }
        }
    }

    // ======== Video Snapshot ========
    private fun showSnapshotDialog(file: File, tempDir: File? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_video_snapshot, null)
        val timeInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.snapshotTimeInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.media_tool_video_snapshot)
            .setView(dialogView)
            .setPositiveButton(R.string.media_tool_start) { _, _ ->
                val timeMs = ((timeInput.text.toString().toDoubleOrNull() ?: 0.0) * 1000).toLong()
                doSnapshot(file, timeMs, tempDir)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> tempDir?.deleteRecursively() }
            .setOnCancelListener { tempDir?.deleteRecursively() }
            .show()
    }

    private fun doSnapshot(file: File, timeMs: Long, tempDir: File? = null) {
        val outputDir = OutputPaths.resolve(OutputPaths.SNAPSHOT).also { it.mkdirs() }
        val outputFile = FileTypeUtils.getUniqueFile(outputDir, "${file.nameWithoutExtension}_${timeMs}ms", "jpg")

        launchExclusiveOperation {
            try {
                val result = withContext(Dispatchers.IO) {
                    try {
                        FFmpegJni.videoSnapshot(file.absolutePath, outputFile.absolutePath, timeMs)
                    } catch (e: Exception) { -1 }
                }
                val msg = if (result == 0) {
                    MediaScanner.scan(outputFile)
                    getString(R.string.media_tool_snapshot_success, outputFile.name)
                } else {
                    outputFile.delete()
                    val nativeError = try { FFmpegJni.getLastError() } catch (_: Exception) { "" }
                    getString(R.string.media_tool_snapshot_failed) +
                        if (nativeError.isNotEmpty()) "\n$nativeError" else ""
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            } finally {
                tempDir?.deleteRecursively()
            }
        }
    }

    // ======== GIF Maker ========
    private fun showGifDialog(file: File, tempDir: File? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_media_trim, null)
        val startInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.startTimeInput)
        val endInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.endTimeInput)

        probeDurationAndFillEnd(file, endInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.media_tool_gif_maker)
            .setView(dialogView)
            .setPositiveButton(R.string.media_tool_start) { _, _ ->
                val startMs = ((startInput.text.toString().toDoubleOrNull() ?: 0.0) * 1000).toLong()
                val endMs = ((endInput.text.toString().toDoubleOrNull()
                    ?: return@setPositiveButton) * 1000).toLong()
                if (endMs <= startMs) {
                    Snackbar.make(binding.root, R.string.media_tool_invalid_time_range, Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                doGifMake(file, startMs, endMs, tempDir)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> tempDir?.deleteRecursively() }
            .setOnCancelListener { tempDir?.deleteRecursively() }
            .show()
    }

    private fun doGifMake(file: File, startMs: Long, endMs: Long, tempDir: File? = null) {
        val outputDir = OutputPaths.resolve(OutputPaths.GIF).also { it.mkdirs() }
        val outputFile = FileTypeUtils.getUniqueFile(outputDir, file.nameWithoutExtension, "gif")

        launchExclusiveOperation {
            try {
                showOperationProgress(getString(R.string.media_tool_gif_maker))
                val result = withContext(Dispatchers.IO) {
                    try {
                        FFmpegJni.gifMake(
                            file.absolutePath, outputFile.absolutePath, startMs, endMs,
                            GIF_DEFAULT_WIDTH, GIF_DEFAULT_FPS,
                            makeProgressCallback(getString(R.string.media_tool_gif_maker))
                        )
                    } catch (e: Exception) { -1 }
                }
                hideOperationProgress()
                val msg = if (result == 0) {
                    MediaScanner.scan(outputFile)
                    getString(R.string.media_tool_gif_success, outputFile.name)
                } else {
                    outputFile.delete()
                    val nativeError = try { FFmpegJni.getLastError() } catch (_: Exception) { "" }
                    getString(R.string.media_tool_gif_failed) +
                        if (nativeError.isNotEmpty()) "\n$nativeError" else ""
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            } finally {
                tempDir?.deleteRecursively()
            }
        }
    }

    // ======== Media Merge ========
    private fun doMediaMerge(files: List<File>) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Probe all files to check codec consistency
            val infos = mutableListOf<Pair<File, MediaInfo>>()
            for (file in files) {
                val info = probeFile(file)
                if (info != null) {
                    infos.add(file to info)
                }
            }

            if (infos.size < 2) {
                showPrerequisiteDialog(R.string.media_tool_probe_failed)
                return@launch
            }

            // Check if all video codecs and audio codecs are consistent
            val firstInfo = infos.first().second
            val firstVideoCodec = firstInfo.videoCodec?.lowercase()
            val firstAudioCodec = firstInfo.audioCodec?.lowercase()
            val firstWidth = firstInfo.width
            val firstHeight = firstInfo.height

            val codecMismatch = infos.drop(1).any { (_, info) ->
                val videoDifferent = firstInfo.hasVideo && info.hasVideo &&
                    (info.videoCodec?.lowercase() != firstVideoCodec ||
                     info.width != firstWidth || info.height != firstHeight)
                val audioDifferent = firstInfo.hasAudio && info.hasAudio &&
                    (info.audioCodec?.lowercase() != firstAudioCodec)
                videoDifferent || audioDifferent
            }

            if (codecMismatch) {
                // Build mismatch description
                val mismatchDesc = buildString {
                    for ((i, pair) in infos.withIndex()) {
                        val (file, info) = pair
                        appendLine("${i + 1}. ${file.name}")
                        if (info.hasVideo) {
                            append("   ${getString(R.string.media_tool_info_video)}: ${info.videoCodec}")
                            append(" ${info.width}x${info.height}")
                            appendLine()
                        }
                        if (info.hasAudio) {
                            append("   ${getString(R.string.media_tool_info_audio)}: ${info.audioCodec}")
                            appendLine()
                        }
                    }
                }.trimEnd()

                val message = getString(R.string.media_tool_merge_transcode_message, mismatchDesc)

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.media_tool_merge_transcode_title)
                    .setMessage(message)
                    .setPositiveButton(R.string.media_tool_merge_transcode_and_merge) { _, _ ->
                        doMediaMergeWithTranscode(files, firstWidth, firstHeight)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            } else {
                // Codecs are consistent, proceed with direct stream concatenation
                doMediaMergeDirect(files)
            }
        }
    }

    /** Direct stream concatenation (same codec/resolution) */
    private fun doMediaMergeDirect(files: List<File>) {
        val outputDir = OutputPaths.resolve(OutputPaths.MERGED).also { it.mkdirs() }
        val ext = files.first().extension.ifEmpty { "mp4" }
        val outputFile = FileTypeUtils.getUniqueFile(outputDir, "merged", ext)
        val inputPaths = files.map { it.absolutePath }.toTypedArray()

        launchExclusiveOperation {
            showOperationProgress(getString(R.string.media_tool_media_merge))
            val result = withContext(Dispatchers.IO) {
                try {
                    FFmpegJni.mergeFiles(inputPaths, outputFile.absolutePath,
                        makeProgressCallback(getString(R.string.media_tool_media_merge)))
                } catch (e: Exception) {
                    Log.e(TAG, "mergeFiles exception", e)
                    -1
                }
            }
            hideOperationProgress()
            if (result == 0) {
                MediaScanner.scan(outputFile)
                Snackbar.make(binding.root,
                    getString(R.string.media_tool_merge_success, outputFile.name),
                    Snackbar.LENGTH_LONG).show()
            } else {
                outputFile.delete()
                val nativeError = try { FFmpegJni.getLastError() } catch (_: Exception) { "" }
                Log.e(TAG, "Direct merge FAILED: result=$result error=$nativeError")

                if (isAdded) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.media_tool_merge_failed)
                        .setMessage(nativeError.ifEmpty { getString(R.string.media_tool_error_unknown) })
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
        }
    }

    /** Transcode all files to a common format then merge (different codec/resolution) */
    private fun doMediaMergeWithTranscode(
        files: List<File>,
        targetWidth: Int,
        targetHeight: Int
    ) {
        val outputDir = OutputPaths.resolve(OutputPaths.MERGED).also { it.mkdirs() }
        val outputFile = FileTypeUtils.getUniqueFile(outputDir, "merged", "mp4")
        val inputPaths = files.map { it.absolutePath }.toTypedArray()

        launchExclusiveOperation {
            showOperationProgress(getString(R.string.media_tool_merge_transcoding))
            val result = withContext(Dispatchers.IO) {
                try {
                    FFmpegJni.mergeFilesTranscode(
                        inputPaths, outputFile.absolutePath,
                        targetWidth, targetHeight, 0,
                        makeProgressCallback(getString(R.string.media_tool_merge_transcoding))
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "mergeFilesTranscode exception", e)
                    -1
                }
            }
            hideOperationProgress()
            if (result == 0) {
                MediaScanner.scan(outputFile)
                Snackbar.make(binding.root,
                    getString(R.string.media_tool_merge_success, outputFile.name),
                    Snackbar.LENGTH_LONG).show()
            } else {
                outputFile.delete()
                val nativeError = try { FFmpegJni.getLastError() } catch (_: Exception) { "" }
                Log.e(TAG, "Transcode merge FAILED: result=$result error=$nativeError")

                if (isAdded) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.media_tool_merge_failed)
                        .setMessage(nativeError.ifEmpty { getString(R.string.media_tool_error_unknown) })
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
        }
    }

    // ======== Media Info ========
    private fun showMediaInfo(file: File) {
        viewLifecycleOwner.lifecycleScope.launch {
            val info = withContext(Dispatchers.IO) {
                try {
                    val mi = MediaInfo()
                    FFmpegJni.getMediaInfo(file.absolutePath, mi)
                    mi
                } catch (e: Exception) { null }
            }

            if (info == null) {
                Snackbar.make(binding.root, R.string.media_tool_info_failed, Snackbar.LENGTH_SHORT).show()
                return@launch
            }

            val details = buildString {
                appendLine("${getString(R.string.media_tool_info_file)}: ${file.name}")
                appendLine("${getString(R.string.media_tool_info_size)}: ${FormatUtils.formatSize(file.length())}")
                info.formatName?.let {
                    appendLine("${getString(R.string.media_tool_info_format)}: $it")
                }
                if (info.durationMs > 0) {
                    appendLine("${getString(R.string.media_tool_info_duration)}: ${info.formatDuration()}")
                }
                if (info.hasVideo) {
                    appendLine("\n── ${getString(R.string.media_tool_info_video)} ──")
                    appendLine("${getString(R.string.media_tool_info_codec)}: ${info.videoCodec}")
                    appendLine("${getString(R.string.media_tool_info_resolution)}: ${info.width}x${info.height}")
                    if (info.videoBitrate > 0)
                        appendLine("${getString(R.string.media_tool_info_bitrate)}: ${info.videoBitrate} kbps")
                }
                if (info.hasAudio) {
                    appendLine("\n── ${getString(R.string.media_tool_info_audio)} ──")
                    appendLine("${getString(R.string.media_tool_info_codec)}: ${info.audioCodec}")
                    appendLine("${getString(R.string.media_tool_info_sample_rate)}: ${info.sampleRate} Hz")
                    appendLine("${getString(R.string.media_tool_info_channels)}: ${info.channels}")
                    if (info.audioBitrate > 0)
                        appendLine("${getString(R.string.media_tool_info_bitrate)}: ${info.audioBitrate} kbps")
                }
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.media_tool_media_info)
                .setMessage(details)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    // ======== Image Compress (FFmpeg) ========
    private fun doImageCompress(files: List<File>) {
        val outputDir = OutputPaths.resolve(OutputPaths.COMPRESSED).also { it.mkdirs() }

        launchExclusiveOperation {
            var success = 0
            var failed = 0
            var totalSaved = 0L

            for ((i, file) in files.withIndex()) {
                showOperationProgress(
                    "${getString(R.string.media_tool_image_compress)} (${i + 1}/${files.size})",
                    ((i) * 100) / files.size
                )
                val ext = file.extension.lowercase().let {
                    if (it in setOf("jpg", "jpeg", "png", "webp", "bmp")) it else "jpg"
                }
                val outputExt = if (ext == "bmp") "jpg" else ext
                val outputFile = FileTypeUtils.getUniqueFile(
                    outputDir, "${file.nameWithoutExtension}_compressed", outputExt
                )
                val result = withContext(Dispatchers.IO) {
                    try {
                        FFmpegJni.imageCompress(
                            file.absolutePath, outputFile.absolutePath,
                            IMAGE_COMPRESS_QUALITY, IMAGE_COMPRESS_MAX_DIM, IMAGE_COMPRESS_MAX_DIM
                        )
                    } catch (e: Exception) { -1 }
                }
                if (result == 0 && outputFile.exists()) {
                    success++
                    totalSaved += maxOf(0L, file.length() - outputFile.length())
                    MediaScanner.scan(outputFile)
                } else {
                    failed++
                    outputFile.delete()
                }
            }
            hideOperationProgress()
            Snackbar.make(
                binding.root,
                getString(R.string.image_compress_result, success, FormatUtils.formatSize(totalSaved)),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    // ======== Image Enhance (FFmpeg unsharp) ========
    private fun doImageEnhance(files: List<File>) {
        val outputDir = OutputPaths.resolve(OutputPaths.ENHANCED).also { it.mkdirs() }

        launchExclusiveOperation {
            var success = 0
            var failed = 0
            for ((i, file) in files.withIndex()) {
                showOperationProgress(
                    "${getString(R.string.media_tool_image_enhance)} (${i + 1}/${files.size})",
                    ((i) * 100) / files.size
                )
                val ext = file.extension.lowercase().let {
                    if (it in setOf("jpg", "jpeg", "png", "webp")) it else "jpg"
                }
                val outputFile = FileTypeUtils.getUniqueFile(
                    outputDir, "${file.nameWithoutExtension}_enhanced", ext
                )
                val result = withContext(Dispatchers.IO) {
                    try {
                        FFmpegJni.imageEnhance(
                            file.absolutePath, outputFile.absolutePath,
                            IMAGE_ENHANCE_STRENGTH
                        )
                    } catch (e: Exception) { -1 }
                }
                if (result == 0 && outputFile.exists()) {
                    success++
                    MediaScanner.scan(outputFile)
                } else {
                    failed++
                    outputFile.delete()
                }
            }
            hideOperationProgress()
            Snackbar.make(
                binding.root,
                getString(R.string.media_tool_enhance_result, success, failed),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    // ======== Video Enhance (FFmpeg unsharp) ========
    private fun doVideoEnhance(files: List<File>) {
        val outputDir = OutputPaths.resolve(OutputPaths.ENHANCED).also { it.mkdirs() }

        launchExclusiveOperation {
            var success = 0
            var failed = 0
            showOperationProgress(getString(R.string.media_tool_video_enhance) + " (0/${files.size})")
            for ((i, file) in files.withIndex()) {
                val outputFile = FileTypeUtils.getUniqueFile(
                    outputDir, "${file.nameWithoutExtension}_enhanced", "mp4"
                )
                val label = "${getString(R.string.media_tool_video_enhance)} (${i + 1}/${files.size})"
                val result = withContext(Dispatchers.IO) {
                    try {
                        FFmpegJni.videoEnhance(
                            file.absolutePath, outputFile.absolutePath,
                            VIDEO_ENHANCE_STRENGTH, 0, makeProgressCallback(label)
                        )
                    } catch (e: Exception) { -1 }
                }
                if (result == 0 && outputFile.exists()) {
                    success++
                    MediaScanner.scan(outputFile)
                } else {
                    failed++
                    outputFile.delete()
                }
            }
            hideOperationProgress()
            Snackbar.make(
                binding.root,
                getString(R.string.media_tool_enhance_result, success, failed),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    // ======== Error detail dialog ========
    private fun showErrorDialog(title: String, message: String) {
        if (!isAdded) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton(R.string.media_tool_copy_error) { _, _ ->
                val clipboard = requireContext().getSystemService(
                    android.content.Context.CLIPBOARD_SERVICE
                ) as android.content.ClipboardManager
                clipboard.setPrimaryClip(
                    android.content.ClipData.newPlainText("error", message)
                )
                Snackbar.make(binding.root, R.string.media_tool_error_copied, Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        if (operationJob?.isActive == true) {
            operationJob?.cancel()
            g_cancel = true
            FFmpegJni.cancel()
        }
        super.onDestroyView()
    }

}
