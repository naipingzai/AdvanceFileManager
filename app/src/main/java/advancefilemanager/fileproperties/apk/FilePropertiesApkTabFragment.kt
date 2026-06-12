/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.apk

import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import com.advancefilemanager.R
import com.advancefilemanager.compat.longVersionCodeCompat
import com.advancefilemanager.file.FileItem
import com.advancefilemanager.file.isApk
import com.advancefilemanager.fileproperties.FilePropertiesTabFragment
import com.advancefilemanager.util.ParcelableArgs
import com.advancefilemanager.util.ParcelableParceler
import com.advancefilemanager.util.Stateful
import com.advancefilemanager.util.args
import com.advancefilemanager.util.getQuantityString
import com.advancefilemanager.util.getStringArray
import com.advancefilemanager.util.isGetPackageArchiveInfoCompatible
import com.advancefilemanager.util.viewModels

class FilePropertiesApkTabFragment : FilePropertiesTabFragment() {
    private val args by args<Args>()

    private val viewModel by viewModels { { FilePropertiesApkTabViewModel(args.path) } }

    override fun onResume() {
        super.onResume()

        viewModel.apkInfoLiveData.observe(viewLifecycleOwner) { onApkInfoChanged(it) }
    }

    override fun refresh() {
        viewModel.reload()
    }

    private fun onApkInfoChanged(stateful: Stateful<ApkInfo>) {
        bindView(stateful) { apkInfo ->
            addItemView(R.string.file_properties_apk_label, apkInfo.label)
            val packageInfo = apkInfo.packageInfo
            addItemView(R.string.file_properties_apk_package_name, packageInfo.packageName)
            addItemView(
                R.string.file_properties_apk_version, getString(
                    R.string.file_properties_apk_version_format, packageInfo.versionName,
                    packageInfo.longVersionCodeCompat
                )
            )
            val applicationInfo = packageInfo.applicationInfo!!
            addItemView(
                R.string.file_properties_apk_min_sdk_version,
                getSdkVersionText(applicationInfo.minSdkVersion)
            )
            addItemView(
                R.string.file_properties_apk_target_sdk_version,
                getSdkVersionText(applicationInfo.targetSdkVersion)
            )
            val requestedPermissionsSize = packageInfo.requestedPermissions?.size ?: 0
            addItemView(
                R.string.file_properties_apk_requested_permissions,
                if (requestedPermissionsSize == 0) {
                    getString(R.string.file_properties_apk_requested_permissions_zero)
                } else {
                    getQuantityString(
                        R.plurals.file_properties_apk_requested_permissions_positive_format,
                        requestedPermissionsSize, requestedPermissionsSize
                    )
                }, if (requestedPermissionsSize == 0) {
                    null
                } else {
                    {
                        PermissionListDialogFragment.show(
                            packageInfo.requestedPermissions!!, this@FilePropertiesApkTabFragment
                        )
                    }
                }
            )
            addItemView(
                R.string.file_properties_apk_signature_digests,
                if (apkInfo.signingCertificateDigests.isNotEmpty()) {
                    apkInfo.signingCertificateDigests.joinToString("\n")
                } else {
                    getString(R.string.file_properties_apk_signature_digests_empty)
                }
            )
            if (apkInfo.pastSigningCertificateDigests.isNotEmpty()) {
                addItemView(
                    R.string.file_properties_apk_past_signature_digests,
                    apkInfo.pastSigningCertificateDigests.joinToString("\n")
                )
            }
        }
    }

    private fun getSdkVersionText(sdkVersion: Int): String {
        val names = getStringArray(R.array.file_properties_apk_sdk_version_names)
        val codeNames = getStringArray(R.array.file_properties_apk_sdk_version_codenames)
        return getString(
            R.string.file_properties_apk_sdk_version_format,
            names[sdkVersion.coerceIn(names.indices)],
            codeNames[sdkVersion.coerceIn(codeNames.indices)], sdkVersion
        )
    }

    companion object {
        fun isAvailable(file: FileItem): Boolean =
            file.mimeType.isApk && file.path.isGetPackageArchiveInfoCompatible
    }

    @Parcelize
    class Args(val path: @WriteWith<ParcelableParceler> Path) : ParcelableArgs
}
