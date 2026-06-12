/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.protocol

/**
 * Represents a built-in feature with its metadata.
 */
data class FeatureInfo(
    /** Feature unique ID */
    val id: String,
    /** Feature display title */
    val title: String,
    /** Feature description */
    val description: String?,
    /** Supported MIME types */
    val mimeTypes: List<String>,
    /** Feature category */
    val category: String,
    /** Sub-features list */
    val subFeatures: List<FeatureSubItem> = emptyList()
)

/**
 * A sub-feature within a feature module (e.g., "format convert" within ffmpeg-tools).
 */
data class FeatureSubItem(
    /** Sub-feature ID, format: featureId.subId */
    val id: String,
    /** Display title */
    val title: String,
    /** Description */
    val description: String?,
    /** Supported MIME types */
    val mimeTypes: List<String>,
    /** Parent feature ID */
    val featureId: String,
    /** Action type identifier */
    val actionType: String
)

/** Feature categories */
object FeatureCategory {
    const val VIEWER = "viewer"
    const val TOOL = "tool"
}