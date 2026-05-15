/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun formatSize_zero() {
        assertThat(FormatUtils.formatSize(0)).isEqualTo("0 B")
    }

    @Test
    fun formatSize_bytes() {
        assertThat(FormatUtils.formatSize(1)).isEqualTo("1 B")
        assertThat(FormatUtils.formatSize(512)).isEqualTo("512 B")
        assertThat(FormatUtils.formatSize(1023)).isEqualTo("1023 B")
    }

    @Test
    fun formatSize_kilobytes() {
        assertThat(FormatUtils.formatSize(1024)).isEqualTo("1.0 KB")
        assertThat(FormatUtils.formatSize(1536)).isEqualTo("1.5 KB")
        assertThat(FormatUtils.formatSize(1024 * 1023L)).isEqualTo("1023.0 KB")
    }

    @Test
    fun formatSize_megabytes() {
        assertThat(FormatUtils.formatSize(1024 * 1024L)).isEqualTo("1.0 MB")
        assertThat(FormatUtils.formatSize(1024 * 1024L * 500)).isEqualTo("500.0 MB")
    }

    @Test
    fun formatSize_gigabytes() {
        assertThat(FormatUtils.formatSize(1024L * 1024 * 1024)).isEqualTo("1.00 GB")
        assertThat(FormatUtils.formatSize(1024L * 1024 * 1024 * 2)).isEqualTo("2.00 GB")
    }

    @Test
    fun formatSize_largeValue() {
        // 1 TB = 1024 GB
        assertThat(FormatUtils.formatSize(1024L * 1024 * 1024 * 1024)).isEqualTo("1024.00 GB")
    }
}
