/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Instrumented tests for file system operations.
 * Tests basic file I/O that the app depends on.
 */
@RunWith(AndroidJUnit4::class)
class FileSystemTest {

    private lateinit var context: Context
    private lateinit var testDir: File

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        testDir = File(context.cacheDir, "test_filesystem")
        testDir.deleteRecursively()
        testDir.mkdirs()
    }

    @Test
    fun createFile_andRead() {
        val file = File(testDir, "test.txt")
        file.writeText("Hello, World!")
        assertThat(file.exists()).isTrue()
        assertThat(file.readText()).isEqualTo("Hello, World!")
    }

    @Test
    fun createDirectory_andList() {
        val dir = File(testDir, "subdir")
        dir.mkdirs()
        File(dir, "a.txt").createNewFile()
        File(dir, "b.txt").createNewFile()
        val files = dir.listFiles()?.map { it.name }?.sorted()
        assertThat(files).containsExactly("a.txt", "b.txt").inOrder()
    }

    @Test
    fun deleteFile() {
        val file = File(testDir, "to_delete.txt")
        file.createNewFile()
        assertThat(file.exists()).isTrue()
        file.delete()
        assertThat(file.exists()).isFalse()
    }

    @Test
    fun renameFile() {
        val src = File(testDir, "original.txt")
        val dst = File(testDir, "renamed.txt")
        src.writeText("content")
        src.renameTo(dst)
        assertThat(dst.exists()).isTrue()
        assertThat(dst.readText()).isEqualTo("content")
        assertThat(src.exists()).isFalse()
    }

    @Test
    fun copyFile() {
        val src = File(testDir, "source.txt")
        val dst = File(testDir, "copy.txt")
        src.writeText("copy me")
        src.copyTo(dst)
        assertThat(dst.readText()).isEqualTo("copy me")
        assertThat(src.readText()).isEqualTo("copy me")
    }

    @Test
    fun largeFile_writeAndReadSize() {
        val file = File(testDir, "large.bin")
        val data = ByteArray(1024 * 1024) { (it % 256).toByte() }
        FileOutputStream(file).use { it.write(data) }
        assertThat(file.length()).isEqualTo(1024 * 1024L)
    }

    @Test
    fun chineseFileName() {
        val file = File(testDir, "中文文件名.txt")
        file.writeText("中文内容")
        assertThat(file.exists()).isTrue()
        assertThat(file.readText()).isEqualTo("中文内容")
    }

    @Test
    fun specialCharacterFileName() {
        val file = File(testDir, "file (1) [test].txt")
        file.writeText("special")
        assertThat(file.exists()).isTrue()
        assertThat(file.readText()).isEqualTo("special")
    }

    @Test
    fun nestedDirectories() {
        val deep = File(testDir, "a/b/c/d")
        deep.mkdirs()
        assertThat(deep.isDirectory).isTrue()
        val file = File(deep, "deep.txt")
        file.writeText("deep content")
        assertThat(file.readText()).isEqualTo("deep content")
    }

    @Test
    fun emptyFile() {
        val file = File(testDir, "empty.txt")
        file.createNewFile()
        assertThat(file.length()).isEqualTo(0L)
        assertThat(file.readText()).isEmpty()
    }
}
