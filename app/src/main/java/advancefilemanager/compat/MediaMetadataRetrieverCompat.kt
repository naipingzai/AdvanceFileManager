/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

val KClass<MediaMetadataRetriever>.METADATA_KEY_SAMPLERATE: Int
    get() = 38

fun MediaMetadataRetriever.getFrameAtTimeCompat(
    timeUs: Long,
    option: Int,
    params: MediaMetadataRetriever.BitmapParams?
): Bitmap? =
    if (params != null) {
        getFrameAtTime(timeUs, option, params)
    } else {
        getFrameAtTime(timeUs, option)
    }

fun MediaMetadataRetriever.getScaledFrameAtTimeCompat(
    timeUs: Long,
    option: Int,
    dstWidth: Int,
    dstHeight: Int,
    params: MediaMetadataRetriever.BitmapParams?
): Bitmap? =
    if (params != null) {
        getScaledFrameAtTime(timeUs, option, dstWidth, dstHeight, params)
    } else {
        getScaledFrameAtTime(timeUs, option, dstWidth, dstHeight)
    }

@OptIn(ExperimentalContracts::class)
inline fun <R> MediaMetadataRetriever.use(block: (MediaMetadataRetriever) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return (this as AutoCloseable).use { block(this) }
}
