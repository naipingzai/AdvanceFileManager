/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.filelist

import java8.nio.file.Path
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.file.isSupportedArchive
import naipingzai.materialfile.provider.archive.archiveFile
import naipingzai.materialfile.provider.archive.isArchivePath
import naipingzai.materialfile.provider.document.isDocumentPath
import naipingzai.materialfile.provider.document.resolver.DocumentResolver
import naipingzai.materialfile.provider.linux.isLinuxPath

val Path.name: String
    get() = fileName?.toString() ?: if (isArchivePath) archiveFile.fileName.toString() else "/"

fun Path.toUserFriendlyString(): String = if (isLinuxPath) toFile().path else toUri().toString()

fun Path.isArchiveFile(mimeType: MimeType): Boolean = !isArchivePath && mimeType.isSupportedArchive

val Path.isLocalPath: Boolean
    get() =
        isLinuxPath || (isDocumentPath && DocumentResolver.isLocal(this as DocumentResolver.Path))

val Path.isRemotePath: Boolean
    get() = !isLocalPath
