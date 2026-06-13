/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.parcelize.Parcelize
import com.advancefilemanager.R
import com.advancefilemanager.databinding.BatchRenameDialogBinding
import com.advancefilemanager.file.FileItem
import com.advancefilemanager.util.ParcelableArgs
import com.advancefilemanager.util.args
import com.advancefilemanager.util.layoutInflater
import com.advancefilemanager.util.putArgs
import com.advancefilemanager.util.show
import com.advancefilemanager.ui.BackgroundOverlayManager

class BatchRenameDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private lateinit var binding: Binding

    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = Binding.inflate(requireContext().layoutInflater)
        
        // Set default name
        if (savedInstanceState == null) {
            binding.nameEdit.setText(R.string.batch_rename_default_name)
            binding.nameEdit.selectAll()
        }
        
        // Add text watcher to update preview
        binding.nameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePreview()
            }
        })
        
        updatePreview()
        
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.batch_rename_title)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .apply {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { onOk() }
                    BackgroundOverlayManager.applyDialogOverlay(requireContext(), this@apply)
                }
            }
    }

    private fun updatePreview() {
        val baseName = binding.nameEdit.text.toString().trim()
        if (baseName.isEmpty()) {
            binding.previewText.text = ""
            return
        }
        
        val files = args.files.toList()
        val count = files.size
        val digitCount = count.toString().length
        
        val previewLines = mutableListOf<String>()
        val maxPreview = minOf(5, count)
        
        for (i in 0 until maxPreview) {
            val file = files[i]
            val extension = file.extension
            val number = (i + 1).toString().padStart(digitCount, '0')
            val newName = if (extension.isNotEmpty()) {
                "$baseName-$number.$extension"
            } else {
                "$baseName-$number"
            }
            previewLines.add("${file.name} → $newName")
        }
        
        if (count > maxPreview) {
            previewLines.add("...")
            // Show last file
            val lastFile = files.last()
            val lastExtension = lastFile.extension
            val lastNumber = count.toString().padStart(digitCount, '0')
            val lastNewName = if (lastExtension.isNotEmpty()) {
                "$baseName-$lastNumber.$lastExtension"
            } else {
                "$baseName-$lastNumber"
            }
            previewLines.add("${lastFile.name} → $lastNewName")
        }
        
        binding.previewText.text = previewLines.joinToString("\n")
    }

    private fun onOk() {
        val baseName = binding.nameEdit.text.toString().trim()
        if (baseName.isEmpty()) {
            binding.nameLayout.error = getString(R.string.file_name_error_empty)
            return
        }
        if (baseName.contains('/') || baseName.contains('\\') || baseName.contains('\u0000')) {
            binding.nameLayout.error = getString(R.string.file_name_error_invalid)
            return
        }
        
        listener.batchRenameFiles(args.files, baseName)
        dismiss()
    }

    companion object {
        fun show(files: FileItemSet, fragment: Fragment) {
            BatchRenameDialogFragment().putArgs(Args(files)).show(fragment)
        }
    }

    @Parcelize
    class Args(val files: FileItemSet) : ParcelableArgs

    private class Binding(
        val root: View,
        val nameLayout: TextInputLayout,
        val nameEdit: EditText,
        val previewText: TextView
    ) {
        companion object {
            fun inflate(inflater: LayoutInflater): Binding {
                val binding = BatchRenameDialogBinding.inflate(inflater)
                return Binding(
                    binding.root,
                    binding.nameLayout,
                    binding.nameEdit,
                    binding.previewText
                )
            }
        }
    }

    interface Listener {
        fun batchRenameFiles(files: FileItemSet, baseName: String)
    }
}
