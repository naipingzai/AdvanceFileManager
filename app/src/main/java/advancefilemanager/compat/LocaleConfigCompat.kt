/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.app.LocaleConfig
import android.content.Context
import androidx.core.os.LocaleListCompat

class LocaleConfigCompat(context: Context) {
    var status = 0
        private set

    var supportedLocales: LocaleListCompat? = null
        private set

    init {
        val platformLocaleConfig = LocaleConfig(context)
        status = platformLocaleConfig.status
        supportedLocales = platformLocaleConfig.supportedLocales
            ?.let { LocaleListCompat.wrap(it) }
    }

    companion object {
        const val STATUS_SUCCESS = 0
        const val STATUS_NOT_SPECIFIED = 1
        const val STATUS_PARSING_FAILED = 2
    }
}
