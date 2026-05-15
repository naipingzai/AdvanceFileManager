/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.apk

import androidx.lifecycle.ViewModel

class PermissionListViewModel(permissionNames: Array<String>) : ViewModel() {
    val permissionListLiveData = PermissionListLiveData(permissionNames)
}
