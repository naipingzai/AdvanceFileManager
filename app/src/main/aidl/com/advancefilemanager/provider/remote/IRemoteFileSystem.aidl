package com.advancefilemanager.provider.remote;

import com.advancefilemanager.provider.remote.ParcelableException;

interface IRemoteFileSystem {
    void close(out ParcelableException exception);
}