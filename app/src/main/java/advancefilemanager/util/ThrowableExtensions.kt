/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.util

inline fun <reified T : Throwable> Throwable.findCauseByClass(): T? {
    var current: Throwable? = this
    do {
        if (current is T) {
            return current
        }
        current = current!!.cause
    } while (current != null)
    return null
}
