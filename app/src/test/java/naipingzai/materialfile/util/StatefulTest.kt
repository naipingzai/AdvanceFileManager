/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StatefulTest {

    @Test
    fun loading_nullValue() {
        val state: Stateful<String> = Loading(null)
        assertThat(state.value).isNull()
        assertThat(state).isInstanceOf(Loading::class.java)
    }

    @Test
    fun loading_withValue() {
        val state: Stateful<String> = Loading("cached")
        assertThat(state.value).isEqualTo("cached")
    }

    @Test
    fun success_value() {
        val state: Stateful<String> = Success("result")
        assertThat(state.value).isEqualTo("result")
        assertThat(state).isInstanceOf(Success::class.java)
    }

    @Test
    fun failure_valueAndThrowable() {
        val ex = RuntimeException("fail")
        val state: Stateful<String> = Failure("partial", ex)
        assertThat(state.value).isEqualTo("partial")
        assertThat(state).isInstanceOf(Failure::class.java)
        assertThat((state as Failure).throwable).isSameInstanceAs(ex)
    }

    @Test
    fun failure_nullValue() {
        val ex = RuntimeException("fail")
        val state: Stateful<String> = Failure(null, ex)
        assertThat(state.value).isNull()
    }

    @Test
    fun equality_success() {
        assertThat(Success("a")).isEqualTo(Success("a"))
        assertThat(Success("a")).isNotEqualTo(Success("b"))
    }

    @Test
    fun equality_loading() {
        assertThat(Loading<String>(null)).isEqualTo(Loading<String>(null))
        assertThat(Loading("a")).isNotEqualTo(Loading("b"))
    }
}
