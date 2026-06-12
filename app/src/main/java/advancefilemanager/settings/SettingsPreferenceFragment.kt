/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.os.Bundle
import com.advancefilemanager.R
import com.advancefilemanager.ui.PreferenceFragmentCompat

class SettingsPreferenceFragment : PreferenceFragmentCompat() {
    private lateinit var localePreference: LocalePreference

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        localePreference = preferenceScreen.findPreference(getString(R.string.pref_key_locale))!!
    }

    override fun onResume() {
        super.onResume()

        // Refresh locale preference summary because we aren't notified for an external change
        // between system default and the locale that's the current system default.
        localePreference.notifyChanged()
    }
}
