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
class Base64Test {

    @Test
    fun roundTrip_simpleString() {
        val original = "Hello, World!".toByteArray()
        val encoded = original.toBase64()
        val decoded = encoded.toByteArray()
        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun roundTrip_emptyArray() {
        val original = byteArrayOf()
        val encoded = original.toBase64()
        val decoded = encoded.toByteArray()
        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun roundTrip_binaryData() {
        val original = byteArrayOf(0, 1, 2, 127, -128, -1)
        val encoded = original.toBase64()
        val decoded = encoded.toByteArray()
        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun asBase64_createsValue() {
        val b64 = "SGVsbG8=".asBase64()
        assertThat(b64.value).isEqualTo("SGVsbG8=")
    }

    @Test
    fun encode_knownValue() {
        val encoded = "Hello".toByteArray().toBase64()
        assertThat(encoded.value).isEqualTo("SGVsbG8=")
    }

    @Test
    fun decode_knownValue() {
        val decoded = "SGVsbG8=".asBase64().toByteArray()
        assertThat(String(decoded)).isEqualTo("Hello")
    }
}
