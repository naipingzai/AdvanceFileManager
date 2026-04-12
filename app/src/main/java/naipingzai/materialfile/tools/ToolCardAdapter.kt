/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import naipingzai.materialfile.databinding.ToolCardItemBinding

class ToolCardAdapter(
    private val items: List<ToolItem>,
    private val onItemClick: (ToolItem) -> Unit
) : RecyclerView.Adapter<ToolCardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ToolCardItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ToolCardItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.binding.root.context
        holder.binding.apply {
            cardView.setOnClickListener { onItemClick(item) }
            featureIcon.setImageResource(item.iconRes)
            featureTitle.text = context.getString(item.titleRes)
            featureDescription.text = context.getString(item.descriptionRes)
        }
    }

    override fun getItemCount(): Int = items.size
}
