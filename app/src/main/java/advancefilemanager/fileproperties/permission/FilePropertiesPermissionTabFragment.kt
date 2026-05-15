/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.permission

import android.os.Bundle
import com.advancefilemanager.R
import com.advancefilemanager.file.FileItem
import com.advancefilemanager.fileproperties.FilePropertiesFileViewModel
import com.advancefilemanager.fileproperties.FilePropertiesTabFragment
import com.advancefilemanager.provider.common.PosixFileAttributes
import com.advancefilemanager.provider.common.PosixPrincipal
import com.advancefilemanager.provider.common.toInt
import com.advancefilemanager.provider.common.toModeString
import com.advancefilemanager.util.Stateful
import com.advancefilemanager.util.viewModels

class FilePropertiesPermissionTabFragment : FilePropertiesTabFragment() {
    private val viewModel by viewModels<FilePropertiesFileViewModel>({ requireParentFragment() })

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.fileLiveData.observe(viewLifecycleOwner) { onFileChanged(it) }
    }

    override fun refresh() {
        viewModel.reload()
    }

    private fun onFileChanged(stateful: Stateful<FileItem>) {
        bindView(stateful) { file ->
            val attributes = file.attributes as PosixFileAttributes
            val owner = attributes.owner()
            addItemView(
                R.string.file_properties_permission_owner, getPrincipalText(owner), owner?.let {
                    { SetOwnerDialogFragment.show(file, this@FilePropertiesPermissionTabFragment) }
                }
            )
            val group = attributes.group()
            addItemView(
                R.string.file_properties_permission_group, getPrincipalText(group), group?.let {
                    { SetGroupDialogFragment.show(file, this@FilePropertiesPermissionTabFragment) }
                }
            )
            val mode = attributes.mode()
            addItemView(
                R.string.file_properties_permission_mode, if (mode != null) {
                    getString(
                        R.string.file_properties_permission_mode_format, mode.toModeString(),
                        mode.toInt()
                    )
                } else {
                    getString(R.string.unknown)
                }, if (mode != null && !attributes.isSymbolicLink) {
                    { SetModeDialogFragment.show(file, this@FilePropertiesPermissionTabFragment) }
                } else {
                    null
                }
            )
            val seLinuxContext = attributes.seLinuxContext()
            if (seLinuxContext != null) {
                addItemView(
                    R.string.file_properties_permission_selinux_context,
                    if (seLinuxContext.isNotEmpty()) {
                        seLinuxContext.toString()
                    } else {
                        getString(R.string.empty_placeholder)
                    }
                ) {
                    SetSeLinuxContextDialogFragment.show(
                        file, this@FilePropertiesPermissionTabFragment
                    )
                }
            }
        }
    }

    private fun getPrincipalText(principal: PosixPrincipal?) =
        if (principal != null) {
            if (principal.name != null) {
                getString(
                    R.string.file_properties_permission_principal_format, principal.name,
                    principal.id
                )
            } else {
                principal.id.toString()
            }
        } else {
            getString(R.string.unknown)
        }

    companion object {
        fun isAvailable(file: FileItem): Boolean {
            val attributes = file.attributes
            return attributes is PosixFileAttributes && (attributes.owner() != null
                || attributes.group() != null || attributes.mode() != null
                || attributes.seLinuxContext() != null)
        }
    }
}
