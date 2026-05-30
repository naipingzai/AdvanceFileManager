/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.ffmpegtools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.advancefilemanager.plugin.ffmpegtools.databinding.FragmentFfmpegFeatureBinding
import java.io.File

class FFmpegFeatureFragment : Fragment() {

    private lateinit var binding: FragmentFfmpegFeatureBinding
    private lateinit var feature: MediaToolFeature
    private lateinit var filePath: String

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

        val actionType = requireArguments().getString(ARG_ACTION_TYPE)!!
        filePath = requireArguments().getString(ARG_FILE_PATH)!!
        feature = MediaToolFeature.entries.find { it.actionType == actionType }!!

        setupFileInfo()
        setupOutputOptions()
        setupActionButton()
    }

    private fun setupFileInfo() {
        val file = File(filePath)
        binding.fileName.text = file.name
        val sizeStr = formatFileSize(file.length())
        binding.fileInfo.text = "$sizeStr · ${file.extension.uppercase()}"
    }

    private fun setupOutputOptions() {
        val formats = getOutputFormats()
        if (formats.isEmpty()) {
            binding.outputLabel.visibility = View.GONE
            binding.outputFormatLayout.visibility = View.GONE
            return
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, formats)
        binding.outputFormat.setAdapter(adapter)
        binding.outputFormat.setText(formats.firstOrNull() ?: "", false)
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
        }
    }

    private fun setupActionButton() {
        binding.actionButton.text = getString(feature.titleRes)
        binding.actionButton.setOnClickListener {
            startProcessing()
        }
    }

    private fun startProcessing() {
        binding.actionButton.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        binding.progressText.visibility = View.VISIBLE
        binding.progressText.text = "处理中..."

        val outputFormat = binding.outputFormat.text.toString()
        val inputFile = File(filePath)
        val outputFile = generateOutputFile(inputFile, outputFormat)

        Thread {
            try {
                val result = executeFFmpeg(inputFile, outputFile, outputFormat)
                requireActivity().runOnUiThread {
                    if (result == 0) {
                        binding.progressBar.visibility = View.GONE
                        binding.progressText.text = "完成: ${outputFile.name}"
                        Toast.makeText(requireContext(), "处理完成: ${outputFile.name}", Toast.LENGTH_LONG).show()
                    } else {
                        binding.progressText.text = "失败: ${FFmpegJni.getLastError()}"
                    }
                    binding.actionButton.isEnabled = true
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.progressText.text = "错误: ${e.message}"
                    binding.actionButton.isEnabled = true
                }
            }
        }.start()
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
            else -> inputFile.extension
        }
        val dir = inputFile.parentFile ?: inputFile
        var output = File(dir, "${baseName}_output.$ext")
        var counter = 1
        while (output.exists()) {
            output = File(dir, "${baseName}_output_${counter}.$ext")
            counter++
        }
        return output
    }

    private fun executeFFmpeg(inputFile: File, outputFile: File, format: String): Int {
        return FFmpegJni.convert(
            inputFile.absolutePath,
            outputFile.absolutePath,
            object : FFmpegJni.ProgressCallback {
                override fun onProgress(percent: Int) {
                    requireActivity().runOnUiThread {
                        binding.progressBar.progress = percent
                        binding.progressText.text = "处理中... $percent%"
                    }
                }
            }
        )
    }

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

        fun newInstance(feature: MediaToolFeature, filePath: String): FFmpegFeatureFragment {
            return FFmpegFeatureFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACTION_TYPE, feature.actionType)
                    putString(ARG_FILE_PATH, filePath)
                }
            }
        }
    }
}
