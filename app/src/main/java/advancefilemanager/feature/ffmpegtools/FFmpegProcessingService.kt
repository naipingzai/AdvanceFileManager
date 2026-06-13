package com.advancefilemanager.feature.ffmpegtools

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.advancefilemanager.R

class FFmpegProcessingService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): FFmpegProcessingService = this@FFmpegProcessingService
    }

    private val binder = LocalBinder()

    private var processingThread: Thread? = null
    private var isProcessing = false
    private var lastPercent = 0

    companion object {
        private const val CHANNEL_ID = "ffmpeg_processing_v2"
        private const val NOTIFICATION_ID = 9001

        const val EXTRA_INPUT_PATH = "input_path"
        const val EXTRA_OUTPUT_PATH = "output_path"
        const val EXTRA_ACTION_TYPE = "action_type"
        const val EXTRA_START_MS = "start_ms"
        const val EXTRA_END_MS = "end_ms"
        const val EXTRA_WIDTH = "width"
        const val EXTRA_FPS = "fps"

        var isRunning = false
            private set

        fun startProcessing(
            context: android.content.Context,
            inputPath: String,
            outputPath: String,
            actionType: String,
            startMs: Long = 0,
            endMs: Long = 0,
            width: Int = 0,
            fps: Int = 10
        ) {
            isRunning = true
            val intent = Intent(context, FFmpegProcessingService::class.java).apply {
                putExtra(EXTRA_INPUT_PATH, inputPath)
                putExtra(EXTRA_OUTPUT_PATH, outputPath)
                putExtra(EXTRA_ACTION_TYPE, actionType)
                putExtra(EXTRA_START_MS, startMs)
                putExtra(EXTRA_END_MS, endMs)
                putExtra(EXTRA_WIDTH, width)
                putExtra(EXTRA_FPS, fps)
            }
            context.startService(intent)
        }

        fun stopProcessing(context: android.content.Context) {
            FFmpegJni.cancel()
            isRunning = false
            sendCompleteBroadcast(context, false, context.getString(R.string.cancel))
            try {
                context.stopService(Intent(context, FFmpegProcessingService::class.java))
            } catch (_: Exception) {}
        }

        private fun buildNotification(text: String): Notification {
            return NotificationCompat.Builder(com.advancefilemanager.app.application, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_image)
                .setContentTitle(com.advancefilemanager.app.application.getString(R.string.ffmpeg_processing_notification_title))
                .setContentText(text)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()
        }

        private fun sendProgressBroadcast(context: android.content.Context, percent: Int) {
            val intent = Intent(FFmpegFeatureFragment.ACTION_PROGRESS_UPDATE).apply {
                putExtra(FFmpegFeatureFragment.EXTRA_PERCENT, percent)
                setPackage(context.packageName)
            }
            context.sendBroadcast(intent)
        }

        private fun sendCompleteBroadcast(context: android.content.Context, success: Boolean, error: String = "") {
            val intent = Intent(FFmpegFeatureFragment.ACTION_PROCESSING_COMPLETE).apply {
                putExtra(FFmpegFeatureFragment.EXTRA_SUCCESS, success)
                putExtra(FFmpegFeatureFragment.EXTRA_ERROR, error)
                setPackage(context.packageName)
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        // Create notification channel fresh with localized text
        val name = getString(R.string.notification_channel_ffmpeg_processing_name)
        val desc = getString(R.string.notification_channel_ffmpeg_processing_description)
        val channel = android.app.NotificationChannel(CHANNEL_ID, name, android.app.NotificationManager.IMPORTANCE_LOW)
            .apply { description = desc }
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.createNotificationChannel(channel)

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(getString(R.string.ffmpeg_processing_notification_title) + " 0%"),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val inputPath = intent.getStringExtra(EXTRA_INPUT_PATH) ?: run {
            stopSelf(); return START_NOT_STICKY
        }
        val outputPath = intent.getStringExtra(EXTRA_OUTPUT_PATH) ?: run {
            stopSelf(); return START_NOT_STICKY
        }
        val actionType = intent.getStringExtra(EXTRA_ACTION_TYPE) ?: run {
            stopSelf(); return START_NOT_STICKY
        }
        val startMs = intent.getLongExtra(EXTRA_START_MS, 0)
        val endMs = intent.getLongExtra(EXTRA_END_MS, 0)
        val width = intent.getIntExtra(EXTRA_WIDTH, 0)
        val fps = intent.getIntExtra(EXTRA_FPS, 10)

        isProcessing = true
        isRunning = true

        processingThread = Thread {
            val callback = object : FFmpegJni.ProgressCallback {
                override fun onProgress(percent: Int) {
                    if (percent != lastPercent) {
                        lastPercent = percent
                        updateNotification(percent)
                        sendProgressBroadcast(this@FFmpegProcessingService, percent)
                    }
                }
            }

            val result = when (actionType) {
                "gif_maker" -> FFmpegJni.gifMake(inputPath, outputPath, startMs, endMs, width, fps, callback)
                "media_trim" -> FFmpegJni.trim(inputPath, outputPath, startMs, endMs, callback)
                "video_snapshot" -> FFmpegJni.videoSnapshot(inputPath, outputPath, startMs)
                "extract_audio" -> FFmpegJni.extractAudio(inputPath, outputPath, callback)
                "video_compress" -> FFmpegJni.videoCompress(inputPath, outputPath, 1000, 0, 0, 0, callback)
                "format_convert" -> FFmpegJni.convert(inputPath, outputPath, callback)
                "video_enhance" -> FFmpegJni.videoEnhance(inputPath, outputPath, 1.5f, 0, callback)
                "image_enhance" -> FFmpegJni.imageEnhance(inputPath, outputPath, 1.5f)
                else -> FFmpegJni.convert(inputPath, outputPath, callback)
            }

            isProcessing = false
            isRunning = false

            val success = (result == 0)
            val title = if (success) {
                getString(R.string.ffmpeg_processing_notification_done)
            } else {
                FFmpegJni.getLastError().ifEmpty { getString(R.string.failed) }
            }

            updateNotificationComplete(title)
            sendCompleteBroadcast(this@FFmpegProcessingService, success, title)

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                stopForegroundCompat(ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }, 1500)
        }.apply { start() }

        return START_NOT_STICKY
    }

    private fun updateNotification(percent: Int) {
        val text = getString(R.string.ffmpeg_processing_notification_progress, percent)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_image)
            .setContentTitle(getString(R.string.ffmpeg_processing_notification_title))
            .setContentText(text)
            .setProgress(100, percent, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun updateNotificationComplete(text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_image)
            .setContentTitle(text)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isProcessing) {
            FFmpegJni.cancel()
        }
        isRunning = false
        isProcessing = false
    }

    private fun stopForegroundCompat(remove: Int) {
        ServiceCompat.stopForeground(this, remove)
    }
}