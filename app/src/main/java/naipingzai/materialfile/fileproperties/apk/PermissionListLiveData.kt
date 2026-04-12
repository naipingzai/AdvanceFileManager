/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.fileproperties.apk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import androidx.lifecycle.MutableLiveData
import naipingzai.materialfile.app.packageManager
import naipingzai.materialfile.util.Failure
import naipingzai.materialfile.util.Loading
import naipingzai.materialfile.util.Stateful
import naipingzai.materialfile.util.Success
import naipingzai.materialfile.util.getPermissionInfoOrNull
import naipingzai.materialfile.util.valueCompat

class PermissionListLiveData(
    private val permissionNames: Array<String>
) : MutableLiveData<Stateful<List<PermissionItem>>>() {
    init {
        loadValue()
    }

    private fun loadValue() {
        value = Loading(value?.value)
        Dispatchers.IO.asExecutor().execute {
            val value = try {
                val permissions = permissionNames.map { name ->
                    val packageManager = packageManager
                    val permissionInfo = packageManager.getPermissionInfoOrNull(name, 0)
                    val label = permissionInfo?.loadLabel(packageManager)?.toString()
                        .takeIf { it != name }
                    val description = permissionInfo?.loadDescription(packageManager)?.toString()
                    PermissionItem(name, permissionInfo, label, description)
                }
                Success(permissions)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }
}
