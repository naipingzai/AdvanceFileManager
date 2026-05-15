/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools.formatconvert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import com.advancefilemanager.R
import com.advancefilemanager.databinding.FormatConvertItemBinding
import com.advancefilemanager.ui.CheckableItemBackground
import com.advancefilemanager.util.FormatUtils
import java.io.File

class FormatConvertAdapter(
    private val items: List<FormatConvertFragment.ConvertItem>,
    private val onItemClick: (Int) -> Unit,
    private val onItemLongClick: (Int) -> Unit = {},
    private val onSelectionChanged: () -> Unit = {}
) : RecyclerView.Adapter<FormatConvertAdapter.ViewHolder>() {

    val selectedPositions = mutableSetOf<Int>()
    val isInSelectionMode: Boolean get() = selectedPositions.isNotEmpty()

    class ViewHolder(val binding: FormatConvertItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FormatConvertItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.itemLayout.background = CheckableItemBackground.create(0f, 0f, parent.context)
        val holder = ViewHolder(binding)
        binding.itemLayout.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            if (isInSelectionMode) {
                toggleSelection(pos)
            } else {
                onItemClick(pos)
            }
        }
        binding.itemLayout.setOnLongClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnLongClickListener false
            if (!isInSelectionMode) {
                toggleSelection(pos)
                true
            } else {
                false
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.getOrNull(position) ?: return
        holder.binding.apply {
            itemLayout.isChecked = position in selectedPositions
            itemLayout.isClickable = true

            // Thumbnail / icon
            when (item.fileType) {
                FormatConvertFragment.FileType.IMAGE,
                FormatConvertFragment.FileType.VIDEO -> {
                    iconImage.isVisible = false
                    thumbnailImage.apply {
                        dispose()
                        setImageDrawable(null)
                        isVisible = true
                        load(File(item.path)) {
                            size(160, 160)
                            crossfade(true)
                        }
                    }
                }
                FormatConvertFragment.FileType.AUDIO -> {
                    thumbnailImage.dispose()
                    thumbnailImage.isVisible = false
                    thumbnailImage.setImageDrawable(null)
                    iconImage.isVisible = true
                    iconImage.setImageResource(R.drawable.audio_icon_white_24dp)
                }
                else -> {
                    thumbnailImage.dispose()
                    thumbnailImage.isVisible = false
                    thumbnailImage.setImageDrawable(null)
                    iconImage.isVisible = true
                    iconImage.setImageResource(R.drawable.file_icon_white_24dp)
                }
            }

            // File name
            nameText.text = item.name

            // Description: type · size · media info
            val typeTag = FormatConvertFragment.getFileTypeLabel(root.context, item.fileType)
            descriptionText.text = buildString {
                append(typeTag)
                append(" · ")
                append(FormatUtils.formatSize(item.size))
                if (item.mediaInfo.isNotEmpty()) {
                    append(" · ")
                    append(item.mediaInfo)
                }
            }

            // Format conversion chips: INPUT → OUTPUT
            inputFormatChip.text = item.inputFormat.uppercase()
            outputFormatChip.text = item.outputFormat.uppercase()

            // Path
            pathText.text = item.path

            // Status
            if (item.status.isNotEmpty()) {
                statusText.isVisible = true
                statusText.text = buildString {
                    append(item.status)
                    if (item.progress in 0..99) {
                        append(" ${item.progress}%")
                    }
                }
            } else {
                statusText.isVisible = false
            }

            // Per-item progress bar
            if (item.progress in 0..99 && item.isConverting) {
                itemProgressBar.isVisible = true
                itemProgressBar.progress = item.progress
            } else {
                itemProgressBar.isVisible = false
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun toggleSelection(position: Int) {
        if (position in selectedPositions) {
            selectedPositions.remove(position)
        } else {
            selectedPositions.add(position)
        }
        notifyItemChanged(position)
        onSelectionChanged()
    }

    fun selectAll() {
        selectedPositions.clear()
        for (i in items.indices) selectedPositions.add(i)
        notifyDataSetChanged()
        onSelectionChanged()
    }

    fun clearSelection() {
        val old = selectedPositions.toSet()
        selectedPositions.clear()
        old.forEach { notifyItemChanged(it) }
        onSelectionChanged()
    }

}
