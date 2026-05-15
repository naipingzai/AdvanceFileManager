/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.view.Menu
import androidx.core.view.MenuCompat

fun Menu.setGroupDividerEnabledCompat(groupDividerEnabled: Boolean) {
    MenuCompat.setGroupDividerEnabled(this, groupDividerEnabled)
}
