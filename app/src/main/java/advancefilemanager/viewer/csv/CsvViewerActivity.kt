/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.csv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.advancefilemanager.app.AppActivity
import com.advancefilemanager.util.putArgs

class CsvViewerActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            val fragment = CsvViewerFragment().putArgs(CsvViewerFragment.Args(intent))
            supportFragmentManager.commit { add(android.R.id.content, fragment) }
        }
    }
}
