/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.document

import android.provider.DocumentsContract
import java8.nio.file.ProviderMismatchException
import java8.nio.file.attribute.BasicFileAttributes
import com.advancefilemanager.util.hasBits

val BasicFileAttributes.documentSupportsThumbnail: Boolean
    get() {
        this as? DocumentFileAttributes ?: throw ProviderMismatchException(toString())
        return flags().hasBits(DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL)
    }
