/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.filelist

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FileSortOptionsTest {

    @Test
    fun byEnum_hasAllValues() {
        val values = FileSortOptions.By.values()
        assertThat(values).hasLength(4)
        assertThat(values.map { it.name }).containsExactly(
            "NAME", "TYPE", "SIZE", "LAST_MODIFIED"
        )
    }

    @Test
    fun orderEnum_hasAllValues() {
        val values = FileSortOptions.Order.values()
        assertThat(values).hasLength(2)
        assertThat(values.map { it.name }).containsExactly(
            "ASCENDING", "DESCENDING"
        )
    }

    @Test
    fun dataClassProperties() {
        val options = FileSortOptions(
            by = FileSortOptions.By.NAME,
            order = FileSortOptions.Order.ASCENDING,
            isDirectoriesFirst = true
        )
        assertThat(options.by).isEqualTo(FileSortOptions.By.NAME)
        assertThat(options.order).isEqualTo(FileSortOptions.Order.ASCENDING)
        assertThat(options.isDirectoriesFirst).isTrue()
    }

    @Test
    fun equality() {
        val opt1 = FileSortOptions(FileSortOptions.By.SIZE, FileSortOptions.Order.DESCENDING, false)
        val opt2 = FileSortOptions(FileSortOptions.By.SIZE, FileSortOptions.Order.DESCENDING, false)
        assertThat(opt1).isEqualTo(opt2)
    }

    @Test
    fun inequality() {
        val opt1 = FileSortOptions(FileSortOptions.By.NAME, FileSortOptions.Order.ASCENDING, true)
        val opt2 = FileSortOptions(FileSortOptions.By.SIZE, FileSortOptions.Order.ASCENDING, true)
        assertThat(opt1).isNotEqualTo(opt2)
    }

    @Test
    fun copy_withChanges() {
        val original = FileSortOptions(FileSortOptions.By.NAME, FileSortOptions.Order.ASCENDING, true)
        val copy = original.copy(order = FileSortOptions.Order.DESCENDING)
        assertThat(copy.by).isEqualTo(FileSortOptions.By.NAME)
        assertThat(copy.order).isEqualTo(FileSortOptions.Order.DESCENDING)
        assertThat(copy.isDirectoriesFirst).isTrue()
    }

    @Test
    fun createComparator_returnsNonNull() {
        val options = FileSortOptions(
            FileSortOptions.By.NAME,
            FileSortOptions.Order.ASCENDING,
            false
        )
        val comparator = options.createComparator()
        assertThat(comparator).isNotNull()
    }
}
