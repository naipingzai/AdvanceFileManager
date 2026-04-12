/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AnyExtensionsTest {

    @Test
    fun hash_singleValue() {
        val h = Any().hash("a")
        assertThat(h).isNotEqualTo(0)
    }

    @Test
    fun hash_multipleValues() {
        val h1 = Any().hash("a", 1, null)
        val h2 = Any().hash("a", 1, null)
        assertThat(h1).isEqualTo(h2)
    }

    @Test
    fun hash_differentValues() {
        val h1 = Any().hash("a", 1)
        val h2 = Any().hash("b", 2)
        assertThat(h1).isNotEqualTo(h2)
    }

    @Test
    fun hash_nullValues() {
        val h = Any().hash(null, null)
        assertThat(h).isEqualTo(Any().hash(null, null))
    }
}
