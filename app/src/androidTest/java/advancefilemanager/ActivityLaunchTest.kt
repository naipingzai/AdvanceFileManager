/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager

import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import com.advancefilemanager.filelist.FileListActivity

/**
 * Instrumented tests for Activity component registration.
 * Verifies activities are declared in manifest and resolvable.
 */
@RunWith(AndroidJUnit4::class)
class ActivityLaunchTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun fileListActivity_isDeclaredInManifest() {
        val intent = Intent(context, FileListActivity::class.java)
        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        assertThat(resolveInfo).isNotNull()
    }
}
