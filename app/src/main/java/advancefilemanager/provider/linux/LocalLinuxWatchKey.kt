/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.linux

import com.advancefilemanager.provider.common.AbstractWatchKey

internal class LocalLinuxWatchKey(
    watchService: LocalLinuxWatchService,
    path: LinuxPath,
    val watchDescriptor: Int
) : AbstractWatchKey<LocalLinuxWatchKey, LinuxPath>(watchService, path)
