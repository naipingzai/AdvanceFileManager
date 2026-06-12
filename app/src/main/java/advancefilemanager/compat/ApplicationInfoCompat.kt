/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.content.pm.ApplicationInfo
import com.advancefilemanager.hiddenapi.RestrictedHiddenApi
import com.advancefilemanager.util.lazyReflectedField

@RestrictedHiddenApi
private val longVersionCodeField by lazyReflectedField(
    ApplicationInfo::class.java, "longVersionCode"
)

val ApplicationInfo.longVersionCodeCompat: Long
    get() = longVersionCodeField.getLong(this)
