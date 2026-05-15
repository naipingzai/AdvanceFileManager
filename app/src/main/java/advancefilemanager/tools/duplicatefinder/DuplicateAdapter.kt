/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools.duplicatefinder

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import java8.nio.file.Path
import java8.nio.file.Paths
import java8.nio.file.attribute.BasicFileAttributes
import com.advancefilemanager.R
import com.advancefilemanager.databinding.DuplicateGroupHeaderBinding
import com.advancefilemanager.databinding.ToolFileItemBinding
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.file.guessFromPath
import com.advancefilemanager.file.iconRes
import com.advancefilemanager.file.isImage
import com.advancefilemanager.file.isMedia
import com.advancefilemanager.provider.common.readAttributes
import com.advancefilemanager.ui.CheckableItemBackground

class DuplicateAdapter(
    private val groups: List<DuplicateFinderFragment.DuplicateGroup>,
    private val onFileCheck: (groupIndex: Int, fileIndex: Int) -> Unit,
    private val onFileOpen: (groupIndex: Int, fileIndex: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_FILE = 1
    }

    data class FlatItem(
        val groupIndex: Int,
        val fileIndex: Int, // -1 for header
        val isHeader: Boolean
    )

    private var flatList: List<FlatItem> = buildFlatList()

    fun refreshFlatList() {
        flatList = buildFlatList()
    }

    /**
     * Find the position in the flat list for a specific (groupIndex, fileIndex) pair.
     * Returns -1 if not found.
     */
    fun findFlatPosition(groupIndex: Int, fileIndex: Int): Int =
        flatList.indexOfFirst { it.groupIndex == groupIndex && it.fileIndex == fileIndex }

    private fun buildFlatList(): List<FlatItem> {
        val list = mutableListOf<FlatItem>()
        groups.forEachIndexed { groupIndex, group ->
            list.add(FlatItem(groupIndex, -1, true))
            group.files.forEachIndexed { fileIndex, _ ->
                list.add(FlatItem(groupIndex, fileIndex, false))
            }
        }
        return list
    }

    class HeaderViewHolder(val binding: DuplicateGroupHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    class FileViewHolder(val binding: ToolFileItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (flatList[position].isHeader) VIEW_TYPE_HEADER else VIEW_TYPE_FILE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(DuplicateGroupHeaderBinding.inflate(inflater, parent, false))
        } else {
            val binding = ToolFileItemBinding.inflate(inflater, parent, false)
            binding.itemLayout.background =
                CheckableItemBackground.create(0f, 0f, parent.context)
            val holder = FileViewHolder(binding)
            binding.itemLayout.setOnClickListener {
                val flat = flatList.getOrNull(holder.bindingAdapterPosition) ?: return@setOnClickListener
                onFileOpen(flat.groupIndex, flat.fileIndex)
            }
            binding.itemLayout.setOnLongClickListener {
                val flat = flatList.getOrNull(holder.bindingAdapterPosition) ?: return@setOnLongClickListener false
                onFileCheck(flat.groupIndex, flat.fileIndex)
                true
            }
            binding.iconLayout.setOnClickListener {
                val flat = flatList.getOrNull(holder.bindingAdapterPosition) ?: return@setOnClickListener
                onFileCheck(flat.groupIndex, flat.fileIndex)
            }
            holder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val flatItem = flatList.getOrNull(position) ?: return
        val group = groups.getOrNull(flatItem.groupIndex) ?: return

        when (holder) {
            is HeaderViewHolder -> {
                val context = holder.binding.root.context
                holder.binding.apply {
                    headerTitle.text = context.getString(
                        R.string.duplicate_group_header,
                        flatItem.groupIndex + 1,
                        group.files.size
                    )
                    val size = android.text.format.Formatter.formatFileSize(context, group.size)
                    headerDescription.text = context.getString(
                        R.string.duplicate_group_size, size
                    ) + " · " + context.getString(
                        R.string.duplicate_group_hash, group.hash.take(16)
                    )
                }
            }
            is FileViewHolder -> {
                val file = group.files.getOrNull(flatItem.fileIndex) ?: return
                val context = holder.binding.root.context
                holder.binding.apply {
                    itemLayout.isChecked = file.isChecked

                    val mimeType = MimeType.guessFromPath(file.name)
                    val iconResId = mimeType.iconRes
                    iconImage.apply {
                        isVisible = true
                        setImageResource(iconResId)
                    }

                    // Load thumbnail for images and media files
                    thumbnailImage.apply {
                        dispose()
                        setImageDrawable(null)
                        val supportsThumbnail = mimeType.isImage || mimeType.isMedia
                        isVisible = supportsThumbnail
                        if (supportsThumbnail) {
                            try {
                                val nioPath: Path = Paths.get(file.path)
                                val attrs = nioPath.readAttributes(
                                    BasicFileAttributes::class.java
                                )
                                load(nioPath to attrs) {
                                    listener { _, _ ->
                                        iconImage.isVisible = false
                                    }
                                }
                            } catch (_: Exception) {
                                isVisible = false
                            }
                        }
                    }

                    nameText.text = file.name
                    val lastModified = DateUtils.getRelativeTimeSpanString(
                        file.lastModified, System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE
                    )
                    val size = android.text.format.Formatter.formatFileSize(context, file.size)
                    val separator = context.getString(R.string.file_item_description_separator)
                    descriptionText.text = listOf(lastModified, size).joinToString(separator)
                    pathText.text = file.path
                }
            }
        }
    }

    override fun getItemCount(): Int = flatList.size
}
