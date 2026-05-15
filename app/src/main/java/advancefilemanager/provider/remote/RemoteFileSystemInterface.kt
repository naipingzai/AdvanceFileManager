/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.remote

import java8.nio.file.FileSystem

class RemoteFileSystemInterface(private val fileSystem: FileSystem) : IRemoteFileSystem.Stub() {
    override fun close(exception: ParcelableException) {
        tryRun(exception) { fileSystem.close() }
    }
}
