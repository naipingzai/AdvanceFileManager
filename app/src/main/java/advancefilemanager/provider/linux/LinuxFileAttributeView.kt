/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.linux

import android.os.Parcel
import android.os.Parcelable
import com.advancefilemanager.compat.readBooleanCompat
import com.advancefilemanager.compat.writeBooleanCompat
import com.advancefilemanager.provider.root.RootPosixFileAttributeView
import com.advancefilemanager.provider.root.RootablePosixFileAttributeView
import com.advancefilemanager.util.readParcelable

internal class LinuxFileAttributeView constructor(
    private val path: LinuxPath,
    private val noFollowLinks: Boolean
) : RootablePosixFileAttributeView(
    path, LocalLinuxFileAttributeView(path.toByteString(), noFollowLinks),
    { RootPosixFileAttributeView(it) }
) {
    private constructor(source: Parcel) : this(
        source.readParcelable()!!, source.readBooleanCompat()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(path, flags)
        dest.writeBooleanCompat(noFollowLinks)
    }

    companion object {
        val SUPPORTED_NAMES = LocalLinuxFileAttributeView.SUPPORTED_NAMES

        @JvmField
        val CREATOR = object : Parcelable.Creator<LinuxFileAttributeView> {
            override fun createFromParcel(source: Parcel): LinuxFileAttributeView =
                LinuxFileAttributeView(source)

            override fun newArray(size: Int): Array<LinuxFileAttributeView?> = arrayOfNulls(size)
        }
    }
}
