/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.advancefilemanager.R
import com.advancefilemanager.util.show

class CreateFileDialogFragment : FileNameDialogFragment() {
    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_create_file_title

    override fun onOk(name: String) {
        listener.createFile(name)
    }

    companion object {
        fun show(fragment: Fragment) {
            CreateFileDialogFragment().show(fragment)
        }
    }

    interface Listener : FileNameDialogFragment.Listener {
        fun createFile(name: String)
    }
}
