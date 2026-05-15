/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.content.res.Resources
import androidx.annotation.DimenRes
import androidx.core.content.res.ResourcesCompat

fun Resources.getFloatCompat(@DimenRes id: Int) = ResourcesCompat.getFloat(this, id)
