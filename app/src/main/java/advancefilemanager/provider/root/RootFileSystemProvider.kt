/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.root

import java8.nio.file.FileSystem
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.attribute.FileAttributeView
import com.advancefilemanager.provider.remote.RemoteFileSystemProvider
import com.advancefilemanager.provider.remote.RemoteInterface
import java.net.URI

open class RootFileSystemProvider(scheme: String) : RemoteFileSystemProvider(
    RemoteInterface { RootFileService.getRemoteFileSystemProviderInterface(scheme) }
) {
    override fun getScheme(): String {
        throw UnsupportedOperationException("Not supported")
    }

    override fun newFileSystem(uri: URI, env: Map<String, *>): FileSystem {
        throw UnsupportedOperationException("Not supported")
    }

    override fun getFileSystem(uri: URI): FileSystem {
        throw UnsupportedOperationException("Not supported")
    }

    override fun getPath(uri: URI): Path {
        throw UnsupportedOperationException("Not supported")
    }

    override fun <V : FileAttributeView> getFileAttributeView(
        path: Path,
        type: Class<V>,
        vararg options: LinkOption
    ): V? {
        throw UnsupportedOperationException("Not supported")
    }
}
