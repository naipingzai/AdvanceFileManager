/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools.filecompare

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import com.advancefilemanager.R
import com.advancefilemanager.databinding.FileCompareFragmentBinding
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.filelist.FileListActivity
import com.advancefilemanager.util.FormatUtils
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class FileCompareFragment : Fragment() {
    private lateinit var binding: FileCompareFragmentBinding
    private var file1Path: String? = null
    private var file2Path: String? = null

    private val file1PickerLauncher = registerForActivityResult(
        FileListActivity.OpenFileContract()
    ) { path: java8.nio.file.Path? ->
        path ?: return@registerForActivityResult
        file1Path = path.toFile().absolutePath
        binding.file1Text.text = file1Path
    }

    private val file2PickerLauncher = registerForActivityResult(
        FileListActivity.OpenFileContract()
    ) { path: java8.nio.file.Path? ->
        path ?: return@registerForActivityResult
        file2Path = path.toFile().absolutePath
        binding.file2Text.text = file2Path
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FileCompareFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val embedded = arguments?.getBoolean("embedded") == true
        if (embedded) {
            (binding.toolbar.parent as? android.view.View)?.visibility = android.view.View.GONE
        } else {
            val activity = requireActivity() as AppCompatActivity
            activity.setSupportActionBar(binding.toolbar)
            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        binding.selectFile1Button.setOnClickListener { file1PickerLauncher.launch(listOf(MimeType.ANY)) }
        binding.selectFile2Button.setOnClickListener { file2PickerLauncher.launch(listOf(MimeType.ANY)) }
        binding.compareButton.setOnClickListener { compareFiles() }

        if (savedInstanceState != null) {
            file1Path = savedInstanceState.getString(KEY_FILE1)
            file2Path = savedInstanceState.getString(KEY_FILE2)
            file1Path?.let { binding.file1Text.text = it }
            file2Path?.let { binding.file2Text.text = it }
        } else {
            // Auto-fill from arguments / intent (when launched from a file)
            val extraPath = arguments?.getString(EXTRA_PATH)
                ?: activity?.intent?.getStringExtra(EXTRA_PATH)
            if (!extraPath.isNullOrBlank()) {
                val f = java.io.File(extraPath)
                if (f.isFile) {
                    file1Path = f.absolutePath
                    binding.file1Text.text = file1Path
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_FILE1, file1Path)
        outState.putString(KEY_FILE2, file2Path)
    }

    companion object {
        const val EXTRA_PATH = "extra_path"
        private const val KEY_FILE1 = "file1_path"
        private const val KEY_FILE2 = "file2_path"
        private const val MAX_TEXT_SIZE = 512 * 1024L // 512 KB max for text comparison
        private const val MAX_DIFF_LINES = 5000 // Max lines for LCS diff to prevent OOM
    }

    private data class CompareStrings(
        val infoHeader: String,
        val file1Info: String,
        val file2Info: String,
        val sizeDiff: String,
        val sizeSame: String,
        val identical: String,
        val different: String,
        val textDiffHeader: String,
        val binaryFiles: String,
        val filesTooLarge: String,
        val noTextDiff: String,
        val diffCountFormat: String,
        val truncatedFormat: String,
        val colorError: Int,
        val colorSuccess: Int
    )

    private fun compareFiles() {
        val path1 = file1Path
        val path2 = file2Path
        if (path1 == null || path2 == null) {
            Snackbar.make(binding.root, R.string.file_compare_select_both, Snackbar.LENGTH_SHORT).show()
            return
        }

        val f1 = File(path1)
        val f2 = File(path2)
        if (!f1.exists() || !f2.exists()) {
            Snackbar.make(binding.root, R.string.file_compare_file_not_found, Snackbar.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.isVisible = true
        binding.compareButton.isEnabled = false
        binding.resultScrollView.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            // Pre-fetch all string resources on main thread
            val strings = CompareStrings(
                infoHeader = getString(R.string.file_compare_info_header),
                file1Info = getString(R.string.file_compare_file1_info, f1.name, FormatUtils.formatSize(f1.length())),
                file2Info = getString(R.string.file_compare_file2_info, f2.name, FormatUtils.formatSize(f2.length())),
                sizeDiff = getString(R.string.file_compare_size_diff),
                sizeSame = getString(R.string.file_compare_size_same),
                identical = getString(R.string.file_compare_identical),
                different = getString(R.string.file_compare_different),
                textDiffHeader = getString(R.string.file_compare_text_diff),
                binaryFiles = getString(R.string.file_compare_binary_files),
                filesTooLarge = getString(R.string.file_compare_files_too_large),
                noTextDiff = getString(R.string.file_compare_no_text_diff),
                diffCountFormat = getString(R.string.file_compare_diff_count),
                truncatedFormat = getString(R.string.file_compare_truncated),
                colorError = MaterialColors.getColor(requireView(), com.google.android.material.R.attr.colorError, Color.RED),
                colorSuccess = MaterialColors.getColor(requireView(), com.google.android.material.R.attr.colorPrimary, Color.parseColor("#4CAF50"))
            )
            try {
                val result = withContext(Dispatchers.IO) {
                    compareFilesInternal(f1, f2, strings)
                }
                binding.resultScrollView.isVisible = true
                binding.resultText.text = result
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Snackbar.make(binding.root, R.string.open_file_error, Snackbar.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.isVisible = false
                binding.compareButton.isEnabled = true
            }
        }
    }

    private suspend fun compareFilesInternal(f1: File, f2: File, strings: CompareStrings): SpannableStringBuilder {
        val sb = SpannableStringBuilder()

        // Basic info
        appendHeader(sb, strings.infoHeader)
        appendLine(sb, strings.file1Info)
        appendLine(sb, strings.file2Info)
        appendLine(sb, "")

        // Size comparison
        if (f1.length() != f2.length()) {
            appendColored(sb, strings.sizeDiff, strings.colorError)
            appendLine(sb, "")
        } else {
            appendColored(sb, strings.sizeSame, strings.colorSuccess)
            appendLine(sb, "")
        }

        // Hash comparison
        val hash1 = computeMD5(f1)
        val hash2 = computeMD5(f2)
        appendLine(sb, "MD5 File1: $hash1")
        appendLine(sb, "MD5 File2: $hash2")
        if (hash1 == hash2) {
            appendColored(sb, strings.identical, strings.colorSuccess)
            appendLine(sb, "")
            return sb
        } else {
            appendColored(sb, strings.different, strings.colorError)
            appendLine(sb, "")
        }

        // Text diff (only for small text files)
        if (f1.length() <= MAX_TEXT_SIZE && f2.length() <= MAX_TEXT_SIZE) {
            try {
                val lines1 = f1.readLines()
                val lines2 = f2.readLines()
                if (lines1.size > MAX_DIFF_LINES || lines2.size > MAX_DIFF_LINES) {
                    appendLine(sb, "")
                    appendHeader(sb, strings.textDiffHeader)
                    appendLine(sb, strings.filesTooLarge)
                } else {
                    appendLine(sb, "")
                    appendHeader(sb, strings.textDiffHeader)
                    appendTextDiff(sb, lines1, lines2, strings)
                }
            } catch (e: Exception) {
                appendLine(sb, strings.binaryFiles)
            }
        } else {
            appendLine(sb, strings.filesTooLarge)
        }

        return sb
    }

    private fun appendTextDiff(
        sb: SpannableStringBuilder,
        lines1: List<String>,
        lines2: List<String>,
        strings: CompareStrings
    ) {
        val displayLimit = 200 // Limit diff display
        var diffCount = 0

        // Use LCS (Longest Common Subsequence) to compute a meaningful diff
        val edits = computeLcsDiff(lines1, lines2)

        for (edit in edits) {
            if (diffCount >= displayLimit) break
            when (edit) {
                is DiffEdit.Equal -> { /* skip unchanged lines */ }
                is DiffEdit.Delete -> {
                    appendColored(sb, "- ${edit.lineNum}: ${edit.text}", strings.colorError)
                    appendLine(sb, "")
                    diffCount++
                }
                is DiffEdit.Insert -> {
                    appendColored(sb, "+ ${edit.lineNum}: ${edit.text}", strings.colorSuccess)
                    appendLine(sb, "")
                    diffCount++
                }
            }
        }

        if (diffCount == 0) {
            appendLine(sb, strings.noTextDiff)
        } else {
            val totalDiffs = edits.count { it !is DiffEdit.Equal }
            appendLine(sb, String.format(strings.diffCountFormat, totalDiffs))
        }

        if (edits.count { it !is DiffEdit.Equal } > displayLimit) {
            appendLine(sb, String.format(strings.truncatedFormat, displayLimit))
        }
    }

    private sealed class DiffEdit {
        data class Equal(val lineNum: Int, val text: String) : DiffEdit()
        data class Delete(val lineNum: Int, val text: String) : DiffEdit()
        data class Insert(val lineNum: Int, val text: String) : DiffEdit()
    }

    /**
     * Simple O(ND) / O(NM) LCS-based diff. For very large inputs this could be
     * slow, but we already cap text at 512 KB / 200 display lines.
     */
    private fun computeLcsDiff(a: List<String>, b: List<String>): List<DiffEdit> {
        val n = a.size
        val m = b.size

        // Build LCS table (space-optimized would be better for huge files,
        // but we limit to 512KB text so this is fine)
        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                dp[i][j] = if (a[i] == b[j]) {
                    dp[i + 1][j + 1] + 1
                } else {
                    maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }

        // Trace back to produce edit list
        val result = mutableListOf<DiffEdit>()
        var i = 0
        var j = 0
        while (i < n || j < m) {
            when {
                i < n && j < m && a[i] == b[j] -> {
                    result.add(DiffEdit.Equal(i + 1, a[i]))
                    i++; j++
                }
                j < m && (i >= n || dp[i][j + 1] >= dp[i + 1][j]) -> {
                    result.add(DiffEdit.Insert(j + 1, b[j]))
                    j++
                }
                else -> {
                    result.add(DiffEdit.Delete(i + 1, a[i]))
                    i++
                }
            }
        }
        return result
    }

    private fun appendHeader(sb: SpannableStringBuilder, text: String) {
        val start = sb.length
        sb.append(text)
        sb.append("\n")
        sb.setSpan(
            StyleSpan(Typeface.BOLD), start, start + text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun appendLine(sb: SpannableStringBuilder, text: String) {
        sb.append(text)
        sb.append("\n")
    }

    private fun appendColored(sb: SpannableStringBuilder, text: String, color: Int) {
        val start = sb.length
        sb.append(text)
        sb.setSpan(
            ForegroundColorSpan(color), start, sb.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private suspend fun computeMD5(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                coroutineContext.ensureActive()
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

}
