/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.R
import com.advancefilemanager.feature.protocol.FeatureInfo
import com.advancefilemanager.feature.protocol.FeatureSubItem
import com.advancefilemanager.settings.BasicSettings
import com.google.android.material.materialswitch.MaterialSwitch

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

        val section = arguments?.getString(ARG_SECTION) ?: SECTION_BASIC
        val activity = requireActivity() as AppCompatActivity
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setTitle(getTitleRes(section))
        toolbar.setNavigationOnClickListener { activity.finish() }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val features = FeatureManager.getAllFeatures()
            .filter { it.id != "ebook-viewer" }
        recyclerView.adapter = EntrySettingsAdapter(features, requireContext(), section)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().sendBroadcast(Intent(ACTION_ENTRY_SETTINGS_CHANGED))
    }

    private fun getTitleRes(section: String): Int = when (section) {
        SECTION_BASIC -> R.string.entry_settings_basic_section
        SECTION_FILE_TOOLS -> R.string.feature_settings_file_tools
        SECTION_MEDIA_TOOLS -> R.string.feature_settings_ffmpeg_tools
        else -> R.string.entry_settings_basic_section
    }

    companion object {
        const val ACTION_ENTRY_SETTINGS_CHANGED =
            "com.advancefilemanager.ENTRY_SETTINGS_CHANGED"

        const val ARG_SECTION = "section"
        const val SECTION_BASIC = "basic"
        const val SECTION_FILE_TOOLS = "file_tools"
        const val SECTION_MEDIA_TOOLS = "media_tools"

        fun newInstance(section: String): FeatureSettingsFragment {
            return FeatureSettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SECTION, section)
                }
            }
        }
    }

    private sealed class ListItem {
        data class BasicItem(val key: String, val titleResId: Int, val descResId: Int) : ListItem()
        data class SubFeatureItem(val feature: FeatureInfo, val sub: FeatureSubItem) : ListItem()
        data class SingleFeatureItem(val feature: FeatureInfo) : ListItem()
    }

    private class EntrySettingsAdapter(
        features: List<FeatureInfo>,
        private val context: Context,
        section: String
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val items: List<ListItem> = buildItems(features, section)

        private fun buildItems(features: List<FeatureInfo>, section: String): List<ListItem> {
            return when (section) {
                SECTION_BASIC -> buildBasicItems()
                SECTION_FILE_TOOLS -> buildFileToolItems(features)
                SECTION_MEDIA_TOOLS -> buildMediaToolItems(features)
                else -> buildBasicItems()
            }
        }

        private fun buildBasicItems(): List<ListItem> {
            return listOf(
                ListItem.BasicItem("open_with", R.string.basic_settings_open_with, R.string.basic_settings_open_with_desc),
                ListItem.BasicItem("archive", R.string.file_item_action_archive, R.string.basic_settings_archive_desc),
                ListItem.BasicItem("share", R.string.share, R.string.basic_settings_share_desc),
                ListItem.BasicItem("copy_path", R.string.file_list_action_copy_path, R.string.basic_settings_copy_path_desc),
                ListItem.BasicItem("add_bookmark", R.string.file_list_action_add_bookmark, R.string.basic_settings_add_bookmark_desc),
                ListItem.BasicItem("properties", R.string.file_item_action_properties, R.string.basic_settings_properties_desc)
            )
        }

        private fun buildFileToolItems(features: List<FeatureInfo>): List<ListItem> {
            val feature = features.find { it.id == "file-tools" } ?: return emptyList()
            return if (feature.subFeatures.isNotEmpty()) {
                feature.subFeatures.map { sub -> ListItem.SubFeatureItem(feature, sub) }
            } else {
                listOf(ListItem.SingleFeatureItem(feature))
            }
        }

        private fun buildMediaToolItems(features: List<FeatureInfo>): List<ListItem> {
            val feature = features.find { it.id == "ffmpeg-tools" } ?: return emptyList()
            return if (feature.subFeatures.isNotEmpty()) {
                feature.subFeatures.map { sub -> ListItem.SubFeatureItem(feature, sub) }
            } else {
                listOf(ListItem.SingleFeatureItem(feature))
            }
        }

        companion object {
            private const val TYPE_BASIC_ITEM = 0
            private const val TYPE_SUB_FEATURE = 1
            private const val TYPE_SINGLE_FEATURE = 2
        }

        override fun getItemViewType(position: Int) = when (items[position]) {
            is ListItem.BasicItem -> TYPE_BASIC_ITEM
            is ListItem.SubFeatureItem -> TYPE_SUB_FEATURE
            is ListItem.SingleFeatureItem -> TYPE_SINGLE_FEATURE
        }

        override fun getItemCount() = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_BASIC_ITEM -> {
                    val view = inflater.inflate(R.layout.feature_settings_item, parent, false)
                    BasicItemViewHolder(view)
                }
                TYPE_SUB_FEATURE -> {
                    val view = inflater.inflate(R.layout.feature_settings_item, parent, false)
                    SubFeatureViewHolder(view)
                }
                else -> {
                    val view = inflater.inflate(R.layout.feature_settings_item, parent, false)
                    SingleFeatureViewHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is ListItem.BasicItem -> (holder as BasicItemViewHolder).bind(item)
                is ListItem.SubFeatureItem -> (holder as SubFeatureViewHolder).bind(item.sub, item.feature)
                is ListItem.SingleFeatureItem -> (holder as SingleFeatureViewHolder).bind(item.feature)
            }
        }

        class BasicItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val titleText: TextView = view.findViewById(R.id.titleText)
            private val descText: TextView = view.findViewById(R.id.descriptionText)
            private val switchView: MaterialSwitch = view.findViewById(R.id.switchWidget)

            fun bind(item: ListItem.BasicItem) {
                titleText.text = context.getString(item.titleResId)
                descText.text = context.getString(item.descResId)
                switchView.isChecked = BasicSettings.isFileOperationEnabled(context, item.key)

                switchView.setOnCheckedChangeListener { _, isChecked ->
                    BasicSettings.setFileOperationEnabled(context, item.key, isChecked)
                }
                itemView.setOnClickListener { switchView.toggle() }
            }

            private val context: Context get() = itemView.context
        }

        class SubFeatureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val titleText: TextView = view.findViewById(R.id.titleText)
            private val descText: TextView = view.findViewById(R.id.descriptionText)
            private val switchView: MaterialSwitch = view.findViewById(R.id.switchWidget)

            fun bind(sub: FeatureSubItem, feature: FeatureInfo) {
                titleText.text = sub.title
                descText.text = sub.description
                val isFeatureEnabled = FeatureSettings.isFeatureEnabled(feature.id)
                val isSubEnabled = FeatureSettings.isSubFeatureEnabled(sub.id)

                switchView.isChecked = isSubEnabled
                switchView.isEnabled = isFeatureEnabled
                titleText.isEnabled = isFeatureEnabled
                descText.isEnabled = isFeatureEnabled
                itemView.isEnabled = isFeatureEnabled
                itemView.alpha = if (isFeatureEnabled) 1.0f else 0.5f

                switchView.setOnCheckedChangeListener { _, isChecked ->
                    FeatureSettings.setSubFeatureEnabled(sub.id, isChecked)
                }
                itemView.setOnClickListener {
                    if (isFeatureEnabled) switchView.toggle()
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
                itemView.setOnClickListener { switchView.toggle() }
            }
        }
    }
}
