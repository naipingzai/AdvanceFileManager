/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.protocol

import android.content.ComponentName
import android.graphics.drawable.Drawable

/**
 * 表示一个已发现的插件信息。
 */
data class PluginInfo(
    /** 插件唯一 ID */
    val id: String,
    /** 插件显示标题 */
    val title: CharSequence,
    /** 插件描述 */
    val description: CharSequence?,
    /** 插件图标 */
    val icon: Drawable?,
    /** 支持的 MIME 类型列表 */
    val mimeTypes: List<String>,
    /** 插件分类 */
    val category: String,
    /** 插件 Activity 的 ComponentName */
    val componentName: ComponentName,
    /** 插件所在包名 */
    val packageName: String,
    /** 插件版本 */
    val version: Int = 1,
    /** 插件提供的子功能列表 */
    val features: List<PluginFeature> = emptyList()
)
