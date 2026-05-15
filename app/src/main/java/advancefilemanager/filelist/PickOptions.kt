/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import com.advancefilemanager.file.MimeType

class PickOptions(
    val mode: Mode,
    val fileName: String?,
    val readOnly: Boolean,
    val mimeTypes: List<MimeType>,
    val localOnly: Boolean,
    val allowMultiple: Boolean
) {
    enum class Mode {
        OPEN_FILE,
        CREATE_FILE,
        OPEN_DIRECTORY
    }
}
