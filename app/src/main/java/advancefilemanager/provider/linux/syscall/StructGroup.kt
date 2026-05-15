/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.linux.syscall

import com.advancefilemanager.provider.common.ByteString

class StructGroup(
    val gr_name: ByteString?,
    val gr_passwd: ByteString?,
    val gr_gid: Int,
    val gr_mem: Array<ByteString>?
)
