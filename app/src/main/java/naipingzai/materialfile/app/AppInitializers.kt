/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.app

import android.os.Build
import android.webkit.WebView
import naipingzai.materialfile.BuildConfig
import naipingzai.materialfile.coil.initializeCoil
import naipingzai.materialfile.filejob.fileJobNotificationTemplate
import naipingzai.materialfile.hiddenapi.HiddenApi
import naipingzai.materialfile.provider.FileSystemProviders
import naipingzai.materialfile.settings.Settings
import naipingzai.materialfile.storage.StorageVolumeListLiveData

val appInitializers = listOf(
    ::initializeCrashlytics,
    ::disableHiddenApiChecks,
    ::initializeWebViewDebugging,
    ::initializeCoil,
    ::initializeFileSystemProviders,
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
