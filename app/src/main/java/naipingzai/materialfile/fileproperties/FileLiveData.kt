/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.fileproperties

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java8.nio.file.Path
import naipingzai.materialfile.file.FileItem
import naipingzai.materialfile.file.loadFileItem
import naipingzai.materialfile.util.Failure
import naipingzai.materialfile.util.Loading
import naipingzai.materialfile.util.Stateful
import naipingzai.materialfile.util.Success
import naipingzai.materialfile.util.valueCompat

class FileLiveData private constructor(
    path: Path,
    file: FileItem?
) : PathObserverLiveData<Stateful<FileItem>>(path) {
    constructor(path: Path) : this(path, null)

    constructor(file: FileItem) : this(file.path, file)

    init {
        if (file != null) {
            value = Success(file)
        } else {
            loadValue()
        }
        observe()
    }

    override fun loadValue() {
        value = Loading(value?.value)
        Dispatchers.IO.asExecutor().execute {
            val value = try {
                val file = path.loadFileItem()
                Success(file)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }
}
