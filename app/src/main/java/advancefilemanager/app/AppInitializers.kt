/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.app

import android.os.Build
import android.webkit.WebView
import com.advancefilemanager.BuildConfig
import com.advancefilemanager.coil.initializeCoil
import com.advancefilemanager.filejob.fileJobNotificationTemplate
import com.advancefilemanager.hiddenapi.HiddenApi
import com.advancefilemanager.plugin.ffmpegtools.FFmpegFeatureProvider
import com.advancefilemanager.plugin.filetools.FileToolsFeatureProvider
import com.advancefilemanager.plugin.protocol.PluginManager
import com.advancefilemanager.provider.FileSystemProviders
import com.advancefilemanager.settings.Settings
import com.advancefilemanager.storage.StorageVolumeListLiveData

val appInitializers = listOf(
    ::initializeCrashlytics,
    ::disableHiddenApiChecks,
    ::initializeWebViewDebugging,
    ::initializeCoil,
    ::initializeFileSystemProviders,
    ::initializePlugins,
    ::upgradeApp,
    ::initializeLiveDataObjects,
    ::createNotificationChannels
)

private fun initializeCrashlytics() {
    // Firebase Crashlytics removed - no network functionality
}

private fun disableHiddenApiChecks() {
    HiddenApi.disableHiddenApiChecks()
}

private fun initializeWebViewDebugging() {
    if (BuildConfig.DEBUG) {
        WebView.setWebContentsDebuggingEnabled(true)
    }
}

private fun initializeFileSystemProviders() {
    FileSystemProviders.install()
    FileSystemProviders.overflowWatchEvents = true
}

private fun initializePlugins() {
    PluginManager.registerFeatureProvider(FFmpegFeatureProvider)
    PluginManager.registerFeatureProvider(FileToolsFeatureProvider)
}

private fun initializeLiveDataObjects() {
    // Force initialization of LiveData objects so that it won't happen on a background thread.
    StorageVolumeListLiveData.value
    Settings.FILE_LIST_DEFAULT_DIRECTORY.value
}

private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.createNotificationChannels(
            listOf(
                backgroundActivityStartNotificationTemplate.channelTemplate,
                fileJobNotificationTemplate.channelTemplate
            ).map { it.create(application) }
        )
    }
}
