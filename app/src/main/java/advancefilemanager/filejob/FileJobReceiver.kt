/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filejob

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.advancefilemanager.app.application

class FileJobReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (val action = intent.action) {
            ACTION_CANCEL -> {
                val jobId = intent.getIntExtra(EXTRA_JOB_ID, 0)
                FileJobService.cancelJob(jobId)
            }
            else -> {
                // Unknown action, ignore silently
            }
        }
    }

    companion object {
        private const val ACTION_CANCEL = "cancel"

        private const val EXTRA_JOB_ID = "jobId"

        fun createIntent(jobId: Int): Intent =
            Intent(application, FileJobReceiver::class.java)
                .setAction(ACTION_CANCEL)
                .putExtra(EXTRA_JOB_ID, jobId)
    }
}
