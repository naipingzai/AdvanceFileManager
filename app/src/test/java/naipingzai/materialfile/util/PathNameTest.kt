/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PathNameTest {

    // --- PathName tests ---

    @Test
    fun pathName_fileName_simple() {
        assertThat("/home/user/file.txt".asPathName().fileName).isEqualTo("file.txt")
    }

    @Test
    fun pathName_fileName_rootFile() {
        assertThat("/file.txt".asPathName().fileName).isEqualTo("file.txt")
    }

    @Test
    fun pathName_fileName_rootOnly() {
        assertThat("/".asPathName().fileName).isNull()
    }

    @Test
    fun pathName_directoryName_simple() {
        assertThat("/home/user/file.txt".asPathName().directoryName).isEqualTo("/home/user")
    }

    @Test
    fun pathName_directoryName_rootFile() {
        assertThat("/file.txt".asPathName().directoryName).isEqualTo("")
    }

    @Test
    fun pathName_directoryName_noSlash() {
        assertThat("file.txt".asPathName().directoryName).isNull()
    }

    @Test
    fun asPathName_valid() {
        assertThat("/some/path".asPathName().value).isEqualTo("/some/path")
    }

    @Test
    fun asPathNameOrNull_valid() {
        assertThat("/path".asPathNameOrNull()).isNotNull()
    }

    @Test
    fun asPathNameOrNull_empty() {
        assertThat("".asPathNameOrNull()).isNull()
    }

    @Test
    fun asPathNameOrNull_nullChar() {
        assertThat("path\u0000name".asPathNameOrNull()).isNull()
    }

    // --- FileName tests ---

    @Test
    fun fileName_singleExtension_simple() {
        assertThat("file.txt".asFileName().singleExtension).isEqualTo("txt")
    }

    @Test
    fun fileName_singleExtension_noExtension() {
        assertThat("file".asFileName().singleExtension).isEmpty()
    }

    @Test
    fun fileName_singleExtension_dotfile() {
        assertThat(".hidden".asFileName().singleExtension).isEqualTo("hidden")
    }

    @Test
    fun fileName_extensions_single() {
        assertThat("file.txt".asFileName().extensions).isEqualTo("txt")
    }

    @Test
    fun fileName_extensions_double_tarGz() {
        assertThat("archive.tar.gz".asFileName().extensions).isEqualTo("tar.gz")
    }

    @Test
    fun fileName_extensions_double_tarBz2() {
        assertThat("archive.tar.bz2".asFileName().extensions).isEqualTo("tar.bz2")
    }

    @Test
    fun fileName_extensions_double_tarXz() {
        assertThat("archive.tar.xz".asFileName().extensions).isEqualTo("tar.xz")
    }

    @Test
    fun fileName_baseName_simple() {
        assertThat("file.txt".asFileName().baseName).isEqualTo("file")
    }

    @Test
    fun fileName_baseName_doubleExt() {
        assertThat("archive.tar.gz".asFileName().baseName).isEqualTo("archive")
    }

    @Test
    fun fileName_baseName_noExtension() {
        assertThat("file".asFileName().baseName).isEqualTo("file")
    }

    @Test
    fun asFileName_valid() {
        assertThat("file.txt".asFileName().value).isEqualTo("file.txt")
    }

    @Test
    fun asFileNameOrNull_valid() {
        assertThat("file.txt".asFileNameOrNull()).isNotNull()
    }

    @Test
    fun asFileNameOrNull_empty() {
        assertThat("".asFileNameOrNull()).isNull()
    }

    @Test
    fun asFileNameOrNull_containsSlash() {
        assertThat("path/file".asFileNameOrNull()).isNull()
    }

    @Test
    fun asFileNameOrNull_containsNullChar() {
        assertThat("file\u0000.txt".asFileNameOrNull()).isNull()
    }
}
