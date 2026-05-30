/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.os.Build
import android.os.Bundle
import com.advancefilemanager.R
import com.advancefilemanager.app.ToolHostActivity
import com.advancefilemanager.plugin.PluginSettingsFragment
import com.advancefilemanager.ui.PreferenceFragmentCompat
import com.advancefilemanager.util.startActivitySafe

class SettingsPreferenceFragment : PreferenceFragmentCompat() {
    private lateinit var localePreference: LocalePreference

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        localePreference = preferenceScreen.findPreference(getString(R.string.pref_key_locale))!!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            localePreference.setApplicationLocalesPre33 = { locales ->
                val activity = requireActivity() as SettingsActivity
                activity.setApplicationLocalesPre33(locales)
            }
        }

        findPreference<androidx.preference.Preference>("pref_key_plugin_manage")?.setOnPreferenceClickListener {
            val intent = ToolHostActivity.createIntent<PluginSettingsFragment>(
                R.string.plugin_settings_title
            )
            startActivitySafe(intent)
            true
        }
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Refresh locale preference summary because we aren't notified for an external change
            // between system default and the locale that's the current system default.
            localePreference.notifyChanged()
        }
    }
}
