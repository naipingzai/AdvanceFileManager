/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.service.quicksettings.TileService

fun TileService.doWithStartForegroundServiceAllowed(action: () -> Unit) {
    action()
}
