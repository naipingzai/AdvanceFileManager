/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.permission

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.advancefilemanager.provider.common.PosixFileModeBit
import com.advancefilemanager.util.toEnumSet
import com.advancefilemanager.util.valueCompat

class SetModeViewModel(mode: Set<PosixFileModeBit>) : ViewModel() {
    private val _modeLiveData: MutableLiveData<Set<PosixFileModeBit>> = MutableLiveData(mode)
    val modeLiveData: LiveData<Set<PosixFileModeBit>>
        get() = _modeLiveData
    val mode: Set<PosixFileModeBit>
        get() = _modeLiveData.valueCompat

    fun toggleModeBit(modeBit: PosixFileModeBit) {
        val mode = _modeLiveData.valueCompat.toEnumSet()
        if (modeBit in mode) {
            mode -= modeBit
        } else {
            mode += modeBit
        }
        _modeLiveData.value = mode
    }
}
