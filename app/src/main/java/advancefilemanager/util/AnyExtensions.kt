/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.util

fun Any.hash(vararg values: Any?): Int = values.contentDeepHashCode()
