/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.apk

import android.content.pm.PermissionInfo

class PermissionItem(
    val name: String,
    val permissionInfo: PermissionInfo?,
    val label: String?,
    val description: String?
)
