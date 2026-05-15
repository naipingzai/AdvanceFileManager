/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools.imagecompress

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import com.advancefilemanager.R
import com.advancefilemanager.databinding.ImageCompressFragmentBinding
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.filelist.FileListActivity
import com.advancefilemanager.tools.FileTypeUtils
import com.advancefilemanager.tools.OutputPaths
import com.advancefilemanager.tools.formatconvert.FFmpegJni
import com.advancefilemanager.util.FormatUtils
import com.advancefilemanager.provider.linux.media.MediaScanner
import java.io.File
import kotlinx.coroutines.CancellationException

class ImageCompressFragment : Fragment() {
    private lateinit var binding: ImageCompressFragmentBinding
    private val imageList = mutableListOf<ImageItem>()
    private lateinit var adapter: ImageItemAdapter
    private var compressJob: Job? = null

    private val filePickerLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths: List<java8.nio.file.Path> ->
        if (paths.isEmpty()) return@registerForActivityResult
        var skipped = 0
        paths.forEach { path ->
            val file = path.toFile()
            if (FileTypeUtils.isImageFile(file.name)) {
                addImage(file)
            } else {
                skipped++
            }
        }
        if (skipped > 0) {
            Snackbar.make(binding.root, R.string.image_compress_not_image, Snackbar.LENGTH_SHORT).show()
        }
    }

    @Parcelize
    data class ImageItem(
        val path: String,
        val name: String,
        val originalSize: Long,
        var compressedSize: Long = 0,
        var status: String = ""
    ) : Parcelable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ImageCompressFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        adapter = ImageItemAdapter(imageList) { position ->
            removeImageAt(position)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.addFileButton.setOnClickListener {
            val imageMimes = listOf(MimeType.IMAGE_ANY)
            filePickerLauncher.launch(imageMimes)
        }
        binding.compressButton.setOnClickListener { startCompress() }

        binding.qualitySlider.value = DEFAULT_QUALITY
        binding.qualitySlider.addOnChangeListener { _, value, _ ->
            binding.qualityValueText.text = "${value.toInt()}%"
        }

        binding.maxDimensionSlider.value = DEFAULT_MAX_DIMENSION
        binding.maxDimensionSlider.addOnChangeListener { _, value, _ ->
            binding.maxDimensionValueText.text = "${value.toInt()}px"
        }

        if (savedInstanceState != null) {
            val saved = savedInstanceState.getParcelableArrayList<ImageItem>(KEY_IMAGE_LIST)
            if (saved != null) {
                imageList.clear()
                imageList.addAll(saved)
                adapter.notifyDataSetChanged()
            }
            binding.qualitySlider.value = savedInstanceState.getFloat(KEY_QUALITY, DEFAULT_QUALITY)
            binding.maxDimensionSlider.value = savedInstanceState.getFloat(KEY_MAX_DIMENSION, DEFAULT_MAX_DIMENSION)
            binding.deleteOriginalSwitch.isChecked = savedInstanceState.getBoolean(KEY_DELETE_ORIGINAL, false)
        }

        updateCount()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val saveList = if (imageList.size > MAX_SAVED_FILES) ArrayList(imageList.take(MAX_SAVED_FILES))
            else ArrayList(imageList)
        outState.putParcelableArrayList(KEY_IMAGE_LIST, saveList)
        if (::binding.isInitialized) {
            outState.putFloat(KEY_QUALITY, binding.qualitySlider.value)
            outState.putFloat(KEY_MAX_DIMENSION, binding.maxDimensionSlider.value)
            outState.putBoolean(KEY_DELETE_ORIGINAL, binding.deleteOriginalSwitch.isChecked)
        }
    }

    companion object {
        private const val KEY_IMAGE_LIST = "image_list"
        private const val KEY_QUALITY = "quality"
        private const val KEY_MAX_DIMENSION = "max_dimension"
        private const val MAX_SAVED_FILES = 500
        private const val DEFAULT_QUALITY = 80f
        private const val DEFAULT_MAX_DIMENSION = 1920f
        private const val KEY_DELETE_ORIGINAL = "delete_original"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compressJob?.cancel()
    }

    private fun addImage(file: File) {
        if (imageList.any { it.path == file.absolutePath }) return
        imageList.add(
            ImageItem(
                path = file.absolutePath,
                name = file.name,
                originalSize = file.length()
            )
        )
        adapter.notifyItemInserted(imageList.size - 1)
        updateCount()
    }

    private fun updateCount() {
        binding.imageCountText.text = getString(R.string.image_compress_count, imageList.size)
    }

    private fun removeImageAt(position: Int) {
        if (position !in imageList.indices) return
        val item = imageList[position]
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.image_compress_remove_title)
            .setMessage(getString(R.string.image_compress_remove_message, item.name))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                imageList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, imageList.size - position)
                updateCount()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun startCompress() {
        if (imageList.isEmpty()) {
            Snackbar.make(binding.root, R.string.image_compress_no_images, Snackbar.LENGTH_SHORT).show()
            return
        }

        val quality = binding.qualitySlider.value.toInt()
        val maxDimension = binding.maxDimensionSlider.value.toInt()
        val outputFormat = when {
            binding.chipPng.isChecked -> "png"
            binding.chipWebp.isChecked -> "webp"
            else -> "jpg"
        }
        val deleteOriginal = binding.deleteOriginalSwitch.isChecked

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.image_compress_confirm_title)
            .setMessage(getString(R.string.image_compress_confirm_message, imageList.size, quality))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                executeCompress(quality, maxDimension, outputFormat, deleteOriginal)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun executeCompress(quality: Int, maxDimension: Int, outputFormat: String, deleteOriginal: Boolean) {
        binding.progressBar.isVisible = true
        binding.compressButton.isEnabled = false

        compressJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                var successCount = 0
                var totalSaved = 0L

                for (i in imageList.indices) {
                    if (!isActive) break
                    val item = imageList[i]
                    val result = withContext(Dispatchers.IO) {
                        compressImage(File(item.path), quality, maxDimension, outputFormat)
                    }

                    if (result != null) {
                        successCount++
                        val saved = maxOf(0L, item.originalSize - result)
                        totalSaved += saved
                        imageList[i] = item.copy(
                            compressedSize = result,
                            status = getString(R.string.image_compress_saved, FormatUtils.formatSize(saved))
                        )
                        if (deleteOriginal) {
                            withContext(Dispatchers.IO) {
                                try {
                                    val originalFile = File(item.path)
                                    if (originalFile.exists()) originalFile.delete()
                                } catch (_: Exception) { }
                            }
                        }
                    } else {
                        imageList[i] = item.copy(
                            status = getString(R.string.image_compress_failed)
                        )
                    }
                    adapter.notifyItemChanged(i)
                }

                binding.progressBar.isVisible = false
                binding.compressButton.isEnabled = true

                Snackbar.make(
                    binding.root,
                    getString(R.string.image_compress_result, successCount, FormatUtils.formatSize(totalSaved)),
                    Snackbar.LENGTH_LONG
                ).show()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.progressBar.isVisible = false
                binding.compressButton.isEnabled = true
                Snackbar.make(
                    binding.root,
                    getString(R.string.image_compress_error, e.message ?: e.toString()),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun compressImage(
        file: File,
        quality: Int,
        maxDimension: Int,
        outputFormat: String
    ): Long? {
        val outputDir = OutputPaths.resolve(OutputPaths.COMPRESSED)
        if (!outputDir.mkdirs() && !outputDir.isDirectory) return null

        val ext = when (outputFormat) {
            "png" -> "png"
            "webp" -> "webp"
            else -> "jpg"
        }
        val baseName = file.nameWithoutExtension
        val outputFile = FileTypeUtils.getUniqueFile(outputDir, "${baseName}_compressed", ext)

        return try {
            val ret = FFmpegJni.imageCompress(
                file.absolutePath, outputFile.absolutePath,
                quality, maxDimension, maxDimension
            )
            if (ret == 0 && outputFile.exists()) {
                MediaScanner.scan(outputFile)
                outputFile.length()
            } else {
                outputFile.delete()
                null
            }
        } catch (e: Exception) {
            outputFile.delete()
            null
        }
    }
}
