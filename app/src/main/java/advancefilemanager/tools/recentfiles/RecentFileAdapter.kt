/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools.recentfiles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.R
import com.advancefilemanager.databinding.ToolFileItemBinding
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.file.guessFromPath
import com.advancefilemanager.file.iconRes
import com.advancefilemanager.ui.CheckableItemBackground
import com.advancefilemanager.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentFileAdapter(
    private val items: List<RecentFilesFragment.RecentFileItem>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<RecentFileAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    class ViewHolder(val binding: ToolFileItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ToolFileItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.itemLayout.background = CheckableItemBackground.create(0f, 0f, parent.context)
        val holder = ViewHolder(binding)
        binding.itemLayout.setOnClickListener { onItemClick(holder.bindingAdapterPosition) }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.getOrNull(position) ?: return
        val context = holder.binding.root.context

        holder.binding.apply {
            itemLayout.isChecked = false

            val mimeType = if (item.isDirectory) MimeType.DIRECTORY
                else MimeType.guessFromPath(item.name)
            iconImage.apply {
                isVisible = true
                setImageResource(mimeType.iconRes)
            }
            thumbnailImage.apply {
                isVisible = false
                setImageDrawable(null)
            }

            nameText.text = item.name
            descriptionText.text = context.getString(
                R.string.recent_files_item_info,
                FormatUtils.formatSize(item.size),
                dateFormat.format(Date(item.lastModified))
            )
            pathText.text = item.path
        }
    }

    override fun getItemCount(): Int = items.size

}
