/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.hiddenapi

object HiddenApi {
    fun disableHiddenApiChecks() {
        System.loadLibrary("hiddenapi")
    }
}
