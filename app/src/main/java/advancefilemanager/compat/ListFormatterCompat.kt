/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.icu.text.ListFormatter

object ListFormatterCompat {
    fun format(vararg items: Any?): String =
        ListFormatter.getInstance().format(*items)

    fun format(items: Collection<*>): String =
        ListFormatter.getInstance().format(items)
}
