/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.linux.syscall

import com.advancefilemanager.provider.common.ByteString

class StructDirent(
    val d_ino: Long, /*ino_t*/
    val d_off: Long, /*off64_t*/
    val d_reclen: Int, /*unsigned short*/
    val d_type: Int, /*unsigned char*/
    val d_name: ByteString
)
