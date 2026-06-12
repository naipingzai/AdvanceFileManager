/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.document

import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import com.advancefilemanager.provider.common.AbstractPathObservable
import com.advancefilemanager.provider.content.resolver.ResolverException
import com.advancefilemanager.provider.document.resolver.DocumentResolver

internal class DocumentPathObservable(
    path: DocumentPath,
    intervalMillis: Long
) : AbstractPathObservable(intervalMillis) {
    private val cursor: Cursor

    private val contentObserver = object : ContentObserver(handler) {
        override fun deliverSelfNotifications(): Boolean = true

        override fun onChange(selfChange: Boolean) {
            notifyObservers()
        }
    }

    init {
        val uri = try {
            path.observableUri
        } catch (e: ResolverException) {
            throw e.toFileSystemException(path.toString())
        }
        cursor = try {
            DocumentResolver.query(uri, emptyArray(), null)
        } catch (e: ResolverException) {
            throw e.toFileSystemException(path.toString())
        }
        cursor.registerContentObserver(contentObserver)
    }

    override fun onCloseLocked() {
        cursor.unregisterContentObserver(contentObserver)
        cursor.close()
    }

    private val DocumentPath.observableUri: Uri
        @Throws(ResolverException::class)
        get() = DocumentResolver.getDocumentChildrenUri(this)
}
