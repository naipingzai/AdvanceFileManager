/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.storage

import com.advancefilemanager.settings.Settings
import com.advancefilemanager.util.removeFirst
import com.advancefilemanager.util.valueCompat

object Storages {
    fun addOrReplace(storage: Storage) {
        val storages = Settings.STORAGES.valueCompat.toMutableList().apply {
            val index = indexOfFirst { it.id == storage.id }
            if (index != -1) {
                this[index] = storage
            } else {
                this += storage
            }
        }
        Settings.STORAGES.putValue(storages)
    }

    fun replace(storage: Storage) {
        val storages = Settings.STORAGES.valueCompat.toMutableList()
            .apply { this[indexOfFirst { it.id == storage.id }] = storage }
        Settings.STORAGES.putValue(storages)
    }

    fun move(fromPosition: Int, toPosition: Int) {
        val bookmarkDirectories = Settings.STORAGES.valueCompat.toMutableList()
            .apply { add(toPosition, removeAt(fromPosition)) }
        Settings.STORAGES.putValue(bookmarkDirectories)
    }

    fun remove(storage: Storage) {
        val bookmarkDirectories = Settings.STORAGES.valueCompat.toMutableList()
            .apply { removeFirst { it.id == storage.id } }
        Settings.STORAGES.putValue(bookmarkDirectories)
    }
}
