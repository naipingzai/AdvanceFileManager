/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.checksum

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java8.nio.file.Path
import com.advancefilemanager.util.Stateful

class FilePropertiesChecksumTabViewModel(path: Path) : ViewModel() {
    private val _checksumInfoLiveData = ChecksumInfoLiveData(path)
    val checksumInfoLiveData: LiveData<Stateful<ChecksumInfo>>
        get() = _checksumInfoLiveData

    fun reload() {
        _checksumInfoLiveData.loadValue()
    }

    override fun onCleared() {
        _checksumInfoLiveData.close()
    }
}
