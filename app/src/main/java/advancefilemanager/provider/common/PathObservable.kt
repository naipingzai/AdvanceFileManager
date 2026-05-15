/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import java.io.Closeable

interface PathObservable : Closeable {
    fun addObserver(observer: () -> Unit)

    fun removeObserver(observer: () -> Unit)
}
