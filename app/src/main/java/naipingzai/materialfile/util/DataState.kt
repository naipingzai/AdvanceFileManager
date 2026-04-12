/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

/**
 * 通用的数据加载状态封装。
 *
 * 用于表示异步数据加载的三种状态：加载中、成功、失败。
 */
sealed class DataState<T> {
    abstract val data: T?

    data class Loading<T>(override val data: T? = null) : DataState<T>()

    data class Success<T>(override val data: T) : DataState<T>()

    data class Error<T>(override val data: T?, val throwable: Throwable) : DataState<T>()
}

fun <T> DataState<T>.toLoading(): DataState.Loading<T> =
    this as? DataState.Loading ?: DataState.Loading(data)

fun <T> DataState<T>.toError(throwable: Throwable): DataState.Error<T> =
    DataState.Error(data, throwable)
