/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.file

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class DurationExtensionsTest {

    @Test
    fun format_zeroSeconds() {
        assertThat(Duration.ZERO.format()).isEqualTo("00:00")
    }

    @Test
    fun format_seconds() {
        assertThat(Duration.ofSeconds(45).format()).isEqualTo("00:45")
    }

    @Test
    fun format_minutes() {
        assertThat(Duration.ofMinutes(2).plusSeconds(5).format()).isEqualTo("02:05")
    }

    @Test
    fun format_hours() {
        val d = Duration.ofHours(1).plusMinutes(2).plusSeconds(3)
        assertThat(d.format()).isEqualTo("1:02:03")
    }
}
