/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.content.Context
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.advancefilemanager.ui.applyOverlay

class UiSettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.ui_settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setTitle(R.string.ui_settings_title)
        toolbar.setNavigationOnClickListener { activity.finish() }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = UiSettingsAdapter(requireContext())
    }

    companion object {
        private const val PREF_NAME = "ui_settings"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_SPACING_SCALE = "spacing_scale"
        private const val KEY_LIST_ITEM_HEIGHT_SCALE = "list_item_height_scale"
        private const val KEY_ICON_SCALE = "icon_scale"
        private const val KEY_SCREEN_MARGIN_SCALE = "screen_margin_scale"
        private const val KEY_DIALOG_PADDING_SCALE = "dialog_padding_scale"
        private const val KEY_BUTTON_SPACING_SCALE = "button_spacing_scale"
        private const val KEY_BLUR_INTENSITY = "blur_intensity"

        fun applyPreset(context: Context, preset: String) {
            val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            when (preset) {
                "mode_a" -> {
                    editor.putFloat(KEY_FONT_SCALE, 0.85f)
                    editor.putFloat(KEY_SPACING_SCALE, 0.8f)
                    editor.putFloat(KEY_LIST_ITEM_HEIGHT_SCALE, 0.85f)
                    editor.putFloat(KEY_ICON_SCALE, 0.9f)
                    editor.putFloat(KEY_SCREEN_MARGIN_SCALE, 0.75f)
                    editor.putFloat(KEY_DIALOG_PADDING_SCALE, 0.85f)
                    editor.putFloat(KEY_BUTTON_SPACING_SCALE, 0.8f)
                    editor.putInt(KEY_BLUR_INTENSITY, 30)
                }
                "mode_b" -> {
                    editor.putFloat(KEY_FONT_SCALE, 1.0f)
                    editor.putFloat(KEY_SPACING_SCALE, 1.0f)
                    editor.putFloat(KEY_LIST_ITEM_HEIGHT_SCALE, 1.0f)
                    editor.putFloat(KEY_ICON_SCALE, 1.0f)
                    editor.putFloat(KEY_SCREEN_MARGIN_SCALE, 1.0f)
                    editor.putFloat(KEY_DIALOG_PADDING_SCALE, 1.0f)
                    editor.putFloat(KEY_BUTTON_SPACING_SCALE, 1.0f)
                    editor.putInt(KEY_BLUR_INTENSITY, 50)
                }
                "mode_c" -> {
                    editor.putFloat(KEY_FONT_SCALE, 1.15f)
                    editor.putFloat(KEY_SPACING_SCALE, 1.2f)
                    editor.putFloat(KEY_LIST_ITEM_HEIGHT_SCALE, 1.15f)
                    editor.putFloat(KEY_ICON_SCALE, 1.1f)
                    editor.putFloat(KEY_SCREEN_MARGIN_SCALE, 1.25f)
                    editor.putFloat(KEY_DIALOG_PADDING_SCALE, 1.15f)
                    editor.putFloat(KEY_BUTTON_SPACING_SCALE, 1.2f)
                    editor.putInt(KEY_BLUR_INTENSITY, 70)
                }
            }
            editor.apply()
            notifySettingsChanged(context)
        }

        private fun notifySettingsChanged(context: Context) {
            val intent = android.content.Intent("com.advancefilemanager.UI_SETTINGS_CHANGED")
            context.sendBroadcast(intent)
        }
    }

    private data class SettingItem(
        val titleResId: Int,
        val subtitleResId: Int,
        val key: String,
        val defaultValue: Float = 1.0f,
        val valueFrom: Float = 0.5f,
        val valueTo: Float = 2.0f,
        val stepSize: Float = 0.05f
    )

    private data class IntSettingItem(
        val titleResId: Int,
        val subtitleResId: Int,
        val key: String,
        val defaultValue: Int = 50,
        val valueFrom: Int = 0,
        val valueTo: Int = 100,
        val stepSize: Int = 5
    )

    private class UiSettingsAdapter(private val context: Context) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val items = listOf(
            SettingItem(
                R.string.ui_settings_font_size_title,
                R.string.ui_settings_font_size_subtitle,
                KEY_FONT_SCALE
            ),
            SettingItem(
                R.string.ui_settings_spacing_title,
                R.string.ui_settings_spacing_subtitle,
                KEY_SPACING_SCALE
            ),
            SettingItem(
                R.string.ui_settings_list_item_height_title,
                R.string.ui_settings_list_item_height_subtitle,
                KEY_LIST_ITEM_HEIGHT_SCALE
            ),
            SettingItem(
                R.string.ui_settings_icon_size_title,
                R.string.ui_settings_icon_size_subtitle,
                KEY_ICON_SCALE
            ),
            SettingItem(
                R.string.ui_settings_screen_margin_title,
                R.string.ui_settings_screen_margin_subtitle,
                KEY_SCREEN_MARGIN_SCALE
            ),
            SettingItem(
                R.string.ui_settings_dialog_padding_title,
                R.string.ui_settings_dialog_padding_subtitle,
                KEY_DIALOG_PADDING_SCALE
            ),
            SettingItem(
                R.string.ui_settings_button_spacing_title,
                R.string.ui_settings_button_spacing_subtitle,
                KEY_BUTTON_SPACING_SCALE
            )
        )

        private val intItems = listOf(
            IntSettingItem(
                R.string.ui_settings_blur_intensity_title,
                R.string.ui_settings_blur_intensity_subtitle,
                KEY_BLUR_INTENSITY,
                defaultValue = 50,
                valueFrom = 0,
                valueTo = 100,
                stepSize = 5
            )
        )

        companion object {
            private const val TYPE_PRESET = 0
            private const val TYPE_SLIDER = 1
            private const val TYPE_INT_SLIDER = 2
            private const val KEY_FONT_SCALE = UiSettingsFragment.KEY_FONT_SCALE
            private const val KEY_SPACING_SCALE = UiSettingsFragment.KEY_SPACING_SCALE
            private const val KEY_LIST_ITEM_HEIGHT_SCALE = UiSettingsFragment.KEY_LIST_ITEM_HEIGHT_SCALE
            private const val KEY_ICON_SCALE = UiSettingsFragment.KEY_ICON_SCALE
            private const val KEY_SCREEN_MARGIN_SCALE = UiSettingsFragment.KEY_SCREEN_MARGIN_SCALE
            private const val KEY_DIALOG_PADDING_SCALE = UiSettingsFragment.KEY_DIALOG_PADDING_SCALE
            private const val KEY_BUTTON_SPACING_SCALE = UiSettingsFragment.KEY_BUTTON_SPACING_SCALE
            private const val KEY_BLUR_INTENSITY = UiSettingsFragment.KEY_BLUR_INTENSITY
        }

        override fun getItemViewType(position: Int): Int = when (position) {
            0 -> TYPE_PRESET
            in 1..items.size -> TYPE_SLIDER
            else -> TYPE_INT_SLIDER
        }

        override fun getItemCount(): Int = items.size + intItems.size + 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_PRESET -> PresetViewHolder(
                    inflater.inflate(R.layout.feature_settings_item_header, parent, false)
                )
                TYPE_INT_SLIDER -> IntSliderViewHolder(
                    inflater.inflate(R.layout.ui_settings_slider_item, parent, false)
                )
                else -> SliderViewHolder(
                    inflater.inflate(R.layout.ui_settings_slider_item, parent, false)
                )
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is PresetViewHolder -> holder.bind(context, this)
                is SliderViewHolder -> {
                    val item = items[position - 1]
                    holder.bind(context, item)
                }
                is IntSliderViewHolder -> {
                    val item = intItems[position - items.size - 1]
                    holder.bind(context, item)
                }
            }
        }

        fun refreshAll() {
            notifyDataSetChanged()
        }

        class PresetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val headerText: TextView = view.findViewById(R.id.headerText)
            private val expandIcon: ImageView? = view.findViewById(R.id.expandIcon)
            private val headerSwitch: com.google.android.material.materialswitch.MaterialSwitch? =
                view.findViewById(R.id.headerSwitch)

            fun bind(context: Context, adapter: UiSettingsAdapter) {
                headerText.text = context.getString(R.string.ui_settings_preset_title)

                expandIcon?.visibility = View.GONE
                headerSwitch?.visibility = View.GONE

                itemView.setOnClickListener {
                    val presets = context.resources.getStringArray(R.array.ui_settings_scale_entries)
                    val values = context.resources.getStringArray(R.array.ui_settings_scale_values)
                    MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.ui_settings_preview_title)
                        .setItems(presets) { _, which ->
                            UiSettingsFragment.applyPreset(context, values[which])
                            adapter.refreshAll()
                        }
                        .create()
                        .applyOverlay(context)
                        .show()
                }
            }
        }

        class SliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val titleText: TextView = view.findViewById(R.id.titleText)
            private val valueText: TextView = view.findViewById(R.id.valueText)
            private val slider: Slider = view.findViewById(R.id.slider)

            fun bind(context: Context, item: SettingItem) {
                titleText.text = context.getString(item.titleResId)
                val prefs = context.getSharedPreferences(
                    UiSettingsFragment.PREF_NAME, Context.MODE_PRIVATE
                )
                val currentValue = prefs.getFloat(item.key, item.defaultValue)

                slider.valueFrom = item.valueFrom
                slider.valueTo = item.valueTo
                slider.stepSize = item.stepSize
                slider.value = currentValue

                valueText.text = "${(currentValue * 100).toInt()}%"

                slider.addOnChangeListener { _, value, fromUser ->
                    if (fromUser) {
                        valueText.text = "${(value * 100).toInt()}%"
                        prefs.edit().putFloat(item.key, value).apply()
                        UiSettingsFragment.notifySettingsChanged(context)
                    }
                }
            }
        }

        class IntSliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val titleText: TextView = view.findViewById(R.id.titleText)
            private val valueText: TextView = view.findViewById(R.id.valueText)
            private val slider: Slider = view.findViewById(R.id.slider)

            fun bind(context: Context, item: IntSettingItem) {
                titleText.text = context.getString(item.titleResId)
                val prefs = context.getSharedPreferences(
                    UiSettingsFragment.PREF_NAME, Context.MODE_PRIVATE
                )
                val currentValue = prefs.getInt(item.key, item.defaultValue)

                slider.valueFrom = item.valueFrom.toFloat()
                slider.valueTo = item.valueTo.toFloat()
                slider.stepSize = item.stepSize.toFloat()
                slider.value = currentValue.toFloat()

                valueText.text = "$currentValue%"

                slider.addOnChangeListener { _, value, fromUser ->
                    if (fromUser) {
                        valueText.text = "${value.toInt()}%"
                        prefs.edit().putInt(item.key, value.toInt()).apply()
                        UiSettingsFragment.notifySettingsChanged(context)
                    }
                }
            }
        }
    }
}
