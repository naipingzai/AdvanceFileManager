/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import java8.nio.file.Path
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.file.isSupportedArchive
import com.advancefilemanager.provider.archive.archiveFile
import com.advancefilemanager.provider.archive.isArchivePath
import com.advancefilemanager.provider.document.isDocumentPath
import com.advancefilemanager.provider.document.resolver.DocumentResolver
import com.advancefilemanager.provider.linux.isLinuxPath

val Path.name: String
    get() = fileName?.toString() ?: if (isArchivePath) archiveFile.fileName.toString() else "/"

fun Path.toUserFriendlyString(): String = if (isLinuxPath) toFile().path else toUri().toString()

fun Path.isArchiveFile(mimeType: MimeType): Boolean = !isArchivePath && mimeType.isSupportedArchive

val Path.isLocalPath: Boolean
    get() =
        isLinuxPath || (isDocumentPath && DocumentResolver.isLocal(this as DocumentResolver.Path))

val Path.isRemotePath: Boolean
    get() = !isLocalPath
