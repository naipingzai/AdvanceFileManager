/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.apk

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import android.os.Build
import java8.nio.file.Path
import com.advancefilemanager.app.packageManager
import com.advancefilemanager.fileproperties.PathObserverLiveData
import com.advancefilemanager.util.Failure
import com.advancefilemanager.util.Loading
import com.advancefilemanager.util.Stateful
import com.advancefilemanager.util.Success
import com.advancefilemanager.util.getPackageArchiveInfoCompat
import com.advancefilemanager.util.sha1Digest
import com.advancefilemanager.util.toHexString
import com.advancefilemanager.util.valueCompat
import java.io.IOException

class ApkInfoLiveData(path: Path) : PathObserverLiveData<Stateful<ApkInfo>>(path) {
    init {
        loadValue()
        observe()
    }

    override fun loadValue() {
        value = Loading(value?.value)
        Dispatchers.IO.asExecutor().execute {
            val value = try {
                // We must always pass in PackageManager.GET_SIGNATURES for
                // PackageManager.getPackageArchiveInfo() to call
                // PackageParser.collectCertificates().
                @Suppress("DEPRECATION")
                var packageInfoFlags = (PackageManager.GET_PERMISSIONS
                    or PackageManager.GET_SIGNATURES)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfoFlags = packageInfoFlags or PackageManager.GET_SIGNING_CERTIFICATES
                }
                val (packageInfo, closeable) =
                    packageManager.getPackageArchiveInfoCompat(path, packageInfoFlags)
                val apkInfo = closeable.use {
                    val applicationInfo = packageInfo?.applicationInfo
                        ?: throw IOException("ApplicationInfo is null")
                    val label = applicationInfo.loadLabel(packageManager).toString()
                    val signingCertificates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        // PackageInfo.signatures returns only the oldest certificate if there are
                        // past certificates on P and above for compatibility.
                        packageInfo.signingInfo?.apkContentsSigners
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.signatures
                    } ?: emptyArray()
                    val signingCertificateDigests = signingCertificates
                        .map { it.toByteArray().sha1Digest().toHexString() }
                    val pastSigningCertificates =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val signingInfo = packageInfo.signingInfo
                            // SigningInfo.getSigningCertificateHistory() may return the current
                            // certificate if there are no past certificates.
                            if (signingInfo?.hasPastSigningCertificates() == true) {
                                // SigningInfo.getSigningCertificateHistory() also returns the
                                // current certificate.
                                signingInfo.signingCertificateHistory?.toMutableList()
                                    ?.apply { removeAll(signingCertificates) }
                            } else {
                                null
                            }
                        } else {
                            null
                        } ?: emptyList()
                    val pastSigningCertificateDigests = pastSigningCertificates
                        .map { it.toByteArray().sha1Digest().toHexString() }
                    ApkInfo(
                        packageInfo, label, signingCertificateDigests, pastSigningCertificateDigests
                    )
                }
                Success(apkInfo)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }
}
