/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import com.advancefilemanager.R
import com.advancefilemanager.settings.UiSettingsManager
import com.advancefilemanager.util.ParcelableArgs
import com.advancefilemanager.util.args
import com.advancefilemanager.util.getQuantityString
import com.advancefilemanager.util.putArgs
import com.advancefilemanager.util.show
import com.advancefilemanager.ui.BackgroundOverlayManager
import com.advancefilemanager.ui.applyOverlay

class ConfirmDeleteFilesDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val files = args.files
        val context = requireContext()
        val message = if (files.size == 1) {
            val file = files.single()
            val messageRes = if (file.attributesNoFollowLinks.isDirectory) {
                R.string.file_delete_message_directory_format
            } else {
                R.string.file_delete_message_file_format
            }
            getString(messageRes, file.name)
        } else {
            val allDirectories = files.all { it.attributesNoFollowLinks.isDirectory }
            val allFiles = files.none { it.attributesNoFollowLinks.isDirectory }
            val messageRes = when {
                allDirectories -> R.plurals.file_delete_message_multiple_directories_format
                allFiles -> R.plurals.file_delete_message_multiple_files_format
                else -> R.plurals.file_delete_message_multiple_mixed_format
            }
            getQuantityString(messageRes, files.size, files.size)
        }
        val dialog = MaterialAlertDialogBuilder(context)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> listener.deleteFiles(files) }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .applyOverlay(context)

        // Apply UI settings for consistent font size and spacing
        dialog.setOnShowListener {
            val paddingScale = UiSettingsManager.getDialogPaddingScale(context)
            dialog.window?.let { window ->
                // Apply dim amount based on blur settings
                val dimAmount = UiSettingsManager.getBlurIntensity(context)
                if (dimAmount > 0f) {
                    val params = window.attributes
                    params.dimAmount = 0.2f + dimAmount * 0.6f
                    window.attributes = params
                }
            }
        }
        return dialog
    }

    companion object {
        fun show(files: FileItemSet, fragment: Fragment) {
            ConfirmDeleteFilesDialogFragment().putArgs(Args(files)).show(fragment)
        }
    }

    @Parcelize
    class Args(val files: FileItemSet) : ParcelableArgs

    interface Listener {
        fun deleteFiles(files: FileItemSet)
    }
}
