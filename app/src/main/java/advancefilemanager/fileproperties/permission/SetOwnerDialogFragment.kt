/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.permission

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import java8.nio.file.Path
import com.advancefilemanager.R
import com.advancefilemanager.file.FileItem
import com.advancefilemanager.filejob.FileJobService
import com.advancefilemanager.provider.common.PosixFileAttributes
import com.advancefilemanager.provider.common.PosixPrincipal
import com.advancefilemanager.provider.common.PosixUser
import com.advancefilemanager.provider.common.toByteString
import com.advancefilemanager.util.SelectionLiveData
import com.advancefilemanager.util.putArgs
import com.advancefilemanager.util.show
import com.advancefilemanager.util.viewModels

class SetOwnerDialogFragment : SetPrincipalDialogFragment() {
    override val viewModel: SetPrincipalViewModel by viewModels { { SetOwnerViewModel() } }

    @StringRes
    override val titleRes: Int = R.string.file_properties_permission_set_owner_title

    override fun createAdapter(selectionLiveData: SelectionLiveData<Int>): PrincipalListAdapter =
        UserListAdapter(selectionLiveData)

    override val PosixFileAttributes.principal: PosixPrincipal
        get() = owner()!!

    override fun setPrincipal(path: Path, principal: PrincipalItem, recursive: Boolean) {
        val owner = PosixUser(principal.id, principal.name?.toByteString())
        FileJobService.setOwner(path, owner, recursive, requireContext())
    }

    companion object {
        fun show(file: FileItem, fragment: Fragment) {
            SetOwnerDialogFragment().putArgs(Args(file)).show(fragment)
        }
    }
}
