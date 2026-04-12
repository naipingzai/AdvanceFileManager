/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LongExtensionsTest {

    @Test
    fun hasBits_allBitsSet() {
        assertThat(0xFFL.hasBits(0xFFL)).isTrue()
    }

    @Test
    fun hasBits_someBitsSet() {
        assertThat(0b1010L.hasBits(0b1010L)).isTrue()
        assertThat(0b1010L.hasBits(0b0010L)).isTrue()
    }

    @Test
    fun hasBits_noBitsSet() {
        assertThat(0b1010L.hasBits(0b0101L)).isFalse()
    }

    @Test
    fun hasBits_highBits() {
        val high = 1L shl 63
        assertThat(high.hasBits(high)).isTrue()
        assertThat(0L.hasBits(high)).isFalse()
    }

    @Test
    fun andInv_clearsSpecifiedBits() {
        assertThat(0xFFL andInv 0x0FL).isEqualTo(0xF0L)
    }

    @Test
    fun andInv_clearAll() {
        assertThat(0xFFL andInv 0xFFL).isEqualTo(0L)
    }
}
