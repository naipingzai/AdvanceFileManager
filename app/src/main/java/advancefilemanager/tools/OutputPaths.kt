/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools

import android.os.Environment
import java.io.File

object OutputPaths {
    const val BASE = "MaterialFile"

    const val ENCRYPTED = "$BASE/Encrypted"
    const val DECRYPTED = "$BASE/Decrypted"
    const val COMPRESSED = "$BASE/Compressed"
    const val CONVERTED_IMAGE = "$BASE/Converted/image"
    const val MERGED = "$BASE/Merged"
    const val EXTRACTED_AUDIO = "$BASE/ExtractedAudio"
    const val TRIMMED = "$BASE/Trimmed"
    const val SNAPSHOT = "$BASE/Snapshot"
    const val GIF = "$BASE/GIF"
    const val APK_BACKUP = "$BASE/APK_Backup"
    const val ENHANCED = "$BASE/Enhanced"

    fun converted(subDir: String): String = "$BASE/Converted/$subDir"

    /**
     * Environment.getExternalStorageDirectory() is deprecated since API 29.
     * It still works on all target APIs with MANAGE_EXTERNAL_STORAGE permission,
     * which this app already requests for its file-manager functionality.
     */
    @Suppress("DEPRECATION")
    fun resolve(relativePath: String): File =
        File(Environment.getExternalStorageDirectory(), relativePath)
}
