/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.provider.common

import java8.nio.file.Path
import java8.nio.file.WatchEvent

class LocalWatchKey(
    watchService: LocalWatchService,
    path: Path,
    @Volatile
    internal var kinds: Set<WatchEvent.Kind<*>>
) : AbstractWatchKey<LocalWatchKey, Path>(watchService, path)
