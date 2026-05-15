/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools.encryption

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

class EncryptionFileAdapter(
    private val items: List<EncryptionFragment.EncryptionFileItem>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<EncryptionFileAdapter.ViewHolder>() {

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
            itemLayout.setOnLongClickListener {
                onRemoveClick(holder.bindingAdapterPosition)
                true
            }

            iconImage.apply {
                isVisible = true
                setImageResource(
                    if (item.name.endsWith(".enc"))
                        R.drawable.lock_icon_white_24dp
                    else
                        MimeType.GENERIC.iconRes
                )
            }
            thumbnailImage.apply {
                isVisible = false
                setImageDrawable(null)
            }

            nameText.text = item.name
            descriptionText.text = buildString {
                append(FormatUtils.formatSize(item.size))
                if (item.status.isNotEmpty()) {
                    append(" · ")
                    append(item.status)
                }
            }
            pathText.text = item.path
        }
    }

    override fun getItemCount(): Int = items.size

}
