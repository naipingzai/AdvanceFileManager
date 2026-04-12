/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.filelist

import android.content.Intent
import android.os.Bundle
import java8.nio.file.Path
import naipingzai.materialfile.app.AppActivity
import naipingzai.materialfile.app.application
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.file.asMimeTypeOrNull
import naipingzai.materialfile.file.fileProviderUri
import naipingzai.materialfile.filejob.FileJobService
import naipingzai.materialfile.provider.archive.isArchivePath
import naipingzai.materialfile.util.createViewIntent
import naipingzai.materialfile.util.extraPath
import naipingzai.materialfile.util.startActivitySafe

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
        private const val ACTION_OPEN_FILE = "naipingzai.materialfile.intent.action.OPEN_FILE"

        fun createIntent(path: Path, mimeType: MimeType): Intent =
            Intent(ACTION_OPEN_FILE)
                .setPackage(application.packageName)
                .setType(mimeType.value)
                .apply { extraPath = path }
    }
}
