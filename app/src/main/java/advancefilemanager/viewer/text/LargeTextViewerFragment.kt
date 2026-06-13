/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.text

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import com.advancefilemanager.R
import com.advancefilemanager.databinding.LargeTextViewerFragmentBinding
import com.advancefilemanager.provider.common.newInputStream
import com.advancefilemanager.provider.common.size
import com.advancefilemanager.util.ParcelableArgs
import com.advancefilemanager.util.args
import com.advancefilemanager.util.extraPath
import com.advancefilemanager.util.finish
import java8.nio.file.Path
import java.io.ByteArrayOutputStream
import com.advancefilemanager.ui.applyOverlay
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class LargeTextViewerFragment : Fragment() {
    private val fragmentArgs by args<Args>()
    private lateinit var binding: LargeTextViewerFragmentBinding
    private lateinit var adapter: TextLineAdapter
    private val lines = mutableListOf<String>()

    private var loadJob: Job? = null
    private var nextByteOffset = 0L
    private var isFullyLoaded = false
    private var isLoading = false
    private var totalFileSize = 0L
    private var encoding: Charset = StandardCharsets.UTF_8
    private var filePath: Path? = null

    companion object {
        private const val CHUNK_BYTE_SIZE = 512 * 1024
        private const val AUTO_LOAD_THRESHOLD = 200
        private val LINE_SPLIT_REGEX = Regex("\\r\\n|\\n|\\r")
        private val COMMON_CHARSETS = listOf(
            "UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE", "UTF-32",
            "GBK", "GB2312", "GB18030", "Big5",
            "Shift_JIS", "EUC-JP", "ISO-2022-JP",
            "EUC-KR",
            "ISO-8859-1", "ISO-8859-15",
            "Windows-1252", "Windows-1251",
            "US-ASCII"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        LargeTextViewerFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.large_text_viewer, menu)
                val encItem = menu.findItem(R.id.action_encoding)
                encodingSubMenu = encItem.subMenu
                encodingSubMenu?.let { sub ->
                    for (name in COMMON_CHARSETS) {
                        val cs = runCatching { Charset.forName(name) }.getOrNull() ?: continue
                        sub.add(Menu.NONE, Menu.FIRST, Menu.NONE, cs.displayName())
                            .titleCondensed = cs.name()
                    }
                    sub.setGroupCheckable(Menu.NONE, true, true)
                    updateEncodingMenu()
                }
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.action_go_to_line -> { showGoToLineDialog(); true }
                    Menu.FIRST -> {
                        val charsetName = menuItem.titleCondensed?.toString()
                        if (charsetName != null) {
                            val newEncoding = Charset.forName(charsetName)
                            reloadWithEncoding(newEncoding)
                        }
                        true
                    }
                    else -> false
                }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val path = fragmentArgs.intent.extraPath
        if (path == null) {
            finish()
            return
        }
        filePath = path

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.title = path.fileName.toString()

        adapter = TextLineAdapter(lines)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Auto-load when scrolling near the bottom
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0 || isFullyLoaded || isLoading) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (lastVisible >= lines.size - AUTO_LOAD_THRESHOLD) {
                    loadNextChunk()
                }
            }
        })

        loadFile(path)
    }

    private fun loadFile(path: Path) {
        // Reset state
        lines.clear()
        adapter.notifyDataSetChanged()
        nextByteOffset = 0L
        isFullyLoaded = false
        isLoading = false

        binding.progress.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE

        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                totalFileSize = withContext(Dispatchers.IO) {
                    try { path.size() } catch (_: Exception) { -1L }
                }
                loadNextChunk()
                binding.progress.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                binding.progress.visibility = View.GONE
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = e.toString()
            }
        }
    }

    private fun loadNextChunk() {
        if (isFullyLoaded || isLoading) return
        val path = filePath ?: return
        isLoading = true
        binding.loadingMore.visibility = View.VISIBLE

        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    readChunk(path, nextByteOffset, encoding)
                }

                if (!isActive) return@launch

                val insertStart = lines.size
                lines.addAll(result.lines)
                adapter.notifyItemRangeInserted(insertStart, result.lines.size)
                adapter.updateLineNumberWidth(lines.size)
                nextByteOffset = result.newByteOffset
                isFullyLoaded = result.isEof
                updateInfoBar()
            } catch (e: Exception) {
                e.printStackTrace()
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = e.toString()
            } finally {
                isLoading = false
                binding.loadingMore.visibility = View.GONE
                binding.progress.visibility = View.GONE
            }
        }
    }

    private data class ChunkResult(
        val lines: List<String>,
        val newByteOffset: Long,
        val isEof: Boolean
    )

    private fun readChunk(
        path: Path,
        fromOffset: Long,
        charset: Charset
    ): ChunkResult {
        java.io.RandomAccessFile(path.toFile(), "r").use { raf ->
            raf.seek(fromOffset)

            val temp = ByteArray(CHUNK_BYTE_SIZE)
            var totalRead = 0
            while (totalRead < CHUNK_BYTE_SIZE) {
                val n = raf.read(temp, totalRead, CHUNK_BYTE_SIZE - totalRead)
                if (n == -1) break
                totalRead += n
            }

            if (totalRead == 0) {
                return ChunkResult(emptyList(), fromOffset, true)
            }

            val bytes = if (totalRead == temp.size) temp else temp.copyOf(totalRead)
            val isEof = totalRead < CHUNK_BYTE_SIZE
            val rawText = String(bytes, charset)

            if (isEof) {
                val resultLines = splitLines(rawText)
                return ChunkResult(resultLines, fromOffset + totalRead, true)
            }

            val lastLf = rawText.lastIndexOf('\n')
            val lastCr = rawText.lastIndexOf('\r')
            val lastNewline = maxOf(lastLf, lastCr)

            if (lastNewline < 0) {
                return ChunkResult(listOf(rawText), fromOffset + totalRead, false)
            }

            val completeText = rawText.substring(0, lastNewline + 1)
            val resultLines = splitLines(completeText)
            val bytesUsed = completeText.toByteArray(charset).size.toLong()
            return ChunkResult(resultLines, fromOffset + bytesUsed, false)
        }
    }

    private fun splitLines(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        // Split on any line ending: \r\n, \n, \r
        val result = text.split(LINE_SPLIT_REGEX)
        // If text ends with a newline, split leaves an empty string at the end — remove it
        return if (result.lastOrNull()?.isEmpty() == true) result.dropLast(1) else result
    }

    private fun updateInfoBar() {
        val sizeStr = if (totalFileSize > 0) formatFileSize(totalFileSize) else "?"
        val loadedStr = formatFileSize(nextByteOffset)
        val pct = if (totalFileSize > 0) (nextByteOffset * 100 / totalFileSize).toInt() else 0
        val status = if (isFullyLoaded) {
            getString(R.string.large_text_viewer_info_loaded, lines.size, sizeStr)
        } else {
            getString(
                R.string.large_text_viewer_info_partial,
                lines.size, pct, loadedStr, sizeStr
            )
        }
        binding.infoBar.text = status
        binding.infoBar.visibility = View.VISIBLE
    }

    private fun formatFileSize(bytes: Long): String {
        return com.advancefilemanager.util.FormatUtils.formatSize(bytes)
    }

    private fun reloadWithEncoding(newEncoding: Charset) {
        encoding = newEncoding
        val path = filePath ?: return
        loadFile(path)
    }

    // region Menu

    private var encodingSubMenu: android.view.SubMenu? = null



    private fun updateEncodingMenu() {
        val sub = encodingSubMenu ?: return
        val charsetName = encoding.name()
        sub.children.find { it.titleCondensed == charsetName }?.isChecked = true
    }

    private fun showGoToLineDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "1"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.large_text_viewer_go_to_line)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val lineNum = input.text.toString().toIntOrNull() ?: return@setPositiveButton
                val position = (lineNum - 1).coerceIn(0, lines.size - 1)
                (binding.recyclerView.layoutManager as? LinearLayoutManager)
                    ?.scrollToPositionWithOffset(position, 0)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .applyOverlay(requireContext())
            .show()
        input.requestFocus()
    }

    // endregion

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()
    }

    @Parcelize
    class Args(val intent: Intent) : ParcelableArgs
}
