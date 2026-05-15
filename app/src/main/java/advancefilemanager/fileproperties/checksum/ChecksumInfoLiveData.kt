/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.checksum

import java8.nio.file.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.advancefilemanager.fileproperties.PathObserverLiveData
import com.advancefilemanager.provider.common.newInputStream
import com.advancefilemanager.util.Failure
import com.advancefilemanager.util.Loading
import com.advancefilemanager.util.Stateful
import com.advancefilemanager.util.Success
import com.advancefilemanager.util.toHexString
import com.advancefilemanager.util.valueCompat

class ChecksumInfoLiveData(path: Path) : PathObserverLiveData<Stateful<ChecksumInfo>>(path) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    init {
        loadValue()
        observe()
    }

    override fun loadValue() {
        job?.cancel()
        value = Loading(value?.value)
        job = scope.launch {
            val value = try {
                val messageDigests =
                    ChecksumInfo.Algorithm.entries.associateWith { it.createMessageDigest() }
                path.newInputStream().use { inputStream ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val readSize = inputStream.read(buffer)
                        if (readSize == -1) {
                            break
                        }
                        messageDigests.values.forEach { it.update(buffer, 0, readSize) }
                    }
                }
                val checksumInfo = ChecksumInfo(
                    messageDigests.mapValues { it.value.digest().toHexString() }
                )
                Success(checksumInfo)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }

    override fun close() {
        super.close()

        job?.cancel()
    }
}
