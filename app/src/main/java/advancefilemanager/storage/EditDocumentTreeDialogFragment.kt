/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.storage

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import com.advancefilemanager.R
import com.advancefilemanager.databinding.EditDocumentTreeDialogBinding
import com.advancefilemanager.util.ParcelableArgs
import com.advancefilemanager.util.args
import com.advancefilemanager.util.finish
import com.advancefilemanager.util.layoutInflater
import com.advancefilemanager.util.setTextWithSelection
import com.advancefilemanager.ui.applyOverlay

class EditDocumentTreeDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private lateinit var binding: EditDocumentTreeDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.storage_edit_document_tree_title)
            .apply {
                binding = EditDocumentTreeDialogBinding.inflate(context.layoutInflater)
                val documentTree = args.documentTree
                binding.nameLayout.placeholderText = documentTree.getDefaultName(
                    binding.nameLayout.context
                )
                if (savedInstanceState == null) {
                    binding.nameEdit.setTextWithSelection(
                        documentTree.getName(binding.nameEdit.context)
                    )
                }
                binding.uriText.setText(documentTree.uri.value.toString())
                val linuxPath = documentTree.linuxPath
                binding.pathLayout.isVisible = linuxPath != null
                binding.pathText.setText(linuxPath)
                setView(binding.root)
            }
            .setPositiveButton(android.R.string.ok) { _, _ -> save() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            .setNeutralButton(R.string.remove) { _, _ -> remove() }
            .create()
            .applyOverlay(requireContext())
            .apply {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }

    private fun save() {
        val customName = binding.nameEdit.text.toString()
            .takeIf { it.isNotEmpty() && it != binding.nameLayout.placeholderText }
        val documentTree = args.documentTree.copy(customName = customName)
        Storages.replace(documentTree)
        finish()
    }

    private fun remove() {
        Storages.remove(args.documentTree)
        finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        finish()
    }

    @Parcelize
    class Args(val documentTree: DocumentTree) : ParcelableArgs

}
