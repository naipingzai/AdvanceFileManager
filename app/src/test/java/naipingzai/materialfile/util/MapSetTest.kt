/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MapSetTest {

    @Test
    fun add_unique() {
        val set = MapSet<Int, String> { it.length }
        assertThat(set.add("ab")).isTrue()
        assertThat(set).containsExactly("ab")
    }

    @Test
    fun add_duplicate_replaces() {
        val set = MapSet<Int, String> { it.length }
        set.add("ab")
        // Same key (length=2), should replace
        assertThat(set.add("cd")).isFalse()
        assertThat(set).containsExactly("cd")
    }

    @Test
    fun contains_byKey() {
        val set = MapSet<Int, String> { it.length }
        set.add("hello") // key=5
        // "world" also has key=5, so contains returns true
        assertThat(set.contains("world")).isTrue()
        // "hi" has key=2, not in set
        assertThat(set.contains("hi")).isFalse()
    }

    @Test
    fun remove_existing() {
        val set = MapSet<Int, String> { it.length }
        set.add("abc")
        assertThat(set.remove("xyz")).isTrue() // same key=3
        assertThat(set).isEmpty()
    }

    @Test
    fun remove_nonExisting() {
        val set = MapSet<Int, String> { it.length }
        set.add("abc")
        assertThat(set.remove("ab")).isFalse() // key=2, not in set
    }

    @Test
    fun size() {
        val set = MapSet<Int, String> { it.length }
        assertThat(set.size).isEqualTo(0)
        set.add("a")
        set.add("bb")
        assertThat(set.size).isEqualTo(2)
    }

    @Test
    fun clear() {
        val set = MapSet<Int, String> { it.length }
        set.add("a")
        set.add("bb")
        set.clear()
        assertThat(set).isEmpty()
    }

    @Test
    fun iterator() {
        val set = MapSet<Int, String> { it.length }
        set.add("a")
        set.add("bb")
        set.add("ccc")
        assertThat(set).containsExactly("a", "bb", "ccc")
    }

    @Test
    fun linkedMapSet_preservesOrder() {
        val set = LinkedMapSet<Int, String> { it.length }
        set.add("ccc")
        set.add("a")
        set.add("bb")
        assertThat(set.toList()).containsExactly("ccc", "a", "bb").inOrder()
    }
}
