/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.navigation

import androidx.lifecycle.MediatorLiveData
import com.advancefilemanager.settings.Settings

object StandardDirectoriesLiveData : MediatorLiveData<List<StandardDirectory>>() {
    init {
        // Initialize value before we have any active observer.
        loadValue()
        addSource(Settings.STANDARD_DIRECTORY_SETTINGS) { loadValue() }
    }

    private fun loadValue() {
        value = standardDirectories
    }
}
