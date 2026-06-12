/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.RotateDrawable
import kotlin.reflect.KClass

fun KClass<RotateDrawable>.createCompat(): RotateDrawable = RotateDrawable()

var RotateDrawable.isPivotXRelativeCompat: Boolean
    get() = isPivotXRelative
    set(value) { isPivotXRelative = value }

var RotateDrawable.pivotXCompat: Float
    get() = pivotX
    set(value) { pivotX = value }

var RotateDrawable.isPivotYRelativeCompat: Boolean
    get() = isPivotYRelative
    set(value) { isPivotYRelative = value }

var RotateDrawable.pivotYCompat: Float
    get() = pivotY
    set(value) { pivotY = value }

var RotateDrawable.drawableCompat: Drawable?
    // The get/setDrawable() methods were on RotateDrawable and are now on DrawableWrapper, so this
    // is fine because both are classes and invoke-virtual works for both.
    @SuppressLint("NewApi")
    get() = drawable
    @SuppressLint("NewApi")
    set(value) {
        drawable = value
    }
