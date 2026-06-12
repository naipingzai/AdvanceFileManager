/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.content.Context

object BasicSettings {
    private const val PREFS_NAME = "basic_settings"
    private const val KEY_PREFIX = "file_op_"

    fun isFileOperationEnabled(context: Context, operation: String): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("$KEY_PREFIX$operation", true)
    }

    fun setFileOperationEnabled(context: Context, operation: String, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("$KEY_PREFIX$operation", enabled)
            .apply()
    }
}
