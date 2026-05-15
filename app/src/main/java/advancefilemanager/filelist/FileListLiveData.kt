/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import java8.nio.file.DirectoryIteratorException
import java8.nio.file.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.advancefilemanager.file.FileItem
import com.advancefilemanager.file.loadFileItem
import com.advancefilemanager.provider.common.newDirectoryStream
import com.advancefilemanager.util.CloseableLiveData
import com.advancefilemanager.util.Failure
import com.advancefilemanager.util.Loading
import com.advancefilemanager.util.Stateful
import com.advancefilemanager.util.Success
import com.advancefilemanager.util.valueCompat
import java.io.IOException

class FileListLiveData(private val path: Path) : CloseableLiveData<Stateful<List<FileItem>>>() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    private val observer: PathObserver

    @Volatile
    private var isChangedWhileInactive = false

    init {
        loadValue()
        observer = PathObserver(path) { onChangeObserved() }
    }

    fun loadValue() {
        job?.cancel()
        value = Loading(value?.value)
        job = scope.launch {
            val value = try {
                path.newDirectoryStream().use { directoryStream ->
                    val fileList = mutableListOf<FileItem>()
                    for (path in directoryStream) {
                        try {
                            fileList.add(path.loadFileItem())
                        } catch (e: DirectoryIteratorException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    Success(fileList as List<FileItem>)
                }
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }

    private fun onChangeObserved() {
        if (hasActiveObservers()) {
            loadValue()
        } else {
            isChangedWhileInactive = true
        }
    }

    override fun onActive() {
        if (isChangedWhileInactive) {
            loadValue()
            isChangedWhileInactive = false
        }
    }

    override fun close() {
        observer.close()
        job?.cancel()
    }
}
