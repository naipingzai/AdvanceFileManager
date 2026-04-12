/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.viewer.hex

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.HexViewerFragmentBinding
import naipingzai.materialfile.provider.common.newInputStream
import naipingzai.materialfile.provider.common.size
import naipingzai.materialfile.util.ParcelableArgs
import naipingzai.materialfile.util.args
import naipingzai.materialfile.util.extraPath
import naipingzai.materialfile.util.finish
import java8.nio.file.Path
import java.io.IOException

class HexViewerFragment : Fragment() {
    private val args by args<Args>()
    private lateinit var binding: HexViewerFragmentBinding
    private lateinit var adapter: HexAdapter
    private val hexRows = mutableListOf<HexRow>()
    private var loadJob: Job? = null

    private var currentOffset = 0L
    private var totalFileSize = 0L

    companion object {
        private const val BYTES_PER_ROW = 16
        private const val CHUNK_SIZE = 16 * 1024 // 16KB per load chunk
        private const val MAX_PRELOAD_SIZE = 1024 * 1024L // 1MB initial load
        private const val LOAD_MORE_SIZE = 10 * 1024 * 1024L // 10MB per "load more"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        HexViewerFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.hex_viewer, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.action_go_to_offset -> { showGoToOffsetDialog(); true }
                    else -> false
                }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val path = args.intent.extraPath
        if (path == null) {
            finish()
            return
        }

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        activity.title = path.fileName.toString()

        adapter = HexAdapter(hexRows)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        loadHexData(path)
    }

    private fun loadHexData(path: Path) {
        binding.progress.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE

        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val fileSize = withContext(Dispatchers.IO) { path.size() }

                binding.fileSizeText.text = getString(
                    R.string.hex_viewer_file_size, formatFileSize(fileSize)
                )
                binding.fileSizeText.visibility = View.VISIBLE

                val loadSize = fileSize.coerceAtMost(MAX_PRELOAD_SIZE)
                val rows = withContext(Dispatchers.IO) {
                    val result = mutableListOf<HexRow>()
                    path.newInputStream().use { input ->
                        val buffer = ByteArray(BYTES_PER_ROW)
                        var offset = 0L
                        var totalRead = 0L
                        while (totalRead < loadSize) {
                            val bytesRead = input.read(buffer)
                            if (bytesRead == -1) break
                            val rowBytes = buffer.copyOf(bytesRead)
                            result.add(HexRow(offset, rowBytes))
                            offset += bytesRead
                            totalRead += bytesRead
                        }
                    }
                    result
                }

                hexRows.clear()
                hexRows.addAll(rows)
                adapter.notifyDataSetChanged()
                binding.progress.visibility = View.GONE

                currentOffset = loadSize
                totalFileSize = fileSize

                if (fileSize > MAX_PRELOAD_SIZE) {
                    updateLoadMoreButton(path)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.progress.visibility = View.GONE
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = e.toString()
            }
        }
    }

    private fun updateLoadMoreButton(path: Path) {
        val remaining = totalFileSize - currentOffset
        if (remaining <= 0) {
            binding.loadMoreButton.visibility = View.GONE
            return
        }
        binding.loadMoreButton.visibility = View.VISIBLE
        binding.loadMoreButton.isEnabled = true
        binding.loadMoreButton.text = getString(
            R.string.hex_viewer_load_more,
            formatFileSize(remaining)
        )
        binding.loadMoreButton.setOnClickListener {
            loadNextChunk(path)
        }
    }

    private fun loadNextChunk(path: Path) {
        binding.loadMoreButton.isEnabled = false
        binding.loadMoreButton.text = getString(R.string.hex_viewer_loading)

        val chunkEnd = (currentOffset + LOAD_MORE_SIZE).coerceAtMost(totalFileSize)

        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val rows = withContext(Dispatchers.IO) {
                    val result = mutableListOf<HexRow>()
                    path.newInputStream().use { input ->
                        var skipped = 0L
                        while (skipped < currentOffset) {
                            val n = input.skip(currentOffset - skipped)
                            if (n <= 0) break
                            skipped += n
                        }
                        val buffer = ByteArray(BYTES_PER_ROW)
                        var offset = currentOffset
                        while (offset < chunkEnd) {
                            val bytesRead = input.read(buffer)
                            if (bytesRead == -1) break
                            val rowBytes = buffer.copyOf(bytesRead)
                            result.add(HexRow(offset, rowBytes))
                            offset += bytesRead
                        }
                    }
                    result
                }

                val insertStart = hexRows.size
                hexRows.addAll(rows)
                adapter.notifyItemRangeInserted(insertStart, rows.size)
                currentOffset = chunkEnd
                updateLoadMoreButton(path)
            } catch (e: Exception) {
                binding.loadMoreButton.isEnabled = true
                binding.loadMoreButton.text = getString(R.string.hex_viewer_load_more_retry)
            }
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }



    private fun showGoToOffsetDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "0x00000000"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setPadding(48, 24, 48, 24)
        }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.hex_viewer_go_to_offset)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val text = input.text.toString().trim()
                    .removePrefix("0x").removePrefix("0X")
                try {
                    val offset = text.toLong(16)
                    val rowIndex = (offset / BYTES_PER_ROW).toInt()
                    if (rowIndex in hexRows.indices) {
                        (binding.recyclerView.layoutManager as LinearLayoutManager)
                            .scrollToPositionWithOffset(rowIndex, 0)
                    }
                } catch (_: NumberFormatException) {}
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()
    }

    @Parcelize
    class Args(val intent: Intent) : ParcelableArgs
}

data class HexRow(val offset: Long, val bytes: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HexRow) return false
        return offset == other.offset && bytes.contentEquals(other.bytes)
    }
    override fun hashCode(): Int = 31 * offset.hashCode() + bytes.contentHashCode()
}
