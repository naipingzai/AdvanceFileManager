/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import com.advancefilemanager.R
import com.advancefilemanager.util.ParcelableArgs
import com.advancefilemanager.util.ParcelableParceler
import com.advancefilemanager.util.args
import com.advancefilemanager.util.putArgs
import com.advancefilemanager.util.show

class NavigateToPathDialogFragment : PathDialogFragment() {
    private val args by args<Args>()

    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_list_navigate_to_title

    override val initialName: String?
        get() = args.path.toUserFriendlyString()

    override fun onOk(path: Path) {
        listener.navigateTo(path)
    }

    companion object {
        fun show(path: Path, fragment: Fragment) {
            NavigateToPathDialogFragment().putArgs(Args(path)).show(fragment)
        }
    }

    @Parcelize
    class Args(val path: @WriteWith<ParcelableParceler> Path) : ParcelableArgs

    interface Listener : NameDialogFragment.Listener {
        fun navigateTo(path: Path)
    }
}
