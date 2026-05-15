/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties

import java8.nio.file.Path
import com.advancefilemanager.filelist.PathObserver
import com.advancefilemanager.util.CloseableLiveData

abstract class PathObserverLiveData<T>(protected val path: Path) : CloseableLiveData<T>() {
    private lateinit var observer: PathObserver

    @Volatile
    private var changedWhileInactive = false

    protected fun observe() {
        observer = PathObserver(path) { onChangeObserved() }
    }

    abstract fun loadValue()

    private fun onChangeObserved() {
        if (hasActiveObservers()) {
            loadValue()
        } else {
            changedWhileInactive = true
        }
    }

    override fun onActive() {
        if (changedWhileInactive) {
            loadValue()
            changedWhileInactive = false
        }
    }

    override fun close() {
        observer.close()
    }
}
