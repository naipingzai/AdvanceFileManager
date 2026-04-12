/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DataStateTest {

    @Test
    fun loading_noData() {
        val state: DataState<String> = DataState.Loading()
        assertThat(state.data).isNull()
        assertThat(state).isInstanceOf(DataState.Loading::class.java)
    }

    @Test
    fun loading_withData() {
        val state: DataState<String> = DataState.Loading("cached")
        assertThat(state.data).isEqualTo("cached")
    }

    @Test
    fun success_data() {
        val state: DataState<String> = DataState.Success("result")
        assertThat(state.data).isEqualTo("result")
        assertThat(state).isInstanceOf(DataState.Success::class.java)
    }

    @Test
    fun error_dataAndThrowable() {
        val ex = RuntimeException("fail")
        val state: DataState<String> = DataState.Error("partial", ex)
        assertThat(state.data).isEqualTo("partial")
        assertThat(state).isInstanceOf(DataState.Error::class.java)
        assertThat((state as DataState.Error).throwable).isSameInstanceAs(ex)
    }

    @Test
    fun error_noData() {
        val ex = RuntimeException("fail")
        val state: DataState<String> = DataState.Error(null, ex)
        assertThat(state.data).isNull()
    }

    @Test
    fun toLoading_fromSuccess() {
        val state: DataState<String> = DataState.Success("data")
        val loading = state.toLoading()
        assertThat(loading).isInstanceOf(DataState.Loading::class.java)
        assertThat(loading.data).isEqualTo("data")
    }

    @Test
    fun toLoading_fromLoading_returnsSameType() {
        val state: DataState<String> = DataState.Loading("data")
        val loading = state.toLoading()
        assertThat(loading.data).isEqualTo("data")
    }

    @Test
    fun toError_fromSuccess() {
        val ex = RuntimeException("fail")
        val state: DataState<String> = DataState.Success("data")
        val error = state.toError(ex)
        assertThat(error.data).isEqualTo("data")
        assertThat(error.throwable).isSameInstanceAs(ex)
    }

    @Test
    fun toError_fromLoading() {
        val ex = RuntimeException("fail")
        val state: DataState<String> = DataState.Loading(null)
        val error = state.toError(ex)
        assertThat(error.data).isNull()
        assertThat(error.throwable).isSameInstanceAs(ex)
    }
}
