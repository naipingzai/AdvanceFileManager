/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.navigation

import com.google.common.truth.Truth.assertThat
import java8.nio.file.Path
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class BookmarkDirectoryTest {

    private fun mockPath(name: String): Path {
        val path = mock(Path::class.java)
        val fileNamePath = mock(Path::class.java)
        `when`(fileNamePath.toString()).thenReturn(name)
        `when`(path.fileName).thenReturn(fileNamePath)
        return path
    }

    @Test
    fun constructor_generatesRandomId() {
        val path = mockPath("Downloads")
        val dir1 = BookmarkDirectory(null, path)
        val dir2 = BookmarkDirectory(null, path)
        // Random IDs should (overwhelmingly likely) be different
        assertThat(dir1.id).isNotEqualTo(dir2.id)
    }

    @Test
    fun name_returnsCustomName_whenSet() {
        val path = mockPath("Downloads")
        val dir = BookmarkDirectory(123L, "My Folder", path)
        assertThat(dir.name).isEqualTo("My Folder")
    }

    @Test
    fun name_returnsDefaultName_whenCustomNameNull() {
        val path = mockPath("Downloads")
        val dir = BookmarkDirectory(123L, null, path)
        assertThat(dir.name).isEqualTo("Downloads")
    }

    @Test
    fun name_returnsDefaultName_whenCustomNameEmpty() {
        val path = mockPath("Pictures")
        val dir = BookmarkDirectory(123L, "", path)
        assertThat(dir.name).isEqualTo("Pictures")
    }

    @Test
    fun defaultName_returnsPathFileName() {
        val path = mockPath("Music")
        val dir = BookmarkDirectory(123L, "Custom", path)
        assertThat(dir.defaultName).isEqualTo("Music")
    }

    @Test
    fun customName_stored() {
        val path = mockPath("test")
        val dir = BookmarkDirectory(1L, "custom", path)
        assertThat(dir.customName).isEqualTo("custom")
    }

    @Test
    fun id_stored() {
        val path = mockPath("test")
        val dir = BookmarkDirectory(42L, null, path)
        assertThat(dir.id).isEqualTo(42L)
    }

    @Test
    fun path_stored() {
        val path = mockPath("test")
        val dir = BookmarkDirectory(1L, null, path)
        assertThat(dir.path).isSameInstanceAs(path)
    }

    @Test
    fun equality_sameValues() {
        val path = mockPath("test")
        val dir1 = BookmarkDirectory(1L, "name", path)
        val dir2 = BookmarkDirectory(1L, "name", path)
        assertThat(dir1).isEqualTo(dir2)
    }

    @Test
    fun inequality_differentId() {
        val path = mockPath("test")
        val dir1 = BookmarkDirectory(1L, "name", path)
        val dir2 = BookmarkDirectory(2L, "name", path)
        assertThat(dir1).isNotEqualTo(dir2)
    }
}
