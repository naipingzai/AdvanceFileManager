/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.emptysearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.ToolFileItemBinding
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.file.iconRes
import naipingzai.materialfile.ui.CheckableItemBackground

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
                context.getString(R.string.empty_search_type_folder)
            } else {
                context.getString(R.string.empty_search_type_file)
            }
            pathText.text = item.path
        }
    }

    override fun getItemCount(): Int = items.size
}
