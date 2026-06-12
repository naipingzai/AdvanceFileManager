/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.app.LocaleManagerCompat
import androidx.preference.ListPreference
import androidx.preference.Preference.SummaryProvider
import com.advancefilemanager.R
import com.advancefilemanager.app.application
import com.advancefilemanager.util.toList
import java.util.Locale

class LocalePreference : ListPreference {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        val context = context
        val systemDefaultEntry = context.getString(R.string.system_default)
        // Prefer using the system setting because it has better support for locales.
        intent = Intent(
            Settings.ACTION_APP_LOCALE_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        summaryProvider = SummaryProvider<LocalePreference> {
            applicationLocale?.sentenceCasedLocalizedDisplayName ?: systemDefaultEntry
        }
    }

    private val Locale.sentenceCasedLocalizedDisplayName: String
        // See com.android.internal.app.LocaleHelper.toSentenceCase() for a proper case conversion
        // implementation which requires android.icu.text.CaseMap that's only available on API 29+.
        @Suppress("DEPRECATION")
        get() = getDisplayName(this).replaceFirstChar { if (it.isLowerCase()) it.titlecase(this) else it.toString() }

    override fun getPersistedString(defaultReturnValue: String?): String =
        applicationLocale?.toLanguageTag() ?: VALUE_SYSTEM_DEFAULT

    override fun persistString(value: String?): Boolean {
        return true
    }

    private val applicationLocale: Locale?
        get() = LocaleManagerCompat.getApplicationLocales(application).toList().firstOrNull()

    override fun onClick() {
        // Don't show dialog if we have an intent.
        if (intent != null) {
            return
        }

        super.onClick()
    }

    // Exposed for SettingsPreferenceFragment.onResume().
    public override fun notifyChanged() {
        super.notifyChanged()
    }

    companion object {
        private const val VALUE_SYSTEM_DEFAULT = ""
    }
}
