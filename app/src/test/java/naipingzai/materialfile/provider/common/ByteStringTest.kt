/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.provider.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ByteStringTest {

    @Test
    fun fromString_basic() {
        val bs = "Hello".toByteString()
        assertThat(bs.toString()).isEqualTo("Hello")
        assertThat(bs.length).isEqualTo(5)
    }

    @Test
    fun empty() {
        assertThat(ByteString.EMPTY.isEmpty()).isTrue()
        assertThat(ByteString.EMPTY.isNotEmpty()).isFalse()
        assertThat(ByteString.EMPTY.length).isEqualTo(0)
    }

    @Test
    fun fromBytes() {
        val bs = ByteString.fromBytes(byteArrayOf(65, 66, 67))
        assertThat(bs.toString()).isEqualTo("ABC")
    }

    @Test
    fun fromBytes_range() {
        val bs = ByteString.fromBytes(byteArrayOf(65, 66, 67, 68), 1, 3)
        assertThat(bs.toString()).isEqualTo("BC")
    }

    @Test
    fun indexOperator() {
        val bs = "ABC".toByteString()
        assertThat(bs[0]).isEqualTo(65.toByte())
        assertThat(bs[2]).isEqualTo(67.toByte())
    }

    @Test
    fun toBytes_returnsCopy() {
        val bs = "Hello".toByteString()
        val bytes = bs.toBytes()
        bytes[0] = 0  // modify the copy
        assertThat(bs[0]).isEqualTo('H'.code.toByte()) // original unchanged
    }

    @Test
    fun startsWith() {
        val bs = "Hello World".toByteString()
        assertThat(bs.startsWith("Hello".toByteString())).isTrue()
        assertThat(bs.startsWith("World".toByteString())).isFalse()
    }

    @Test
    fun startsWith_atOffset() {
        val bs = "Hello World".toByteString()
        assertThat(bs.startsWith("World".toByteString(), 6)).isTrue()
    }

    @Test
    fun endsWith() {
        val bs = "Hello World".toByteString()
        assertThat(bs.endsWith("World".toByteString())).isTrue()
        assertThat(bs.endsWith("Hello".toByteString())).isFalse()
    }

    @Test
    fun indexOf_byte() {
        val bs = "Hello".toByteString()
        assertThat(bs.indexOf('l'.code.toByte())).isEqualTo(2)
        assertThat(bs.indexOf('z'.code.toByte())).isEqualTo(-1)
    }

    @Test
    fun indexOf_byte_fromIndex() {
        val bs = "Hello".toByteString()
        assertThat(bs.indexOf('l'.code.toByte(), 3)).isEqualTo(3)
    }

    @Test
    fun lastIndexOf_byte() {
        val bs = "Hello".toByteString()
        assertThat(bs.lastIndexOf('l'.code.toByte())).isEqualTo(3)
    }

    @Test
    fun contains_byte() {
        val bs = "ABC".toByteString()
        assertThat(bs.contains('B'.code.toByte())).isTrue()
        assertThat(bs.contains('Z'.code.toByte())).isFalse()
    }

    @Test
    fun indexOf_substring() {
        val bs = "Hello World!".toByteString()
        assertThat(bs.indexOf("World".toByteString())).isEqualTo(6)
        assertThat(bs.indexOf("xyz".toByteString())).isEqualTo(-1)
    }

    @Test
    fun lastIndexOf_substring() {
        val bs = "abcabc".toByteString()
        assertThat(bs.lastIndexOf("abc".toByteString())).isEqualTo(3)
    }

    @Test
    fun contains_substring() {
        val bs = "Hello World!".toByteString()
        assertThat(bs.contains("World".toByteString())).isTrue()
    }

    @Test
    fun substring() {
        val bs = "Hello World".toByteString()
        assertThat(bs.substring(0, 5).toString()).isEqualTo("Hello")
        assertThat(bs.substring(6).toString()).isEqualTo("World")
    }

    @Test
    fun substring_entireString() {
        val bs = "Hello".toByteString()
        assertThat(bs.substring(0, 5)).isSameInstanceAs(bs)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun substring_outOfBounds() {
        "Hello".toByteString().substring(-1, 5)
    }

    @Test
    fun plus_operator() {
        val a = "Hello".toByteString()
        val b = " World".toByteString()
        assertThat((a + b).toString()).isEqualTo("Hello World")
    }

    @Test
    fun plus_emptyString() {
        val a = "Hello".toByteString()
        assertThat((a + ByteString.EMPTY)).isSameInstanceAs(a)
    }

    @Test
    fun split() {
        val bs = "a/b/c".toByteString()
        val parts = bs.split("/".toByteString())
        assertThat(parts.map { it.toString() }).containsExactly("a", "b", "c").inOrder()
    }

    @Test
    fun split_noDelimiter() {
        val bs = "hello".toByteString()
        val parts = bs.split("/".toByteString())
        assertThat(parts).hasSize(1)
        assertThat(parts[0].toString()).isEqualTo("hello")
    }

    @Test
    fun equality() {
        val a = "Hello".toByteString()
        val b = ByteString.fromBytes("Hello".toByteArray())
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun inequality() {
        assertThat("Hello".toByteString()).isNotEqualTo("World".toByteString())
    }

    @Test
    fun compareTo() {
        val a = "abc".toByteString()
        val b = "abd".toByteString()
        assertThat(a < b).isTrue()
    }

    @Test
    fun compareTo_differentLength() {
        val a = "ab".toByteString()
        val b = "abc".toByteString()
        assertThat(a < b).isTrue()
    }

    @Test
    fun cstr_addsNullTerminator() {
        val bs = "abc".toByteString()
        val cstr = bs.cstr
        assertThat(cstr).hasLength(4)
        assertThat(cstr.last()).isEqualTo(0)
    }

    @Test
    fun iterator() {
        val bs = "ABC".toByteString()
        val collected = mutableListOf<Byte>()
        for (b in bs) collected.add(b)
        assertThat(collected).containsExactly(65.toByte(), 66.toByte(), 67.toByte()).inOrder()
    }

    @Test
    fun indices_range() {
        val bs = "abc".toByteString()
        assertThat(bs.indices).isEqualTo(0..2)
        assertThat(bs.lastIndex).isEqualTo(2)
    }

    @Test
    fun byteToByteString() {
        val bs = 65.toByte().toByteString()
        assertThat(bs.length).isEqualTo(1)
        assertThat(bs[0]).isEqualTo(65.toByte())
    }

    @Test
    fun byteArrayToByteString() {
        val bs = byteArrayOf(1, 2, 3).toByteString()
        assertThat(bs.length).isEqualTo(3)
    }

    @Test
    fun byteArrayToByteString_range() {
        val bs = byteArrayOf(1, 2, 3, 4).toByteString(1, 3)
        assertThat(bs.length).isEqualTo(2)
        assertThat(bs[0]).isEqualTo(2.toByte())
    }

    @Test
    fun stringToByteString() {
        val bs = "test".toByteString()
        assertThat(bs.toString()).isEqualTo("test")
    }
}
