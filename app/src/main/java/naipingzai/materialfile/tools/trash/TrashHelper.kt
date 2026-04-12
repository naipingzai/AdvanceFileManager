/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.trash

import android.os.Environment
import java8.nio.file.Path
import java8.nio.file.Paths
import java8.nio.file.attribute.BasicFileAttributes
import naipingzai.materialfile.provider.common.copyTo
import naipingzai.materialfile.provider.common.createDirectories
import naipingzai.materialfile.provider.common.delete
import naipingzai.materialfile.provider.common.exists
import naipingzai.materialfile.provider.common.moveTo
import naipingzai.materialfile.provider.common.newDirectoryStream
import naipingzai.materialfile.provider.common.readAttributes
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TrashHelper {

    fun getTrashDir(): Path {
        val dir = Paths.get(Environment.getExternalStorageDirectory().absolutePath).resolve(".trash")
        if (!dir.exists()) {
            dir.createDirectories()
        }
        return dir
    }

    fun getTrashMetaDir(): Path {
        val dir = getTrashDir().resolve(".meta")
        if (!dir.exists()) {
            dir.createDirectories()
        }
        return dir
    }

    /**
     * Move a file or directory to trash.
     * Returns true if successfully moved, false if failed (caller should fall back to permanent deletion).
     */
    fun moveToTrash(path: Path): Boolean {
        return try {
            val trashDir = getTrashDir()
            val metaDir = getTrashMetaDir()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
            val trashName = "${timestamp}_${path.fileName}"
            val trashPath = trashDir.resolve(trashName)
            val metaPath = metaDir.resolve("$trashName.meta")

            var movedSuccessfully = false
            try {
                path.moveTo(trashPath)
                movedSuccessfully = true
            } catch (_: IOException) {
                // moveTo failed (cross-filesystem, permissions, etc.)
            }

            if (movedSuccessfully) {
                // File already moved; write meta separately so a meta failure
                // doesn't trigger the copy+delete fallback (which would delete the
                // successfully-moved file since the source no longer exists).
                try {
                    metaPath.toFile().writeText(path.toString())
                } catch (_: IOException) {
                    // Meta write failed — file is in trash but without origin info.
                    // This is acceptable; the file won't be lost.
                }
                true
            } else {
                // moveTo failed — try copy + delete as fallback
                try {
                    copyRecursively(path, trashPath)
                    metaPath.toFile().writeText(path.toString())
                    deleteRecursively(path)
                    true
                } catch (e2: Exception) {
                    // Clean up failed copy
                    try { deleteRecursively(trashPath) } catch (_: Exception) {}
                    try { metaPath.delete() } catch (_: Exception) {}
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    fun moveToTrash(file: File): Boolean = moveToTrash(Paths.get(file.absolutePath))

    /**
     * Move a file specified by path string to trash.
     */
    fun moveToTrash(pathStr: String): Boolean = moveToTrash(Paths.get(pathStr))

    /**
     * Check if a path is on local storage (can be trashed).
     */
    fun isTrashable(path: Path): Boolean {
        return try {
            path.exists() && path.toString().startsWith("/storage/")
        } catch (e: Exception) {
            false
        }
    }

    fun isTrashable(file: File): Boolean = isTrashable(Paths.get(file.absolutePath))

    private fun copyRecursively(source: Path, target: Path) {
        val attrs = source.readAttributes(BasicFileAttributes::class.java)
        if (attrs.isDirectory) {
            target.createDirectories()
            source.newDirectoryStream().use { stream ->
                for (entry in stream) {
                    copyRecursively(entry, target.resolve(entry.fileName))
                }
            }
        } else {
            source.copyTo(target)
        }
    }

    fun deleteRecursively(path: Path) {
        val attrs = path.readAttributes(BasicFileAttributes::class.java)
        if (attrs.isDirectory) {
            path.newDirectoryStream().use { stream ->
                for (entry in stream) {
                    deleteRecursively(entry)
                }
            }
        }
        path.delete()
    }

    /**
     * Best-effort deletion: swallows exceptions so callers that don't care
     * about partial failures (e.g. emptying trash) can use a simpler API.
     */
    fun deleteRecursivelySafe(path: Path) {
        try {
            deleteRecursively(path)
        } catch (_: IOException) {}
    }

    fun formatFileSize(size: Long): String {
        if (size < 1024) return "$size B"
        val kb = size / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format(Locale.US, "%.2f GB", gb)
    }
}
