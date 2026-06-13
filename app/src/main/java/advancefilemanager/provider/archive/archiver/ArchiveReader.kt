/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.archive.archiver

import androidx.preference.PreferenceManager
import java8.nio.channels.SeekableByteChannel
import java8.nio.charset.StandardCharsets
import java8.nio.file.Path
import com.advancefilemanager.R
import com.advancefilemanager.provider.common.DelegateForceableSeekableByteChannel
import com.advancefilemanager.provider.common.DelegateInputStream
import com.advancefilemanager.provider.common.DelegateNonForceableSeekableByteChannel
import com.advancefilemanager.provider.common.ForceableChannel
import com.advancefilemanager.provider.common.PosixFileMode
import com.advancefilemanager.provider.common.PosixFileType
import com.advancefilemanager.provider.common.newByteChannel
import com.advancefilemanager.provider.common.newInputStream
import com.advancefilemanager.provider.root.isRunningAsRoot
import com.advancefilemanager.provider.root.rootContext
import com.advancefilemanager.settings.Settings
import com.advancefilemanager.util.valueCompat
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

object ArchiveReader {
    @Throws(IOException::class)
    fun readEntries(
        file: Path,
        passwords: List<String>,
        rootPath: Path
    ): Pair<Map<Path, ReadArchive.Entry>, Map<Path, List<Path>>> {
        val entries = mutableMapOf<Path, ReadArchive.Entry>()
        val rawEntries = readEntries(file, passwords)
        for (entry in rawEntries) {
            var path = rootPath.resolve(entry.name)
            // Normalize an absolute path to prevent path traversal attack.
            if (!path.isAbsolute) {
                throw AssertionError("Path must be absolute: $path")
            }
            if (path.nameCount > 0) {
                path = path.normalize()
                if (path.nameCount == 0) {
                    // Don't allow a path to become the root path only after normalization.
                    continue
                }
            } else {
                if (!entry.isDirectory) {
                    // Ignore a root path that's not a directory
                    continue
                }
            }
            entries.getOrPut(path) { entry }
        }
        entries.getOrPut(rootPath) { createDirectoryEntry("") }
        val tree = mutableMapOf<Path, MutableList<Path>>()
        tree[rootPath] = mutableListOf()
        val paths = entries.keys.toList()
        for (path in paths) {
            var path = path
            while (true) {
                val parentPath = path.parent ?: break
                val entry = entries[path]!!
                if (entry.isDirectory) {
                    tree.getOrPut(path) { mutableListOf() }
                }
                tree.getOrPut(parentPath) { mutableListOf() }.add(path)
                if (entries.containsKey(parentPath)) {
                    break
                }
                entries[parentPath] = createDirectoryEntry(parentPath.toString())
                path = parentPath
            }
        }
        return entries to tree
    }

    private fun createDirectoryEntry(name: String): ReadArchive.Entry {
        require(!name.endsWith("/")) { "name $name should not end with a slash" }
        return ReadArchive.Entry(
            name, false, null, null, null, PosixFileType.DIRECTORY, 0, null, null,
            PosixFileMode.DIRECTORY_DEFAULT, null
        )
    }

    @Throws(IOException::class)
    private fun readEntries(file: Path, passwords: List<String>): List<ReadArchive.Entry> {
        val charset = archiveFileNameCharset
        val (archive, closeable) = openArchive(file, passwords)
        return closeable.use {
            buildList {
                while (true) {
                    this += archive.readEntry(charset) ?: break
                }
            }
        }
    }

    @Throws(IOException::class)
    fun newInputStream(file: Path, passwords: List<String>, entry: ReadArchive.Entry): InputStream? {
        val charset = archiveFileNameCharset
        val (archive, closeable) = openArchive(file, passwords)
        var successful = false
        return try {
            while (true) {
                val currentEntry = archive.readEntry(charset) ?: break
                if (currentEntry.name != entry.name) {
                    continue
                }
                successful = true
                break
            }
            if (successful) {
                CloseableInputStream(archive.newDataInputStream(), closeable)
            } else {
                null
            }
        } finally {
            if (!successful) {
                closeable.close()
            }
        }
    }

    @Throws(IOException::class)
    private fun openArchive(
        file: Path,
        passwords: List<String>
    ): Pair<ReadArchive, Closeable> {
        // Use the path-based constructor to directly open the file via
        // readOpenFileName, bypassing JNI callbacks that can trigger
        // a libarchive bug where archive_read_open1() calls abort().
        var successful = false
        try {
            val archive = ReadArchive(file, passwords)
            successful = true
            // Return a closeable that only closes the archive (no external resource).
            return archive to archive
        } finally {
            // No external resource to close for path-based constructor.
        }
    }

    // size() may be called repeatedly for ZIP and 7Z, so make it cached to improve performance.
    private fun CacheSizeSeekableByteChannel(channel: SeekableByteChannel): SeekableByteChannel =
        if (channel is ForceableChannel) {
            CacheSizeForceableSeekableByteChannel(channel)
        } else {
            CacheSizeNonForceableSeekableByteChannel(channel)
        }

    private class CacheSizeNonForceableSeekableByteChannel(
        channel: SeekableByteChannel
    ) : DelegateNonForceableSeekableByteChannel(channel) {
        private val size: Long by lazy { super.size() }

        override fun size(): Long = size
    }

    private class CacheSizeForceableSeekableByteChannel(
        channel: SeekableByteChannel
    ) : DelegateForceableSeekableByteChannel(channel) {
        private val size: Long by lazy { super.size() }

        override fun size(): Long = size
    }

    private val archiveFileNameCharset: Charset
        get() =
            if (isRunningAsRoot) {
                try {
                    val sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(rootContext)
                    val key = rootContext.getString(R.string.pref_key_archive_file_name_encoding)
                    val defaultValue = rootContext.getString(
                        R.string.pref_default_value_archive_file_name_encoding
                    )
                    Charset.forName(sharedPreferences.getString(key, defaultValue)!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                    StandardCharsets.UTF_8
                }
            } else {
                Charset.forName(Settings.ARCHIVE_FILE_NAME_ENCODING.valueCompat)
            }

    private class ArchiveCloseable(
        private val archive: ReadArchive,
        private val closeable: Closeable
    ) : Closeable {
        override fun close() {
            @Suppress("ConvertTryFinallyToUseCall")
            try {
                archive.close()
            } finally {
                closeable.close()
            }
        }
    }

    private class CloseableInputStream(
        inputStream: InputStream,
        private val closeable: Closeable
    ) : DelegateInputStream(inputStream) {
        @Throws(IOException::class)
        override fun close() {
            super.close()

            closeable.close()
        }
    }
}
