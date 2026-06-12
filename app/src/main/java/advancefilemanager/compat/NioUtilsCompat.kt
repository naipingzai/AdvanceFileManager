/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import com.advancefilemanager.hiddenapi.RestrictedHiddenApi
import com.advancefilemanager.util.lazyReflectedMethod
import java.io.Closeable
import java.io.FileDescriptor
import java.nio.channels.FileChannel

object NioUtilsCompat {
    @RestrictedHiddenApi
    private val newFileChannelMethod by lazyReflectedMethod(
        "java.nio.NioUtils", "newFileChannel", Closeable::class.java, FileDescriptor::class.java,
        Int::class.java
    )

    fun newFileChannel(ioObject: Closeable, fd: FileDescriptor, flags: Int): FileChannel =
        newFileChannelMethod.invoke(null, ioObject, fd, flags) as FileChannel
}
