package com.advancefilemanager.provider.remote;

import com.advancefilemanager.provider.remote.ParcelableException;
import com.advancefilemanager.util.RemoteCallback;

interface IRemotePathObservable {
    void addObserver(in RemoteCallback observer);

    void close(out ParcelableException exception);
}