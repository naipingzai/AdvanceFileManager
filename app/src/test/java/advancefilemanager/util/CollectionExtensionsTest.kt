/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CollectionExtensionsTest {

    private enum class TestEnum { A, B, C, D, E, F }

    @Test
    fun enumSetOf_empty() {
        val set = enumSetOf<TestEnum>()
        assertThat(set).isEmpty()
    }

    @Test
    fun enumSetOf_single() {
        val set = enumSetOf(TestEnum.A)
        assertThat(set).containsExactly(TestEnum.A)
    }

    @Test
    fun enumSetOf_two() {
        val set = enumSetOf(TestEnum.A, TestEnum.B)
        assertThat(set).containsExactly(TestEnum.A, TestEnum.B)
    }

    @Test
    fun enumSetOf_three() {
        val set = enumSetOf(TestEnum.A, TestEnum.B, TestEnum.C)
        assertThat(set).containsExactly(TestEnum.A, TestEnum.B, TestEnum.C)
    }

    @Test
    fun enumSetOf_varargs() {
        val set = enumSetOf(TestEnum.A, TestEnum.B, TestEnum.C, TestEnum.D, TestEnum.E, TestEnum.F)
        assertThat(set).containsExactly(*TestEnum.entries.toTypedArray())
    }

    @Test
    fun toLinkedSet_preservesOrder() {
        val list = listOf(3, 1, 4, 1, 5, 9, 2, 6)
        val set = list.toLinkedSet()
        assertThat(set).containsExactly(3, 1, 4, 5, 9, 2, 6).inOrder()
    }

    @Test
    fun toEnumSet_fromCollection() {
        val list = listOf(TestEnum.B, TestEnum.D)
        val set = list.toEnumSet()
        assertThat(set).containsExactly(TestEnum.B, TestEnum.D)
    }

    @Test
    fun toEnumSet_empty() {
        val list = emptyList<TestEnum>()
        val set = list.toEnumSet()
        assertThat(set).isEmpty()
    }

    @Test
    fun takeIfNotEmpty_collection_nonEmpty() {
        val list = listOf(1, 2, 3)
        assertThat(list.takeIfNotEmpty()).isSameInstanceAs(list)
    }

    @Test
    fun takeIfNotEmpty_collection_empty() {
        assertThat(emptyList<Int>().takeIfNotEmpty()).isNull()
    }

    @Test
    fun removeFirst_mutableCollection() {
        val set = linkedSetOf(10, 20, 30)
        @Suppress("DEPRECATION")
        val first = (set as MutableCollection<Int>).removeFirst()
        assertThat(first).isEqualTo(10)
        assertThat(set).containsExactly(20, 30).inOrder()
    }

    @Test
    fun removeFirst_mutableMap() {
        val map = linkedMapOf("a" to 1, "b" to 2, "c" to 3)
        val entry = map.removeFirst()
        assertThat(entry.key).isEqualTo("a")
        assertThat(entry.value).isEqualTo(1)
        assertThat(map).containsExactly("b", 2, "c", 3)
    }

    @Test
    fun removeFirst_withPredicate_found() {
        val list = mutableListOf(1, 2, 3, 4)
        val removed = list.removeFirst { it % 2 == 0 }
        assertThat(removed).isEqualTo(2)
        assertThat(list).containsExactly(1, 3, 4).inOrder()
    }

    @Test
    fun removeFirst_withPredicate_notFound() {
        val list = mutableListOf(1, 3, 5)
        val removed = list.removeFirst { it % 2 == 0 }
        assertThat(removed).isNull()
        assertThat(list).containsExactly(1, 3, 5)
    }

    @Test
    fun removeFirst_map_withPredicate() {
        val map = linkedMapOf("a" to 1, "b" to 2, "c" to 3)
        val entry = map.removeFirst { it.value > 1 }
        assertThat(entry!!.key).isEqualTo("b")
        assertThat(map).containsExactly("a", 1, "c", 3)
    }
}
