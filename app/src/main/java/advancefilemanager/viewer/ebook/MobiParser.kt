/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.ebook

import android.util.Log
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

/**
 * MOBI file parser — extracts HTML content from .mobi / .azw files.
 *
 * MOBI format:
 * 1. PalmDB header (78 bytes) + record info list (8 bytes each)
 * 2. Record 0: PalmDOC header (16 bytes) + MOBI header + optional EXTH header
 * 3. Records 1..N: PalmDOC-compressed text with optional trailing data
 *
 * MOBI header field offsets (from MOBI magic):
 *   0: "MOBI" magic        12: text encoding      64: first non-book index
 *  68: full name offset    72: full name length   242: extra data flags
 */
class MobiParser {

    data class MobiBook(
        val title: String,
        val html: String
    )

    fun parse(inputStream: InputStream, imageDir: File? = null): MobiBook {
        val data = inputStream.readBytes()
        return parse(data, imageDir)
    }

    private fun parse(data: ByteArray, imageDir: File?): MobiBook {
        if (data.size < 78) {
            throw MobiParseException("File too small to be a valid MOBI file")
        }

        // ── PalmDB header ──
        val name = readCString(data, 0, 32)
        val numRecords = readUInt16(data, 76)
        if (numRecords < 2) {
            throw MobiParseException("No content records found")
        }

        // Record info list starts at offset 78, each entry is 8 bytes
        val recordOffsets = mutableListOf<Int>()
        for (i in 0 until numRecords) {
            val infoOffset = 78 + i * 8
            if (infoOffset + 8 > data.size) break
            recordOffsets.add(readUInt32(data, infoOffset).toInt())
        }
        if (recordOffsets.isEmpty()) {
            throw MobiParseException("No record offsets found")
        }

        // ── Record 0: PalmDOC header (16 bytes) ──
        val rec0 = recordOffsets[0]
        if (rec0 + 16 > data.size) {
            throw MobiParseException("Record 0 truncated")
        }
        val compression = readUInt16(data, rec0)          // 1=none, 2=PalmDOC, 17480=HUFF/CDIC
        val textRecordCount = readUInt16(data, rec0 + 8)

        // ── MOBI header (follows PalmDOC header at rec0 + 16) ──
        var title = name
        var encoding: Charset = Charsets.UTF_8
        var firstNonBookRecord = textRecordCount + 1
        var firstImageRecord = 0  // 0 means no images
        var extraDataFlags = 0

        val mobi = rec0 + 16  // MOBI header start
        if (mobi + 8 <= data.size) {
            val magic = String(data, mobi, 4, Charsets.US_ASCII)
            if (magic == "MOBI") {
                val headerLen = readUInt32(data, mobi + 4).toInt()

                // Text encoding (offset 12): 1252 = CP1252, 65001 = UTF-8
                if (headerLen >= 16 && mobi + 16 <= data.size) {
                    val enc = readUInt32(data, mobi + 12).toInt()
                    encoding = when (enc) {
                        1252 -> Charset.forName("windows-1252")
                        65001 -> Charsets.UTF_8
                        else -> Charsets.UTF_8
                    }
                }

                // MOBI format version (offset 36): only v5+ has extra data flags
                var mobiVersion = 0
                if (headerLen >= 40 && mobi + 40 <= data.size) {
                    mobiVersion = readUInt32(data, mobi + 36).toInt()
                }

                // First non-book index (offset 80)
                if (headerLen >= 84 && mobi + 84 <= data.size) {
                    val fnbr = readUInt32(data, mobi + 80).toInt()
                    if (fnbr in 2..numRecords) {
                        firstNonBookRecord = fnbr
                    }
                }

                // First image record index: try offset 108 (0x6C), then 92 (0x5C)
                if (headerLen >= 112 && mobi + 112 <= data.size) {
                    val fir = readUInt32(data, mobi + 108).toInt()
                    if (fir in 1 until numRecords) {
                        firstImageRecord = fir
                    }
                }
                // Fallback: try offset 92 (0x5C) used in some MOBI variants
                if (firstImageRecord == 0 && headerLen >= 96 && mobi + 96 <= data.size) {
                    val fir = readUInt32(data, mobi + 92).toInt()
                    if (fir in 1 until numRecords) {
                        firstImageRecord = fir
                    }
                }
                // Last resort: if HTML has image refs but no firstImageRecord,
                // use firstNonBookRecord as the first image record
                if (firstImageRecord == 0 && firstNonBookRecord > 1) {
                    firstImageRecord = firstNonBookRecord
                }

                // Full title (offset 68 = name offset, 72 = name length, relative to rec0)
                if (headerLen >= 76 && mobi + 76 <= data.size) {
                    val nameOff = readUInt32(data, mobi + 68).toInt()
                    val nameLen = readUInt32(data, mobi + 72).toInt()
                    val absOff = rec0 + nameOff
                    if (nameLen in 1..500 && absOff >= 0 && absOff + nameLen <= data.size) {
                        title = String(data, absOff, nameLen, encoding)
                    }
                }

                // Extra record data flags (MOBI offset 0xF2 = 242)
                // Only exists in MOBI format version >= 5 (KF7/KF8)
                if (mobiVersion >= 5 && headerLen >= 244 && mobi + 244 <= data.size) {
                    extraDataFlags = readUInt16(data, mobi + 242)
                }
            }
        }

        // ── Extract and decompress text records ──
        val lastTextRecord = minOf(
            firstNonBookRecord - 1,
            textRecordCount,              // PalmDOC says how many text records
            recordOffsets.size - 1
        )

        val textBuilder = StringBuilder()
        for (i in 1..lastTextRecord) {
            val recStart = recordOffsets[i]
            val recEnd = if (i + 1 < recordOffsets.size) recordOffsets[i + 1] else data.size
            if (recStart >= data.size || recEnd > data.size || recStart >= recEnd) continue

            var recordData = data.copyOfRange(recStart, recEnd)

            // Strip trailing data bytes (extra data flags)
            recordData = stripTrailingData(recordData, extraDataFlags)

            val decompressed = when (compression) {
                1 -> recordData
                2 -> decompressPalmDoc(recordData)
                17480 -> recordData  // HUFF/CDIC not supported — pass through raw
                else -> recordData
            }
            textBuilder.append(String(decompressed, encoding))
        }

        var html = textBuilder.toString()
        if (html.isBlank()) {
            throw MobiParseException(
                "No text content extracted (compression=$compression, textRecords=$textRecordCount, " +
                "lastTextRecord=$lastTextRecord, firstNonBook=$firstNonBookRecord, " +
                "extraFlags=$extraDataFlags, records=${recordOffsets.size})"
            )
        }

        // ── Replace image references with file URIs ──
        if (firstImageRecord > 0 && html.contains("recindex", ignoreCase = true)) {
            html = replaceImageReferences(html, data, recordOffsets, firstImageRecord, imageDir)
        }

        // Wrap non-HTML content
        val trimmed = html.trimStart()
        if (!trimmed.startsWith("<html", ignoreCase = true) &&
            !trimmed.startsWith("<!DOCTYPE", ignoreCase = true) &&
            !trimmed.startsWith("<body", ignoreCase = true)) {
            html = "<html><body>$html</body></html>"
        }

        return MobiBook(title = title, html = html)
    }

    /**
     * Replace <img recindex="NNNNN"> references with file URIs.
     * Images are written to imageDir as temporary files.
     * recindex is 1-based: recindex="00001" = first image record.
     */
    private fun replaceImageReferences(
        html: String, data: ByteArray, recordOffsets: List<Int>,
        firstImageRecord: Int, imageDir: File?
    ): String {
        if (imageDir == null) return html
        imageDir.mkdirs()

        // Match recindex="NNNNN" in img tags
        val pattern = Regex(
            """<img\s+([^>]*?)recindex="(\d+)"([^>]*?)\s*/?>""" ,
            RegexOption.IGNORE_CASE
        )
        return pattern.replace(html) { match ->
            val before = match.groupValues[1]
            val recIndexStr = match.groupValues[2]
            val after = match.groupValues[3]
            val recIndex = recIndexStr.toIntOrNull() ?: return@replace match.value

            // recindex is 1-based: image 1 = record at firstImageRecord + 0
            val recordIndex = firstImageRecord + recIndex - 1
            if (recordIndex < 0 || recordIndex >= recordOffsets.size) {
                return@replace match.value
            }

            val recStart = recordOffsets[recordIndex]
            val recEnd = if (recordIndex + 1 < recordOffsets.size) {
                recordOffsets[recordIndex + 1]
            } else {
                data.size
            }
            if (recStart >= data.size || recEnd > data.size || recStart >= recEnd) {
                return@replace match.value
            }

            // Detect extension from magic bytes
            val ext = detectImageExtension(data, recStart)
            val imageFile = File(imageDir, "img_$recIndex.$ext")
            try {
                imageFile.writeBytes(data.copyOfRange(recStart, recEnd))
            } catch (e: Exception) {
                Log.e("MobiParser", "Failed to write image $recIndex", e)
                return@replace match.value
            }
            "<img ${before}src=\"img_$recIndex.$ext\"${after}/>"
        }
    }

    private fun detectImageExtension(data: ByteArray, offset: Int): String {
        if (offset + 4 > data.size) return "jpg"
        return when {
            data[offset] == 0x89.toByte() && data[offset + 1] == 0x50.toByte() &&
                data[offset + 2] == 0x4E.toByte() && data[offset + 3] == 0x47.toByte() -> "png"
            data[offset] == 0x47.toByte() && data[offset + 1] == 0x49.toByte() &&
                data[offset + 2] == 0x46.toByte() -> "gif"
            data[offset] == 0x42.toByte() && data[offset + 1] == 0x4D.toByte() -> "bmp"
            offset + 12 <= data.size && data[offset] == 0x52.toByte() && data[offset + 1] == 0x49.toByte() &&
                data[offset + 8] == 0x57.toByte() && data[offset + 9] == 0x45.toByte() -> "webp"
            else -> "jpg"
        }
    }

    /**
     * Strip trailing data from a text record based on extra data flags.
     *
     * Each set flag bit (except bit 0) means there's a variable-length trailing
     * entry using forward-encoded size. Bit 0 = multibyte trailing data where the
     * last byte's lower 2 bits + 1 = number of trailing bytes to remove.
     */
    private fun stripTrailingData(record: ByteArray, flags: Int): ByteArray {
        if (flags == 0 || record.isEmpty()) return record
        var size = record.size

        // Process bits 1..15 (variable-length trailing entries)
        var i = 15
        while (i > 0) {
            if (flags and (1 shl i) != 0) {
                val consumed = getVariableLengthSize(record, size)
                size -= consumed
                if (size <= 0) return ByteArray(0)
            }
            i--
        }

        // Bit 0: multibyte trailing data
        if (flags and 1 != 0 && size > 0) {
            val trailingBytes = (record[size - 1].toInt() and 0x03) + 1
            size -= trailingBytes
            if (size <= 0) return ByteArray(0)
        }

        return if (size == record.size) record else record.copyOf(size)
    }

    /**
     * Read a variable-length size value from the END of a byte array.
     * Returns the total number of bytes consumed by this trailing entry.
     */
    private fun getVariableLengthSize(data: ByteArray, size: Int): Int {
        var result = 0
        var bitCount = 0
        var pos = size - 1
        while (pos >= 0) {
            val b = data[pos].toInt() and 0xFF
            result = result or ((b and 0x7F) shl bitCount)
            bitCount += 7
            pos--
            if (b and 0x80 != 0 || bitCount >= 28) break
        }
        return result
    }

    /**
     * PalmDOC LZ77 decompression.
     */
    private fun decompressPalmDoc(compressed: ByteArray): ByteArray {
        val output = java.io.ByteArrayOutputStream(compressed.size * 2)
        // Keep decoded bytes accessible for LZ77 back-references
        val decoded = mutableListOf<Byte>()
        var i = 0

        while (i < compressed.size) {
            val c = compressed[i].toInt() and 0xFF
            when {
                c == 0 -> {
                    decoded.add(0)
                    i++
                }
                c in 1..8 -> {
                    for (j in 1..c) {
                        if (i + j < compressed.size) {
                            decoded.add(compressed[i + j])
                        }
                    }
                    i += c + 1
                }
                c in 9..0x7F -> {
                    decoded.add(c.toByte())
                    i++
                }
                c in 0x80..0xBF -> {
                    if (i + 1 >= compressed.size) { i++; continue }
                    val next = compressed[i + 1].toInt() and 0xFF
                    val distance = (((c shl 8) or next) ushr 3) and 0x7FF
                    val length = (next and 0x07) + 3
                    if (distance > 0) {
                        for (j in 0 until length) {
                            val pos = decoded.size - distance
                            decoded.add(if (pos >= 0) decoded[pos] else 0)
                        }
                    }
                    i += 2
                }
                else -> {
                    // 0xC0..0xFF: space + (c XOR 0x80)
                    decoded.add(' '.code.toByte())
                    decoded.add((c xor 0x80).toByte())
                    i++
                }
            }
        }
        val result = ByteArray(decoded.size)
        for (idx in decoded.indices) {
            result[idx] = decoded[idx]
        }
        return result
    }

    private fun readUInt16(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)

    private fun readUInt32(data: ByteArray, offset: Int): Long =
        ((data[offset].toInt() and 0xFF).toLong() shl 24) or
            ((data[offset + 1].toInt() and 0xFF).toLong() shl 16) or
            ((data[offset + 2].toInt() and 0xFF).toLong() shl 8) or
            (data[offset + 3].toInt() and 0xFF).toLong()

    private fun readCString(data: ByteArray, offset: Int, maxLen: Int): String {
        val end = minOf(offset + maxLen, data.size)
        var terminator = end
        for (i in offset until end) {
            if (data[i].toInt() == 0) { terminator = i; break }
        }
        return String(data, offset, terminator - offset, Charsets.US_ASCII).trim()
    }
}

class MobiParseException(message: String) : Exception(message)
