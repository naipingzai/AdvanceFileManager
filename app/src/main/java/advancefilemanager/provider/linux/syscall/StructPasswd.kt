/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.linux.syscall

import com.advancefilemanager.provider.common.ByteString

class StructPasswd(
    val pw_name: ByteString?,
    val pw_uid: Int,
    val pw_gid: Int,
    val pw_gecos: ByteString?,
    val pw_dir: ByteString?,
    val pw_shell: ByteString?
)
