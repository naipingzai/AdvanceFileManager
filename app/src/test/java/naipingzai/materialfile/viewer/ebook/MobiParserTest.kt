/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.viewer.ebook

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class MobiParserTest {

    private lateinit var parser: MobiParser

    @Before
    fun setUp() {
        parser = MobiParser()
    }

    // ── Helper to invoke private methods via reflection ──

    private fun invokePrivate(name: String, paramTypes: Array<Class<*>>, vararg args: Any?): Any? {
        val method = MobiParser::class.java.getDeclaredMethod(name, *paramTypes)
        method.isAccessible = true
        return method.invoke(parser, *args)
    }

    // ── readUInt16 tests ──

    @Test
    fun readUInt16_bigEndian() {
        val data = byteArrayOf(0x01, 0x00)
        val result = invokePrivate("readUInt16",
            arrayOf(ByteArray::class.java, Int::class.java), data, 0
        ) as Int
        assertThat(result).isEqualTo(256) // 0x0100
    }

    @Test
    fun readUInt16_maxValue() {
        val data = byteArrayOf(0xFF.toByte(), 0xFF.toByte())
        val result = invokePrivate("readUInt16",
            arrayOf(ByteArray::class.java, Int::class.java), data, 0
        ) as Int
        assertThat(result).isEqualTo(65535)
    }

    @Test
    fun readUInt16_zero() {
        val data = byteArrayOf(0x00, 0x00)
        val result = invokePrivate("readUInt16",
            arrayOf(ByteArray::class.java, Int::class.java), data, 0
        ) as Int
        assertThat(result).isEqualTo(0)
    }

    @Test
    fun readUInt16_withOffset() {
        val data = byteArrayOf(0x00, 0x00, 0x12, 0x34)
        val result = invokePrivate("readUInt16",
            arrayOf(ByteArray::class.java, Int::class.java), data, 2
        ) as Int
        assertThat(result).isEqualTo(0x1234)
    }

    // ── readUInt32 tests ──

    @Test
    fun readUInt32_bigEndian() {
        val data = byteArrayOf(0x00, 0x01, 0x00, 0x00)
        val result = invokePrivate("readUInt32",
            arrayOf(ByteArray::class.java, Int::class.java), data, 0
        ) as Long
        assertThat(result).isEqualTo(65536L)
    }

    @Test
    fun readUInt32_maxValue() {
        val data = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
        val result = invokePrivate("readUInt32",
            arrayOf(ByteArray::class.java, Int::class.java), data, 0
        ) as Long
        assertThat(result).isEqualTo(4294967295L)
    }

    @Test
    fun readUInt32_zero() {
        val data = byteArrayOf(0x00, 0x00, 0x00, 0x00)
        val result = invokePrivate("readUInt32",
            arrayOf(ByteArray::class.java, Int::class.java), data, 0
        ) as Long
        assertThat(result).isEqualTo(0L)
    }

    // ── readCString tests ──

    @Test
    fun readCString_nullTerminated() {
        val data = "Hello\u0000World".toByteArray(Charsets.US_ASCII)
        val result = invokePrivate("readCString",
            arrayOf(ByteArray::class.java, Int::class.java, Int::class.java),
            data, 0, 32
        ) as String
        assertThat(result).isEqualTo("Hello")
    }

    @Test
    fun readCString_maxLen() {
        val data = "HelloWorld".toByteArray(Charsets.US_ASCII)
        val result = invokePrivate("readCString",
            arrayOf(ByteArray::class.java, Int::class.java, Int::class.java),
            data, 0, 5
        ) as String
        assertThat(result).isEqualTo("Hello")
    }

    @Test
    fun readCString_emptyString() {
        val data = byteArrayOf(0x00, 0x41)
        val result = invokePrivate("readCString",
            arrayOf(ByteArray::class.java, Int::class.java, Int::class.java),
            data, 0, 32
        ) as String
        assertThat(result).isEmpty()
    }

    // ── detectImageExtension tests ──

    @Test
    fun detectImageExtension_png() {
        val data = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        val result = invokePrivate("detectImageExtension",
            arrayOf(ByteArray::class.java, Int::class.java), data, 0
        ) as String
        assertThat(result).isEqualTo("png")
    }

    @Test
    fun detectImageExtension_gif() {
        val data = "GIF89a".toByteArray(Charsets.US_ASCII)
        val result = invokePrivate("detectImageExtension",
            arrayOf(ByteArray::class.java, Int::class.java), data, 0
        ) as String
        assertThat(result).isEqualTo("gif")
    }

    @Test
    fun detectImageExtension_bmp() {
        val data = "BM".toByteArray(Charsets.US_ASCII) + ByteArray(10)
        val result = invokePrivate("detectImageExtension",
            arrayOf(ByteArray::class.java, Int::class.java), data, 0
        ) as String
        assertThat(result).isEqualTo("bmp")
    }

    @Test
    fun detectImageExtension_webp() {
        // RIFF....WEBP
        val data = byteArrayOf(
            0x52, 0x49, 0x46, 0x46,   // RIFF
            0x00, 0x00, 0x00, 0x00,   // size
            0x57, 0x45, 0x42, 0x50    // WEBP
        )
        val result = invokePrivate("detectImageExtension",
            arrayOf(ByteArray::class.java, Int::class.java), data, 0
        ) as String
        assertThat(result).isEqualTo("webp")
    }

    @Test
    fun detectImageExtension_unknownDefaultsJpg() {
        val data = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
        val result = invokePrivate("detectImageExtension",
            arrayOf(ByteArray::class.java, Int::class.java), data, 0
        ) as String
        assertThat(result).isEqualTo("jpg")
    }

    // ── decompressPalmDoc tests ──

    @Test
    fun decompressPalmDoc_literalBytes() {
        // Bytes 0x09..0x7F are literal
        val input = byteArrayOf(0x41, 0x42, 0x43) // "ABC"
        val result = invokePrivate("decompressPalmDoc",
            arrayOf(ByteArray::class.java), input
        ) as ByteArray
        assertThat(String(result, Charsets.US_ASCII)).isEqualTo("ABC")
    }

    @Test
    fun decompressPalmDoc_spaceEncoding() {
        // 0xC0..0xFF: space + (byte XOR 0x80)
        // 0xC1 -> space + (0xC1 ^ 0x80 = 0x41 = 'A')
        val input = byteArrayOf(0xC1.toByte())
        val result = invokePrivate("decompressPalmDoc",
            arrayOf(ByteArray::class.java), input
        ) as ByteArray
        assertThat(String(result, Charsets.US_ASCII)).isEqualTo(" A")
    }

    @Test
    fun decompressPalmDoc_nullByte() {
        val input = byteArrayOf(0x00)
        val result = invokePrivate("decompressPalmDoc",
            arrayOf(ByteArray::class.java), input
        ) as ByteArray
        assertThat(result).hasLength(1)
        assertThat(result[0]).isEqualTo(0)
    }

    @Test
    fun decompressPalmDoc_copyMultipleBytes() {
        // Bytes 1..8: copy next N bytes literally
        // 0x02 means copy next 2 bytes
        val input = byteArrayOf(0x02, 0x58, 0x59) // copy 2 bytes: 'X', 'Y'
        val result = invokePrivate("decompressPalmDoc",
            arrayOf(ByteArray::class.java), input
        ) as ByteArray
        assertThat(String(result, Charsets.US_ASCII)).isEqualTo("XY")
    }

    // ── stripTrailingData tests ──

    @Test
    fun stripTrailingData_noFlags() {
        val record = byteArrayOf(1, 2, 3, 4, 5)
        val result = invokePrivate("stripTrailingData",
            arrayOf(ByteArray::class.java, Int::class.java), record, 0
        ) as ByteArray
        assertThat(result).isEqualTo(record)
    }

    @Test
    fun stripTrailingData_emptyRecord() {
        val record = ByteArray(0)
        val result = invokePrivate("stripTrailingData",
            arrayOf(ByteArray::class.java, Int::class.java), record, 0
        ) as ByteArray
        assertThat(result).isEmpty()
    }

    // ── parse tests with crafted binary data ──

    @Test
    fun parse_tooSmall_throws() {
        val data = ByteArray(50)
        try {
            parser.parse(java.io.ByteArrayInputStream(data))
            assertThat(false).isTrue()
        } catch (e: MobiParseException) {
            assertThat(e.message).contains("too small")
        }
    }

    @Test
    fun parse_noRecords_throws() {
        // Create a minimal PalmDB header with 0 records
        val data = ByteArray(80)
        // numRecords at offset 76 = 0
        data[76] = 0
        data[77] = 0
        try {
            parser.parse(java.io.ByteArrayInputStream(data))
            assertThat(false).isTrue()
        } catch (e: MobiParseException) {
            assertThat(e.message).contains("No content records")
        }
    }

    @Test
    fun mobiBook_dataClass() {
        val book = MobiParser.MobiBook("Title", "<html>Content</html>")
        assertThat(book.title).isEqualTo("Title")
        assertThat(book.html).isEqualTo("<html>Content</html>")
    }
}
