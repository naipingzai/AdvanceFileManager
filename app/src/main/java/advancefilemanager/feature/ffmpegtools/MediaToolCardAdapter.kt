/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.ffmpegtools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.databinding.ItemMediaToolCardBinding

class MediaToolCardAdapter(
    private val features: List<MediaToolFeature>,
    private val onFeatureClick: (MediaToolFeature) -> Unit
) : RecyclerView.Adapter<MediaToolCardAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMediaToolCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onFeatureClick(features[pos])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaToolCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feature = features[position]
        holder.binding.icon.setImageResource(feature.iconRes)
        holder.binding.title.setText(feature.titleRes)
        holder.binding.description.setText(feature.descriptionRes)
    }

    override fun getItemCount(): Int = features.size
}
