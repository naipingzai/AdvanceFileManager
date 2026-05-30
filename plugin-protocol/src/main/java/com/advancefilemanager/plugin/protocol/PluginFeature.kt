/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.protocol

/**
 * 表示插件的一个子功能。
 *
 * 一个插件可以提供多个子功能（如 FFmpeg 工具提供格式转换、音频提取等），
 * 每个功能都可以独立启用/禁用，启用的功能会出现在文件操作的扩展菜单中。
 */
data class PluginFeature(
    /** 功能唯一 ID，格式: pluginId.featureId（如 ffmpeg-tools.format_convert） */
    val id: String,
    /** 功能显示标题 */
    val title: String,
    /** 功能描述 */
    val description: String?,
    /** 该功能支持的 MIME 类型列表 */
    val mimeTypes: List<String>,
    /** 所属插件 ID */
    val pluginId: String,
    /** 操作类型标识（传递给插件 Activity 的 ACTION_TYPE） */
    val actionType: String
)
