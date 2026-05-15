/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.util

import androidx.core.os.LocaleListCompat
import java.util.Locale

fun LocaleListCompat.toList(): List<Locale> = List(size()) { this[it]!! }
