/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filejob

enum class FileJobConflictAction {
    MERGE_OR_REPLACE,
    RENAME,
    SKIP,
    CANCEL,
    CANCELED
}
