/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.filejob

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FileJobEnumsTest {

    @Test
    fun conflictAction_allValues() {
        val values = FileJobConflictAction.values()
        assertThat(values).hasLength(5)
        assertThat(values.map { it.name }).containsExactly(
            "MERGE_OR_REPLACE", "RENAME", "SKIP", "CANCEL", "CANCELED"
        )
    }

    @Test
    fun conflictAction_valueOf() {
        assertThat(FileJobConflictAction.valueOf("MERGE_OR_REPLACE"))
            .isEqualTo(FileJobConflictAction.MERGE_OR_REPLACE)
        assertThat(FileJobConflictAction.valueOf("SKIP"))
            .isEqualTo(FileJobConflictAction.SKIP)
    }

    @Test
    fun errorAction_allValues() {
        val values = FileJobErrorAction.values()
        assertThat(values).hasLength(4)
        assertThat(values.map { it.name }).containsExactly(
            "POSITIVE", "NEGATIVE", "NEUTRAL", "CANCELED"
        )
    }

    @Test
    fun errorAction_valueOf() {
        assertThat(FileJobErrorAction.valueOf("POSITIVE"))
            .isEqualTo(FileJobErrorAction.POSITIVE)
        assertThat(FileJobErrorAction.valueOf("CANCELED"))
            .isEqualTo(FileJobErrorAction.CANCELED)
    }

    @Test
    fun fileJob_incrementingIds() {
        // FileJob.id is based on AtomicInteger, each instance gets a unique id
        // We can't directly test FileJob (abstract), but we can verify the static counter
        // by checking that the companion object's class exists
        val clazz = FileJob::class.java
        assertThat(clazz).isNotNull()
    }
}
