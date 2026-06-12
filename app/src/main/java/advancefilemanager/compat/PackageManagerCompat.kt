/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.advancefilemanager.util.andInv
import com.advancefilemanager.util.hasBits

object PackageManagerCompat {
    @SuppressLint("InlinedApi")
    const val MATCH_UNINSTALLED_PACKAGES = PackageManager.MATCH_UNINSTALLED_PACKAGES
}

fun PackageManager.getPackageArchiveInfoCompat(archiveFilePath: String, flags: Int): PackageInfo? {
    var packageInfo = getPackageArchiveInfo(archiveFilePath, flags)
    if (packageInfo == null) {
        val flagsWithoutGetSigningInfo = flags.andInv(
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES or PackageManager.GET_SIGNING_CERTIFICATES
        )
        if (flags != flagsWithoutGetSigningInfo) {
            packageInfo = getPackageArchiveInfo(archiveFilePath, flagsWithoutGetSigningInfo)
                ?.apply {
                    @Suppress("DEPRECATION")
                    if (flags.hasBits(PackageManager.GET_SIGNATURES)) {
                        signatures = emptyArray()
                    }
                    // Don't create a fake SigningInfo - just leave it as null
                    // The calling code already handles null signingInfo
                }
        }
    }
    return packageInfo
}
