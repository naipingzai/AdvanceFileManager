/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.storage.StorageVolume
import java.io.File

val StorageVolume.pathCompat: String
    get() = directory?.path ?: ""

val StorageVolume.pathFileCompat: File
    get() = directory ?: File("")

val StorageVolume.directoryCompat: File?
    get() = directory

@SuppressLint("NewApi")
fun StorageVolume.getDescriptionCompat(context: Context): String = getDescription(context)

val StorageVolume.isPrimaryCompat: Boolean
    @SuppressLint("NewApi")
    get() = isPrimary

val StorageVolume.isRemovableCompat: Boolean
    @SuppressLint("NewApi")
    get() = isRemovable

val StorageVolume.isEmulatedCompat: Boolean
    @SuppressLint("NewApi")
    get() = isEmulated

val StorageVolume.uuidCompat: String?
    @SuppressLint("NewApi")
    get() = uuid

val StorageVolume.stateCompat: String
    @SuppressLint("NewApi")
    get() = state

fun StorageVolume.createOpenDocumentTreeIntentCompat(): Intent =
    createOpenDocumentTreeIntent()
