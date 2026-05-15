/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.util

import java.util.Locale

/**
 * 文件大小格式化工具。
 *
 * 将字节数转换为人类可读的文件大小字符串 (B, KB, MB, GB)。
 */
object FormatUtils {

    /**
     * 将字节数格式化为可读字符串。
     *
     * 规则:
     * - < 1024: 显示为 "xxx B"
     * - < 1MB: 显示为 "x.x KB" (1位小数)
     * - < 1GB: 显示为 "x.x MB" (1位小数)
     * - >= 1GB: 显示为 "x.xx GB" (2位小数)
     *
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    fun formatSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format(Locale.US, "%.2f GB", gb)
    }
}
