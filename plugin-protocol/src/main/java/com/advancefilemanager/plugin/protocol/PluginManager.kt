/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.protocol

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build

/**
 * 插件发现和调用管理器。
 *
 * 通过 PackageManager 查询声明了插件协议 Action 的已安装应用，
 * 并提供启动插件的便捷方法。
 */
object PluginManager {

    private val featureProviders = mutableListOf<PluginFeatureProvider>()

    /**
     * 注册一个插件功能提供者。
     * 各插件模块在初始化时调用此方法注册自己的功能。
     */
    fun registerFeatureProvider(provider: PluginFeatureProvider) {
        if (featureProviders.none { it.pluginId == provider.pluginId }) {
            featureProviders.add(provider)
        }
    }

    /**
     * 发现所有已安装的插件。
     */
    fun discoverPlugins(context: Context): List<PluginInfo> {
        val pm = context.packageManager
        val intent = Intent(PluginContract.ACTION_PLUGIN)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_META_DATA
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, flags as PackageManager.ResolveInfoFlags)
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, flags as Int)
        }

        return resolveInfos.mapNotNull { resolveInfo ->
            parsePluginInfo(context, resolveInfo)
        }
    }

    /**
     * 查找支持指定 MIME 类型的插件。
     */
    fun findPluginsForMimeType(context: Context, mimeType: String): List<PluginInfo> {
        return discoverPlugins(context).filter { plugin ->
            plugin.mimeTypes.any { pattern -> matchMimeType(pattern, mimeType) }
        }
    }

    /**
     * 查找指定分类的插件。
     */
    fun findPluginsByCategory(context: Context, category: String): List<PluginInfo> {
        return discoverPlugins(context).filter { it.category == category }
    }

    /**
     * 查找支持指定 MIME 类型的所有插件子功能。
     */
    fun findFeaturesForMimeType(context: Context, mimeType: String): List<Pair<PluginInfo, PluginFeature>> {
        return discoverPlugins(context).flatMap { plugin ->
            plugin.features
                .filter { feature -> feature.mimeTypes.any { matchMimeType(it, mimeType) } }
                .map { feature -> plugin to feature }
        }
    }

    /**
     * 创建启动插件的 Intent。
     *
     * @param plugin 目标插件信息
     * @param filePath 要操作的文件路径
     * @param mimeType 文件 MIME 类型
     * @param fileUri 文件 content:// URI（通过 FileProvider 授权）
     * @param actionType 具体操作类型（可选）
     */
    fun createPluginIntent(
        plugin: PluginInfo,
        filePath: String? = null,
        mimeType: String? = null,
        fileUri: Uri? = null,
        actionType: String? = null,
        filePaths: Array<String>? = null
    ): Intent {
        return Intent(PluginContract.ACTION_PLUGIN_EXECUTE).apply {
            component = plugin.componentName
            fileUri?.let {
                data = it
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            mimeType?.let { type = it }
            filePath?.let { putExtra(PluginContract.EXTRA_FILE_PATH, it) }
            filePaths?.let { putExtra(PluginContract.EXTRA_FILE_PATHS, it) }
            actionType?.let { putExtra(PluginContract.EXTRA_ACTION_TYPE, it) }
        }
    }

    private fun parsePluginInfo(context: Context, resolveInfo: ResolveInfo): PluginInfo? {
        val activityInfo = resolveInfo.activityInfo ?: return null
        val metaData = activityInfo.metaData ?: return null
        val pm = context.packageManager

        val id = metaData.getString(PluginContract.META_PLUGIN_ID) ?: return null
        val title = metaData.getInt(PluginContract.META_PLUGIN_TITLE).let { resId ->
            if (resId != 0) {
                try {
                    pm.getResourcesForApplication(activityInfo.applicationInfo).getString(resId)
                } catch (e: Exception) {
                    metaData.getString(PluginContract.META_PLUGIN_TITLE) ?: id
                }
            } else {
                metaData.getString(PluginContract.META_PLUGIN_TITLE) ?: id
            }
        }
        val description = metaData.getInt(PluginContract.META_PLUGIN_DESCRIPTION).let { resId ->
            if (resId != 0) {
                try {
                    pm.getResourcesForApplication(activityInfo.applicationInfo).getString(resId)
                } catch (e: Exception) {
                    metaData.getString(PluginContract.META_PLUGIN_DESCRIPTION)
                }
            } else {
                metaData.getString(PluginContract.META_PLUGIN_DESCRIPTION)
            }
        }
        val icon = metaData.getInt(PluginContract.META_PLUGIN_ICON).let { resId ->
            if (resId != 0) {
                try {
                    pm.getResourcesForApplication(activityInfo.applicationInfo).getDrawable(resId)
                } catch (e: Exception) {
                    activityInfo.loadIcon(pm)
                }
            } else {
                activityInfo.loadIcon(pm)
            }
        }
        val mimeTypes = metaData.getString(PluginContract.META_PLUGIN_MIME_TYPES)
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()
        val category = metaData.getString(PluginContract.META_PLUGIN_CATEGORY)
            ?: PluginContract.CATEGORY_TOOL
        val version = metaData.getInt(PluginContract.META_PLUGIN_VERSION, 1)

        return PluginInfo(
            id = id,
            title = title,
            description = description,
            icon = icon,
            mimeTypes = mimeTypes,
            category = category,
            componentName = android.content.ComponentName(
                activityInfo.packageName,
                activityInfo.name
            ),
            packageName = activityInfo.packageName,
            version = version,
            features = featureProviders.find { it.pluginId == id }?.getFeatures() ?: emptyList()
        )
    }

    // MIME 类型通配符匹配。支持 "video/*" 匹配 "video/mp4" 等。
    private fun matchMimeType(pattern: String, mimeType: String): Boolean {
        if (pattern == "*/*") return true
        if (pattern == mimeType) return true
        val (patternType, patternSubtype) = pattern.split("/", limit = 2).let {
            if (it.size == 2) it[0] to it[1] else return false
        }
        val (mimeMainType, _) = mimeType.split("/", limit = 2).let {
            if (it.size == 2) it[0] to it[1] else return false
        }
        return patternSubtype == "*" && patternType == mimeMainType
    }
}
