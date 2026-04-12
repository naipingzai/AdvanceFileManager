/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.provider.archive

import java8.nio.file.StandardOpenOption
import naipingzai.materialfile.provider.common.OpenOptions

internal fun OpenOptions.checkForArchive() {
    if (write) {
        throw UnsupportedOperationException(StandardOpenOption.WRITE.toString())
    }
    if (append) {
        throw UnsupportedOperationException(StandardOpenOption.APPEND.toString())
    }
    if (truncateExisting) {
        throw UnsupportedOperationException(StandardOpenOption.TRUNCATE_EXISTING.toString())
    }
    if (create) {
        throw UnsupportedOperationException(StandardOpenOption.CREATE.toString())
    }
    if (createNew) {
        throw UnsupportedOperationException(StandardOpenOption.CREATE_NEW.toString())
    }
    if (deleteOnClose) {
        throw UnsupportedOperationException(StandardOpenOption.DELETE_ON_CLOSE.toString())
    }
    if (sync) {
        throw UnsupportedOperationException(StandardOpenOption.SYNC.toString())
    }
    if (dsync) {
        throw UnsupportedOperationException(StandardOpenOption.DSYNC.toString())
    }
}
