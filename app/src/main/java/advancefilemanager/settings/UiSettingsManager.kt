/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources

/**
 * 管理UI显示设置，提供字体和间距的缩放功能
 */
object UiSettingsManager {
    private const val PREF_NAME = "ui_settings"
    private const val KEY_FONT_SCALE = "font_scale"
    private const val KEY_SPACING_SCALE = "spacing_scale"
    private const val KEY_LIST_ITEM_HEIGHT_SCALE = "list_item_height_scale"
    private const val KEY_ICON_SCALE = "icon_scale"
    private const val KEY_SCREEN_MARGIN_SCALE = "screen_margin_scale"
    private const val KEY_DIALOG_PADDING_SCALE = "dialog_padding_scale"
    private const val KEY_BUTTON_SPACING_SCALE = "button_spacing_scale"
    private const val KEY_BLUR_INTENSITY = "blur_intensity"

    fun getFontScale(context: Context): Float =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_FONT_SCALE, 1.0f)

    fun getSpacingScale(context: Context): Float =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_SPACING_SCALE, 1.0f)

    fun getListItemHeightScale(context: Context): Float =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_LIST_ITEM_HEIGHT_SCALE, 1.0f)

    fun getIconScale(context: Context): Float =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_ICON_SCALE, 1.0f)

    fun getScreenMarginScale(context: Context): Float =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_SCREEN_MARGIN_SCALE, 1.0f)

    fun getDialogPaddingScale(context: Context): Float =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_DIALOG_PADDING_SCALE, 1.0f)

    fun getButtonSpacingScale(context: Context): Float =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_BUTTON_SPACING_SCALE, 1.0f)

    fun getBlurIntensity(context: Context): Float {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return try {
            prefs.getInt(KEY_BLUR_INTENSITY, 50) / 100f
        } catch (_: ClassCastException) {
            val oldFloat = prefs.getFloat(KEY_BLUR_INTENSITY, 0.5f)
            val newInt = (oldFloat * 100).toInt()
            prefs.edit().remove(KEY_BLUR_INTENSITY).putInt(KEY_BLUR_INTENSITY, newInt).apply()
            newInt / 100f
        }
    }

    /**
     * 创建应用了字体缩放和间距缩放的Configuration
     */
    fun createConfiguration(context: Context): Configuration {
        val config = Configuration(context.resources.configuration)
        config.fontScale = getFontScale(context)
        val spacingScale = getSpacingScale(context)
        if (spacingScale != 1.0f) {
            val baseDpi = context.resources.displayMetrics.densityDpi
            config.densityDpi = (baseDpi * spacingScale).toInt()
        }
        return config
    }

    /**
     * 获取应用了UI设置的Context
     */
    fun wrapContext(context: Context): Context {
        val config = createConfiguration(context)
        return context.createConfigurationContext(config)
    }

    /**
     * 获取缩放后的尺寸值
     */
    fun getScaledDimension(context: Context, baseValue: Int, scaleType: ScaleType): Int {
        val scale = when (scaleType) {
            ScaleType.FONT -> getFontScale(context)
            ScaleType.SPACING -> getSpacingScale(context)
            ScaleType.LIST_ITEM_HEIGHT -> getListItemHeightScale(context)
            ScaleType.ICON -> getIconScale(context)
            ScaleType.SCREEN_MARGIN -> getScreenMarginScale(context)
            ScaleType.DIALOG_PADDING -> getDialogPaddingScale(context)
            ScaleType.BUTTON_SPACING -> getButtonSpacingScale(context)
        }
        return (baseValue * scale).toInt()
    }

    enum class ScaleType {
        FONT, SPACING, LIST_ITEM_HEIGHT, ICON, SCREEN_MARGIN, DIALOG_PADDING, BUTTON_SPACING
    }
}
