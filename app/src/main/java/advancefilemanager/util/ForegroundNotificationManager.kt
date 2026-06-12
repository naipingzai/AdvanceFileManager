/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.pm.ServiceInfo
import androidx.core.app.ServiceCompat
import com.advancefilemanager.app.notificationManager
import com.advancefilemanager.compat.stopForegroundCompat

class ForegroundNotificationManager(private val service: Service) {
    private val notifications = mutableMapOf<Int, Notification>()

    private var foregroundId = 0

    private fun startForegroundCompat(id: Int, notification: Notification) {
        ServiceCompat.startForeground(
            service, id, notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    @SuppressLint("MissingPermission")
    fun notify(id: Int, notification: Notification) {
        synchronized(notifications) {
            if (notifications.isEmpty()) {
                startForegroundCompat(id, notification)
                notifications[id] = notification
                foregroundId = id
            } else {
                if (id == foregroundId) {
                    startForegroundCompat(id, notification)
                } else {
                    notificationManager.notify(id, notification)
                }
                notifications[id] = notification
            }
        }
    }

    fun cancel(id: Int) {
        synchronized(notifications) {
            if (id !in notifications) {
                return
            }
            if (id == foregroundId) {
                if (notifications.size == 1) {
                    service.stopForegroundCompat(ServiceCompat.STOP_FOREGROUND_REMOVE)
                    notifications -= id
                    foregroundId = 0
                } else {
                    notifications.entries.find { it.key != id }!!.let {
                        service.startForeground(it.key, it.value)
                        foregroundId = it.key
                    }
                    notificationManager.cancel(id)
                    notifications -= id
                }
            } else {
                notificationManager.cancel(id)
                notifications -= id
            }
        }
    }
}
