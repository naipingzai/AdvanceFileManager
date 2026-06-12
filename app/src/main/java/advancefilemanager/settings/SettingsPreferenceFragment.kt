/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.os.Bundle
import com.advancefilemanager.R
import com.advancefilemanager.app.ToolHostActivity
import com.advancefilemanager.feature.FeatureSettingsFragment
import com.advancefilemanager.ui.PreferenceFragmentCompat
import com.advancefilemanager.util.startActivitySafe

class SettingsPreferenceFragment : PreferenceFragmentCompat() {
    private lateinit var localePreference: LocalePreference

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        localePreference = preferenceScreen.findPreference(getString(R.string.pref_key_locale))!!

        findPreference<androidx.preference.Preference>("pref_key_feature_settings")?.setOnPreferenceClickListener {
            val intent = ToolHostActivity.createIntent<FeatureSettingsFragment>(
                R.string.feature_settings_title
            )
            startActivitySafe(intent)
            true
        }
    }

    override fun onResume() {
        super.onResume()

        // Refresh locale preference summary because we aren't notified for an external change
        // between system default and the locale that's the current system default.
        localePreference.notifyChanged()
    }
}
