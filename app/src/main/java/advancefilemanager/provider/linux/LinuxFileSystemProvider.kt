/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.linux

import java8.nio.file.attribute.FileAttributeView
import com.advancefilemanager.provider.root.RootFileSystemProvider
import com.advancefilemanager.provider.root.RootableFileSystemProvider

object LinuxFileSystemProvider : RootableFileSystemProvider(
    { LocalLinuxFileSystemProvider(it as LinuxFileSystemProvider) },
    { RootFileSystemProvider(LocalLinuxFileSystemProvider.SCHEME) }
) {
    override val localProvider: LocalLinuxFileSystemProvider
        get() = super.localProvider as LocalLinuxFileSystemProvider

    override val rootProvider: RootFileSystemProvider
        get() = super.rootProvider as RootFileSystemProvider

    internal val fileSystem: LinuxFileSystem
        get() = localProvider.fileSystem

    internal fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
        LocalLinuxFileSystemProvider.supportsFileAttributeView(type)
}
