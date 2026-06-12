/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat

@Suppress("UNCHECKED_CAST")
fun <T : View> View.requireViewByIdCompat(@IdRes id: Int): T =
    ViewCompat.requireViewById(this, id) as T

var View.scrollIndicatorsCompat: Int
    get() = ViewCompat.getScrollIndicators(this)
    set(value) {
        ViewCompat.setScrollIndicators(this, value)
    }

fun View.setScrollIndicatorsCompat(indicators: Int, mask: Int) {
    ViewCompat.setScrollIndicators(this, indicators, mask)
}

var View.foregroundCompat: Drawable?
    get() = foreground
    set(value) {
        foreground = value
    }

var View.foregroundGravityCompat: Int
    get() = foregroundGravity
    set(value) {
        foregroundGravity = value
    }

var View.foregroundTintListCompat: ColorStateList?
    get() = foregroundTintList
    set(value) {
        foregroundTintList = value
    }

var View.foregroundTintModeCompat: PorterDuff.Mode?
    get() = foregroundTintMode
    set(value) {
        foregroundTintMode = value
    }
