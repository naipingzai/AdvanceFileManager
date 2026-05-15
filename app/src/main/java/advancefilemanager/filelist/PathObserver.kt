/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import java8.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import com.advancefilemanager.provider.common.PathObservable
import com.advancefilemanager.provider.common.observe
import com.advancefilemanager.util.closeSafe
import java.io.Closeable
import java.io.IOException

class PathObserver(path: Path, @MainThread onChange: () -> Unit) : Closeable {
    private var pathObservable: PathObservable? = null

    private var closed = false
    private val lock = Any()

    private val ioExecutor = Dispatchers.IO.asExecutor()

    init {
        ioExecutor.execute {
            synchronized(lock) {
                if (closed) {
                    return@execute
                }
                pathObservable = try {
                    path.observe(THROTTLE_INTERVAL_MILLIS)
                } catch (e: UnsupportedOperationException) {
                    // Ignored.
                    return@execute
                } catch (e: IOException) {
                    // Ignored.
                    e.printStackTrace()
                    return@execute
                }.apply {
                    val mainHandler = Handler(Looper.getMainLooper())
                    addObserver { mainHandler.post(onChange) }
                }
            }
        }
    }

    override fun close() {
        ioExecutor.execute {
            synchronized(lock) {
                if (closed) {
                    return@execute
                }
                closed = true
                pathObservable?.closeSafe()
            }
        }
    }

    companion object {
        private const val THROTTLE_INTERVAL_MILLIS = 1000L
    }
}
