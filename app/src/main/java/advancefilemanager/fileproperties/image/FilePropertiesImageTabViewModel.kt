/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.image

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java8.nio.file.Path
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.util.Stateful

class FilePropertiesImageTabViewModel(path: Path, mimeType: MimeType) : ViewModel() {
    private val _imageInfoLiveData = ImageInfoLiveData(path, mimeType)
    val imageInfoLiveData: LiveData<Stateful<ImageInfo>>
        get() = _imageInfoLiveData

    fun reload() {
        _imageInfoLiveData.loadValue()
    }

    override fun onCleared() {
        _imageInfoLiveData.close()
    }
}
