/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.pdf

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.advancefilemanager.app.AppActivity
import com.advancefilemanager.util.putArgs

class PdfViewerActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            val fragment = PdfViewerFragment().putArgs(PdfViewerFragment.Args(intent))
            supportFragmentManager.commit { add(android.R.id.content, fragment) }
        }
    }
}
