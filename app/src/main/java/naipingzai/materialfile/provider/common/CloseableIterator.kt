/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.provider.common

import java.io.Closeable

interface CloseableIterator<T> : Iterator<T>, Closeable
