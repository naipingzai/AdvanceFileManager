/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class ColorTest {

    @Test
    fun asColor_roundTrip() {
        val c = 0xFFFF0000.toInt().asColor()
        assertThat(c.value).isEqualTo(0xFFFF0000.toInt())
    }

    @Test
    fun alpha() {
        val c = 0x80FF0000.toInt().asColor()
        assertThat(c.alpha).isEqualTo(0x80)
    }

    @Test
    fun red() {
        val c = 0xFFFF0000.toInt().asColor()
        assertThat(c.red).isEqualTo(255)
    }

    @Test
    fun green() {
        val c = 0xFF00FF00.toInt().asColor()
        assertThat(c.green).isEqualTo(255)
    }

    @Test
    fun blue() {
        val c = 0xFF0000FF.toInt().asColor()
        assertThat(c.blue).isEqualTo(255)
    }

    @Test
    fun withAlpha() {
        val c = 0xFFFF0000.toInt().asColor()
        val halfAlpha = c.withAlpha(128)
        assertThat(halfAlpha.alpha).isEqualTo(128)
        assertThat(halfAlpha.red).isEqualTo(255)
    }

    @Test
    fun withModulatedAlpha_half() {
        val c = 0xFFFF0000.toInt().asColor()
        val modulated = c.withModulatedAlpha(0.5f)
        // 255 * 0.5 = 127.5 → 128
        assertThat(modulated.alpha).isAnyOf(127, 128)
        assertThat(modulated.red).isEqualTo(255)
    }

    @Test
    fun withModulatedAlpha_zero() {
        val c = 0xFFFF0000.toInt().asColor()
        val modulated = c.withModulatedAlpha(0f)
        assertThat(modulated.alpha).isEqualTo(0)
    }

    @Test
    fun compositeOver() {
        // Semi-transparent red over white background
        val red = 0x80FF0000.toInt().asColor()
        val white = 0xFFFFFFFF.toInt().asColor()
        val result = red.compositeOver(white)
        // Result should be opaque pinkish
        assertThat(result.alpha).isEqualTo(255)
        assertThat(result.red).isGreaterThan(127)
    }
}
