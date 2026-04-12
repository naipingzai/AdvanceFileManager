/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.filelist

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import naipingzai.materialfile.R
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.file.asMimeType
import naipingzai.materialfile.file.fileProviderUri
import naipingzai.materialfile.util.ParcelableArgs
import naipingzai.materialfile.util.ParcelableParceler
import naipingzai.materialfile.util.args
import naipingzai.materialfile.util.createViewIntent
import naipingzai.materialfile.util.extraPath
import naipingzai.materialfile.util.finish
import naipingzai.materialfile.util.putArgs
import naipingzai.materialfile.util.startActivitySafe
import naipingzai.materialfile.util.withChooser
import naipingzai.materialfile.viewer.hex.HexViewerActivity
import naipingzai.materialfile.viewer.hex.HexViewerFragment

class OpenFileAsDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(getString(R.string.file_open_as_title_format, args.path.name))
            .apply {
                val allItems = FILE_TYPES.map { getString(it.first) } +
                    getString(R.string.file_open_as_type_hex)
                setItems(allItems.toTypedArray<CharSequence>()) { _, which ->
                    if (which < FILE_TYPES.size) {
                        openAs(FILE_TYPES[which].second)
                    } else {
                        openAsHex()
                    }
                }
            }
            .create()

    private fun openAs(mimeType: MimeType) {
        val intent = args.path.fileProviderUri.createViewIntent(mimeType)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            .apply { extraPath = args.path }
            .withChooser()
        startActivitySafe(intent)
        finish()
    }

    private fun openAsHex() {
        val intent = Intent(requireContext(), HexViewerActivity::class.java).apply {
            extraPath = args.path
        }
        startActivitySafe(intent)
        finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        finish()
    }

    companion object {
        private val FILE_TYPES = listOf(
            R.string.file_open_as_type_text to "text/plain",
            R.string.file_open_as_type_image to "image/*",
            R.string.file_open_as_type_audio to "audio/*",
            R.string.file_open_as_type_video to "video/*",
            R.string.file_open_as_type_directory to MimeType.DIRECTORY.value,
            R.string.file_open_as_type_any to "*/*"
        ).map { it.first to it.second.asMimeType() }
    }

    @Parcelize
    class Args(val path: @WriteWith<ParcelableParceler> Path) : ParcelableArgs
}
