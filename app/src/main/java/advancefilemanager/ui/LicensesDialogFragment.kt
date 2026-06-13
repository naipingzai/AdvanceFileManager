/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.NoticesXmlParser
import de.psdev.licensesdialog.model.Notices
import kotlinx.parcelize.Parcelize
import com.advancefilemanager.R
import com.advancefilemanager.util.ParcelableState
import com.advancefilemanager.util.getState
import com.advancefilemanager.util.putState
import com.advancefilemanager.util.show
import com.advancefilemanager.ui.applyOverlay

class LicensesDialogFragment : AppCompatDialogFragment() {
    private lateinit var notices: Notices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notices = if (savedInstanceState != null) {
            savedInstanceState.getState<State>().notices
        } else {
            NoticesXmlParser.parse(resources.openRawResource(R.raw.licenses))
                .apply { addNotice(LicensesDialog.LICENSES_DIALOG_NOTICE) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putState(State(notices))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.about_licenses_title)
            .apply { setLicensesView(notices) }
            .setNegativeButton(R.string.close, null)
            .create()
            .applyOverlay(requireContext())

    companion object {
        fun show(fragment: Fragment) {
            LicensesDialogFragment().show(fragment)
        }
    }

    @Parcelize
    private class State(val notices: Notices) : ParcelableState

}
