/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.file

import com.google.common.truth.Truth.assertThat
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileTime
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Instant

class BasicFileAttributesExtensionsTest {

    @Test
    fun fileSize_returnsCorrectFileSize() {
        val attrs = mock(BasicFileAttributes::class.java)
        `when`(attrs.size()).thenReturn(1024L)
        val fileSize = attrs.fileSize
        assertThat(fileSize).isEqualTo(1024L.asFileSize())
    }

    @Test
    fun fileSize_zero() {
        val attrs = mock(BasicFileAttributes::class.java)
        `when`(attrs.size()).thenReturn(0L)
        assertThat(attrs.fileSize).isEqualTo(0L.asFileSize())
    }

    @Test
    fun lastModifiedInstant_convertsFileTime() {
        val now = Instant.now()
        val fileTime = FileTime.from(now)
        val attrs = mock(BasicFileAttributes::class.java)
        `when`(attrs.lastModifiedTime()).thenReturn(fileTime)
        assertThat(attrs.lastModifiedInstant).isEqualTo(now)
    }

    @Test
    fun lastModifiedInstant_epoch() {
        val epoch = Instant.EPOCH
        val attrs = mock(BasicFileAttributes::class.java)
        `when`(attrs.lastModifiedTime()).thenReturn(FileTime.from(epoch))
        assertThat(attrs.lastModifiedInstant).isEqualTo(epoch)
    }
}
