/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ByteArrayExtensionsTest {

    @Test
    fun sha1Digest_emptyArray() {
        val result = byteArrayOf().sha1Digest()
        // SHA-1 of empty input = da39a3ee5e6b4b0d3255bfef95601890afd80709
        assertThat(result.toHexString()).isEqualTo("DA39A3EE5E6B4B0D3255BFEF95601890AFD80709")
    }

    @Test
    fun sha1Digest_helloWorld() {
        val result = "Hello, World!".toByteArray().sha1Digest()
        assertThat(result.toHexString()).isEqualTo("0A0A9F2A6772942557AB5355D76AF442F8F65E01")
    }

    @Test
    fun toHexString_emptyArray() {
        assertThat(byteArrayOf().toHexString()).isEmpty()
    }

    @Test
    fun toHexString_singleByte() {
        assertThat(byteArrayOf(0xFF.toByte()).toHexString()).isEqualTo("FF")
        assertThat(byteArrayOf(0x00).toHexString()).isEqualTo("00")
        assertThat(byteArrayOf(0x0A).toHexString()).isEqualTo("0A")
    }

    @Test
    fun toHexString_multipleBytes() {
        assertThat(byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())
            .toHexString()).isEqualTo("DEADBEEF")
    }
}
