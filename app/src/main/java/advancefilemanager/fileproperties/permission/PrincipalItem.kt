/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.permission

import android.content.pm.ApplicationInfo

class PrincipalItem(
    val id: Int,
    val name: String?,
    val applicationInfos: List<ApplicationInfo>,
    val applicationLabels: List<String>
)
