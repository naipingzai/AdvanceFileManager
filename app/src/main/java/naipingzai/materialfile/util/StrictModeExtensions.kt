/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import android.os.StrictMode
import kotlin.reflect.KClass

fun <R> KClass<StrictMode>.withoutPenaltyDeathOnNetwork(block: () -> R): R {
    val oldThreadPolicy = StrictMode.getThreadPolicy()
    val newThreadPolicy = StrictMode.ThreadPolicy.Builder(oldThreadPolicy)
        // There's no API to disable penaltyDeathOnNetwork() but still detect it.
        .permitNetwork()
        .build()
    StrictMode.setThreadPolicy(newThreadPolicy)
    return try {
        block()
    } finally {
        StrictMode.setThreadPolicy(oldThreadPolicy)
    }
}
