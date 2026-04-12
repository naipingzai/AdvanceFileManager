/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import android.os.Handler

/**
 * 防抖 Runnable 包装器。
 *
 * 在连续调用时，只有最后一次调用会在指定延迟后执行。
 * 适用于搜索输入、窗口调整等频繁触发的事件。
 *
 * @param handler 用于调度延迟执行的 Handler
 * @param intervalMillis 防抖间隔 (毫秒)
 * @param block 要执行的代码块
 */
class DebouncedRunnable(
    private val handler: Handler,
    private val intervalMillis: Long,
    block: () -> Unit
) : () -> Unit {
    private val lock = Any()
    private val runnable = Runnable(block)

    override operator fun invoke() {
        synchronized(lock) {
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, intervalMillis)
        }
    }

    /** 取消挂起的执行 */
    fun cancel() {
        synchronized(lock) { handler.removeCallbacks(runnable) }
    }
}
