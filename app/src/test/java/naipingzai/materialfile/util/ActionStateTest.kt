/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ActionStateTest {

    @Test
    fun ready_isReady() {
        val state: ActionState<String, Int> = ActionState.Ready()
        assertThat(state.isReady).isTrue()
        assertThat(state.isRunning).isFalse()
        assertThat(state.isFinished).isFalse()
    }

    @Test
    fun running_isRunning() {
        val state: ActionState<String, Int> = ActionState.Running("arg")
        assertThat(state.isReady).isFalse()
        assertThat(state.isRunning).isTrue()
        assertThat(state.isFinished).isFalse()
    }

    @Test
    fun success_isFinished() {
        val state: ActionState<String, Int> = ActionState.Success("arg", 42)
        assertThat(state.isReady).isFalse()
        assertThat(state.isRunning).isFalse()
        assertThat(state.isFinished).isTrue()
    }

    @Test
    fun error_isFinished() {
        val state: ActionState<String, Int> = ActionState.Error("arg", RuntimeException("test"))
        assertThat(state.isReady).isFalse()
        assertThat(state.isRunning).isFalse()
        assertThat(state.isFinished).isTrue()
    }

    @Test
    fun running_argument() {
        val state = ActionState.Running<String, Int>("myArg")
        assertThat(state.argument).isEqualTo("myArg")
    }

    @Test
    fun success_argumentAndResult() {
        val state = ActionState.Success<String, Int>("myArg", 100)
        assertThat(state.argument).isEqualTo("myArg")
        assertThat(state.result).isEqualTo(100)
    }

    @Test
    fun error_argumentAndThrowable() {
        val ex = RuntimeException("oops")
        val state = ActionState.Error<String, Int>("myArg", ex)
        assertThat(state.argument).isEqualTo("myArg")
        assertThat(state.throwable).isSameInstanceAs(ex)
    }

    @Test
    fun ready_equality() {
        val r1 = ActionState.Ready<String, Int>()
        val r2 = ActionState.Ready<String, Int>()
        assertThat(r1).isEqualTo(r2)
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode())
    }
}
