/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileaction

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.advancefilemanager.app.AppActivity
import com.advancefilemanager.util.args
import com.advancefilemanager.util.putArgs

class ArchivePasswordDialogActivity : AppActivity() {
    private val args by args<ArchivePasswordDialogFragment.Args>()

    private lateinit var fragment: ArchivePasswordDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            fragment = ArchivePasswordDialogFragment().putArgs(args)
            supportFragmentManager.commit {
                add(fragment, ArchivePasswordDialogFragment::class.java.name)
            }
        } else {
            fragment = supportFragmentManager.findFragmentByTag(
                ArchivePasswordDialogFragment::class.java.name
            ) as ArchivePasswordDialogFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isFinishing) {
            fragment.onFinish()
        }
    }
}
