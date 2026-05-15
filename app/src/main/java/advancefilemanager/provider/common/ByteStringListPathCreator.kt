/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import java8.nio.file.FileSystem

interface ByteStringListPathCreator {
    fun getPath(first: ByteString, vararg more: ByteString): ByteStringListPath<*>
}

fun FileSystem.getPath(first: ByteString, vararg more: ByteString): ByteStringListPath<*> =
    (this as ByteStringListPathCreator).getPath(first, *more)
