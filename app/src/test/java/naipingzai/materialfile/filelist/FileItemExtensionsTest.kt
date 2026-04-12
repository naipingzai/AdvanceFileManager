/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.filelist

import com.google.common.truth.Truth.assertThat
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import naipingzai.materialfile.file.FileItem
import naipingzai.materialfile.file.MimeType
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.text.Collator

class FileItemExtensionsTest {

    private fun createFileItem(
        fileName: String,
        isDirectory: Boolean,
        mimeType: MimeType = MimeType.GENERIC
    ): FileItem {
        val path = mock(Path::class.java)
        val fileNamePath = mock(Path::class.java)
        `when`(fileNamePath.toString()).thenReturn(fileName)
        `when`(path.fileName).thenReturn(fileNamePath)
        val attrs = mock(BasicFileAttributes::class.java)
        `when`(attrs.isDirectory).thenReturn(isDirectory)
        `when`(attrs.isSymbolicLink).thenReturn(false)
        val collationKey = Collator.getInstance().getCollationKey(fileName)
        return FileItem(path, collationKey, attrs, null, null, false, mimeType)
    }

    // ── name ──

    @Test
    fun name_returnsFilename() {
        val item = createFileItem("test.txt", false)
        assertThat(item.name).isEqualTo("test.txt")
    }

    // ── baseName ──

    @Test
    fun baseName_fileWithExtension() {
        val item = createFileItem("document.pdf", false)
        assertThat(item.baseName).isEqualTo("document")
    }

    @Test
    fun baseName_fileWithoutExtension() {
        val item = createFileItem("README", false)
        assertThat(item.baseName).isEqualTo("README")
    }

    @Test
    fun baseName_directory_returnsFullName() {
        val item = createFileItem("my.folder", true)
        assertThat(item.baseName).isEqualTo("my.folder")
    }

    // ── extension ──

    @Test
    fun extension_fileWithExtension() {
        val item = createFileItem("photo.jpg", false)
        assertThat(item.extension).isEqualTo("jpg")
    }

    @Test
    fun extension_fileWithDoubleExtension() {
        val item = createFileItem("archive.tar.gz", false)
        assertThat(item.extension).isEqualTo("tar.gz")
    }

    @Test
    fun extension_directory_returnsEmpty() {
        val item = createFileItem("folder", true)
        assertThat(item.extension).isEmpty()
    }

    // ── appDirectoryPackageName ──

    @Test
    fun appDirectoryPackageName_validPackage() {
        val item = createFileItem("com.example.app", true)
        assertThat(item.appDirectoryPackageName).isEqualTo("com.example.app")
    }

    @Test
    fun appDirectoryPackageName_packageWithSuffix() {
        val item = createFileItem("com.example.app-MjA0NTQwMDQ2Mg==", true)
        assertThat(item.appDirectoryPackageName).isEqualTo("com.example.app")
    }

    @Test
    fun appDirectoryPackageName_notDirectory() {
        val item = createFileItem("com.example.app", false)
        assertThat(item.appDirectoryPackageName).isNull()
    }

    @Test
    fun appDirectoryPackageName_invalidPackage() {
        val item = createFileItem("not-a-package", true)
        assertThat(item.appDirectoryPackageName).isNull()
    }

    @Test
    fun appDirectoryPackageName_singleComponent() {
        // Package names must have at least 2 components
        val item = createFileItem("example", true)
        assertThat(item.appDirectoryPackageName).isNull()
    }

    @Test
    fun appDirectoryPackageName_complexPackage() {
        val item = createFileItem("com.android.providers.media", true)
        assertThat(item.appDirectoryPackageName).isEqualTo("com.android.providers.media")
    }

    @Test
    fun appDirectoryPackageName_underscoreInComponent() {
        val item = createFileItem("com.example.my_app", true)
        assertThat(item.appDirectoryPackageName).isEqualTo("com.example.my_app")
    }

    @Test
    fun appDirectoryPackageName_numericAfterFirst() {
        val item = createFileItem("com.example.app2", true)
        assertThat(item.appDirectoryPackageName).isEqualTo("com.example.app2")
    }

    @Test
    fun appDirectoryPackageName_componentStartsWithDigit() {
        // Each component must start with letter
        val item = createFileItem("com.123.app", true)
        assertThat(item.appDirectoryPackageName).isNull()
    }
}
