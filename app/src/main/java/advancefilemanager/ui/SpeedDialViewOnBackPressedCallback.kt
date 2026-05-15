/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.ui

import androidx.activity.OnBackPressedCallback
import com.leinardi.android.speeddial.SpeedDialView

class SpeedDialViewOnBackPressedCallback(
    private val speedDialView: SpeedDialView
) : OnBackPressedCallback(speedDialView.isOpen) {
    init {
        speedDialView.setOnChangeListener(
            object : SpeedDialView.OnChangeListener {
                override fun onMainActionSelected(): Boolean = false

                override fun onToggleChanged(isOpen: Boolean) {
                    isEnabled = speedDialView.isOpen
                }
            }
        )
    }

    override fun handleOnBackPressed() {
        speedDialView.close()
    }
}
