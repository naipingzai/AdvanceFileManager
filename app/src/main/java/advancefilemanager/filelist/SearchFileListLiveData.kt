/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import java8.nio.file.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.advancefilemanager.file.FileItem
import com.advancefilemanager.file.loadFileItem
import com.advancefilemanager.provider.common.search
import com.advancefilemanager.util.CloseableLiveData
import com.advancefilemanager.util.Failure
import com.advancefilemanager.util.Loading
import com.advancefilemanager.util.Stateful
import com.advancefilemanager.util.Success
import com.advancefilemanager.util.valueCompat
import java.io.IOException

class SearchFileListLiveData(
    private val path: Path,
    private val query: String
) : CloseableLiveData<Stateful<List<FileItem>>>() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    init {
        loadValue()
    }

    fun loadValue() {
        job?.cancel()
        value = Loading(emptyList())
        job = scope.launch {
            val fileList = mutableListOf<FileItem>()
            try {
                path.search(query, INTERVAL_MILLIS) { paths: List<Path> ->
                    for (path in paths) {
                        val fileItem = try {
                            path.loadFileItem()
                        } catch (e: IOException) {
                            e.printStackTrace()
                            continue
                        }
                        fileList.add(fileItem)
                    }
                    postValue(Loading(fileList.toList()))
                }
                postValue(Success(fileList))
            } catch (e: Exception) {
                postValue(Failure(valueCompat.value, e))
            }
        }
    }

    override fun close() {
        job?.cancel()
    }

    companion object {
        private const val INTERVAL_MILLIS = 500L
    }
}
