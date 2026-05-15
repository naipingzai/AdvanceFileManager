/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.archive

import android.os.Parcelable
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import com.advancefilemanager.util.ParcelableParceler

@Parcelize
internal data class ArchiveFileKey(
    private val archiveFile: @WriteWith<ParcelableParceler> Path,
    private val entryName: String
) : Parcelable
