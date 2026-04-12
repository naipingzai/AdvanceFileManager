/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.file

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class MimeTypeTest {

    @Test
    fun type_simple() {
        val mime = "image/png".asMimeType()
        assertThat(mime.type).isEqualTo("image")
    }

    @Test
    fun subtype_simple() {
        val mime = "image/png".asMimeType()
        assertThat(mime.subtype).isEqualTo("png")
    }

    @Test
    fun subtype_withParameters() {
        val mime = "text/plain;charset=UTF-8".asMimeType()
        assertThat(mime.subtype).isEqualTo("plain")
    }

    @Test
    fun suffix_present() {
        val mime = "image/svg+xml".asMimeType()
        assertThat(mime.suffix).isEqualTo("xml")
    }

    @Test
    fun suffix_absent() {
        val mime = "image/png".asMimeType()
        assertThat(mime.suffix).isNull()
    }

    @Test
    fun parameters_present() {
        val mime = "text/plain;charset=UTF-8".asMimeType()
        assertThat(mime.parameters).isEqualTo("charset=UTF-8")
    }

    @Test
    fun parameters_absent() {
        val mime = "image/png".asMimeType()
        assertThat(mime.parameters).isNull()
    }

    @Test
    fun match_exactMatch() {
        val pattern = "image/png".asMimeType()
        val target = "image/png".asMimeType()
        assertThat(pattern.match(target)).isTrue()
    }

    @Test
    fun match_wildcardType() {
        val pattern = "*/*".asMimeType()
        val target = "image/png".asMimeType()
        assertThat(pattern.match(target)).isTrue()
    }

    @Test
    fun match_wildcardSubtype() {
        val pattern = "image/*".asMimeType()
        val target = "image/png".asMimeType()
        assertThat(pattern.match(target)).isTrue()
    }

    @Test
    fun match_noMatch() {
        val pattern = "audio/*".asMimeType()
        val target = "image/png".asMimeType()
        assertThat(pattern.match(target)).isFalse()
    }

    @Test
    fun of_withoutParameters() {
        val mime = MimeType.of("image", "png", null)
        assertThat(mime.value).isEqualTo("image/png")
    }

    @Test
    fun of_withParameters() {
        val mime = MimeType.of("text", "plain", "charset=UTF-8")
        assertThat(mime.value).isEqualTo("text/plain;charset=UTF-8")
    }

    @Test
    fun asMimeTypeOrNull_valid() {
        assertThat("image/png".asMimeTypeOrNull()).isNotNull()
    }

    @Test
    fun asMimeTypeOrNull_invalid_noSlash() {
        assertThat("image".asMimeTypeOrNull()).isNull()
    }

    @Test
    fun asMimeTypeOrNull_invalid_empty() {
        assertThat("".asMimeTypeOrNull()).isNull()
    }

    @Test
    fun asMimeTypeOrNull_invalid_slashOnly() {
        assertThat("/".asMimeTypeOrNull()).isNull()
    }

    @Test
    fun asMimeTypeOrNull_invalid_slashAtStart() {
        assertThat("/png".asMimeTypeOrNull()).isNull()
    }

    @Test
    fun companionConstants() {
        assertThat(MimeType.ANY.value).isEqualTo("*/*")
        assertThat(MimeType.APK.value).isEqualTo("application/vnd.android.package-archive")
        assertThat(MimeType.IMAGE_ANY.value).isEqualTo("image/*")
        assertThat(MimeType.VIDEO_ANY.value).isEqualTo("video/*")
        assertThat(MimeType.AUDIO_ANY.value).isEqualTo("audio/*")
        assertThat(MimeType.PDF.value).isEqualTo("application/pdf")
        assertThat(MimeType.TEXT_PLAIN.value).isEqualTo("text/plain")
        assertThat(MimeType.GENERIC.value).isEqualTo("application/octet-stream")
    }
}
