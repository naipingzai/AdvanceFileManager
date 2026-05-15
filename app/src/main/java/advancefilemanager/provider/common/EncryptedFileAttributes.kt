/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import java8.nio.file.attribute.BasicFileAttributes

interface EncryptedFileAttributes {
    fun isEncrypted(): Boolean
}

fun BasicFileAttributes.isEncrypted(): Boolean =
    if (this is EncryptedFileAttributes) isEncrypted() else false
