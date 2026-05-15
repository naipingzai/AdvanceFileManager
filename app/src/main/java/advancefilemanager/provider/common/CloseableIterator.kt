/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import java.io.Closeable

interface CloseableIterator<T> : Iterator<T>, Closeable
