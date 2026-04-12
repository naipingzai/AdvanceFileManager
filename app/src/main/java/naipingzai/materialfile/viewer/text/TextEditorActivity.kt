/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.viewer.text

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naipingzai.materialfile.app.AppActivity
import naipingzai.materialfile.provider.common.size
import naipingzai.materialfile.util.extraPath
import naipingzai.materialfile.util.putArgs

class TextEditorActivity : AppActivity() {
    private var editorFragment: TextEditorFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                val path = intent.extraPath
                val fileSize = if (path != null) {
                    withContext(Dispatchers.IO) {
                        try { path.size() } catch (_: Exception) { 0L }
                    }
                } else 0L

                if (isFinishing || isDestroyed) return@launch

                if (fileSize > LARGE_FILE_THRESHOLD) {
                    val fragment = LargeTextViewerFragment()
                        .putArgs(LargeTextViewerFragment.Args(intent))
                    supportFragmentManager.commit {
                        add(android.R.id.content, fragment)
                    }
                } else {
                    val fragment = TextEditorFragment()
                        .putArgs(TextEditorFragment.Args(intent))
                    editorFragment = fragment
                    supportFragmentManager.commit {
                        add(android.R.id.content, fragment)
                    }
                }
            }
        } else {
            val f = supportFragmentManager.findFragmentById(android.R.id.content)
            if (f is TextEditorFragment) {
                editorFragment = f
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (editorFragment?.onSupportNavigateUp() == true) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    companion object {
        // Files larger than 1MB use the read-only chunked viewer
        private const val LARGE_FILE_THRESHOLD = 1L * 1024 * 1024
    }
}
