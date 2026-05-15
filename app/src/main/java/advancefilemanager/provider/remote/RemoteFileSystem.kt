/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.remote

import java8.nio.file.FileSystem
import java.io.IOException

abstract class RemoteFileSystem(
    private val remoteInterface: RemoteInterface<IRemoteFileSystem>
) : FileSystem() {
    @Throws(IOException::class)
    override fun close() {
        if (!remoteInterface.has()) {
            return
        }
        remoteInterface.get().call { exception -> close(exception) }
    }
}
