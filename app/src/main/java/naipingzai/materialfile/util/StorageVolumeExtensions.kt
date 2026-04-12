package naipingzai.materialfile.util

import android.os.storage.StorageVolume
import naipingzai.materialfile.compat.directoryCompat

val StorageVolume.isMounted: Boolean
    get() = directoryCompat != null
