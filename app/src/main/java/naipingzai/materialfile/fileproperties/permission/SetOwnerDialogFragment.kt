/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.fileproperties.permission

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import java8.nio.file.Path
import naipingzai.materialfile.R
import naipingzai.materialfile.file.FileItem
import naipingzai.materialfile.filejob.FileJobService
import naipingzai.materialfile.provider.common.PosixFileAttributes
import naipingzai.materialfile.provider.common.PosixPrincipal
import naipingzai.materialfile.provider.common.PosixUser
import naipingzai.materialfile.provider.common.toByteString
import naipingzai.materialfile.util.SelectionLiveData
import naipingzai.materialfile.util.putArgs
import naipingzai.materialfile.util.show
import naipingzai.materialfile.util.viewModels

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
