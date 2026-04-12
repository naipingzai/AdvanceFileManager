/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test

class LateInitMutableStateFlowTest {

    @Test
    fun setValue_thenCollect() = runBlocking {
        val flow = LateInitMutableStateFlow<String>()
        flow.value = "hello"
        assertThat(flow.first()).isEqualTo("hello")
    }

    @Test
    fun value_updates() = runBlocking {
        val flow = LateInitMutableStateFlow<Int>()
        flow.value = 1
        assertThat(flow.value).isEqualTo(1)
        flow.value = 2
        assertThat(flow.value).isEqualTo(2)
    }

    @Test
    fun multipleEmissions() = runBlocking {
        val flow = LateInitMutableStateFlow<String>()
        flow.value = "first"
        flow.value = "second"
        assertThat(flow.value).isEqualTo("second")
    }
}
