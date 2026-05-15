/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.file

import android.text.format.DateUtils
import java.time.Duration

fun Duration.format(): String = DateUtils.formatElapsedTime(seconds)
