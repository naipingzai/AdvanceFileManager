/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.provider.common

import java8.nio.file.FileSystem

interface ByteStringListPathCreator {
    fun getPath(first: ByteString, vararg more: ByteString): ByteStringListPath<*>
}

fun FileSystem.getPath(first: ByteString, vararg more: ByteString): ByteStringListPath<*> =
    (this as ByteStringListPathCreator).getPath(first, *more)
