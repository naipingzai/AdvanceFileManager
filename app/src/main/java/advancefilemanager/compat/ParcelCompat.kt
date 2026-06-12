/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.os.Parcelable
import androidx.core.os.ParcelCompat

fun android.os.Parcel.readBooleanCompat(): Boolean = ParcelCompat.readBoolean(this)

fun android.os.Parcel.writeBooleanCompat(value: Boolean) {
    ParcelCompat.writeBoolean(this, value)
}

fun <E : Parcelable?, L : MutableList<E>> android.os.Parcel.readParcelableListCompat(
    list: L,
    classLoader: ClassLoader?
): L {
    @Suppress("UNCHECKED_CAST")
    return readParcelableList(list, classLoader) as L
}

fun <T : Parcelable?> android.os.Parcel.writeParcelableListCompat(value: List<T>?, flags: Int) {
    writeParcelableList(value, flags)
}

@Suppress("UNCHECKED_CAST")
fun <T> android.os.Parcel.readSerializableCompat(): T? = readSerializable() as T?
