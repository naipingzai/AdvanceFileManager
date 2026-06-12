/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.advancefilemanager.feature.protocol.FeatureCategory
import com.advancefilemanager.feature.protocol.FeatureContract
import com.advancefilemanager.feature.protocol.FeatureInfo
import com.advancefilemanager.feature.protocol.FeatureSubItem

/**
 * Central manager for all built-in features.
 *
 * Features are registered at startup and can be queried by MIME type or category.
 * The visibility of each feature in the file operation bar is controlled by FeatureSettings.
 */
object FeatureManager {

    private val registeredFeatures = mutableListOf<FeatureInfo>()

    /**
     * Register a feature. Called during app initialization by each feature module.
     */
    fun registerFeature(feature: FeatureInfo) {
        if (registeredFeatures.none { it.id == feature.id }) {
            registeredFeatures.add(feature)
        }
    }

    /**
     * Get all registered features.
     */
    fun getAllFeatures(): List<FeatureInfo> = registeredFeatures.toList()

    /**
     * Get features that are enabled and match the given MIME type.
     * Used to populate the file/folder action bar.
     */
    fun getEnabledFeaturesForMimeType(context: Context, mimeType: String): List<FeatureInfo> {
        return registeredFeatures.filter { feature ->
            FeatureSettings.isFeatureEnabled(feature.id) &&
                feature.mimeTypes.any { pattern -> matchMimeType(pattern, mimeType) }
        }
    }

    /**
     * Get enabled sub-features matching a MIME type.
     */
    fun getEnabledSubFeaturesForMimeType(
        context: Context,
        mimeType: String
    ): List<Pair<FeatureInfo, FeatureSubItem>> {
        return registeredFeatures
            .filter { FeatureSettings.isFeatureEnabled(it.id) }
            .flatMap { feature ->
                feature.subFeatures
                    .filter { sub ->
                        FeatureSettings.isSubFeatureEnabled(sub.id) &&
                            sub.mimeTypes.any { matchMimeType(it, mimeType) }
                    }
                    .map { sub -> feature to sub }
            }
    }

    /**
     * Get enabled features by category.
     */
    fun getEnabledFeaturesByCategory(category: String): List<FeatureInfo> {
        return registeredFeatures.filter {
            it.category == category && FeatureSettings.isFeatureEnabled(it.id)
        }
    }

    /**
     * Create an Intent to launch a feature activity.
     */
    fun createFeatureIntent(
        featureId: String,
        filePath: String? = null,
        mimeType: String? = null,
        fileUri: Uri? = null,
        actionType: String? = null,
        filePaths: Array<String>? = null
    ): Intent? {
        val feature = registeredFeatures.find { it.id == featureId } ?: return null
        return createFeatureIntent(feature, filePath, mimeType, fileUri, actionType, filePaths)
    }

    /**
     * Create an Intent for a specific feature.
     */
    fun createFeatureIntent(
        feature: FeatureInfo,
        filePath: String? = null,
        mimeType: String? = null,
        fileUri: Uri? = null,
        actionType: String? = null,
        filePaths: Array<String>? = null
    ): Intent {
        val className = getFeatureActivityClass(feature.id)
        return Intent().apply {
            setClassName(
                com.advancefilemanager.app.application,
                className
            )
            fileUri?.let {
                data = it
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            mimeType?.let { type = it }
            filePath?.let { putExtra(FeatureContract.EXTRA_FILE_PATH, it) }
            filePaths?.let { putExtra(FeatureContract.EXTRA_FILE_PATHS, it) }
            actionType?.let { putExtra(FeatureContract.EXTRA_ACTION_TYPE, it) }
        }
    }

    /**
     * Get the activity class name for a feature ID.
     */
    private fun getFeatureActivityClass(featureId: String): String {
        val pkg = "com.advancefilemanager"
        return when (featureId) {
            "file-tools" -> "$pkg.feature.filetools.FileToolsActivity"
            "ffmpeg-tools" -> "$pkg.feature.ffmpegtools.FFmpegToolsActivity"
            "ebook-viewer" -> "$pkg.viewer.ebook.EbookViewerActivity"
            else -> "$pkg.feature.filetools.FileToolsActivity"
        }
    }

    // MIME type wildcard matching. Supports "video/*" matching "video/mp4", etc.
    fun matchMimeType(pattern: String, mimeType: String): Boolean {
        if (pattern == "*/*") return true
        if (pattern == mimeType) return true
        val (patternType, patternSubtype) = pattern.split("/", limit = 2).let {
            if (it.size == 2) it[0] to it[1] else return false
        }
        val (mimeMainType, _) = mimeType.split("/", limit = 2).let {
            if (it.size == 2) it[0] to it[1] else return false
        }
        return patternSubtype == "*" && patternType == mimeMainType
    }
}