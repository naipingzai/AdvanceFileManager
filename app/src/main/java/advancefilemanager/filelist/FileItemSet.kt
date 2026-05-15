/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.Path
import com.advancefilemanager.compat.writeParcelableListCompat
import com.advancefilemanager.file.FileItem
import com.advancefilemanager.util.LinkedMapSet
import com.advancefilemanager.util.readParcelableListCompat

class FileItemSet() : LinkedMapSet<Path, FileItem>(FileItem::path), Parcelable {
    constructor(parcel: Parcel) : this() {
        addAll(parcel.readParcelableListCompat())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelableListCompat(toList(), flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FileItemSet> {
        override fun createFromParcel(parcel: Parcel): FileItemSet = FileItemSet(parcel)

        override fun newArray(size: Int): Array<FileItemSet?> = arrayOfNulls(size)
    }
}

fun fileItemSetOf(vararg files: FileItem) = FileItemSet().apply { addAll(files) }
