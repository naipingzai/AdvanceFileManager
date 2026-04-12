/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import android.os.Handler
import android.os.SystemClock

/**
 * 节流 Runnable 包装器。
 *
 * 保证在指定时间间隔内最多执行一次。
 * 适用于滚动事件、进度更新等需要限频的场景。
 *
 * @param handler 用于调度执行的 Handler
 * @param intervalMillis 最小执行间隔 (毫秒)
 * @param block 要执行的代码块
 */
class ThrottledRunnable(
    private val handler: Handler,
    private val intervalMillis: Long,
    block: () -> Unit
) : () -> Unit {
    private val lock = Any()
    private val runnable = Runnable(block)
    private var scheduledUptimeMillis = 0L

    override operator fun invoke() {
        synchronized(lock) {
            val currentUptimeMillis = SystemClock.uptimeMillis()
            if (scheduledUptimeMillis + intervalMillis < currentUptimeMillis) {
                scheduledUptimeMillis = 0
            }
            when {
                scheduledUptimeMillis == 0L -> {
                    scheduledUptimeMillis = currentUptimeMillis
                    handler.post(runnable)
                }
                scheduledUptimeMillis <= currentUptimeMillis -> {
                    scheduledUptimeMillis += intervalMillis
                    handler.postAtTime(runnable, scheduledUptimeMillis)
                }
                else -> {}
            }
        }
    }

    /** 取消挂起的执行并重置状态 */
    fun cancel() {
        synchronized(lock) {
            scheduledUptimeMillis = 0
            handler.removeCallbacks(runnable)
        }
    }
}
