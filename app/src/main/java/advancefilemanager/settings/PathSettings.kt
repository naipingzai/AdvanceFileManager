/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import androidx.core.content.res.ResourcesCompat
import java8.nio.file.Path
import com.advancefilemanager.R
import com.advancefilemanager.filelist.FileSortOptions
import com.advancefilemanager.filelist.FileViewType

object PathSettings {
    private const val NAME_SUFFIX = "path"

    @Suppress("UNCHECKED_CAST")
    fun getFileListViewType(path: Path): SettingLiveData<FileViewType?> =
        EnumSettingLiveData(
            NAME_SUFFIX, R.string.pref_key_file_list_view_type, path.toString(),
            ResourcesCompat.ID_NULL, FileViewType::class.java
        ) as SettingLiveData<FileViewType?>

    fun getFileListSortOptions(path: Path): SettingLiveData<FileSortOptions?> =
        ParcelValueSettingLiveData(
            NAME_SUFFIX, R.string.pref_key_file_list_sort_options, path.toString(), null
        )
}
