/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import android.content.Context
import java8.nio.file.Path

data class BreadcrumbData(
    val paths: List<Path>,
    val nameProducers: List<(Context) -> String>,
    val selectedIndex: Int
)
