/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import com.advancefilemanager.settings.UiSettingsManager
import com.advancefilemanager.util.asColor
import com.advancefilemanager.util.getColorByAttr
import com.advancefilemanager.util.shortAnimTime
import com.advancefilemanager.util.withModulatedAlpha

object BackgroundOverlayManager {

    private var overlayView: View? = null
    private var isShowing = false
    private var dynamicOverlay: View? = null

    fun getDimAmount(context: Context): Float {
        val intensity = UiSettingsManager.getBlurIntensity(context)
        return if (intensity <= 0f) 0f else (0.2f + intensity * 0.6f).coerceIn(0f, 0.8f)
    }

    fun init(overlay: View) {
        overlayView = overlay
        overlay.setOnClickListener { }
    }

    fun applyDialogOverlay(context: Context, dialog: AlertDialog) {
        val dimAmount = getDimAmount(context)
        if (dimAmount <= 0f) return

        dialog.window?.let { window ->
            val params = window.attributes
            params.dimAmount = dimAmount
            window.attributes = params
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    fun onPopupMenuShow(popupMenu: PopupMenu, anchorView: View) {
        val intensity = UiSettingsManager.getBlurIntensity(anchorView.context)
        if (intensity <= 0f || overlayView == null) return

        showDimOverlay(anchorView.context)

        popupMenu.setOnDismissListener {
            hideOverlay(anchorView.context)
        }
    }

    private fun getOrCreateOverlay(context: Context): View? {
        if (overlayView != null && overlayView!!.isAttachedToWindow) {
            return overlayView
        }
        val activity = context as? Activity ?: return null
        val decorView = activity.window.decorView as? FrameLayout ?: return null
        if (dynamicOverlay != null && dynamicOverlay!!.isAttachedToWindow &&
            dynamicOverlay!!.parent == decorView) {
            return dynamicOverlay
        }
        val overlay = View(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            isClickable = true
            isFocusable = true
            visibility = View.GONE
        }
        decorView.addView(overlay)
        dynamicOverlay = overlay
        return overlay
    }

    fun showDimOverlay(context: Context) {
        val overlay = getOrCreateOverlay(context) ?: return
        if (isShowing) return

        val intensity = UiSettingsManager.getBlurIntensity(context)
        val surfaceColor = context.getColorByAttr(com.google.android.material.R.attr.colorSurface)
        val overlayColor = surfaceColor.asColor().withModulatedAlpha(0.87f * intensity).value

        overlay.setBackgroundColor(overlayColor)
        overlay.visibility = View.VISIBLE
        overlay.alpha = 0f
        overlay.animate()
            .alpha(1f)
            .setDuration(context.shortAnimTime.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isShowing = true
                }
            })
            .start()
    }

    fun hideOverlay(context: Context) {
        val overlay = getOrCreateOverlay(context) ?: return
        if (!isShowing) return

        overlay.animate()
            .alpha(0f)
            .setDuration(context.shortAnimTime.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    resetOverlay(context)
                }
            })
            .start()
    }

    fun resetOverlay(context: Context? = null) {
        val overlay = if (context != null) {
            getOrCreateOverlay(context)
        } else {
            overlayView?.takeIf { it.isAttachedToWindow } ?: dynamicOverlay
        }
        overlay?.let {
            it.visibility = View.GONE
            it.alpha = 0f
            it.background = null
        }
        isShowing = false
    }

    fun forceHide(context: Context? = null) {
        val overlay = if (context != null) {
            getOrCreateOverlay(context)
        } else {
            overlayView?.takeIf { it.isAttachedToWindow } ?: dynamicOverlay
        }
        overlay?.animate()?.cancel()
        resetOverlay(context)
    }
}

fun AlertDialog.applyOverlay(context: Context): AlertDialog {
    setOnShowListener {
        BackgroundOverlayManager.applyDialogOverlay(context, this)
    }
    return this
}
