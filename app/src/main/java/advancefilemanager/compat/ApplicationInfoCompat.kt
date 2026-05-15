/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.content.pm.ApplicationInfo
import android.os.Build
import com.advancefilemanager.hiddenapi.RestrictedHiddenApi
import com.advancefilemanager.util.lazyReflectedField

@RestrictedHiddenApi
private val versionCodeField by lazyReflectedField(ApplicationInfo::class.java, "versionCode")

@RestrictedHiddenApi
private val longVersionCodeField by lazyReflectedField(
    ApplicationInfo::class.java, "longVersionCode"
)

val ApplicationInfo.longVersionCodeCompat: Long
    get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCodeField.getLong(this)
        } else {
            versionCodeField.getInt(this).toLong()
        }
