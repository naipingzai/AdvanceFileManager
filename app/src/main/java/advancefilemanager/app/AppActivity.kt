/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.advancefilemanager.settings.UiSettingsManager
import com.advancefilemanager.ui.BackgroundOverlayManager

abstract class AppActivity : AppCompatActivity() {

    private val uiSettingsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            recreate()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val config = UiSettingsManager.createConfiguration(newBase)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter("com.advancefilemanager.UI_SETTINGS_CHANGED")
        registerReceiver(uiSettingsReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(uiSettingsReceiver)
        } catch (_: Exception) {}
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!super.onSupportNavigateUp()) {
            finish()
        }
        return true
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        BackgroundOverlayManager.showDimOverlay(this)
        return super.onMenuOpened(featureId, menu)
    }

    override fun onPanelClosed(featureId: Int, menu: Menu) {
        BackgroundOverlayManager.hideOverlay(this)
        super.onPanelClosed(featureId, menu)
    }
}
