/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.protocol

/**
 * 插件协议常量定义。
 *
 * 主 APP 通过 PackageManager.queryIntentActivities() 查询声明了
 * [ACTION_PLUGIN] 的 Activity 来发现已安装的插件。
 *
 * 插件在 AndroidManifest.xml 中声明：
 * ```xml
 * <activity android:name=".PluginEntryActivity" android:exported="true">
 *     <intent-filter>
 *         <action android:name="com.advancefilemanager.intent.action.PLUGIN" />
 *         <category android:name="android.intent.category.DEFAULT" />
 *     </intent-filter>
 *     <meta-data android:name="com.advancefilemanager.plugin.ID" android:value="media-player" />
 *     <meta-data android:name="com.advancefilemanager.plugin.TITLE" android:resource="@string/plugin_title" />
 *     <meta-data android:name="com.advancefilemanager.plugin.DESCRIPTION" android:resource="@string/plugin_description" />
 *     <meta-data android:name="com.advancefilemanager.plugin.ICON" android:resource="@drawable/plugin_icon" />
 *     <meta-data android:name="com.advancefilemanager.plugin.MIME_TYPES" android:value="video/[*],audio/[*]" />
 *     <meta-data android:name="com.advancefilemanager.plugin.CATEGORY" android:value="viewer" />
 * </activity>
 * ```
 */
object PluginContract {

    /** 插件发现 Intent Action */
    const val ACTION_PLUGIN = "com.advancefilemanager.intent.action.PLUGIN"

    /** 插件操作 Intent Action — 用于具体执行插件功能 */
    const val ACTION_PLUGIN_EXECUTE = "com.advancefilemanager.intent.action.PLUGIN_EXECUTE"

    // --- Meta-data keys ---

    /** 插件唯一 ID (String) */
    const val META_PLUGIN_ID = "com.advancefilemanager.plugin.ID"

    /** 插件显示标题 (String resource) */
    const val META_PLUGIN_TITLE = "com.advancefilemanager.plugin.TITLE"

    /** 插件描述 (String resource) */
    const val META_PLUGIN_DESCRIPTION = "com.advancefilemanager.plugin.DESCRIPTION"

    /** 插件图标 (Drawable resource) */
    const val META_PLUGIN_ICON = "com.advancefilemanager.plugin.ICON"

    // 插件支持的 MIME 类型，逗号分隔 (String)，如 "video/*,audio/*"
    const val META_PLUGIN_MIME_TYPES = "com.advancefilemanager.plugin.MIME_TYPES"

    /**
     * 插件分类 (String)，可选值：
     * - "viewer" : 文件查看器（打开文件时使用）
     * - "tool" : 文件工具（在扩展菜单中展示）
     * - "converter" : 格式转换器
     */
    const val META_PLUGIN_CATEGORY = "com.advancefilemanager.plugin.CATEGORY"

    /** 插件版本 (int) */
    const val META_PLUGIN_VERSION = "com.advancefilemanager.plugin.VERSION"

    // --- Intent extras (主 APP → 插件) ---

    /** 文件 URI (Uri) — 单文件操作 */
    const val EXTRA_FILE_URI = "com.advancefilemanager.extra.FILE_URI"

    /** 文件路径 (String) — 单文件操作的绝对路径 */
    const val EXTRA_FILE_PATH = "com.advancefilemanager.extra.FILE_PATH"

    /** 文件路径列表 (String[]) — 多文件操作 */
    const val EXTRA_FILE_PATHS = "com.advancefilemanager.extra.FILE_PATHS"

    /** 文件 MIME 类型 (String) */
    const val EXTRA_MIME_TYPE = "com.advancefilemanager.extra.MIME_TYPE"

    /** 操作类型 (String) — 具体的插件操作，如 "play", "convert", "compress" */
    const val EXTRA_ACTION_TYPE = "com.advancefilemanager.extra.ACTION_TYPE"

    // --- Intent extras (插件 → 主 APP) ---

    /** 操作结果文件路径 (String) */
    const val EXTRA_RESULT_PATH = "com.advancefilemanager.extra.RESULT_PATH"

    /** 操作结果文件路径列表 (String[]) */
    const val EXTRA_RESULT_PATHS = "com.advancefilemanager.extra.RESULT_PATHS"

    // --- Plugin categories ---

    const val CATEGORY_VIEWER = "viewer"
    const val CATEGORY_TOOL = "tool"
    const val CATEGORY_CONVERTER = "converter"
}
