/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import android.content.Context
import android.os.Build
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileTime
import com.advancefilemanager.file.FileItem
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.file.getBrokenSymbolicLinkName
import com.advancefilemanager.file.getName
import com.advancefilemanager.file.isApk
import com.advancefilemanager.file.isImage
import com.advancefilemanager.file.isMedia
import com.advancefilemanager.file.isPdf
import com.advancefilemanager.provider.archive.createArchiveRootPath
import com.advancefilemanager.provider.document.documentSupportsThumbnail
import com.advancefilemanager.provider.document.isDocumentPath
import com.advancefilemanager.provider.linux.isLinuxPath
import com.advancefilemanager.settings.Settings
import com.advancefilemanager.util.asFileName
import com.advancefilemanager.util.isGetPackageArchiveInfoCompatible
import com.advancefilemanager.util.isMediaMetadataRetrieverCompatible
import com.advancefilemanager.util.valueCompat
import java.text.CollationKey

val FileItem.name: String
    get() = path.name

val FileItem.baseName: String
    get() = if (attributes.isDirectory) name else name.asFileName().baseName

val FileItem.extension: String
    get() = if (attributes.isDirectory) "" else name.asFileName().extensions

fun FileItem.getMimeTypeName(context: Context): String {
        if (attributesNoFollowLinks.isSymbolicLink && isSymbolicLinkBroken) {
            return MimeType.getBrokenSymbolicLinkName(context)
        }
        return mimeType.getName(extension, context)
    }

val FileItem.isArchiveFile: Boolean
    get() = path.isArchiveFile(mimeType)

val FileItem.isListable: Boolean
    get() = attributes.isDirectory || isArchiveFile

val FileItem.listablePath: Path
    get() = if (isArchiveFile) path.createArchiveRootPath() else path

// @see PathAttributesFetcher.fetch
val FileItem.supportsThumbnail: Boolean
    get() {
        if (path.isDocumentPath && attributes.documentSupportsThumbnail) {
            return true
        }
        return when {
            mimeType.isApk && path.isGetPackageArchiveInfoCompatible -> true
            mimeType.isImage -> true
            mimeType.isMedia && path.isMediaMetadataRetrieverCompatible -> true
            mimeType.isPdf && (path.isLinuxPath || path.isDocumentPath) ->
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                    || Settings.SHOW_PDF_THUMBNAIL_PRE_28.valueCompat
            else -> false
        }
    }

// @see android.content.pm.parsing.ApkLiteParseUtils.parsePackageSplitNames
// @see android.content.pm.parsing.ParsingPackageUtils.validateName
// @see com.android.server.pm.PackageManagerService.getNextCodePath
private const val PACKAGE_NAME_COMPONENT_PATTERN = "[A-Za-z][0-9A-Z_a-z]*"
private const val PACKAGE_NAME_PATTERN =
    "$PACKAGE_NAME_COMPONENT_PATTERN(?:\\.$PACKAGE_NAME_COMPONENT_PATTERN)+"
private const val BASE64_URL_SAFE_CHARACTER_CLASS = "[0-9A-Za-z\\-_]"
private const val BASE64_URL_SAFE_PATTERN = ("(?:$BASE64_URL_SAFE_CHARACTER_CLASS{4})*"
    + "(?:$BASE64_URL_SAFE_CHARACTER_CLASS{3}=|$BASE64_URL_SAFE_CHARACTER_CLASS{2}==)?")
private val APP_DIRECTORY_REGEX =
    Regex("($PACKAGE_NAME_PATTERN)(?:-$BASE64_URL_SAFE_PATTERN)?")

val FileItem.appDirectoryPackageName: String?
    get() {
        if (!attributes.isDirectory) {
            return null
        }
        return APP_DIRECTORY_REGEX.matchEntire(name)?.groupValues?.get(1)
    }

fun FileItem.createDummyArchiveRoot(): FileItem =
    FileItem(
        path.createArchiveRootPath(), DummyCollationKey(), DummyArchiveRootBasicFileAttributes(),
        null, null, false, MimeType.DIRECTORY
    )

// Dummy collation key only to be added to the selection set, which may be used to determine file
// type when confirming deletion.
private class DummyCollationKey : CollationKey("") {
    override fun compareTo(other: CollationKey?): Int {
        throw UnsupportedOperationException()
    }

    override fun toByteArray(): ByteArray {
        throw UnsupportedOperationException()
    }
}

// Dummy attributes only to be added to the selection set, which may be used to determine file
// type when confirming deletion.
private class DummyArchiveRootBasicFileAttributes : BasicFileAttributes {
    override fun lastModifiedTime(): FileTime {
        throw UnsupportedOperationException()
    }

    override fun lastAccessTime(): FileTime {
        throw UnsupportedOperationException()
    }

    override fun creationTime(): FileTime {
        throw UnsupportedOperationException()
    }

    override fun isRegularFile(): Boolean = false

    override fun isDirectory(): Boolean = true

    override fun isSymbolicLink(): Boolean = false

    override fun isOther(): Boolean = false

    override fun size(): Long {
        throw UnsupportedOperationException()
    }

    override fun fileKey(): Any {
        throw UnsupportedOperationException()
    }
}
