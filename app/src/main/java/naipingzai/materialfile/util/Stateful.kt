/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

/**
 * 通用的有状态值包装。
 *
 * 比 [DataState] 更轻量，适合简单的 Loading/Success/Failure 三态场景。
 */
sealed class Stateful<T> {
    abstract val value: T?
}

data class Loading<T>(override val value: T?) : Stateful<T>()

data class Failure<T>(override val value: T?, val throwable: Throwable) : Stateful<T>()

data class Success<T>(override val value: T) : Stateful<T>()
