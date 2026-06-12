/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature

import android.content.Context
import android.content.SharedPreferences
import com.advancefilemanager.app.application

/**
 * Feature enable/disable state management.
 * Stores each feature and sub-feature's enabled state in SharedPreferences.
 *
 * Features enabled here will show in the file/folder action bar;
 * disabled features are hidden from the UI.
 */
object FeatureSettings {
    private const val PREFS_NAME = "feature_settings"
    private const val KEY_PREFIX_FEATURE = "feature_enabled_"
    private const val KEY_PREFIX_SUB_FEATURE = "sub_feature_enabled_"

    private val prefs: SharedPreferences by lazy {
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Check if a top-level feature is enabled.
     * Features default to enabled (true) for first-time users.
     */
    fun isFeatureEnabled(featureId: String): Boolean {
        return prefs.getBoolean("$KEY_PREFIX_FEATURE$featureId", true)
    }

    fun setFeatureEnabled(featureId: String, enabled: Boolean) {
        prefs.edit().putBoolean("$KEY_PREFIX_FEATURE$featureId", enabled).apply()
    }

    /**
     * Check if a sub-feature (action) is enabled.
     * Sub-features default to enabled (true) for first-time users.
     */
    fun isSubFeatureEnabled(subFeatureId: String): Boolean {
        return prefs.getBoolean("$KEY_PREFIX_SUB_FEATURE$subFeatureId", true)
    }

    fun setSubFeatureEnabled(subFeatureId: String, enabled: Boolean) {
        prefs.edit().putBoolean("$KEY_PREFIX_SUB_FEATURE$subFeatureId", enabled).apply()
    }
}