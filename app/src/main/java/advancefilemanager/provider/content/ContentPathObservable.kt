/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.content

import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import com.advancefilemanager.provider.common.AbstractPathObservable
import com.advancefilemanager.provider.content.resolver.Resolver
import com.advancefilemanager.provider.content.resolver.ResolverException

internal class ContentPathObservable(
    uri: Uri,
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
        cursor = try {
            Resolver.query(uri, emptyArray(), null, null, null)
        } catch (e: ResolverException) {
            throw e.toFileSystemException(uri.toString())
        }
        cursor.registerContentObserver(contentObserver)
    }

    override fun onCloseLocked() {
        cursor.unregisterContentObserver(contentObserver)
        cursor.close()
    }
}
