/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ByteStringBuilderTest {

    @Test
    fun empty_builder() {
        val builder = ByteStringBuilder()
        assertThat(builder.isEmpty).isTrue()
        assertThat(builder.length).isEqualTo(0)
    }

    @Test
    fun append_byte() {
        val builder = ByteStringBuilder()
        builder.append(65.toByte()) // 'A'
        assertThat(builder.length).isEqualTo(1)
        assertThat(builder[0]).isEqualTo(65.toByte())
        assertThat(builder.toString()).isEqualTo("A")
    }

    @Test
    fun append_byteArray() {
        val builder = ByteStringBuilder()
        builder.append(byteArrayOf(65, 66, 67))
        assertThat(builder.length).isEqualTo(3)
        assertThat(builder.toString()).isEqualTo("ABC")
    }

    @Test
    fun append_byteArray_range() {
        val builder = ByteStringBuilder()
        builder.append(byteArrayOf(65, 66, 67, 68), 1, 3)
        assertThat(builder.toString()).isEqualTo("BC")
    }

    @Test
    fun append_byteString() {
        val builder = ByteStringBuilder()
        builder.append("Hello".toByteString())
        assertThat(builder.toString()).isEqualTo("Hello")
    }

    @Test
    fun append_chained() {
        val result = ByteStringBuilder()
            .append("Hello".toByteString())
            .append(' '.code.toByte())
            .append("World".toByteString())
            .toByteString()
        assertThat(result.toString()).isEqualTo("Hello World")
    }

    @Test
    fun toByteString() {
        val builder = ByteStringBuilder()
        builder.append(byteArrayOf(1, 2, 3))
        val bs = builder.toByteString()
        assertThat(bs.length).isEqualTo(3)
    }

    @Test
    fun constructFromByteString() {
        val bs = "initial".toByteString()
        val builder = ByteStringBuilder(bs)
        assertThat(builder.length).isEqualTo(7)
        assertThat(builder.toString()).isEqualTo("initial")
    }

    @Test
    fun capacity_grows() {
        val builder = ByteStringBuilder(2)
        assertThat(builder.capacity()).isEqualTo(2)
        builder.append(byteArrayOf(1, 2, 3, 4, 5))
        assertThat(builder.capacity()).isAtLeast(5)
        assertThat(builder.length).isEqualTo(5)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun get_outOfBounds() {
        val builder = ByteStringBuilder()
        builder.append(65.toByte())
        builder[1] // out of bounds
    }

    @Test
    fun isEmpty_afterAppend() {
        val builder = ByteStringBuilder()
        assertThat(builder.isEmpty).isTrue()
        builder.append(1.toByte())
        assertThat(builder.isEmpty).isFalse()
    }
}
