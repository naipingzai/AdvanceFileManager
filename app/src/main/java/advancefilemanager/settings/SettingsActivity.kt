/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.add
import androidx.fragment.app.commit
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import com.advancefilemanager.app.AppActivity
import com.advancefilemanager.util.BundleParceler
import com.advancefilemanager.util.ParcelableArgs
import com.advancefilemanager.util.createIntent
import com.advancefilemanager.util.getArgsOrNull
import com.advancefilemanager.util.putArgs
import com.advancefilemanager.util.startActivitySafe

class SettingsActivity : AppActivity() {
    private var isRestarting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val args = intent.extras?.getArgsOrNull<Args>()
        val savedInstanceState = savedInstanceState ?: args?.savedInstanceState
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            supportFragmentManager.commit { add<SettingsFragment>(android.R.id.content) }
        }
    }

    fun setApplicationLocalesPre33(locales: LocaleListCompat) {
        // HACK: Prevent this activity from being recreated due to locale change.
        delegate.onDestroy()
        AppCompatDelegate.setApplicationLocales(locales)
        restart()
    }

    private fun restart() {
        val savedInstanceState = Bundle().apply {
            onSaveInstanceState(this)
        }
        finish()
        try {
            val intent = SettingsActivity::class.createIntent().putArgs(Args(savedInstanceState))
            startActivitySafe(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            isRestarting = true
        } catch (e: Exception) {
            // If restart fails, reset isRestarting to allow normal interaction
            isRestarting = false
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return isRestarting || super.dispatchKeyEvent(event)
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyShortcutEvent(event: KeyEvent): Boolean {
        return isRestarting || super.dispatchKeyShortcutEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return isRestarting || super.dispatchTouchEvent(event)
    }

    override fun dispatchTrackballEvent(event: MotionEvent): Boolean {
        return isRestarting || super.dispatchTrackballEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        return isRestarting || super.dispatchGenericMotionEvent(event)
    }

    @Parcelize
    class Args(val savedInstanceState: @WriteWith<BundleParceler> Bundle?) : ParcelableArgs
}
