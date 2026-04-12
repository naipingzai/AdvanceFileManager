/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.compat

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.FrameLayout

var FrameLayout.foregroundCompat: Drawable?
    // The get/setForeground() methods were on FrameLayout and are now on View, so this is fine
    // because both are classes and invoke-virtual works for both.
    @SuppressLint("NewApi")
    get() = foreground
    @SuppressLint("NewApi")
    set(value) {
        foreground = value
    }
