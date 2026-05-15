/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StandardDirectorySettings(
    val id: String,
    val customTitle: String?,
    val isEnabled: Boolean
) : Parcelable
