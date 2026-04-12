/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AutoCloseableExtensionsTest {

    @Test
    fun closeSafe_normalClose() {
        var closed = false
        val closeable = AutoCloseable { closed = true }
        closeable.closeSafe()
        assertThat(closed).isTrue()
    }

    @Test
    fun closeSafe_throwingClose_doesNotPropagate() {
        val closeable = AutoCloseable { throw RuntimeException("close failed") }
        // Should not throw
        closeable.closeSafe()
    }

    @Test
    fun closeSafe_ioException_doesNotPropagate() {
        val closeable = AutoCloseable { throw java.io.IOException("IO error") }
        closeable.closeSafe()
    }
}
