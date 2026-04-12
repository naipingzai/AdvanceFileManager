/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.file

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FileSizeTest {

    @Test
    fun asFileSize() {
        val size = 1024L.asFileSize()
        assertThat(size.value).isEqualTo(1024)
    }

    @Test
    fun isHumanReadableInBytes_smallFile() {
        assertThat(500L.asFileSize().isHumanReadableInBytes).isTrue()
    }

    @Test
    fun isHumanReadableInBytes_exactly900() {
        assertThat(900L.asFileSize().isHumanReadableInBytes).isTrue()
    }

    @Test
    fun isHumanReadableInBytes_largeFile() {
        assertThat(901L.asFileSize().isHumanReadableInBytes).isFalse()
    }

    @Test
    fun isHumanReadableInBytes_zero() {
        assertThat(0L.asFileSize().isHumanReadableInBytes).isTrue()
    }

    @Test
    fun isHumanReadableInBytes_gigabyte() {
        assertThat((1024L * 1024 * 1024).asFileSize().isHumanReadableInBytes).isFalse()
    }
}
