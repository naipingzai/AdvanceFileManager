/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools

import naipingzai.materialfile.R
import naipingzai.materialfile.app.ToolHostActivity
import naipingzai.materialfile.tools.encryption.EncryptionFragment
import naipingzai.materialfile.tools.filecompare.FileCompareFragment
import naipingzai.materialfile.tools.recentfiles.RecentFilesFragment
import naipingzai.materialfile.tools.trash.TrashFragment
import naipingzai.materialfile.util.createIntent

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
        const val ARG_PAGE = "naipingzai.materialfile.extra.TOOL_GROUP_PAGE"
    }
}
