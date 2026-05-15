/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.advancefilemanager.viewer.audio.AudioPlayerActivity
import com.advancefilemanager.viewer.csv.CsvViewerActivity
import com.advancefilemanager.viewer.ebook.EbookViewerActivity
import com.advancefilemanager.viewer.hex.HexViewerActivity
import com.advancefilemanager.viewer.image.ImageViewerActivity
import com.advancefilemanager.viewer.pdf.PdfViewerActivity
import com.advancefilemanager.viewer.text.TextEditorActivity
import com.advancefilemanager.viewer.video.VideoViewerActivity
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for all viewer Activity registrations.
 * Verifies each viewer is declared in manifest and its component is enabled.
 */
@RunWith(AndroidJUnit4::class)
class ViewerActivityTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val pm get() = context.packageManager

    @Test
    fun csvViewerActivity_isDeclared() {
        verifyComponentEnabled(CsvViewerActivity::class.java)
    }

    @Test
    fun imageViewerActivity_isDeclared() {
        verifyComponentEnabled(ImageViewerActivity::class.java)
    }

    @Test
    fun videoViewerActivity_isDeclared() {
        verifyComponentEnabled(VideoViewerActivity::class.java)
    }

    @Test
    fun audioPlayerActivity_isDeclared() {
        verifyComponentEnabled(AudioPlayerActivity::class.java)
    }

    @Test
    fun textEditorActivity_isDeclared() {
        verifyComponentEnabled(TextEditorActivity::class.java)
    }

    @Test
    fun pdfViewerActivity_isDeclared() {
        verifyComponentEnabled(PdfViewerActivity::class.java)
    }

    @Test
    fun ebookViewerActivity_isDeclared() {
        verifyComponentEnabled(EbookViewerActivity::class.java)
    }

    @Test
    fun hexViewerActivity_isDeclared() {
        verifyComponentEnabled(HexViewerActivity::class.java)
    }

    private fun <T : android.app.Activity> verifyComponentEnabled(activityClass: Class<T>) {
        val component = ComponentName(context, activityClass)
        val info = pm.getActivityInfo(component, 0)
        assertThat(info).isNotNull()
        assertThat(info.enabled).isTrue()
    }
}
