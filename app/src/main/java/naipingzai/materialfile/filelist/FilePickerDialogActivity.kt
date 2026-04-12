/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.filelist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.commit
import java8.nio.file.Path
import naipingzai.materialfile.R
import naipingzai.materialfile.app.AppActivity
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.util.createIntent
import naipingzai.materialfile.util.extraPathList
import naipingzai.materialfile.util.putArgs

class FilePickerDialogActivity : AppActivity() {
    private lateinit var fragment: FileListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.file_picker_dialog_activity)

        // Size the dialog card to ~90% width and ~75% height
        val card = findViewById<View>(R.id.dialogCard)
        card.post {
            val parent = card.parent as View
            val lp = card.layoutParams as ViewGroup.MarginLayoutParams
            lp.width = (parent.width * 0.90).toInt()
            lp.height = (parent.height * 0.78).toInt()
            card.layoutParams = lp
        }

        // Click outside card to dismiss
        findViewById<View>(android.R.id.content).setOnClickListener { finish() }
        card.setOnClickListener { /* consume click */ }

        if (savedInstanceState == null) {
            fragment = FileListFragment().putArgs(FileListFragment.Args(intent))
            supportFragmentManager.commit {
                add(R.id.fragmentContainer, fragment)
            }
        } else {
            fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                as FileListFragment
        }
    }

    class OpenMultipleFilesContract : ActivityResultContract<List<MimeType>, List<Path>>() {
        override fun createIntent(context: Context, input: List<MimeType>): Intent =
            FilePickerDialogActivity::class.createIntent()
                .setAction(Intent.ACTION_OPEN_DOCUMENT)
                .setType(MimeType.ANY.value)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                .putExtra(Intent.EXTRA_MIME_TYPES, input.map { it.value }.toTypedArray())

        override fun parseResult(resultCode: Int, intent: Intent?): List<Path> =
            if (resultCode == RESULT_OK) intent?.extraPathList ?: emptyList() else emptyList()
    }
}
