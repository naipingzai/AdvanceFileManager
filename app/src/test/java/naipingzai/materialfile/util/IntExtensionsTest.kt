/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IntExtensionsTest {

    @Test
    fun hasBits_allBitsSet() {
        assertThat(0xFF.hasBits(0xFF)).isTrue()
    }

    @Test
    fun hasBits_someBitsSet() {
        assertThat(0b1010.hasBits(0b1010)).isTrue()
        assertThat(0b1010.hasBits(0b0010)).isTrue()
    }

    @Test
    fun hasBits_noBitsSet() {
        assertThat(0b1010.hasBits(0b0101)).isFalse()
    }

    @Test
    fun hasBits_zero() {
        assertThat(0.hasBits(0)).isTrue()
        assertThat(0xFF.hasBits(0)).isTrue()
    }

    @Test
    fun andInv_clearsSpecifiedBits() {
        assertThat(0xFF andInv 0x0F).isEqualTo(0xF0)
    }

    @Test
    fun andInv_noOverlap() {
        assertThat(0xF0 andInv 0x0F).isEqualTo(0xF0)
    }

    @Test
    fun andInv_clearAll() {
        assertThat(0xFF andInv 0xFF).isEqualTo(0)
    }
}
