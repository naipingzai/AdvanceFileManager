/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.storage

import android.content.Context
import android.content.Intent
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import naipingzai.materialfile.app.DialogHostActivity
import naipingzai.materialfile.file.ExternalStorageUri
import naipingzai.materialfile.file.displayName
import naipingzai.materialfile.util.createDocumentsUiViewDirectoryIntent
import naipingzai.materialfile.util.putArgs
import kotlin.random.Random

@Parcelize
data class ExternalStorageShortcut(
    override val id: Long,
    override val customName: String?,
    val uri: ExternalStorageUri
) : Storage() {
    constructor(
        id: Long?,
        customName: String?,
        uri: ExternalStorageUri
    ) : this(id ?: Random.nextLong(), customName, uri)

    override fun getDefaultName(context: Context): String = uri.displayName

    override val description: String
        get() = uri.value.toString()

    override val path: Path?
        get() = null

    override fun createIntent(): Intent = uri.value.createDocumentsUiViewDirectoryIntent()

    override fun createEditIntent(): Intent =
        DialogHostActivity.createIntent<EditExternalStorageShortcutDialogFragment>()
            .putArgs(EditExternalStorageShortcutDialogFragment.Args(this))
}
