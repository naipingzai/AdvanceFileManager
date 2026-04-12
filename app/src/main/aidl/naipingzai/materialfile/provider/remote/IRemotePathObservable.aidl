package naipingzai.materialfile.provider.remote;

import naipingzai.materialfile.provider.remote.ParcelableException;
import naipingzai.materialfile.util.RemoteCallback;

interface IRemotePathObservable {
    void addObserver(in RemoteCallback observer);

    void close(out ParcelableException exception);
}