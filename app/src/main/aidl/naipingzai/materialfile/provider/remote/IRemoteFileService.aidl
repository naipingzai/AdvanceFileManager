package naipingzai.materialfile.provider.remote;

import naipingzai.materialfile.provider.remote.IRemoteFileSystem;
import naipingzai.materialfile.provider.remote.IRemoteFileSystemProvider;
import naipingzai.materialfile.provider.remote.IRemotePosixFileAttributeView;
import naipingzai.materialfile.provider.remote.IRemotePosixFileStore;
import naipingzai.materialfile.provider.remote.ParcelableObject;

interface IRemoteFileService {
    IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(String scheme);

    IRemoteFileSystem getRemoteFileSystemInterface(in ParcelableObject fileSystem);

    IRemotePosixFileStore getRemotePosixFileStoreInterface(in ParcelableObject fileStore);

    IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
        in ParcelableObject attributeView
    );
}