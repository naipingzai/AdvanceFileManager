/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import java8.nio.file.Path

class PollingWatchKey(
    watchService: PollingWatchService,
    path: Path
) : AbstractWatchKey<PollingWatchKey, Path>(watchService, path)
