/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileTypeUtilsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    // ── isImageFile ──

    @Test
    fun isImageFile_jpg() {
        assertThat(FileTypeUtils.isImageFile("photo.jpg")).isTrue()
    }

    @Test
    fun isImageFile_jpeg() {
        assertThat(FileTypeUtils.isImageFile("photo.JPEG")).isTrue()
    }

    @Test
    fun isImageFile_png() {
        assertThat(FileTypeUtils.isImageFile("image.png")).isTrue()
    }

    @Test
    fun isImageFile_bmp() {
        assertThat(FileTypeUtils.isImageFile("wall.bmp")).isTrue()
    }

    @Test
    fun isImageFile_webp() {
        assertThat(FileTypeUtils.isImageFile("anim.webp")).isTrue()
    }

    @Test
    fun isImageFile_tiff() {
        assertThat(FileTypeUtils.isImageFile("scan.tiff")).isTrue()
        assertThat(FileTypeUtils.isImageFile("scan.tif")).isTrue()
    }

    @Test
    fun isImageFile_gif_notSupported() {
        // GIF is not in FFMPEG_IMAGE_EXTENSIONS
        assertThat(FileTypeUtils.isImageFile("anim.gif")).isFalse()
    }

    @Test
    fun isImageFile_video_notImage() {
        assertThat(FileTypeUtils.isImageFile("movie.mp4")).isFalse()
    }

    @Test
    fun isImageFile_noExtension() {
        assertThat(FileTypeUtils.isImageFile("README")).isFalse()
    }

    // ── matchesFileType ──

    @Test
    fun matchesFileType_all_matchesAnything() {
        assertThat(FileTypeUtils.matchesFileType("any.xyz", FileTypeUtils.FileTypeFilter.ALL)).isTrue()
    }

    @Test
    fun matchesFileType_image() {
        assertThat(FileTypeUtils.matchesFileType("photo.jpg", FileTypeUtils.FileTypeFilter.IMAGE)).isTrue()
        assertThat(FileTypeUtils.matchesFileType("photo.heic", FileTypeUtils.FileTypeFilter.IMAGE)).isTrue()
        assertThat(FileTypeUtils.matchesFileType("video.mp4", FileTypeUtils.FileTypeFilter.IMAGE)).isFalse()
    }

    @Test
    fun matchesFileType_video() {
        assertThat(FileTypeUtils.matchesFileType("movie.mp4", FileTypeUtils.FileTypeFilter.VIDEO)).isTrue()
        assertThat(FileTypeUtils.matchesFileType("clip.mkv", FileTypeUtils.FileTypeFilter.VIDEO)).isTrue()
        assertThat(FileTypeUtils.matchesFileType("photo.jpg", FileTypeUtils.FileTypeFilter.VIDEO)).isFalse()
    }

    @Test
    fun matchesFileType_audio() {
        assertThat(FileTypeUtils.matchesFileType("song.mp3", FileTypeUtils.FileTypeFilter.AUDIO)).isTrue()
        assertThat(FileTypeUtils.matchesFileType("track.flac", FileTypeUtils.FileTypeFilter.AUDIO)).isTrue()
        assertThat(FileTypeUtils.matchesFileType("photo.jpg", FileTypeUtils.FileTypeFilter.AUDIO)).isFalse()
    }

    @Test
    fun matchesFileType_document() {
        assertThat(FileTypeUtils.matchesFileType("file.pdf", FileTypeUtils.FileTypeFilter.DOCUMENT)).isTrue()
        assertThat(FileTypeUtils.matchesFileType("sheet.xlsx", FileTypeUtils.FileTypeFilter.DOCUMENT)).isTrue()
        assertThat(FileTypeUtils.matchesFileType("notes.md", FileTypeUtils.FileTypeFilter.DOCUMENT)).isTrue()
        assertThat(FileTypeUtils.matchesFileType("photo.jpg", FileTypeUtils.FileTypeFilter.DOCUMENT)).isFalse()
    }

    @Test
    fun matchesFileType_caseInsensitive() {
        assertThat(FileTypeUtils.matchesFileType("FILE.PDF", FileTypeUtils.FileTypeFilter.DOCUMENT)).isTrue()
        assertThat(FileTypeUtils.matchesFileType("VIDEO.MP4", FileTypeUtils.FileTypeFilter.VIDEO)).isTrue()
    }

    // ── getUniqueFile ──

    @Test
    fun getUniqueFile_noConflict() {
        val dir = tempFolder.root
        val file = FileTypeUtils.getUniqueFile(dir, "test", "txt")
        assertThat(file.name).isEqualTo("test.txt")
    }

    @Test
    fun getUniqueFile_withConflict() {
        val dir = tempFolder.root
        java.io.File(dir, "test.txt").createNewFile()
        val file = FileTypeUtils.getUniqueFile(dir, "test", "txt")
        assertThat(file.name).isEqualTo("test_1.txt")
    }

    @Test
    fun getUniqueFile_multipleConflicts() {
        val dir = tempFolder.root
        java.io.File(dir, "test.txt").createNewFile()
        java.io.File(dir, "test_1.txt").createNewFile()
        val file = FileTypeUtils.getUniqueFile(dir, "test", "txt")
        assertThat(file.name).isEqualTo("test_2.txt")
    }

    @Test
    fun getUniqueFile_emptyExtension() {
        val dir = tempFolder.root
        val file = FileTypeUtils.getUniqueFile(dir, "Makefile", "")
        assertThat(file.name).isEqualTo("Makefile")
    }

    // ── Extension sets ──

    @Test
    fun imageExtensions_containsCommon() {
        assertThat(FileTypeUtils.IMAGE_EXTENSIONS).containsAtLeast("jpg", "png", "gif", "webp")
    }

    @Test
    fun videoExtensions_containsCommon() {
        assertThat(FileTypeUtils.VIDEO_EXTENSIONS).containsAtLeast("mp4", "mkv", "avi", "webm")
    }

    @Test
    fun audioExtensions_containsCommon() {
        assertThat(FileTypeUtils.AUDIO_EXTENSIONS).containsAtLeast("mp3", "flac", "wav", "aac")
    }

    @Test
    fun documentExtensions_containsCommon() {
        assertThat(FileTypeUtils.DOCUMENT_EXTENSIONS).containsAtLeast("pdf", "doc", "xlsx", "csv")
    }

    // ── FileTypeFilter enum ──

    @Test
    fun fileTypeFilter_allValues() {
        val values = FileTypeUtils.FileTypeFilter.values()
        assertThat(values).hasLength(5)
        assertThat(values.map { it.name }).containsExactly(
            "ALL", "IMAGE", "VIDEO", "AUDIO", "DOCUMENT"
        )
    }
}
