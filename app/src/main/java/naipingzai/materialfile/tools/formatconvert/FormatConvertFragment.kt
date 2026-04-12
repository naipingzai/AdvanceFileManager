/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.formatconvert

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.FormatConvertFragmentBinding
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.filelist.FilePickerDialogActivity
import naipingzai.materialfile.tools.FileTypeUtils
import naipingzai.materialfile.tools.OutputPaths
import naipingzai.materialfile.util.FormatUtils
import naipingzai.materialfile.provider.linux.media.MediaScanner
import java.io.File
import kotlinx.coroutines.CancellationException

class FormatConvertFragment : Fragment() {
    private lateinit var binding: FormatConvertFragmentBinding
    private val fileList = mutableListOf<ConvertItem>()
    private lateinit var adapter: FormatConvertAdapter
    private var convertJob: Job? = null
    private var isConverting = false
    private var selectionMenu: Menu? = null
    private var backPressCallback: OnBackPressedCallback? = null
    private var convertingBackCallback: OnBackPressedCallback? = null

    enum class FileType { IMAGE, VIDEO, AUDIO, UNKNOWN }

    companion object {
        private const val KEY_FILE_LIST = "file_list"
        private const val MAX_SAVED_FILES = 500
        const val EXTRA_FILE_PATHS = "naipingzai.materialfile.extra.FILE_PATHS"
        val IMAGE_FORMATS = arrayOf("jpg", "png", "webp")
        val VIDEO_FORMATS = arrayOf("mp4", "mkv", "avi", "webm", "ts", "mov")
        val AUDIO_FORMATS = arrayOf("mp3", "aac", "flac", "ogg", "wav", "m4a")

        fun detectFileType(ext: String): FileType = when (ext) {
            in FileTypeUtils.IMAGE_EXTENSIONS -> FileType.IMAGE
            in FileTypeUtils.VIDEO_EXTENSIONS -> FileType.VIDEO
            in FileTypeUtils.AUDIO_EXTENSIONS -> FileType.AUDIO
            else -> FileType.UNKNOWN
        }

        fun getFormatsForType(type: FileType): Array<String> = when (type) {
            FileType.IMAGE -> IMAGE_FORMATS
            FileType.VIDEO -> VIDEO_FORMATS
            FileType.AUDIO -> AUDIO_FORMATS
            FileType.UNKNOWN -> emptyArray()
        }

        fun getDefaultOutputFormat(type: FileType): String = when (type) {
            FileType.IMAGE -> "jpg"
            FileType.VIDEO -> "mp4"
            FileType.AUDIO -> "mp3"
            FileType.UNKNOWN -> ""
        }

        fun getFileTypeLabel(context: android.content.Context, type: FileType): String = when (type) {
            FileType.IMAGE -> context.getString(R.string.file_type_image)
            FileType.VIDEO -> context.getString(R.string.file_type_video)
            FileType.AUDIO -> context.getString(R.string.file_type_audio)
            FileType.UNKNOWN -> context.getString(R.string.file_type_unknown)
        }
    }

    private val filePickerLauncher = registerForActivityResult(
        FilePickerDialogActivity.OpenMultipleFilesContract()
    ) { paths: List<java8.nio.file.Path> ->
        if (paths.isEmpty()) return@registerForActivityResult
        var added = 0
        var skipped = 0
        paths.forEach { path ->
            val file = path.toFile()
            val ext = file.extension.lowercase()
            val type = detectFileType(ext)
            if (type != FileType.UNKNOWN) {
                addFile(file, type)
                added++
            } else {
                skipped++
            }
        }
        if (skipped > 0) {
            Snackbar.make(
                binding.root,
                getString(R.string.format_convert_unsupported_files, skipped),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    @Parcelize
    data class ConvertItem(
        val path: String,
        val name: String,
        val size: Long,
        val fileType: FileType = FileType.UNKNOWN,
        var inputFormat: String = "",
        var outputFormat: String = "",
        var mediaInfo: String = "",
        var status: String = "",
        var progress: Int = -1,
        var isConverting: Boolean = false
    ) : Parcelable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FormatConvertFragmentBinding.inflate(inflater, container, false)
        .also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        adapter = FormatConvertAdapter(
            items = fileList,
            onItemClick = { position -> showFormatEditDialog(position) },
            onItemLongClick = { },
            onSelectionChanged = { updateSelectionUI() }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Selection mode menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.format_convert_selection, menu)
                selectionMenu = menu
                updateSelectionMenuVisibility()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_select_all -> {
                        adapter.selectAll()
                        true
                    }
                    R.id.action_deselect_all -> {
                        adapter.clearSelection()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)

        binding.addFileButton.setOnClickListener {
            filePickerLauncher.launch(
                listOf(MimeType.IMAGE_ANY, MimeType.VIDEO_ANY, MimeType.AUDIO_ANY)
            )
        }
        binding.convertButton.setOnClickListener { startConvert() }
        binding.batchFormatButton.setOnClickListener { showBatchFormatDialog() }
        binding.removeSelectedButton.setOnClickListener { removeSelectedFiles() }

        // Back press exits selection mode
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    adapter.clearSelection()
                }
            }.also { backPressCallback = it }
        )

        // Back press confirms exit during conversion
        convertingBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.format_convert_title)
                    .setMessage(R.string.format_convert_exit_confirm)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        convertJob?.cancel()
                        FFmpegJni.cancel()
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, convertingBackCallback!!
        )

        // Restore saved state
        if (savedInstanceState != null) {
            val saved = savedInstanceState.getParcelableArrayList<ConvertItem>(KEY_FILE_LIST)
            if (saved != null) {
                // Reset any "converting" status since the job was cancelled on rotation
                saved.forEach { item ->
                    if (item.isConverting) {
                        item.status = ""
                        item.isConverting = false
                    }
                }
                fileList.clear()
                fileList.addAll(saved)
                adapter.notifyDataSetChanged()
            }
        } else {
            // Load initial files from Intent extras (e.g. from file list context menu)
            val initialPaths = arguments?.getStringArray(EXTRA_FILE_PATHS)
            if (initialPaths != null) {
                for (path in initialPaths) {
                    val file = File(path)
                    if (file.exists()) {
                        val ext = file.extension.lowercase()
                        val type = detectFileType(ext)
                        if (type != FileType.UNKNOWN) {
                            addFile(file, type)
                        }
                    }
                }
            }
        }

        updateCount()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val saveList = if (fileList.size > MAX_SAVED_FILES) ArrayList(fileList.take(MAX_SAVED_FILES))
            else ArrayList(fileList)
        outState.putParcelableArrayList(KEY_FILE_LIST, saveList)
    }

    private fun updateSelectionUI() {
        val count = adapter.selectedPositions.size
        val activity = requireActivity() as AppCompatActivity
        val inSelection = adapter.isInSelectionMode
        if (inSelection) {
            activity.supportActionBar?.title = getString(
                R.string.format_convert_selected_count, count
            )
        } else {
            activity.supportActionBar?.title = getString(R.string.format_convert_title)
        }
        // Toggle bottom bars
        binding.convertButton.isVisible = !inSelection
        binding.selectionActionBar.isVisible = inSelection
        // Enable/disable back press interception
        backPressCallback?.isEnabled = inSelection
        updateSelectionMenuVisibility()
    }

    private fun updateSelectionMenuVisibility() {
        selectionMenu?.let { menu ->
            val inSelection = adapter.isInSelectionMode
            menu.findItem(R.id.action_select_all)?.isVisible = fileList.isNotEmpty()
            menu.findItem(R.id.action_deselect_all)?.isVisible = inSelection
        }
    }

    private fun removeSelectedFiles() {
        val positions = adapter.selectedPositions.sortedDescending()
        positions.forEach { pos ->
            if (pos in fileList.indices) {
                fileList.removeAt(pos)
            }
        }
        adapter.selectedPositions.clear()
        adapter.notifyDataSetChanged()
        updateSelectionUI()
        updateCount()
    }

    private fun showBatchFormatDialog() {
        val selectedItems = adapter.selectedPositions
            .filter { it in fileList.indices }
            .map { fileList[it] }
        if (selectedItems.isEmpty()) return

        val types = selectedItems.map { it.fileType }.toSet()

        if (types.size > 1) {
            // Mixed types — show per-type format dialog
            showBatchMixedTypeDialog(selectedItems, types)
            return
        }

        // All same type
        val type = types.first()
        val formats = getFormatsForType(type)
        val dialogView = layoutInflater.inflate(R.layout.format_convert_edit_dialog, null)
        val outputChipGroup = dialogView.findViewById<ChipGroup>(R.id.outputFormatChipGroup)

        var selectedFormat = getDefaultOutputFormat(type)
        formats.forEach { format ->
            val chip = layoutInflater.inflate(
                R.layout.chip_filter, outputChipGroup, false
            ) as Chip
            chip.text = format.uppercase()
            chip.isChecked = format == selectedFormat
            chip.id = View.generateViewId()
            outputChipGroup.addView(chip)
        }
        outputChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = outputChipGroup.findViewById<Chip>(checkedIds[0])
                selectedFormat = chip?.text?.toString()?.lowercase() ?: selectedFormat
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.format_convert_batch_title, selectedItems.size))
            .setMessage(getString(
                R.string.format_convert_batch_info,
                getFileTypeLabel(requireContext(), type),
                selectedItems.size
            ))
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                applyBatchFormat(selectedFormat)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showBatchMixedTypeDialog(
        selectedItems: List<ConvertItem>,
        types: Set<FileType>
    ) {
        val dialogView = layoutInflater.inflate(R.layout.format_convert_batch_mixed_dialog, null)
        val imageChipGroup = dialogView.findViewById<ChipGroup>(R.id.imageFormatChipGroup)
        val videoChipGroup = dialogView.findViewById<ChipGroup>(R.id.videoFormatChipGroup)
        val audioChipGroup = dialogView.findViewById<ChipGroup>(R.id.audioFormatChipGroup)
        val imageSection = dialogView.findViewById<View>(R.id.imageSectionLayout)
        val videoSection = dialogView.findViewById<View>(R.id.videoSectionLayout)
        val audioSection = dialogView.findViewById<View>(R.id.audioSectionLayout)

        val imageCount = selectedItems.count { it.fileType == FileType.IMAGE }
        val videoCount = selectedItems.count { it.fileType == FileType.VIDEO }
        val audioCount = selectedItems.count { it.fileType == FileType.AUDIO }

        var imageFormat = getDefaultOutputFormat(FileType.IMAGE)
        var videoFormat = getDefaultOutputFormat(FileType.VIDEO)
        var audioFormat = getDefaultOutputFormat(FileType.AUDIO)

        // Setup image section
        if (FileType.IMAGE in types) {
            imageSection.isVisible = true
            IMAGE_FORMATS.forEach { format ->
                val chip = layoutInflater.inflate(
                    R.layout.chip_filter, imageChipGroup, false
                ) as Chip
                chip.text = format.uppercase()
                chip.isChecked = format == imageFormat
                chip.id = View.generateViewId()
                imageChipGroup.addView(chip)
            }
            imageChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                if (checkedIds.isNotEmpty()) {
                    val chip = imageChipGroup.findViewById<Chip>(checkedIds[0])
                    imageFormat = chip?.text?.toString()?.lowercase() ?: imageFormat
                }
            }
        } else {
            imageSection.isVisible = false
        }

        // Setup video section
        if (FileType.VIDEO in types) {
            videoSection.isVisible = true
            VIDEO_FORMATS.forEach { format ->
                val chip = layoutInflater.inflate(
                    R.layout.chip_filter, videoChipGroup, false
                ) as Chip
                chip.text = format.uppercase()
                chip.isChecked = format == videoFormat
                chip.id = View.generateViewId()
                videoChipGroup.addView(chip)
            }
            videoChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                if (checkedIds.isNotEmpty()) {
                    val chip = videoChipGroup.findViewById<Chip>(checkedIds[0])
                    videoFormat = chip?.text?.toString()?.lowercase() ?: videoFormat
                }
            }
        } else {
            videoSection.isVisible = false
        }

        // Setup audio section
        if (FileType.AUDIO in types) {
            audioSection.isVisible = true
            AUDIO_FORMATS.forEach { format ->
                val chip = layoutInflater.inflate(
                    R.layout.chip_filter, audioChipGroup, false
                ) as Chip
                chip.text = format.uppercase()
                chip.isChecked = format == audioFormat
                chip.id = View.generateViewId()
                audioChipGroup.addView(chip)
            }
            audioChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                if (checkedIds.isNotEmpty()) {
                    val chip = audioChipGroup.findViewById<Chip>(checkedIds[0])
                    audioFormat = chip?.text?.toString()?.lowercase() ?: audioFormat
                }
            }
        } else {
            audioSection.isVisible = false
        }

        val summary = buildString {
            val parts = mutableListOf<String>()
            if (imageCount > 0) parts.add(getString(R.string.format_convert_summary_image, imageCount))
            if (videoCount > 0) parts.add(getString(R.string.format_convert_summary_video, videoCount))
            if (audioCount > 0) parts.add(getString(R.string.format_convert_summary_audio, audioCount))
            append(parts.joinToString(", "))
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.format_convert_batch_title, selectedItems.size))
            .setMessage(summary)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                adapter.selectedPositions
                    .filter { it in fileList.indices }
                    .forEach { pos ->
                        val item = fileList[pos]
                        val newFormat = when (item.fileType) {
                            FileType.IMAGE -> imageFormat
                            FileType.VIDEO -> videoFormat
                            FileType.AUDIO -> audioFormat
                            FileType.UNKNOWN -> item.outputFormat
                        }
                        fileList[pos] = item.copy(outputFormat = newFormat)
                    }
                adapter.clearSelection()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun applyBatchFormat(format: String) {
        adapter.selectedPositions
            .filter { it in fileList.indices }
            .forEach { pos ->
                val item = fileList[pos]
                fileList[pos] = item.copy(outputFormat = format)
            }
        adapter.clearSelection()
        adapter.notifyDataSetChanged()
    }

    private fun addFile(file: File, type: FileType) {
        if (fileList.any { it.path == file.absolutePath }) return
        val inputExt = file.extension.lowercase()
        val outputFormat = getDefaultOutputFormat(type)
        val item = ConvertItem(
            path = file.absolutePath,
            name = file.name,
            size = file.length(),
            fileType = type,
            inputFormat = inputExt,
            outputFormat = outputFormat
        )
        fileList.add(item)
        adapter.notifyItemInserted(fileList.size - 1)
        updateCount()
        updateSelectionMenuVisibility()

        // Probe media info
        run {
            val idx = fileList.size - 1
            viewLifecycleOwner.lifecycleScope.launch {
                val info = withContext(Dispatchers.IO) {
                    try {
                        val mi = MediaInfo()
                        FFmpegJni.getMediaInfo(file.absolutePath, mi)
                        mi.summary()
                    } catch (e: Exception) { "" }
                }
                if (idx < fileList.size && fileList[idx].path == file.absolutePath) {
                    fileList[idx] = fileList[idx].copy(mediaInfo = info)
                    adapter.notifyItemChanged(idx)
                }
            }
        }
    }

    private fun updateCount() {
        val imageCount = fileList.count { it.fileType == FileType.IMAGE }
        val videoCount = fileList.count { it.fileType == FileType.VIDEO }
        val audioCount = fileList.count { it.fileType == FileType.AUDIO }
        binding.fileCountText.text = getString(R.string.format_convert_count, fileList.size)
        binding.fileSummaryText.text = if (fileList.isEmpty()) {
            getString(R.string.format_convert_empty_hint)
        } else {
            buildString {
                val parts = mutableListOf<String>()
                if (imageCount > 0) parts.add(getString(R.string.format_convert_summary_image, imageCount))
                if (videoCount > 0) parts.add(getString(R.string.format_convert_summary_video, videoCount))
                if (audioCount > 0) parts.add(getString(R.string.format_convert_summary_audio, audioCount))
                append(parts.joinToString("  "))
            }
        }
    }

    private fun showFormatEditDialog(position: Int) {
        if (position !in fileList.indices || isConverting) return
        val item = fileList[position]
        val context = requireContext()

        val dialogView = layoutInflater.inflate(R.layout.format_convert_edit_dialog, null)
        val outputChipGroup = dialogView.findViewById<ChipGroup>(R.id.outputFormatChipGroup)

        val formats = getFormatsForType(item.fileType)
        var selectedOutputFormat = item.outputFormat
        formats.forEach { format ->
            val chip = layoutInflater.inflate(
                R.layout.chip_filter, outputChipGroup, false
            ) as Chip
            chip.text = format.uppercase()
            chip.isChecked = format == item.outputFormat
            chip.id = View.generateViewId()
            outputChipGroup.addView(chip)
        }
        outputChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = outputChipGroup.findViewById<Chip>(checkedIds[0])
                selectedOutputFormat = chip?.text?.toString()?.lowercase() ?: item.outputFormat
            }
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(item.name)
            .setMessage(
                getString(
                    R.string.format_convert_edit_info,
                    item.inputFormat.uppercase(),
                    getFileTypeLabel(requireContext(), item.fileType),
                    FormatUtils.formatSize(item.size)
                )
            )
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                fileList[position] = item.copy(outputFormat = selectedOutputFormat)
                adapter.notifyItemChanged(position)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun startConvert() {
        if (fileList.isEmpty()) {
            Snackbar.make(binding.root, R.string.format_convert_no_files, Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        // Validate format compatibility
        val invalidItems = fileList.filter { item ->
            val targetFormats = getFormatsForType(item.fileType)
            item.outputFormat !in targetFormats
        }
        if (invalidItems.isNotEmpty()) {
            val names = invalidItems.joinToString("\n") { "• ${it.name}" }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.format_convert_error_title)
                .setMessage(getString(R.string.format_convert_format_mismatch, names))
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.format_convert_confirm_title)
            .setMessage(getString(R.string.format_convert_confirm_message, fileList.size))
            .setPositiveButton(android.R.string.ok) { _, _ -> executeConvert() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setConvertingUiState(converting: Boolean) {
        isConverting = converting
        binding.progressBar.isVisible = converting
        binding.convertButton.isEnabled = !converting
        binding.addFileButton.isEnabled = !converting
        binding.removeSelectedButton.isEnabled = !converting
        binding.batchFormatButton.isEnabled = !converting
        convertingBackCallback?.isEnabled = converting
    }

    private fun executeConvert() {
        setConvertingUiState(true)

        convertJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                var successCount = 0
                var failedCount = 0

                for (i in fileList.indices) {
                    if (!isActive) break
                    val item = fileList[i]
                    val targetFormat = item.outputFormat

                    fileList[i] = item.copy(
                        status = getString(R.string.format_convert_converting),
                        progress = 0,
                        isConverting = true
                    )
                    adapter.notifyItemChanged(i)

                    val result = withContext(Dispatchers.IO) {
                        when (item.fileType) {
                            FileType.IMAGE -> convertImage(File(item.path), targetFormat)
                            FileType.VIDEO, FileType.AUDIO ->
                                convertMedia(File(item.path), targetFormat, item.fileType, i)
                            FileType.UNKNOWN -> false
                        }
                    }

                    if (result) {
                        successCount++
                        fileList[i] = fileList[i].copy(
                            status = getString(R.string.format_convert_success),
                            progress = 100,
                            isConverting = false
                        )
                    } else {
                        failedCount++
                        val lastError = try {
                            FFmpegJni.getLastError().takeIf { it.isNotEmpty() }
                        } catch (_: Exception) { null }
                        val failMsg = if (lastError != null) {
                            "${getString(R.string.format_convert_failed)}: $lastError"
                        } else {
                            getString(R.string.format_convert_failed)
                        }
                        fileList[i] = fileList[i].copy(
                            status = failMsg,
                            progress = -1,
                            isConverting = false
                        )
                    }
                    adapter.notifyItemChanged(i)
                }

                setConvertingUiState(false)

                Snackbar.make(
                    binding.root,
                    getString(R.string.format_convert_result, successCount, failedCount),
                    Snackbar.LENGTH_LONG
                ).show()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setConvertingUiState(false)
                Snackbar.make(
                    binding.root,
                    getString(R.string.format_convert_error, e.message ?: e.toString()),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun convertImage(file: File, outputFormat: String): Boolean {
        val outputDir = OutputPaths.resolve(OutputPaths.CONVERTED_IMAGE)
        if (!outputDir.mkdirs() && !outputDir.isDirectory) return false
        val outFile = FileTypeUtils.getUniqueFile(outputDir, file.nameWithoutExtension, outputFormat)
        return try {
            val quality = if (outputFormat == "png") 100 else 95
            val ret = FFmpegJni.imageCompress(
                file.absolutePath, outFile.absolutePath, quality, 0, 0
            )
            if (ret == 0) { MediaScanner.scan(outFile); true }
            else { outFile.delete(); false }
        } catch (e: Exception) {
            outFile.delete()
            false
        }
    }

    @Volatile
    private var lastProgressUpdateTime = 0L

    private fun convertMedia(
        file: File, outputFormat: String, type: FileType, index: Int
    ): Boolean {
        val subDir = when (type) {
            FileType.VIDEO -> "video"
            FileType.AUDIO -> "audio"
            else -> "other"
        }
        val outputDir = OutputPaths.resolve(OutputPaths.converted(subDir))
        if (!outputDir.mkdirs() && !outputDir.isDirectory) return false
        val baseName = file.nameWithoutExtension
        val outFile = FileTypeUtils.getUniqueFile(outputDir, baseName, outputFormat)
        return try {
            lastProgressUpdateTime = 0L
            val ret = FFmpegJni.convert(
                file.absolutePath, outFile.absolutePath,
                object : FFmpegJni.ProgressCallback {
                    override fun onProgress(percent: Int) {
                        val now = System.currentTimeMillis()
                        if (percent != 100 && now - lastProgressUpdateTime < 200) return
                        lastProgressUpdateTime = now
                        try {
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                                if (index < fileList.size) {
                                    val current = fileList[index]
                                    if (current.progress != percent) {
                                        fileList[index] = current.copy(progress = percent)
                                        adapter.notifyItemChanged(index)
                                    }
                                }
                            }
                        } catch (_: IllegalStateException) {
                            // Fragment view already destroyed
                        }
                    }
                }
            )
            if (ret != 0) outFile.delete()
            else MediaScanner.scan(outFile)
            ret == 0
        } catch (e: Exception) {
            outFile.delete()
            false
        }
    }

    override fun onDestroyView() {
        if (convertJob?.isActive == true) {
            convertJob?.cancel()
            FFmpegJni.cancel()
        }
        super.onDestroyView()
    }

}
