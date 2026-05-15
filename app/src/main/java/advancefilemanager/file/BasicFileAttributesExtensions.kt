/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.file

import java.time.Instant
import java8.nio.file.attribute.BasicFileAttributes

val BasicFileAttributes.fileSize: FileSize
    get() = size().asFileSize()

val BasicFileAttributes.lastModifiedInstant: Instant
    get() = lastModifiedTime().toInstant()
