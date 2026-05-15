/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ByteStringExtensionsTest {

    // ── isNullOrEmpty ──

    @Test
    fun isNullOrEmpty_null() {
        val bs: ByteString? = null
        assertThat(bs.isNullOrEmpty()).isTrue()
    }

    @Test
    fun isNullOrEmpty_empty() {
        assertThat(ByteString.EMPTY.isNullOrEmpty()).isTrue()
    }

    @Test
    fun isNullOrEmpty_nonEmpty() {
        assertThat("abc".toByteString().isNullOrEmpty()).isFalse()
    }

    // ── takeIfNotEmpty ──

    @Test
    fun takeIfNotEmpty_nonEmpty() {
        val bs = "abc".toByteString()
        assertThat(bs.takeIfNotEmpty()).isSameInstanceAs(bs)
    }

    @Test
    fun takeIfNotEmpty_empty() {
        assertThat(ByteString.EMPTY.takeIfNotEmpty()).isNull()
    }

    // ── drop / dropLast ──

    @Test
    fun drop_n() {
        assertThat("Hello".toByteString().drop(2).toString()).isEqualTo("llo")
    }

    @Test
    fun drop_zero() {
        val bs = "Hello".toByteString()
        assertThat(bs.drop(0).toString()).isEqualTo("Hello")
    }

    @Test
    fun drop_moreThanLength() {
        assertThat("Hi".toByteString().drop(10).toString()).isEmpty()
    }

    @Test
    fun dropLast_n() {
        assertThat("Hello".toByteString().dropLast(2).toString()).isEqualTo("Hel")
    }

    @Test
    fun dropLast_moreThanLength() {
        assertThat("Hi".toByteString().dropLast(10).toString()).isEmpty()
    }

    @Test
    fun dropLastWhile_predicate() {
        val bs = "abc...".toByteString()
        val result = bs.dropLastWhile { it == '.'.code.toByte() }
        assertThat(result.toString()).isEqualTo("abc")
    }

    @Test
    fun dropWhile_predicate() {
        val bs = "...abc".toByteString()
        val result = bs.dropWhile { it == '.'.code.toByte() }
        assertThat(result.toString()).isEqualTo("abc")
    }

    // ── take / takeLast ──

    @Test
    fun take_n() {
        assertThat("Hello".toByteString().take(3).toString()).isEqualTo("Hel")
    }

    @Test
    fun take_moreThanLength() {
        assertThat("Hi".toByteString().take(10).toString()).isEqualTo("Hi")
    }

    @Test
    fun takeLast_n() {
        assertThat("Hello".toByteString().takeLast(3).toString()).isEqualTo("llo")
    }

    @Test
    fun takeLastWhile_predicate() {
        val bs = "abc123".toByteString()
        val result = bs.takeLastWhile { it in '0'.code.toByte()..'9'.code.toByte() }
        assertThat(result.toString()).isEqualTo("123")
    }

    @Test
    fun takeWhile_predicate() {
        val bs = "123abc".toByteString()
        val result = bs.takeWhile { it in '0'.code.toByte()..'9'.code.toByte() }
        assertThat(result.toString()).isEqualTo("123")
    }

    // ── substringBefore / substringAfter ──

    @Test
    fun substringBefore_byte_found() {
        val bs = "path/file.txt".toByteString()
        assertThat(bs.substringBefore('/'.code.toByte()).toString()).isEqualTo("path")
    }

    @Test
    fun substringBefore_byte_notFound() {
        val bs = "filename".toByteString()
        assertThat(bs.substringBefore('/'.code.toByte()).toString()).isEqualTo("filename")
    }

    @Test
    fun substringAfter_byte_found() {
        val bs = "path/file.txt".toByteString()
        assertThat(bs.substringAfter('/'.code.toByte()).toString()).isEqualTo("file.txt")
    }

    @Test
    fun substringAfter_byte_notFound() {
        val bs = "filename".toByteString()
        assertThat(bs.substringAfter('/'.code.toByte()).toString()).isEqualTo("filename")
    }

    // ── substringBeforeLast / substringAfterLast ──

    @Test
    fun substringBeforeLast_byte() {
        val bs = "a/b/c".toByteString()
        assertThat(bs.substringBeforeLast('/'.code.toByte()).toString()).isEqualTo("a/b")
    }

    @Test
    fun substringAfterLast_byte() {
        val bs = "a/b/c".toByteString()
        assertThat(bs.substringAfterLast('/'.code.toByte()).toString()).isEqualTo("c")
    }

    @Test
    fun substringAfterLast_byte_notFound() {
        val bs = "filename".toByteString()
        assertThat(bs.substringAfterLast('/'.code.toByte()).toString()).isEqualTo("filename")
    }

    // ── substring with ByteString delimiter ──

    @Test
    fun substringBefore_byteString() {
        val bs = "hello::world".toByteString()
        assertThat(bs.substringBefore("::".toByteString()).toString()).isEqualTo("hello")
    }

    @Test
    fun substringAfter_byteString() {
        val bs = "hello::world".toByteString()
        assertThat(bs.substringAfter("::".toByteString()).toString()).isEqualTo("world")
    }

    @Test
    fun substringBeforeLast_byteString() {
        val bs = "a::b::c".toByteString()
        assertThat(bs.substringBeforeLast("::".toByteString()).toString()).isEqualTo("a::b")
    }

    @Test
    fun substringAfterLast_byteString() {
        val bs = "a::b::c".toByteString()
        assertThat(bs.substringAfterLast("::".toByteString()).toString()).isEqualTo("c")
    }
}
