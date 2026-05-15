/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.util

import android.widget.EditText

fun EditText.setTextWithSelection(text: CharSequence?) {
    setText(text)
    setSelection(0, this.text.length)
}
