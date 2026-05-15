/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.content.pm.PermissionInfo
import androidx.core.content.pm.PermissionInfoCompat

val PermissionInfo.protectionCompat: Int
    get() = PermissionInfoCompat.getProtection(this)
