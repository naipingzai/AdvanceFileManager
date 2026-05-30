/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.databinding.ItemPluginBinding
import com.advancefilemanager.databinding.ItemPluginFeatureBinding
import com.advancefilemanager.plugin.protocol.PluginFeature
import com.advancefilemanager.plugin.protocol.PluginInfo

class PluginListAdapter(
    private val plugins: List<PluginInfo>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private sealed class ListItem {
        data class PluginHeader(val plugin: PluginInfo) : ListItem()
        data class FeatureItem(val feature: PluginFeature, val plugin: PluginInfo) : ListItem()
    }

    private val items = mutableListOf<ListItem>()

    init {
        rebuildItems()
    }

    private fun rebuildItems() {
        items.clear()
        for (plugin in plugins) {
            items.add(ListItem.PluginHeader(plugin))
            if (PluginSettings.isPluginEnabled(plugin.id)) {
                for (feature in plugin.features) {
                    items.add(ListItem.FeatureItem(feature, plugin))
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ListItem.PluginHeader -> VIEW_TYPE_PLUGIN
        is ListItem.FeatureItem -> VIEW_TYPE_FEATURE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_PLUGIN -> PluginViewHolder(
                ItemPluginBinding.inflate(inflater, parent, false)
            )
            else -> FeatureViewHolder(
                ItemPluginFeatureBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.PluginHeader -> (holder as PluginViewHolder).bind(item.plugin)
            is ListItem.FeatureItem -> (holder as FeatureViewHolder).bind(item.feature)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class PluginViewHolder(
        private val binding: ItemPluginBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(plugin: PluginInfo) {
            binding.pluginTitle.text = plugin.title
            binding.pluginDescription.text = plugin.description
            binding.pluginSwitch.setOnCheckedChangeListener(null)
            binding.pluginSwitch.isChecked = PluginSettings.isPluginEnabled(plugin.id)
            binding.pluginSwitch.setOnCheckedChangeListener { _, isChecked ->
                PluginSettings.setPluginEnabled(plugin.id, isChecked)
                rebuildItems()
                notifyDataSetChanged()
            }
            binding.root.setOnClickListener {
                binding.pluginSwitch.toggle()
            }
        }
    }

    inner class FeatureViewHolder(
        private val binding: ItemPluginFeatureBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(feature: PluginFeature) {
            binding.featureTitle.text = feature.title
            binding.featureDescription.text = feature.description
            binding.featureDescription.visibility =
                if (feature.description.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.featureSwitch.setOnCheckedChangeListener(null)
            binding.featureSwitch.isChecked = PluginSettings.isFeatureEnabled(feature.id)
            binding.featureSwitch.setOnCheckedChangeListener { _, isChecked ->
                PluginSettings.setFeatureEnabled(feature.id, isChecked)
            }
            binding.root.setOnClickListener {
                binding.featureSwitch.toggle()
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_PLUGIN = 0
        private const val VIEW_TYPE_FEATURE = 1
    }
}
