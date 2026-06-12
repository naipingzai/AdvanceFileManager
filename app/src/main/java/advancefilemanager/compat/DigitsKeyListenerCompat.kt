/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.text.method.DigitsKeyListener
import java.util.Locale

object DigitsKeyListenerCompat {
    fun getInstance(locale: Locale?, sign: Boolean, decimal: Boolean): DigitsKeyListener =
        DigitsKeyListener.getInstance(locale, sign, decimal)
}
