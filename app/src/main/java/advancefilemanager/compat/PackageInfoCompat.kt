/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.content.pm.PackageInfo
import androidx.core.content.pm.PackageInfoCompat

val PackageInfo.longVersionCodeCompat: Long
    get() = PackageInfoCompat.getLongVersionCode(this)
