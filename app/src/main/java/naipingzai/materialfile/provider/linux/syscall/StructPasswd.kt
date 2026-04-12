/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.provider.linux.syscall

import naipingzai.materialfile.provider.common.ByteString

class StructPasswd(
    val pw_name: ByteString?,
    val pw_uid: Int,
    val pw_gid: Int,
    val pw_gecos: ByteString?,
    val pw_dir: ByteString?,
    val pw_shell: ByteString?
)
