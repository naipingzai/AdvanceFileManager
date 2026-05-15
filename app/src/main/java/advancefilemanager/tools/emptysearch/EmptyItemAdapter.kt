/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools.emptysearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.R
import com.advancefilemanager.databinding.ToolFileItemBinding
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.file.iconRes
import com.advancefilemanager.ui.CheckableItemBackground
import com.advancefilemanager.util.FormatUtils

class EmptyItemAdapter(
    private val items: List<EmptySearchFragment.EmptyItem>,
    private val onItemClick: (Int) -> Unit,
    private val onItemLongClick: (Int) -> Unit,
    private val onIconClick: (Int) -> Unit
) : RecyclerView.Adapter<EmptyItemAdapter.ViewHolder>() {

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
            itemLayout.isChecked = item.isChecked
            itemLayout.setOnClickListener { onItemClick(holder.bindingAdapterPosition) }
            itemLayout.setOnLongClickListener {
                onItemLongClick(holder.bindingAdapterPosition)
                true
            }
            iconLayout.setOnClickListener { onIconClick(holder.bindingAdapterPosition) }

            val mimeType = if (item.isDirectory) MimeType.DIRECTORY else MimeType.GENERIC
            iconImage.apply {
                isVisible = true
                setImageResource(mimeType.iconRes)
            }
            thumbnailImage.apply {
                isVisible = false
                setImageDrawable(null)
            }

            nameText.text = item.name
            descriptionText.text = if (item.isDirectory) {
                context.getString(R.string.empty_search_type_folder) + " (" + FormatUtils.formatSize(item.size) + ")"
            } else {
                context.getString(R.string.empty_search_type_file) + " (" + FormatUtils.formatSize(item.size) + ")"
            }
            pathText.text = item.path
        }
    }

    override fun getItemCount(): Int = items.size
}
