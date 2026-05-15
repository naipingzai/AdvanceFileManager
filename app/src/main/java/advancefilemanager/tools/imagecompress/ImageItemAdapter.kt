/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools.imagecompress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.R
import com.advancefilemanager.databinding.ToolFileItemBinding
import com.advancefilemanager.ui.CheckableItemBackground
import com.advancefilemanager.util.FormatUtils

class ImageItemAdapter(
    private val items: List<ImageCompressFragment.ImageItem>,
    private val onItemLongClick: (Int) -> Unit = {}
) : RecyclerView.Adapter<ImageItemAdapter.ViewHolder>() {

    class ViewHolder(val binding: ToolFileItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ToolFileItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.itemLayout.background = CheckableItemBackground.create(0f, 0f, parent.context)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.getOrNull(position) ?: return
        val context = holder.binding.root.context
        holder.binding.apply {
            itemLayout.isChecked = false

            iconImage.apply {
                isVisible = true
                setImageResource(R.drawable.image_icon_white_24dp)
            }
            thumbnailImage.apply {
                isVisible = false
                setImageDrawable(null)
            }

            nameText.text = item.name
            descriptionText.text = buildString {
                append(FormatUtils.formatSize(item.originalSize))
                if (item.compressedSize > 0) {
                    append(" → ")
                    append(FormatUtils.formatSize(item.compressedSize))
                }
                if (item.status.isNotEmpty()) {
                    append(" · ")
                    append(item.status)
                }
            }
            pathText.text = item.path
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(holder.adapterPosition)
            true
        }
    }

    override fun getItemCount(): Int = items.size

}
