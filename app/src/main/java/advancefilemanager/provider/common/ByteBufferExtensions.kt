/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import java.nio.ByteBuffer
import kotlin.reflect.KClass

private val EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0)

val KClass<ByteBuffer>.EMPTY: ByteBuffer
    get() = EMPTY_BYTE_BUFFER
