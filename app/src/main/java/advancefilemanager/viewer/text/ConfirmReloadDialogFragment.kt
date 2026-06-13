/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.text

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.advancefilemanager.R
import com.advancefilemanager.util.show
import com.advancefilemanager.ui.applyOverlay

class ConfirmReloadDialogFragment : AppCompatDialogFragment() {
    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.text_editor_reload_message)
            .setPositiveButton(R.string.keep_editing, null)
            .setNegativeButton(R.string.reload) { _, _ -> listener.reload() }
            .create()
            .applyOverlay(requireContext())
    }

    companion object {
        fun show(fragment: Fragment) {
            ConfirmReloadDialogFragment().show(fragment)
        }
    }

    interface Listener {
        fun reload()
    }

}
