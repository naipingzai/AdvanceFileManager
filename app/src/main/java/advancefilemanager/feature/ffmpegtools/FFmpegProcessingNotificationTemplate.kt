package com.advancefilemanager.feature.ffmpegtools

import com.advancefilemanager.R
import com.advancefilemanager.util.NotificationChannelTemplate
import com.advancefilemanager.util.NotificationTemplate

val ffmpegProcessingNotificationTemplate = NotificationTemplate(
    NotificationChannelTemplate(
        "ffmpeg_processing_v2",
        R.string.notification_channel_ffmpeg_processing_name,
        android.app.NotificationManager.IMPORTANCE_LOW,
        descriptionRes = R.string.notification_channel_ffmpeg_processing_description,
        showBadge = false
    )
)