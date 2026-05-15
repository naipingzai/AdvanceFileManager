/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import android.content.Intent
import android.os.Bundle
import java8.nio.file.Path
import com.advancefilemanager.app.AppActivity
import com.advancefilemanager.app.application
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.file.asMimeTypeOrNull
import com.advancefilemanager.file.fileProviderUri
import com.advancefilemanager.filejob.FileJobService
import com.advancefilemanager.provider.archive.isArchivePath
import com.advancefilemanager.util.createViewIntent
import com.advancefilemanager.util.extraPath
import com.advancefilemanager.util.startActivitySafe

class OpenFileActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val path = intent.extraPath
        val mimeType = intent.type?.asMimeTypeOrNull()
        if (path != null && mimeType != null) {
            openFile(path, mimeType)
        }
        finish()
    }

    private fun openFile(path: Path, mimeType: MimeType) {
        if (path.isArchivePath) {
            FileJobService.open(path, mimeType, false, this)
        } else {
            val intent = path.fileProviderUri.createViewIntent(mimeType)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .apply { extraPath = path }
            startActivitySafe(intent)
        }
    }

    companion object {
        private const val ACTION_OPEN_FILE = "com.advancefilemanager.intent.action.OPEN_FILE"

        fun createIntent(path: Path, mimeType: MimeType): Intent =
            Intent(ACTION_OPEN_FILE)
                .setPackage(application.packageName)
                .setType(mimeType.value)
                .apply { extraPath = path }
    }
}
