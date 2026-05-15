/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.linux.syscall

import com.advancefilemanager.provider.common.ByteString

class StructInotifyEvent(
    val wd: Int,
    val mask: Int, /* uint32_t */
    val cookie: Int, /* uint32_t */
    val name: ByteString?
)
