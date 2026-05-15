/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.navigation

import android.os.Parcelable
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import com.advancefilemanager.filelist.name
import com.advancefilemanager.util.ParcelableParceler
import com.advancefilemanager.util.takeIfNotEmpty
import kotlin.ConsistentCopyVisibility
import java.util.Random

@Parcelize
@ConsistentCopyVisibility
// @see https://youtrack.jetbrains.com/issue/KT-24842
// @Parcelize throws IllegalAccessError if the primary constructor is private.
data class BookmarkDirectory internal constructor(
    val id: Long,
    val customName: String?,
    val path: @WriteWith<ParcelableParceler> Path
) : Parcelable {
    // We cannot simply use path.hashCode() as ID because different bookmark directories may have
    // the same path.
    constructor(customName: String?, path: Path) : this(Random().nextLong(), customName, path)

    val defaultName: String
        get() = path.name

    val name: String
        get() = customName?.takeIfNotEmpty() ?: defaultName
}
