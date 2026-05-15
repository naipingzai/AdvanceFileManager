/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.archive

import android.content.Context
import java8.nio.file.Path
import com.advancefilemanager.fileaction.ArchivePasswordDialogActivity
import com.advancefilemanager.fileaction.ArchivePasswordDialogFragment
import com.advancefilemanager.provider.common.UserAction
import com.advancefilemanager.provider.common.UserActionRequiredException
import com.advancefilemanager.util.createIntent
import com.advancefilemanager.util.putArgs
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
