/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.protocol

/**
 * Feature constants — replaces the old plugin protocol.
 *
 * Each built-in feature is identified by a unique ID and can be
 * enabled/disabled via Settings → Feature Settings.
 */
object FeatureContract {

    // --- Intent extras (app → feature activity) ---

    /** File URI (Uri) — single file operation */
    const val EXTRA_FILE_URI = "com.advancefilemanager.extra.FILE_URI"

    /** File path (String) — single file absolute path */
    const val EXTRA_FILE_PATH = "com.advancefilemanager.extra.FILE_PATH"

    /** File path list (String[]) — multi-file operation */
    const val EXTRA_FILE_PATHS = "com.advancefilemanager.extra.FILE_PATHS"

    /** File MIME type (String) */
    const val EXTRA_MIME_TYPE = "com.advancefilemanager.extra.MIME_TYPE"

    /** Action type (String) — specific feature action */
    const val EXTRA_ACTION_TYPE = "com.advancefilemanager.extra.ACTION_TYPE"

    // --- Intent extras (feature → app) ---

    /** Result file path (String) */
    const val EXTRA_RESULT_PATH = "com.advancefilemanager.extra.RESULT_PATH"

    /** Result file path list (String[]) */
    const val EXTRA_RESULT_PATHS = "com.advancefilemanager.extra.RESULT_PATHS"

    // --- Feature IDs ---

    const val FEATURE_FILE_TOOLS = "file-tools"
    const val FEATURE_FFMPEG_TOOLS = "ffmpeg-tools"
    const val FEATURE_EBOOK_VIEWER = "ebook-viewer"
}