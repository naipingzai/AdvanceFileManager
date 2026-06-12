/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.os.Handler
import android.os.ParcelFileDescriptor
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import java.io.IOException

val StorageManager.storageVolumesCompat: List<StorageVolume>
    get() = storageVolumes

@Throws(IOException::class)
fun StorageManager.openProxyFileDescriptorCompat(
    mode: Int,
    callback: ProxyFileDescriptorCallbackCompat,
    handler: Handler
): ParcelFileDescriptor =
    openProxyFileDescriptor(mode, callback.toProxyFileDescriptorCallback(), handler)
