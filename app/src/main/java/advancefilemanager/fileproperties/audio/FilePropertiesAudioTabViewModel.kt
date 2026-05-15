/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.audio

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java8.nio.file.Path
import com.advancefilemanager.util.Stateful

class FilePropertiesAudioTabViewModel(path: Path) : ViewModel() {
    private val _audioInfoLiveData = AudioInfoLiveData(path)
    val audioInfoLiveData: LiveData<Stateful<AudioInfo>>
        get() = _audioInfoLiveData

    fun reload() {
        _audioInfoLiveData.loadValue()
    }

    override fun onCleared() {
        _audioInfoLiveData.close()
    }
}
