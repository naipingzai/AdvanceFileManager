/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.navigation

import androidx.lifecycle.MediatorLiveData
import com.advancefilemanager.settings.Settings
import com.advancefilemanager.storage.StorageVolumeListLiveData

object NavigationItemListLiveData : MediatorLiveData<List<NavigationItem?>>() {
    init {
        // Initialize value before we have any active observer.
        loadValue()
        addSource(Settings.STORAGES) { loadValue() }
        addSource(StorageVolumeListLiveData) { loadValue() }
        addSource(StandardDirectoriesLiveData) { loadValue() }
        addSource(Settings.BOOKMARK_DIRECTORIES) { loadValue() }
    }

    private fun loadValue() {
        value = navigationItems
    }
}
