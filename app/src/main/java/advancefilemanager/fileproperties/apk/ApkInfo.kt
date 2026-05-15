/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.apk

import android.content.pm.PackageInfo

class ApkInfo(
    val packageInfo: PackageInfo,
    val label: String,
    val signingCertificateDigests: List<String>,
    val pastSigningCertificateDigests: List<String>
)
