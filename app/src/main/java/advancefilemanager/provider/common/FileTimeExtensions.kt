/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import java.time.Instant
import java8.nio.file.attribute.FileTime
import kotlin.reflect.KClass

val KClass<FileTime>.EPOCH: FileTime
    get() = FileTime.from(Instant.EPOCH)
