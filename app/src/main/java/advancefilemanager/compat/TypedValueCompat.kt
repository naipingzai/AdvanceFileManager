/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.util.TypedValue
import androidx.core.util.TypedValueCompat

val TypedValue.complexUnitCompat: Int
    get() = TypedValueCompat.getUnitFromComplexDimension(data)
