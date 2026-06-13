/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.audio

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import com.advancefilemanager.R
import com.advancefilemanager.util.ParcelableArgs
import com.advancefilemanager.util.ParcelableParceler
import com.advancefilemanager.util.args
import com.advancefilemanager.util.putArgs
import com.advancefilemanager.util.show
import com.advancefilemanager.ui.applyOverlay

class ConfirmDeleteAudioDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setMessage(getString(R.string.audio_player_delete_message_format, args.path.fileName))
            .setPositiveButton(android.R.string.ok) { _, _ -> listener.delete(args.path) }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .applyOverlay(requireContext())
    }

    companion object {
        fun show(path: Path, fragment: Fragment) {
            ConfirmDeleteAudioDialogFragment().putArgs(Args(path)).show(fragment)
        }
    }

    @Parcelize
    class Args(val path: @WriteWith<ParcelableParceler> Path) : ParcelableArgs

    interface Listener {
        fun delete(path: Path)
    }

}
