/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import android.os.Parcel
import java.time.Instant
import java8.nio.file.attribute.FileTime
import kotlinx.parcelize.Parceler
import com.advancefilemanager.compat.readSerializableCompat

object FileTimeParceler : Parceler<FileTime?> {
    override fun create(parcel: Parcel): FileTime? =
        parcel.readSerializableCompat<Instant>()?.let { FileTime.from(it) }

    override fun FileTime?.write(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(this?.toInstant())
    }
}
