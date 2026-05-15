/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools

import com.advancefilemanager.R
import com.advancefilemanager.app.ToolHostActivity
import com.advancefilemanager.tools.encryption.EncryptionFragment
import com.advancefilemanager.tools.filecompare.FileCompareFragment
import com.advancefilemanager.tools.recentfiles.RecentFilesFragment
import com.advancefilemanager.tools.trash.TrashFragment
import com.advancefilemanager.util.createIntent

/**
 * 工具分组页面枚举，定义了每个分组中包含的工具列表。
 */
enum class ToolGroupPage {
    FILE_TOOLS {
        override fun createToolItems(): List<ToolItem> = listOf(
            ToolItem(
                R.drawable.compare_icon_white_24dp,
                R.string.navigation_file_compare,
                R.string.tool_desc_file_compare,
                ToolHostActivity.createIntent<FileCompareFragment>(R.string.file_compare_title)
            ),
            ToolItem(
                R.drawable.lock_icon_white_24dp,
                R.string.navigation_encryption,
                R.string.tool_desc_encryption,
                ToolHostActivity.createIntent<EncryptionFragment>(R.string.encryption_title)
            )
        )
    },
    STORAGE_TOOLS {
        override fun createToolItems(): List<ToolItem> = listOf(
            ToolItem(
                R.drawable.delete_icon_white_24dp,
                R.string.navigation_trash,
                R.string.tool_desc_trash,
                ToolHostActivity.createIntent<TrashFragment>(R.string.trash_title)
            ),
            ToolItem(
                R.drawable.history_icon_white_24dp,
                R.string.navigation_recent_files,
                R.string.tool_desc_recent_files,
                ToolHostActivity.createIntent<RecentFilesFragment>(R.string.recent_files_title)
            )
        )
    };

    abstract fun createToolItems(): List<ToolItem>

    companion object {
        const val ARG_PAGE = "com.advancefilemanager.extra.TOOL_GROUP_PAGE"
    }
}
