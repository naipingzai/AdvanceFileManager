/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.coil

import android.content.Context
import android.content.pm.ApplicationInfo
import coil.key.Keyer
import coil.request.Options
import com.advancefilemanager.lib.appiconloader.AppIconLoader
import com.advancefilemanager.R
import com.advancefilemanager.compat.longVersionCodeCompat
import com.advancefilemanager.util.getDimensionPixelSize
import java.io.Closeable

class AppIconApplicationInfoKeyer : Keyer<ApplicationInfo> {
    override fun key(data: ApplicationInfo, options: Options): String =
        AppIconLoader.getIconKey(data, data.longVersionCodeCompat, options.context)
}

class AppIconApplicationInfoFetcherFactory(
    context: Context
) : AppIconFetcher.Factory<ApplicationInfo>(
    // This is used by PrincipalListAdapter.
    context.getDimensionPixelSize(R.dimen.icon_size), context
) {
    override fun getApplicationInfo(data: ApplicationInfo): Pair<ApplicationInfo, Closeable?> =
        data to null
}
