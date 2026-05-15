/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.app.Service
import androidx.core.app.ServiceCompat

fun Service.stopForegroundCompat(flags: Int) {
    ServiceCompat.stopForeground(this, flags)
}
