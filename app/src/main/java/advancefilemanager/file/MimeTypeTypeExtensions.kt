/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.file

val MimeType.isApk: Boolean
    get() = this == MimeType.APK

val MimeType.isSupportedArchive: Boolean
    get() = this in supportedArchiveMimeTypes

private val supportedArchiveMimeTypes = mutableListOf(
    "application/gzip",
    "application/java-archive",
    "application/rar",
    "application/zip",
    "application/zstd",
    "application/vnd.android.package-archive",
    "application/vnd.debian.binary-package",
    "application/vnd.ms-cab-compressed",
    "application/vnd.rar",
    "application/x-7z-compressed",
    "application/x-bzip2",
    "application/x-cab",
    "application/x-compress",
    "application/x-cpio",
    "application/x-deb",
    "application/x-debian-package",
    "application/x-gtar",
    "application/x-gtar-compressed",
    "application/x-iso9660-image",
    "application/x-java-archive",
    "application/x-lha",
    "application/x-lzma",
    "application/x-redhat-package-manager",
    "application/x-tar",
    "application/x-ustar",
    "application/x-xz"
).map { it.asMimeType() }.toSet()

val MimeType.isImage: Boolean
    get() = icon == MimeTypeIcon.IMAGE

val MimeType.isAudio: Boolean
    get() = icon == MimeTypeIcon.AUDIO

val MimeType.isVideo: Boolean
    get() = icon == MimeTypeIcon.VIDEO

val MimeType.isMedia: Boolean
    get() = isAudio || isVideo

val MimeType.isPdf: Boolean
    get() = this == MimeType.PDF

val MimeType.isMobi: Boolean
    get() = value in mobiMimeTypes

val MimeType.isEpub: Boolean
    get() = value == "application/epub+zip"

val MimeType.isEbook: Boolean
    get() = isMobi || isEpub

val MimeType.isCsv: Boolean
    get() = value == "text/csv" || value == "text/comma-separated-values"

val MimeType.isText: Boolean
    get() = icon == MimeTypeIcon.TEXT || icon == MimeTypeIcon.CODE

private val mobiMimeTypes = setOf(
    "application/x-mobipocket-ebook",
    "application/vnd.amazon.ebook",
    "application/vnd.amazon.mobi8-ebook"
)
