/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.filejob

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import naipingzai.materialfile.provider.common.PosixFileStore
import naipingzai.materialfile.util.ActionState
import naipingzai.materialfile.util.isFinished
import naipingzai.materialfile.util.isReady

class FileJobErrorViewModel : ViewModel() {
    private val _remountState =
        MutableStateFlow<ActionState<PosixFileStore, Unit>>(ActionState.Ready())
    val remountState = _remountState.asStateFlow()

    fun remount(fileStore: PosixFileStore) {
        viewModelScope.launch {
            check(_remountState.value.isReady)
            _remountState.value = ActionState.Running(fileStore)
            _remountState.value = try {
                runInterruptible(Dispatchers.IO) {
                    fileStore.isReadOnly = false
                }
                ActionState.Success(fileStore, Unit)
            } catch (e: Exception) {
                ActionState.Error(fileStore, e)
            }
        }
    }

    fun finishRemounting() {
        viewModelScope.launch {
            check(_remountState.value.isFinished)
            _remountState.value = ActionState.Ready()
        }
    }
}
