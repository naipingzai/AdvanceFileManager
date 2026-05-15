package com.advancefilemanager.provider.remote;

import com.advancefilemanager.provider.remote.IRemoteFileSystem;
import com.advancefilemanager.provider.remote.IRemoteFileSystemProvider;
import com.advancefilemanager.provider.remote.IRemotePosixFileAttributeView;
import com.advancefilemanager.provider.remote.IRemotePosixFileStore;
import com.advancefilemanager.provider.remote.ParcelableObject;

interface IRemoteFileService {
    IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(String scheme);

    IRemoteFileSystem getRemoteFileSystemInterface(in ParcelableObject fileSystem);

    IRemotePosixFileStore getRemotePosixFileStoreInterface(in ParcelableObject fileStore);

    IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
        in ParcelableObject attributeView
    );
}