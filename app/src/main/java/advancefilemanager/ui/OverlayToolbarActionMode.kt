/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.ui

import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.advancefilemanager.util.fadeInUnsafe
import com.advancefilemanager.util.fadeOutUnsafe

class OverlayToolbarActionMode(bar: ViewGroup, toolbar: Toolbar) : ToolbarActionMode(bar, toolbar) {
    constructor(toolbar: Toolbar) : this(toolbar, toolbar)

    init {
        bar.isVisible = false
    }

    override fun show(bar: ViewGroup, animate: Boolean) {
        if (animate) {
            bar.fadeInUnsafe()
        } else {
            bar.isVisible = true
        }
    }

    override fun hide(bar: ViewGroup, animate: Boolean) {
        if (animate) {
            bar.fadeOutUnsafe()
        } else {
            bar.isVisible = false
        }
    }
}
