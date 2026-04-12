/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 创建一个延迟初始化的 MutableStateFlow。
 *
 * 内部使用 null 作为初始值，但类型签名保持非空。
 * 适用于需要先创建 StateFlow 再稍后设置初始值的场景。
 *
 * 注意: 在第一个非空值发射前收集此 Flow 可能导致 ClassCastException。
 */
@Suppress("FunctionName", "UNCHECKED_CAST")
fun <T : Any> LateInitMutableStateFlow(): MutableStateFlow<T> =
    MutableStateFlow<T?>(null) as MutableStateFlow<T>
