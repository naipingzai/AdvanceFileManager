/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.apk

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java8.nio.file.Path
import com.advancefilemanager.util.Stateful

class FilePropertiesApkTabViewModel(path: Path) : ViewModel() {
    private val _apkInfoLiveData = ApkInfoLiveData(path)
    val apkInfoLiveData: LiveData<Stateful<ApkInfo>>
        get() = _apkInfoLiveData

    fun reload() {
        _apkInfoLiveData.loadValue()
    }

    override fun onCleared() {
        _apkInfoLiveData.close()
    }
}
