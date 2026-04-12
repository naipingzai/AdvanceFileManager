/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

fun Any.hash(vararg values: Any?): Int = values.contentDeepHashCode()
