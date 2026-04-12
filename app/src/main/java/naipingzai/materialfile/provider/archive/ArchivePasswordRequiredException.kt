/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.provider.archive

import android.content.Context
import java8.nio.file.Path
import naipingzai.materialfile.fileaction.ArchivePasswordDialogActivity
import naipingzai.materialfile.fileaction.ArchivePasswordDialogFragment
import naipingzai.materialfile.provider.common.UserAction
import naipingzai.materialfile.provider.common.UserActionRequiredException
import naipingzai.materialfile.util.createIntent
import naipingzai.materialfile.util.putArgs
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class ArchivePasswordRequiredException(
    private val file: Path,
    reason: String?
) :
    UserActionRequiredException(file.toString(), null, reason) {

    override fun getUserAction(continuation: Continuation<Boolean>, context: Context): UserAction {
        return UserAction(
            ArchivePasswordDialogActivity::class.createIntent().putArgs(
                ArchivePasswordDialogFragment.Args(file) { continuation.resume(it) }
            ), ArchivePasswordDialogFragment.getTitle(context),
            ArchivePasswordDialogFragment.getMessage(file, context)
        )
    }
}
