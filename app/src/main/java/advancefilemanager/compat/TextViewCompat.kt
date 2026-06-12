/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.widget.TextViewCompat

val TextView.isSingleLineCompat: Boolean
    get() = isSingleLine

fun TextView.setTextAppearanceCompat(@StyleRes resId: Int) {
    TextViewCompat.setTextAppearance(this, resId)
}
