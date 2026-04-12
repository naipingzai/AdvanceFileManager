/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharSequenceExtensionsTest {

    @Test
    fun takeIfNotBlank_nonBlank() {
        assertThat("hello".takeIfNotBlank()).isEqualTo("hello")
    }

    @Test
    fun takeIfNotBlank_blank() {
        assertThat("   ".takeIfNotBlank()).isNull()
    }

    @Test
    fun takeIfNotBlank_empty() {
        assertThat("".takeIfNotBlank()).isNull()
    }

    @Test
    fun takeIfNotEmpty_nonEmpty() {
        assertThat("hello".takeIfNotEmpty()).isEqualTo("hello")
    }

    @Test
    fun takeIfNotEmpty_empty() {
        assertThat("".takeIfNotEmpty()).isNull()
    }

    @Test
    fun takeIfNotEmpty_blank() {
        assertThat("   ".takeIfNotEmpty()).isEqualTo("   ")
    }
}
