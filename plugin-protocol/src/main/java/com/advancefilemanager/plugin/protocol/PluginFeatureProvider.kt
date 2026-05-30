/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.protocol

/**
 * 插件功能提供者接口。
 *
 * 每个插件模块实现此接口来声明其子功能列表。
 * 主 APP 通过 ServiceLoader 或注册表发现各插件的功能提供者。
 */
interface PluginFeatureProvider {
    /** 所属插件 ID */
    val pluginId: String

    /** 获取插件提供的所有子功能列表 */
    fun getFeatures(): List<PluginFeature>
}
