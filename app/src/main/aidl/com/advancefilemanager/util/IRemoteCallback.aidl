package com.advancefilemanager.util;

import android.os.Bundle;

interface IRemoteCallback {
    void sendResult(in Bundle result);
}