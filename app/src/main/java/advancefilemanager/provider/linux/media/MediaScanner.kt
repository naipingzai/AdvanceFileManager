/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.linux.media

import android.media.MediaScannerConnection
import android.mtp.MtpConstants
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import java8.nio.channels.FileChannel
import com.advancefilemanager.app.application
import com.advancefilemanager.app.contentResolver
import com.advancefilemanager.hiddenapi.RestrictedHiddenApi
import com.advancefilemanager.provider.common.DelegateFileChannel
import com.advancefilemanager.provider.root.isRunningAsRoot
import com.advancefilemanager.util.lazyReflectedMethod
import java.io.File
import java.io.IOException

/*
 * @see com.android.internal.content.FileSystemProvider
 * @see com.android.providers.media.scan.ModernMediaScanner.java
 */
object MediaScanner {
    fun scan(file: File, isDeleted: Boolean = false) {
        if (isRunningAsRoot) {
            return
        }
        MediaScannerConnection.scanFile(application, arrayOf(file.path), null) { _, _ -> }
    }

    private val deleteMediaStoreEntryHandler by lazy {
        val thread = HandlerThread("DeleteMediaStoreEntry")
        thread.start()
        Handler(thread.looper)
    }

    private fun deleteMediaStoreEntryAsync(file: File) {
        deleteMediaStoreEntryHandler.post {
            try {
                deleteMediaStoreEntrySync(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RestrictedHiddenApi
    private val mediaStoreGetVolumeName by lazyReflectedMethod(
        MediaStore::class.java, "getVolumeName", File::class.java
    )

    // @see com.android.providers.media.scan.ModernMediaScanner.reconcileAndClean
    // @see https://android.googlesource.com/platform/packages/providers/MediaProvider/+/android10-release/src/com/android/providers/media/scan/ModernMediaScanner.java
    // @see https://android.googlesource.com/platform/packages/providers/MediaProvider/+/android11-release/src/com/android/providers/media/scan/ModernMediaScanner.java
    private fun deleteMediaStoreEntrySync(file: File) {
        val file = file.canonicalFile
        val volumeName = mediaStoreGetVolumeName.invoke(null, file) as String
        val uri = MediaStore.Files.getContentUri(volumeName)
            .buildUpon()
            .appendQueryParameter("includePending", "1")
            .appendQueryParameter("deletedata", "false")
            .build()
        @Suppress("DEPRECATION")
        val where = "ifnull(format, ${MtpConstants.FORMAT_UNDEFINED}) != ${
            MtpConstants.FORMAT_ABSTRACT_AV_PLAYLIST} AND ${MediaStore.Files.FileColumns.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)
        contentResolver.delete(uri, where, selectionArgs)
    }

    fun createScanOnCloseFileChannel(fileChannel: FileChannel, file: File): FileChannel =
        if (isRunningAsRoot) {
            fileChannel
        } else {
            object : DelegateFileChannel(fileChannel) {
                @Throws(IOException::class)
                override fun implCloseChannel() {
                    super.implCloseChannel()

                    scan(file)
                }
            }
        }
}
