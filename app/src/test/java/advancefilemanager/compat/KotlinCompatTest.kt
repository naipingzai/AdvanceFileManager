/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.compat

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KotlinCompatTest {

    @Test
    fun reversedCompat_reversesOrder() {
        val comparator = Comparator<Int> { a, b -> a - b }
        val reversed = comparator.reversedCompat()
        assertThat(reversed.compare(1, 2)).isGreaterThan(0)
        assertThat(reversed.compare(2, 1)).isLessThan(0)
        assertThat(reversed.compare(1, 1)).isEqualTo(0)
    }

    @Test
    fun reversedCompat_doubleReverse() {
        val comparator = Comparator<Int> { a, b -> a - b }
        val doubleReversed = comparator.reversedCompat().reversedCompat()
        assertThat(doubleReversed.compare(1, 2)).isLessThan(0)
    }

    @Test
    fun removeFirstCompat_removeHead() {
        val list = mutableListOf(1, 2, 3)
        val removed = list.removeFirstCompat()
        assertThat(removed).isEqualTo(1)
        assertThat(list).containsExactly(2, 3).inOrder()
    }

    @Test(expected = NoSuchElementException::class)
    fun removeFirstCompat_emptyList() {
        mutableListOf<Int>().removeFirstCompat()
    }

    @Test
    fun removeLastCompat_removeTail() {
        val list = mutableListOf(1, 2, 3)
        val removed = list.removeLastCompat()
        assertThat(removed).isEqualTo(3)
        assertThat(list).containsExactly(1, 2).inOrder()
    }

    @Test(expected = NoSuchElementException::class)
    fun removeLastCompat_emptyList() {
        mutableListOf<Int>().removeLastCompat()
    }
}
