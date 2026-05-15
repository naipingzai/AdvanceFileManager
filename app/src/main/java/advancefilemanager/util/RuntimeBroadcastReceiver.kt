/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.advancefilemanager.compat.registerReceiverCompat

class RuntimeBroadcastReceiver(
    private val filter: IntentFilter,
    private val receiver: BroadcastReceiver,
    private val context: Context,
    private val flags: Int = ContextCompat.RECEIVER_NOT_EXPORTED
) {
    fun register() {
        context.registerReceiverCompat(receiver, filter, flags)
    }

    fun unregister() {
        context.unregisterReceiver(receiver)
    }
}
