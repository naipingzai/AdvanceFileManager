/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.file

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class JavaFileTest {

    @Test
    fun isDirectory_rootExists() {
        // Windows root C:\ should exist and be a directory
        val windowsRoot = System.getenv("SystemRoot") ?: "C:\\Windows"
        assertThat(JavaFile.isDirectory(windowsRoot)).isTrue()
    }

    @Test
    fun isDirectory_nonExistentPath() {
        assertThat(JavaFile.isDirectory("/nonexistent_path_12345")).isFalse()
    }

    @Test
    fun isDirectory_filePath() {
        val tmpFile = java.io.File.createTempFile("test_", ".txt")
        try {
            assertThat(JavaFile.isDirectory(tmpFile.absolutePath)).isFalse()
        } finally {
            tmpFile.delete()
        }
    }

    @Test
    fun getTotalSpace_tempDir() {
        val tmpDir = System.getProperty("java.io.tmpdir")!!
        assertThat(JavaFile.getTotalSpace(tmpDir)).isGreaterThan(0)
    }

    @Test
    fun getFreeSpace_tempDir() {
        val tmpDir = System.getProperty("java.io.tmpdir")!!
        assertThat(JavaFile.getFreeSpace(tmpDir)).isGreaterThan(0)
    }

    @Test
    fun getFreeSpace_lessThanOrEqualTotalSpace() {
        val tmpDir = System.getProperty("java.io.tmpdir")!!
        assertThat(JavaFile.getFreeSpace(tmpDir))
            .isAtMost(JavaFile.getTotalSpace(tmpDir))
    }
}
