package com.advancefilemanager.util

import android.os.storage.StorageVolume
import com.advancefilemanager.compat.directoryCompat

val StorageVolume.isMounted: Boolean
    get() = directoryCompat != null
