/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StandardDirectorySettingsTest {

    @Test
    fun dataClassProperties() {
        val settings = StandardDirectorySettings("Download", "My Downloads", true)
        assertThat(settings.id).isEqualTo("Download")
        assertThat(settings.customTitle).isEqualTo("My Downloads")
        assertThat(settings.isEnabled).isTrue()
    }

    @Test
    fun customTitle_nullable() {
        val settings = StandardDirectorySettings("Music", null, false)
        assertThat(settings.customTitle).isNull()
        assertThat(settings.isEnabled).isFalse()
    }

    @Test
    fun equality_sameValues() {
        val s1 = StandardDirectorySettings("a", "b", true)
        val s2 = StandardDirectorySettings("a", "b", true)
        assertThat(s1).isEqualTo(s2)
        assertThat(s1.hashCode()).isEqualTo(s2.hashCode())
    }

    @Test
    fun equality_differentValues() {
        val s1 = StandardDirectorySettings("a", "b", true)
        val s2 = StandardDirectorySettings("a", "c", true)
        assertThat(s1).isNotEqualTo(s2)
    }

    @Test
    fun copy_updatesField() {
        val original = StandardDirectorySettings("x", null, true)
        val copy = original.copy(isEnabled = false)
        assertThat(copy.id).isEqualTo("x")
        assertThat(copy.isEnabled).isFalse()
    }
}
