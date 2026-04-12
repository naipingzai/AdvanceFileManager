package naipingzai.materialfile.provider.remote;

import naipingzai.materialfile.provider.remote.ParcelableException;

interface IRemoteFileSystem {
    void close(out ParcelableException exception);
}