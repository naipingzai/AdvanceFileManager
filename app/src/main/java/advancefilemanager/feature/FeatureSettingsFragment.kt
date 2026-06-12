/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.R
import com.advancefilemanager.feature.protocol.FeatureInfo
import com.advancefilemanager.feature.protocol.FeatureSubItem
import com.google.android.material.materialswitch.MaterialSwitch

/**
 * Settings fragment for managing feature visibility in the file operation bar.
 * Shows individual sub-features as toggleable items grouped by parent feature.
 */
class FeatureSettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.feature_settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setTitle(R.string.feature_settings_title)
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val features = FeatureManager.getAllFeatures()
        val adapter = FeatureAdapter(features)
        recyclerView.adapter = adapter
    }

    /**
     * RecyclerView item: either a group header (parent feature) or a sub-feature toggle.
     */
    private sealed class ListItem {
        data class GroupHeader(val feature: FeatureInfo) : ListItem()
        data class SubFeatureItem(val feature: FeatureInfo, val sub: FeatureSubItem) : ListItem()
        data class SingleFeatureItem(val feature: FeatureInfo) : ListItem()
    }

    private class FeatureAdapter(features: List<FeatureInfo>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val items = buildList {
            for (feature in features) {
                if (feature.subFeatures.isNotEmpty()) {
                    add(ListItem.GroupHeader(feature))
                    for (sub in feature.subFeatures) {
                        add(ListItem.SubFeatureItem(feature, sub))
                    }
                } else {
                    add(ListItem.SingleFeatureItem(feature))
                }
            }
        }

        companion object {
            private const val TYPE_HEADER = 0
            private const val TYPE_SUB_FEATURE = 1
            private const val TYPE_SINGLE_FEATURE = 2
        }

        override fun getItemViewType(position: Int) = when (items[position]) {
            is ListItem.GroupHeader -> TYPE_HEADER
            is ListItem.SubFeatureItem -> TYPE_SUB_FEATURE
            is ListItem.SingleFeatureItem -> TYPE_SINGLE_FEATURE
        }

        override fun getItemCount() = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_HEADER -> {
                    val view = inflater.inflate(R.layout.feature_settings_item_header, parent, false)
                    HeaderViewHolder(view)
                }
                TYPE_SINGLE_FEATURE -> {
                    val view = inflater.inflate(R.layout.feature_settings_item, parent, false)
                    SingleFeatureViewHolder(view)
                }
                else -> {
                    val view = inflater.inflate(R.layout.feature_settings_item, parent, false)
                    SubFeatureViewHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is ListItem.GroupHeader -> (holder as HeaderViewHolder).bind(item.feature)
                is ListItem.SubFeatureItem -> (holder as SubFeatureViewHolder).bind(item.sub)
                is ListItem.SingleFeatureItem -> (holder as SingleFeatureViewHolder).bind(item.feature)
            }
        }

        class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val titleText: TextView = view.findViewById(R.id.headerText)
            fun bind(feature: FeatureInfo) {
                titleText.text = feature.title
            }
        }

        class SubFeatureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val titleText: TextView = view.findViewById(R.id.titleText)
            private val descText: TextView = view.findViewById(R.id.descriptionText)
            private val switchView: MaterialSwitch = view.findViewById(R.id.switchWidget)

            fun bind(sub: FeatureSubItem) {
                titleText.text = sub.title
                descText.text = sub.description
                switchView.isChecked = FeatureSettings.isSubFeatureEnabled(sub.id)

                switchView.setOnCheckedChangeListener { _, isChecked ->
                    FeatureSettings.setSubFeatureEnabled(sub.id, isChecked)
                }

                itemView.setOnClickListener {
                    switchView.toggle()
                }
            }
        }

        class SingleFeatureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val titleText: TextView = view.findViewById(R.id.titleText)
            private val descText: TextView = view.findViewById(R.id.descriptionText)
            private val switchView: MaterialSwitch = view.findViewById(R.id.switchWidget)

            fun bind(feature: FeatureInfo) {
                titleText.text = feature.title
                descText.text = feature.description
                switchView.isChecked = FeatureSettings.isFeatureEnabled(feature.id)

                switchView.setOnCheckedChangeListener { _, isChecked ->
                    FeatureSettings.setFeatureEnabled(feature.id, isChecked)
                }

                itemView.setOnClickListener {
                    switchView.toggle()
                }
            }
        }
    }
}