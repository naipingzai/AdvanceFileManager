/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.filetools

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TrashUtil {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun formatFileSize(size: Long): String = when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> String.format("%.1f KB", size / 1024.0)
        size < 1024 * 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024))
        else -> String.format("%.2f GB", size / (1024.0 * 1024 * 1024))
    }

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    fun moveToTrash(file: File): Boolean {
        val trashDir = File(file.parentFile, ".trash")
        if (!trashDir.exists()) trashDir.mkdirs()
        val dest = File(trashDir, "${System.currentTimeMillis()}_${file.name}")
        return file.renameTo(dest)
    }
}
