/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.navigation

import android.content.Context
import java8.nio.file.Path

interface NavigationRoot {
    val path: Path

    fun getName(context: Context): String
}
