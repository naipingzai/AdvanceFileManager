/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.util

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import kotlin.reflect.KClass

fun KClass<Environment>.supportsExternalStorageManager(): Boolean = true

fun KClass<Environment>.createManageAppAllFilesAccessPermissionIntent(packageName: String): Intent =
    Intent(
        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
        Uri.fromParts("package", packageName, null)
    )
