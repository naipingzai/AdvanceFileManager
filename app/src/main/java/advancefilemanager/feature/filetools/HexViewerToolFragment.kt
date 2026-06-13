/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.filetools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.advancefilemanager.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

class HexViewerToolFragment : Fragment() {

    private lateinit var fileInfoText: TextView
    private lateinit var hexContent: TextView
    private lateinit var loadMoreButton: Button
    private lateinit var scrollView: ScrollView

    private var currentFile: File? = null
    private var currentOffset = 0L
    private val hexBuilder = StringBuilder()

    companion object {
        private const val BYTES_PER_ROW = 16
        private const val CHUNK_SIZE = 4096L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_hex_viewer_tool, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fileInfoText = view.findViewById(R.id.fileInfoText)
        hexContent = view.findViewById(R.id.hexContent)
        loadMoreButton = view.findViewById(R.id.loadMoreButton)
        scrollView = view.findViewById(R.id.hexScrollView)

        loadMoreButton.setOnClickListener { loadMore() }

        val path = arguments?.getString("filePath")
        if (path != null) {
            val file = File(path)
            if (file.exists() && file.isFile) {
                currentFile = file
                fileInfoText.isVisible = true
                fileInfoText.text = "${file.name} · ${TrashUtil.formatFileSize(file.length())}"
                loadMore()
            } else {
                fileInfoText.isVisible = true
                fileInfoText.text = "文件不存在: $path"
            }
        } else {
            fileInfoText.isVisible = true
            fileInfoText.text = "未指定文件"
        }
    }

    private fun loadMore() {
        val file = currentFile ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            loadMoreButton.isVisible = false
            val (text, hasMore) = withContext(Dispatchers.IO) {
                readChunk(file, currentOffset)
            }
            hexBuilder.append(text)
            hexContent.text = hexBuilder.toString()
            loadMoreButton.isVisible = hasMore
            scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        }
    }

    private fun readChunk(file: File, offset: Long): Pair<String, Boolean> {
        val sb = StringBuilder()
        var bytesRead = 0L
        RandomAccessFile(file, "r").use { raf ->
            raf.seek(offset)
            val buffer = ByteArray(BYTES_PER_ROW)
            while (bytesRead < CHUNK_SIZE) {
                val read = raf.read(buffer)
                if (read <= 0) break
                val rowOffset = offset + bytesRead
                sb.append(String.format("%08X  ", rowOffset))
                for (i in 0 until BYTES_PER_ROW) {
                    if (i < read) {
                        sb.append(String.format("%02X ", buffer[i].toInt() and 0xFF))
                    } else {
                        sb.append("   ")
                    }
                    if (i == 7) sb.append(" ")
                }
                sb.append(" |")
                for (i in 0 until read) {
                    val b = buffer[i].toInt() and 0xFF
                    sb.append(if (b in 0x20..0x7E) b.toChar() else '.')
                }
                sb.append("|\n")
                bytesRead += read
            }
            currentOffset = offset + bytesRead
            val hasMore = currentOffset < file.length()
            return Pair(sb.toString(), hasMore)
        }
    }
}
