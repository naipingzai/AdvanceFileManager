/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.video

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import java8.nio.file.Path
import com.advancefilemanager.app.AppActivity
import com.advancefilemanager.util.extraPath
import com.advancefilemanager.util.extraPathList
import com.advancefilemanager.util.putArgs

class VideoViewerActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            val intent = intent
            val position = intent.getIntExtra(EXTRA_POSITION, 0)
            val fragment = VideoViewerFragment()
                .putArgs(VideoViewerFragment.Args(intent, position))
            supportFragmentManager.commit { add(android.R.id.content, fragment) }
        }
    }

    companion object {
        private val EXTRA_POSITION = "${VideoViewerActivity::class.java.name}.extra.POSITION"

        fun putExtras(intent: Intent, paths: List<Path>, position: Int) {
            intent.extraPathList = paths
            intent.putExtra(EXTRA_POSITION, position)
        }
    }
}
