/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.content.Intent

fun Intent.removeFlagsCompat(flags: Int) {
    removeFlags(flags)
}
