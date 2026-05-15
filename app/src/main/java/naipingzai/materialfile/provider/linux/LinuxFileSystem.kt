/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.provider.linux

import android.os.Parcel
import android.os.Parcelable
import naipingzai.materialfile.provider.common.ByteString
import naipingzai.materialfile.provider.common.ByteStringListPathCreator
import naipingzai.materialfile.provider.root.RootFileSystem
import naipingzai.materialfile.provider.root.RootableFileSystem

internal class LinuxFileSystem(provider: LinuxFileSystemProvider) : RootableFileSystem(
    { LocalLinuxFileSystem(it as LinuxFileSystem, provider) }, { RootFileSystem(it) }
), ByteStringListPathCreator {
    override val localFileSystem: LocalLinuxFileSystem
        get() = super.localFileSystem as LocalLinuxFileSystem

    val rootDirectory: LinuxPath
        get() = localFileSystem.rootDirectory

    val defaultDirectory: LinuxPath
        get() = localFileSystem.defaultDirectory

    override fun getPath(first: ByteString, vararg more: ByteString): LinuxPath =
        localFileSystem.getPath(first, *more)

    override fun close() {
        // No-op: file system does not need to be closed explicitly
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {}

    companion object {
        const val SEPARATOR = LocalLinuxFileSystem.SEPARATOR

        @JvmField
        val CREATOR = object : Parcelable.Creator<LinuxFileSystem> {
            override fun createFromParcel(source: Parcel): LinuxFileSystem =
                LinuxFileSystemProvider.fileSystem

            override fun newArray(size: Int): Array<LinuxFileSystem?> = arrayOfNulls(size)
        }
    }
}
