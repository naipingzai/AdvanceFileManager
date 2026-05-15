/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.permission

import androidx.annotation.DrawableRes
import com.advancefilemanager.R
import com.advancefilemanager.util.SelectionLiveData

class UserListAdapter(
    selectionLiveData: SelectionLiveData<Int>
) : PrincipalListAdapter(selectionLiveData) {
    @DrawableRes
    override val principalIconRes: Int = R.drawable.person_icon_control_normal_24dp
}
