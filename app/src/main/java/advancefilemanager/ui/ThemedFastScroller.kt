/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.ui

import android.view.ViewGroup
import com.advancefilemanager.lib.fastscroll.FastScroller
import com.advancefilemanager.lib.fastscroll.FastScrollerBuilder

object ThemedFastScroller {
    fun create(view: ViewGroup): FastScroller = FastScrollerBuilder(view).useMd2Style().build()
}
