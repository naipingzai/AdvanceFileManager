/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.IOException

class ThrowableExtensionsTest {

    @Test
    fun findCauseByClass_directMatch() {
        val ex = IOException("test")
        assertThat(ex.findCauseByClass<IOException>()).isSameInstanceAs(ex)
    }

    @Test
    fun findCauseByClass_nestedCause() {
        val root = IOException("root")
        val wrapped = RuntimeException("wrapped", root)
        assertThat(wrapped.findCauseByClass<IOException>()).isSameInstanceAs(root)
    }

    @Test
    fun findCauseByClass_notFound() {
        val ex = RuntimeException("test")
        assertThat(ex.findCauseByClass<IOException>()).isNull()
    }

    @Test
    fun findCauseByClass_deeplyNested() {
        val root = IOException("root")
        val mid = IllegalArgumentException("mid", root)
        val top = RuntimeException("top", mid)
        assertThat(top.findCauseByClass<IOException>()).isSameInstanceAs(root)
        assertThat(top.findCauseByClass<IllegalArgumentException>()).isSameInstanceAs(mid)
    }
}
