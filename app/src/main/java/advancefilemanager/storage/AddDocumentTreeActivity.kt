/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.storage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.advancefilemanager.app.AppActivity

class AddDocumentTreeActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            supportFragmentManager.commit { add(android.R.id.content, AddDocumentTreeFragment()) }
        }
    }
}
