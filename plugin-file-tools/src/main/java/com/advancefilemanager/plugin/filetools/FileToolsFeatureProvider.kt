/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.filetools

import com.advancefilemanager.plugin.protocol.PluginFeature
import com.advancefilemanager.plugin.protocol.PluginFeatureProvider

object FileToolsFeatureProvider : PluginFeatureProvider {

    override val pluginId: String = "file-tools"

    override fun getFeatures(): List<PluginFeature> = listOf(
        PluginFeature(
            id = "file_search",
            title = "文件搜索",
            description = "按名称搜索文件",
            mimeTypes = listOf("*/*"),
            pluginId = pluginId,
            actionType = "file_search"
        ),
        PluginFeature(
            id = "duplicate_finder",
            title = "重复文件查找",
            description = "查找重复文件",
            mimeTypes = listOf("*/*"),
            pluginId = pluginId,
            actionType = "duplicate_finder"
        ),
        PluginFeature(
            id = "empty_search",
            title = "空文件夹搜索",
            description = "查找空文件和文件夹",
            mimeTypes = listOf("*/*"),
            pluginId = pluginId,
            actionType = "empty_search"
        ),
        PluginFeature(
            id = "recent_files",
            title = "最近文件",
            description = "查看最近修改的文件",
            mimeTypes = listOf("*/*"),
            pluginId = pluginId,
            actionType = "recent_files"
        ),
        PluginFeature(
            id = "hex_viewer",
            title = "十六进制查看",
            description = "以Hex+ASCII并排查看文件",
            mimeTypes = listOf("*/*"),
            pluginId = pluginId,
            actionType = "hex_viewer"
        ),
        PluginFeature(
            id = "encryption",
            title = "文件加密",
            description = "加密或解密文件",
            mimeTypes = listOf("*/*"),
            pluginId = pluginId,
            actionType = "encryption"
        ),
        PluginFeature(
            id = "file_compare",
            title = "文件对比",
            description = "对比两个文件的内容",
            mimeTypes = listOf("*/*"),
            pluginId = pluginId,
            actionType = "file_compare"
        )
    )
}
