/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ListExtensionsTest {

    @Test
    fun startsWith_matchingPrefix() {
        assertThat(listOf(1, 2, 3, 4).startsWith(listOf(1, 2))).isTrue()
    }

    @Test
    fun startsWith_exactMatch() {
        assertThat(listOf(1, 2, 3).startsWith(listOf(1, 2, 3))).isTrue()
    }

    @Test
    fun startsWith_emptyPrefix() {
        assertThat(listOf(1, 2).startsWith(emptyList())).isTrue()
    }

    @Test
    fun startsWith_noMatch() {
        assertThat(listOf(1, 2, 3).startsWith(listOf(2, 3))).isFalse()
    }

    @Test
    fun startsWith_prefixLongerThanList() {
        assertThat(listOf(1).startsWith(listOf(1, 2))).isFalse()
    }

    @Test
    fun startsWith_emptyListEmptyPrefix() {
        assertThat(emptyList<Int>().startsWith(emptyList())).isTrue()
    }

    @Test
    fun endsWith_matchingSuffix() {
        assertThat(listOf(1, 2, 3, 4).endsWith(listOf(3, 4))).isTrue()
    }

    @Test
    fun endsWith_exactMatch() {
        assertThat(listOf(1, 2, 3).endsWith(listOf(1, 2, 3))).isTrue()
    }

    @Test
    fun endsWith_emptySuffix() {
        assertThat(listOf(1, 2).endsWith(emptyList())).isTrue()
    }

    @Test
    fun endsWith_noMatch() {
        assertThat(listOf(1, 2, 3).endsWith(listOf(1, 2))).isFalse()
    }

    @Test
    fun endsWith_suffixLongerThanList() {
        assertThat(listOf(1).endsWith(listOf(1, 2))).isFalse()
    }
}
