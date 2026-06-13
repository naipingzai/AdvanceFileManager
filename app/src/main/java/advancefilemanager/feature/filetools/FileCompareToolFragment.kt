/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.filetools

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.advancefilemanager.R
import com.advancefilemanager.databinding.FragmentFileCompareBinding
import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest

class FileCompareToolFragment : Fragment() {

    private var _binding: FragmentFileCompareBinding? = null
    private val binding get() = _binding!!

    private var file1Path: String? = null
    private var file2Path: String? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val pickFile1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                file1Path = uri.path
                binding.tvFile1Path.text = file1Path ?: getString(R.string.not_selected)
            }
        }
    }

    private val pickFile2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                file2Path = uri.path
                binding.tvFile2Path.text = file2Path ?: getString(R.string.not_selected)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFileCompareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val passedPaths = arguments?.getStringArray("filePaths")
        if (!passedPaths.isNullOrEmpty() && passedPaths.size >= 2) {
            file1Path = passedPaths[0]
            file2Path = passedPaths[1]
            binding.tvFile1Path.text = file1Path
            binding.tvFile2Path.text = file2Path
        } else {
            val rootPath = arguments?.getString("filePath")
            if (rootPath != null) {
                file1Path = rootPath
                binding.tvFile1Path.text = rootPath
            }
        }

        binding.btnSelectFile1.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pickFile1.launch(intent)
        }

        binding.btnSelectFile2.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pickFile2.launch(intent)
        }

        binding.btnCompare.setOnClickListener { compareFiles() }
    }

    private fun compareFiles() {
        val path1 = file1Path
        val path2 = file2Path

        if (path1.isNullOrEmpty() || path2.isNullOrEmpty()) {
            Toast.makeText(requireContext(), R.string.file_compare_select_both, Toast.LENGTH_SHORT).show()
            return
        }

        val f1 = File(path1)
        val f2 = File(path2)

        if (!f1.exists() || !f2.exists()) {
            Toast.makeText(requireContext(), R.string.file_not_exists, Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.resultCard.visibility = View.GONE

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                compareFileContents(f1, f2)
            }
            binding.progressBar.visibility = View.GONE
            binding.resultCard.visibility = View.VISIBLE
            binding.tvResult.text = result
        }
    }

    private fun compareFileContents(f1: File, f2: File): String {
        val sb = StringBuilder()
        sb.appendLine(getString(R.string.file_compare_result_file_header, 1, f1.name))
        sb.appendLine(getString(R.string.file_compare_result_path, f1.absolutePath))
        sb.appendLine(getString(R.string.file_compare_result_size, TrashUtil.formatFileSize(f1.length())))
        sb.appendLine(getString(R.string.file_compare_result_modified, TrashUtil.formatDate(f1.lastModified())))
        sb.appendLine()
        sb.appendLine(getString(R.string.file_compare_result_file_header, 2, f2.name))
        sb.appendLine(getString(R.string.file_compare_result_path, f2.absolutePath))
        sb.appendLine(getString(R.string.file_compare_result_size, TrashUtil.formatFileSize(f2.length())))
        sb.appendLine(getString(R.string.file_compare_result_modified, TrashUtil.formatDate(f2.lastModified())))
        sb.appendLine()

        if (f1.length() != f2.length()) {
            sb.appendLine(getString(R.string.file_compare_result_files_different))
            sb.appendLine(getString(R.string.file_compare_result_size_difference, TrashUtil.formatFileSize(kotlin.math.abs(f1.length() - f2.length()))))
            return sb.toString()
        }

        val hash1 = computeSHA256(f1)
        val hash2 = computeSHA256(f2)

        sb.appendLine(getString(R.string.file_compare_result_sha256))
        sb.appendLine("  " + getString(R.string.file_compare_result_file_header, 1, hash1))
        sb.appendLine("  " + getString(R.string.file_compare_result_file_header, 2, hash2))
        sb.appendLine()

        if (hash1 == hash2) {
            sb.appendLine(getString(R.string.file_compare_result_identical))
        } else {
            sb.appendLine(getString(R.string.file_compare_result_same_size_different))
        }
        return sb.toString()
    }

    private fun computeSHA256(file: File): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            getString(R.string.file_compare_result_hash_failed, e.message ?: "")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        _binding = null
    }
}
