/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin

import android.content.Context
import android.content.SharedPreferences
import com.advancefilemanager.app.application

/**
 * 插件及子功能使能状态管理。
 * 通过 SharedPreferences 存储每个插件和子功能的启用/禁用状态。
 */
object PluginSettings {
    private const val PREFS_NAME = "plugin_settings"
    private const val KEY_PREFIX_ENABLED = "plugin_enabled_"
    private const val KEY_PREFIX_FEATURE = "feature_enabled_"

    private val prefs: SharedPreferences by lazy {
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isPluginEnabled(pluginId: String): Boolean {
        return prefs.getBoolean("$KEY_PREFIX_ENABLED$pluginId", false)
    }

    fun setPluginEnabled(pluginId: String, enabled: Boolean) {
        prefs.edit().putBoolean("$KEY_PREFIX_ENABLED$pluginId", enabled).apply()
    }

    fun isFeatureEnabled(featureId: String): Boolean {
        return prefs.getBoolean("$KEY_PREFIX_FEATURE$featureId", false)
    }

    fun setFeatureEnabled(featureId: String, enabled: Boolean) {
        prefs.edit().putBoolean("$KEY_PREFIX_FEATURE$featureId", enabled).apply()
    }
}
