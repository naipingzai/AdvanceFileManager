/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import java.io.IOException

abstract class PosixFileStore : AbstractFileStore() {
    @Throws(IOException::class)
    abstract fun refresh()

    @Throws(IOException::class)
    abstract fun setReadOnly(readOnly: Boolean)
}
