/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.provider.common

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.attribute.FileTime
import naipingzai.materialfile.compat.readSerializableCompat

class ParcelableFileTime(val value: FileTime) : Parcelable {
    private constructor(source: Parcel) : this(FileTime.from(source.readSerializableCompat()))

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeSerializable(value.toInstant())
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableFileTime> {
            override fun createFromParcel(source: Parcel): ParcelableFileTime =
                ParcelableFileTime(source)

            override fun newArray(size: Int): Array<ParcelableFileTime?> = arrayOfNulls(size)
        }
    }
}

fun FileTime.toParcelable(): ParcelableFileTime = ParcelableFileTime(this)
