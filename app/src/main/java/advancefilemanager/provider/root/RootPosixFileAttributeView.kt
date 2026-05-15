/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.root

import com.advancefilemanager.provider.common.PosixFileAttributeView
import com.advancefilemanager.provider.remote.RemoteInterface
import com.advancefilemanager.provider.remote.RemotePosixFileAttributeView

open class RootPosixFileAttributeView(
    attributeView: PosixFileAttributeView
) : RemotePosixFileAttributeView(
    RemoteInterface { RootFileService.getRemotePosixFileAttributeViewInterface(attributeView) }
) {
    override fun name(): String {
        throw AssertionError()
    }
}
