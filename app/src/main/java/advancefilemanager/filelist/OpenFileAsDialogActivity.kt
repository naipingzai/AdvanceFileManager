/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.advancefilemanager.app.AppActivity
import com.advancefilemanager.util.args
import com.advancefilemanager.util.putArgs

class OpenFileAsDialogActivity : AppActivity() {
    private val args by args<OpenFileAsDialogFragment.Args>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            val fragment = OpenFileAsDialogFragment().putArgs(args)
            supportFragmentManager.commit {
                add(fragment, OpenFileAsDialogFragment::class.java.name)
            }
        }
    }
}
