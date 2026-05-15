/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import java8.nio.file.AccessMode

class AccessModes(val read: Boolean, val write: Boolean, val execute: Boolean)

fun Array<out AccessMode>.toAccessModes(): AccessModes {
    var read = false
    var write = false
    var execute = false
    for (mode in this) {
        when (mode) {
            AccessMode.READ -> read = true
            AccessMode.WRITE -> write = true
            AccessMode.EXECUTE -> execute = true
        }
    }
    return AccessModes(read, write, execute)
}
