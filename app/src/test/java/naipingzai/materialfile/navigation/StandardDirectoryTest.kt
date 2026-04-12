/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StandardDirectoryTest {

    @Test
    fun id_basedOnRelativePath() {
        val dir = StandardDirectory(0, 0, "Download", true)
        assertThat(dir.id).isEqualTo("Download".hashCode().toLong())
    }

    @Test
    fun id_differentPathsDifferentIds() {
        val dir1 = StandardDirectory(0, 0, "Download", true)
        val dir2 = StandardDirectory(0, 0, "Documents", true)
        assertThat(dir1.id).isNotEqualTo(dir2.id)
    }

    @Test
    fun key_isRelativePath() {
        val dir = StandardDirectory(0, 0, "Music", true)
        assertThat(dir.key).isEqualTo("Music")
    }

    @Test
    fun withSettings_updatesCustomTitleAndEnabled() {
        val dir = StandardDirectory(1, 2, "Pictures", true)
        val settings = StandardDirectorySettings("Pictures", "My Photos", false)
        val updated = dir.withSettings(settings)
        assertThat(updated.isEnabled).isFalse()
        assertThat(updated.relativePath).isEqualTo("Pictures")
        assertThat(updated.iconRes).isEqualTo(1)
    }

    @Test
    fun toSettings_convertsCorrectly() {
        val dir = StandardDirectory(1, 2, "Videos", true)
        val settings = dir.toSettings()
        assertThat(settings.id).isEqualTo("Videos")
        assertThat(settings.isEnabled).isTrue()
        assertThat(settings.customTitle).isNull()
    }

    @Test
    fun withSettings_thenToSettings_roundTrip() {
        val dir = StandardDirectory(1, 2, "DCIM", true)
        val settings = StandardDirectorySettings("DCIM", "Camera", false)
        val updated = dir.withSettings(settings)
        val backToSettings = updated.toSettings()
        assertThat(backToSettings.id).isEqualTo("DCIM")
        assertThat(backToSettings.customTitle).isEqualTo("Camera")
        assertThat(backToSettings.isEnabled).isFalse()
    }

    @Test
    fun isEnabled_respectsConstructor() {
        val enabled = StandardDirectory(0, 0, "a", true)
        val disabled = StandardDirectory(0, 0, "b", false)
        assertThat(enabled.isEnabled).isTrue()
        assertThat(disabled.isEnabled).isFalse()
    }
}
