/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.graphics.PointF
import android.view.View
import android.view.ViewGroup
import com.advancefilemanager.hiddenapi.RestrictedHiddenApi
import com.advancefilemanager.util.lazyReflectedMethod

@RestrictedHiddenApi
private val isTransformedTouchPointInViewMethod by lazyReflectedMethod(
    ViewGroup::class.java, "isTransformedTouchPointInView", Float::class.java, Float::class.java,
    View::class.java, PointF::class.java
)

fun ViewGroup.isTransformedTouchPointInViewCompat(
    x: Float,
    y: Float,
    child: View,
    outLocalPoint: PointF?
): Boolean =
    isTransformedTouchPointInViewMethod.invoke(this, x, y, child, outLocalPoint) as Boolean
