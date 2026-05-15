/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools

import java.io.File

/**
 * 文件类型识别与过滤工具。
 *
 * 提供常见文件类型（图片、视频、音频、文档）的扩展名集合，
 * 以及基于扩展名的文件类型过滤和文件名去重功能。
 */
object FileTypeUtils {

    /**
     * 在指定目录下生成一个唯一的文件名。
     *
     * 如果 [dir]/[name].[ext] 已存在，则自动追加递增后缀:
     * "photo.jpg" → "photo_1.jpg" → "photo_2.jpg" …
     *
     * @param dir 目标目录
     * @param name 文件基础名(不含扩展名)
     * @param ext 文件扩展名(不含点号)，为空则不追加扩展名
     * @return 唯一的 File 对象
     */
    fun getUniqueFile(dir: File, name: String, ext: String): File {
        val dotExt = if (ext.isNotEmpty()) ".$ext" else ""
        var file = File(dir, "$name$dotExt")
        var counter = 1
        while (file.exists()) {
            file = File(dir, "${name}_$counter$dotExt")
            counter++
        }
        return file
    }

    /** 常见图片文件扩展名 */
    val IMAGE_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff", "tif", "heic", "heif",
        "raw", "cr2", "nef", "arw", "dng", "psd"
    )

    /** 常见视频文件扩展名 */
    val VIDEO_EXTENSIONS = setOf(
        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp", "ts", "mpg", "mpeg",
        "rmvb", "rm", "vob", "asf"
    )

    /** 常见音频文件扩展名 */
    val AUDIO_EXTENSIONS = setOf(
        "mp3", "flac", "wav", "aac", "ogg", "wma", "m4a", "opus", "ape", "aiff", "alac",
        "amr", "mid", "midi", "ac3", "dts", "pcm"
    )

    /** 常见文档文件扩展名 */
    val DOCUMENT_EXTENSIONS = setOf(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "ods", "odp",
        "csv", "md", "json", "xml", "html", "htm", "epub"
    )

    private val FFMPEG_IMAGE_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "bmp", "webp", "tiff", "tif"
    )

    fun isImageFile(name: String): Boolean {
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in IMAGE_EXTENSIONS
    }

    /** 文件类型过滤器 */
    enum class FileTypeFilter {
        ALL, IMAGE, VIDEO, AUDIO, DOCUMENT
    }

    /**
     * 判断文件名是否匹配指定的文件类型过滤器。
     *
     * @param name 文件名 (含扩展名)
     * @param filter 文件类型过滤器
     * @return 是否匹配
     */
    fun matchesFileType(name: String, filter: FileTypeFilter): Boolean {
        if (filter == FileTypeFilter.ALL) return true
        val ext = name.substringAfterLast('.', "").lowercase()
        return when (filter) {
            FileTypeFilter.IMAGE -> ext in IMAGE_EXTENSIONS
            FileTypeFilter.VIDEO -> ext in VIDEO_EXTENSIONS
            FileTypeFilter.AUDIO -> ext in AUDIO_EXTENSIONS
            FileTypeFilter.DOCUMENT -> ext in DOCUMENT_EXTENSIONS
            FileTypeFilter.ALL -> true
        }
    }
}
