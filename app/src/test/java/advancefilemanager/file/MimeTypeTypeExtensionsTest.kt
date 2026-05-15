/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.file

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class MimeTypeTypeExtensionsTest {

    @Test
    fun isApk() {
        assertThat("application/vnd.android.package-archive".asMimeType().isApk).isTrue()
        assertThat("image/png".asMimeType().isApk).isFalse()
    }

    @Test
    fun isSupportedArchive_zip() {
        assertThat("application/zip".asMimeType().isSupportedArchive).isTrue()
    }

    @Test
    fun isSupportedArchive_tar() {
        assertThat("application/x-tar".asMimeType().isSupportedArchive).isTrue()
    }

    @Test
    fun isSupportedArchive_7z() {
        assertThat("application/x-7z-compressed".asMimeType().isSupportedArchive).isTrue()
    }

    @Test
    fun isSupportedArchive_rar() {
        assertThat("application/rar".asMimeType().isSupportedArchive).isTrue()
        assertThat("application/vnd.rar".asMimeType().isSupportedArchive).isTrue()
    }

    @Test
    fun isSupportedArchive_nonArchive() {
        assertThat("image/png".asMimeType().isSupportedArchive).isFalse()
        assertThat("text/plain".asMimeType().isSupportedArchive).isFalse()
    }

    @Test
    fun isPdf() {
        assertThat("application/pdf".asMimeType().isPdf).isTrue()
        assertThat("text/plain".asMimeType().isPdf).isFalse()
    }

    @Test
    fun isEpub() {
        assertThat("application/epub+zip".asMimeType().isEpub).isTrue()
        assertThat("application/pdf".asMimeType().isEpub).isFalse()
    }

    @Test
    fun isMobi() {
        assertThat("application/x-mobipocket-ebook".asMimeType().isMobi).isTrue()
        assertThat("application/vnd.amazon.ebook".asMimeType().isMobi).isTrue()
        assertThat("application/vnd.amazon.mobi8-ebook".asMimeType().isMobi).isTrue()
        assertThat("application/pdf".asMimeType().isMobi).isFalse()
    }

    @Test
    fun isEbook() {
        assertThat("application/epub+zip".asMimeType().isEbook).isTrue()
        assertThat("application/x-mobipocket-ebook".asMimeType().isEbook).isTrue()
        assertThat("text/plain".asMimeType().isEbook).isFalse()
    }

    @Test
    fun isCsv() {
        assertThat("text/csv".asMimeType().isCsv).isTrue()
        assertThat("text/comma-separated-values".asMimeType().isCsv).isTrue()
        assertThat("text/plain".asMimeType().isCsv).isFalse()
    }

    @Test
    fun isMedia() {
        assertThat("video/mp4".asMimeType().isMedia).isTrue()
        assertThat("audio/mpeg".asMimeType().isMedia).isTrue()
        assertThat("image/png".asMimeType().isMedia).isFalse()
    }
}
