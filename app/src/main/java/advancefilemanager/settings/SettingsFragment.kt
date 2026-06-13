/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.R
import com.advancefilemanager.app.ToolHostActivity
import com.advancefilemanager.storage.StorageListFragment
import com.advancefilemanager.settings.BookmarkDirectoryListFragment
import com.advancefilemanager.util.createIntent
import com.advancefilemanager.util.startActivitySafe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.advancefilemanager.ui.applyOverlay

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.ui_settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as androidx.appcompat.app.AppCompatActivity
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setTitle(R.string.settings_title)
        toolbar.setNavigationOnClickListener { activity.finish() }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = SettingsAdapter(requireContext())
    }

    private class SettingsAdapter(private val context: Context) :
        RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

        private data class SettingItem(
            val titleResId: Int,
            val summaryProvider: () -> String,
            val onClick: () -> Unit
        )

        private val items: List<SettingItem>

        init {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            items = listOf(
                SettingItem(
                    R.string.settings_locale_title,
                    { getLocaleSummary() },
                    { showLocaleDialog() }
                ),
                SettingItem(
                    R.string.settings_file_name_ellipsize_title,
                    { getEllipsizeSummary(prefs) },
                    { showEllipsizeDialog(prefs) }
                ),
                SettingItem(
                    R.string.settings_default_directory_title,
                    { com.advancefilemanager.settings.Settings.FILE_LIST_DEFAULT_DIRECTORY.value?.toString() ?: "" },
                    { showDefaultDirectoryDialog() }
                ),
                SettingItem(
                    R.string.settings_storages_title,
                    { context.getString(R.string.settings_storages_summary_empty) },
                    {
                        context.startActivitySafe(
                            ToolHostActivity.createIntent<StorageListFragment>(R.string.storage_list_title)
                        )
                    }
                ),
                SettingItem(
                    R.string.settings_bookmark_directories_title,
                    { context.getString(R.string.settings_bookmark_directories_summary_empty) },
                    {
                        context.startActivitySafe(
                            ToolHostActivity.createIntent<BookmarkDirectoryListFragment>(R.string.settings_bookmark_directories_title)
                        )
                    }
                ),
                SettingItem(
                    R.string.settings_root_strategy_title,
                    { getRootStrategySummary(prefs) },
                    { showRootStrategyDialog(prefs) }
                ),
                SettingItem(
                    R.string.settings_archive_file_name_encoding_title,
                    { getCharsetSummary(prefs) },
                    { showCharsetDialog(prefs) }
                ),
                SettingItem(
                    R.string.settings_open_apk_default_action_title,
                    { getOpenApkActionSummary(prefs) },
                    { showOpenApkActionDialog(prefs) }
                )
            )
        }

        private fun getLocaleSummary(): String {
            val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            return when {
                currentLocale.isEmpty() -> context.getString(R.string.system_default)
                currentLocale.startsWith("zh") -> "中文"
                currentLocale.startsWith("en") -> "English"
                else -> currentLocale
            }
        }

        private fun showLocaleDialog() {
            val entries = arrayOf(
                context.getString(R.string.system_default),
                "English",
                "中文"
            )
            val values = listOf("", "en", "zh-CN")

            val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            val checkedIdx = when {
                currentLocale.isEmpty() -> 0
                currentLocale.startsWith("zh") -> 2
                currentLocale.startsWith("en") -> 1
                else -> 0
            }

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.settings_locale_title)
                .setSingleChoiceItems(entries, checkedIdx) { dialog, which ->
                    val localeList = if (values[which].isEmpty()) {
                        LocaleListCompat.getEmptyLocaleList()
                    } else {
                        LocaleListCompat.forLanguageTags(values[which])
                    }
                    AppCompatDelegate.setApplicationLocales(localeList)
                    dialog.dismiss()
                }
                .create()
                .applyOverlay(context)
                .show()
        }

        private fun showDefaultDirectoryDialog() {
            android.widget.Toast.makeText(context, R.string.settings_default_directory_title, android.widget.Toast.LENGTH_SHORT).show()
        }

        private fun getEllipsizeSummary(prefs: android.content.SharedPreferences): String {
            val value = prefs.getString("file_name_ellipsize", "marquee") ?: "marquee"
            val entries = context.resources.getStringArray(R.array.settings_file_name_ellipsize_entries)
            val values = context.resources.getStringArray(R.array.pref_entry_values_file_name_ellipsize)
            val idx = values.indexOf(value)
            return if (idx >= 0) entries[idx] else entries[0]
        }

        private fun getRootStrategySummary(prefs: android.content.SharedPreferences): String {
            val value = prefs.getString("root_strategy", "never") ?: "never"
            val entries = context.resources.getStringArray(R.array.settings_root_strategy_entries)
            val values = context.resources.getStringArray(R.array.pref_entry_values_root_strategy)
            val idx = values.indexOf(value)
            return if (idx >= 0) entries[idx] else entries[0]
        }

        private fun getCharsetSummary(prefs: android.content.SharedPreferences): String {
            return prefs.getString("archive_file_name_encoding", "UTF-8") ?: "UTF-8"
        }

        private fun getOpenApkActionSummary(prefs: android.content.SharedPreferences): String {
            val value = prefs.getString("open_apk_default_action", "ask") ?: "ask"
            val entries = context.resources.getStringArray(R.array.settings_open_apk_default_action_entries)
            val values = context.resources.getStringArray(R.array.pref_entry_values_open_apk_default_action)
            val idx = values.indexOf(value)
            return if (idx >= 0) entries[idx] else entries[0]
        }

        private fun showEllipsizeDialog(prefs: android.content.SharedPreferences) {
            val entries = context.resources.getStringArray(R.array.settings_file_name_ellipsize_entries)
            val values = context.resources.getStringArray(R.array.pref_entry_values_file_name_ellipsize)
            val current = prefs.getString("file_name_ellipsize", "marquee") ?: "marquee"
            val checkedIdx = values.indexOf(current).coerceAtLeast(0)

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.settings_file_name_ellipsize_title)
                .setSingleChoiceItems(entries, checkedIdx) { dialog, which ->
                    prefs.edit().putString("file_name_ellipsize", values[which]).apply()
                    notifyDataSetChanged()
                    dialog.dismiss()
                }
                .create()
                .applyOverlay(context)
                .show()
        }

        private fun showRootStrategyDialog(prefs: android.content.SharedPreferences) {
            val entries = context.resources.getStringArray(R.array.settings_root_strategy_entries)
            val values = context.resources.getStringArray(R.array.pref_entry_values_root_strategy)
            val current = prefs.getString("root_strategy", "never") ?: "never"
            val checkedIdx = values.indexOf(current).coerceAtLeast(0)

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.settings_root_strategy_title)
                .setSingleChoiceItems(entries, checkedIdx) { dialog, which ->
                    prefs.edit().putString("root_strategy", values[which]).apply()
                    notifyDataSetChanged()
                    dialog.dismiss()
                }
                .create()
                .applyOverlay(context)
                .show()
        }

        private fun showCharsetDialog(prefs: android.content.SharedPreferences) {
            val charsets = listOf("UTF-8", "GBK", "GB2312", "GB18030", "BIG5", "ISO-8859-1")
            val current = prefs.getString("archive_file_name_encoding", "UTF-8") ?: "UTF-8"
            val checkedIdx = charsets.indexOf(current).coerceAtLeast(0)

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.settings_archive_file_name_encoding_title)
                .setSingleChoiceItems(charsets.toTypedArray(), checkedIdx) { dialog, which ->
                    prefs.edit().putString("archive_file_name_encoding", charsets[which]).apply()
                    notifyDataSetChanged()
                    dialog.dismiss()
                }
                .create()
                .applyOverlay(context)
                .show()
        }

        private fun showOpenApkActionDialog(prefs: android.content.SharedPreferences) {
            val entries = context.resources.getStringArray(R.array.settings_open_apk_default_action_entries)
            val values = context.resources.getStringArray(R.array.pref_entry_values_open_apk_default_action)
            val current = prefs.getString("open_apk_default_action", "ask") ?: "ask"
            val checkedIdx = values.indexOf(current).coerceAtLeast(0)

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.settings_open_apk_default_action_title)
                .setSingleChoiceItems(entries, checkedIdx) { dialog, which ->
                    prefs.edit().putString("open_apk_default_action", values[which]).apply()
                    notifyDataSetChanged()
                    dialog.dismiss()
                }
                .create()
                .applyOverlay(context)
                .show()
        }

        override fun getItemCount() = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.settings_card_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val titleText: TextView = view.findViewById(R.id.titleText)
            private val summaryText: TextView = view.findViewById(R.id.summaryText)

            fun bind(item: SettingItem) {
                titleText.text = itemView.context.getString(item.titleResId)
                summaryText.text = item.summaryProvider()

                itemView.setOnClickListener { item.onClick() }
            }
        }
    }
}
