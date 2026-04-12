/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.filejob

import android.util.Log
import naipingzai.materialfile.util.showToast
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.atomic.AtomicInteger

abstract class FileJob {
    val id = nextId.getAndIncrement()

    internal lateinit var service: FileJobService
        private set

    fun runOn(service: FileJobService) {
        this.service = service
        val jobName = this::class.java.simpleName
        Log.i(TAG, "FileJob started: $jobName (id=$id)")
        try {
            run()
            Log.i(TAG, "FileJob completed: $jobName (id=$id)")
        } catch (e: InterruptedIOException) {
            Log.i(TAG, "FileJob cancelled: $jobName (id=$id)")
        } catch (e: Exception) {
            Log.e(TAG, "FileJob failed: $jobName (id=$id)", e)
            val message = e.localizedMessage ?: e.toString()
            service.showToast(message)
        } finally {
            service.notificationManager.cancel(id)
        }
    }

    @Throws(IOException::class)
    protected abstract fun run()

    companion object {
        private const val TAG = "FileJob"
        private val nextId = AtomicInteger(1)
    }
}
