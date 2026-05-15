/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.image

import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import coil.size.Size
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.DefaultOnImageEventListener
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.advancefilemanager.coil.fadeIn
import com.advancefilemanager.databinding.ImageViewerItemBinding
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.file.asMimeType
import com.advancefilemanager.file.asMimeTypeOrNull
import com.advancefilemanager.file.fileProviderUri
import com.advancefilemanager.provider.common.AndroidFileTypeDetector
import com.advancefilemanager.provider.common.newInputStream
import com.advancefilemanager.provider.common.readAttributes
import com.advancefilemanager.ui.SimpleAdapter
import com.advancefilemanager.util.fadeInUnsafe
import com.advancefilemanager.util.fadeOutUnsafe
import com.advancefilemanager.util.layoutInflater
import com.advancefilemanager.util.shortAnimTime
import kotlin.math.max

class ImageViewerAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val listener: (View) -> Unit
) : SimpleAdapter<Path, ImageViewerAdapter.ViewHolder>() {
    override val hasStableIds: Boolean
        get() = true

    override fun getItemId(position: Int): Long = getItem(position).hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ImageViewerItemBinding.inflate(parent.context.layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = getItem(position)
        val binding = holder.binding
        binding.image.setOnPhotoTapListener { view, _, _ -> listener(view) }
        binding.largeImage.setOnClickListener(listener)
        loadImage(binding, path)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)

        val binding = holder.binding
        binding.image.dispose()
        binding.largeImage.recycle()
    }

    private fun loadImage(binding: ImageViewerItemBinding, path: Path) {
        binding.progress.fadeInUnsafe(true)
        binding.errorText.fadeOutUnsafe()
        binding.image.isVisible = false
        binding.largeImage.isVisible = false
        lifecycleOwner.lifecycleScope.launch {
            val imageInfo = try {
                withContext(Dispatchers.IO) { path.loadImageInfo() }
            } catch (e: Throwable) {
                Log.e(TAG, "Error loading image info: $path", e)
                showError(binding, e)
                return@launch
            }
            try {
                loadImageWithInfo(binding, path, imageInfo)
            } catch (e: Throwable) {
                Log.e(TAG, "Error displaying image: $path", e)
                showError(binding, e)
            }
        }
    }

    private fun Path.loadImageInfo(): ImageInfo {
        val attributes = readAttributes(BasicFileAttributes::class.java)
        val mimeType = AndroidFileTypeDetector.getMimeType(this, attributes).asMimeType()
        val bitmapOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        newInputStream().use { BitmapFactory.decodeStream(it, null, bitmapOptions) }
        return ImageInfo(
            attributes, bitmapOptions.outWidth, bitmapOptions.outHeight,
            bitmapOptions.outMimeType?.asMimeTypeOrNull() ?: mimeType
        )
    }

    private fun loadImageWithInfo(
        binding: ImageViewerItemBinding,
        path: Path,
        imageInfo: ImageInfo
    ) {
        if (!imageInfo.shouldUseLargeImageView) {
            binding.image.apply {
                isVisible = true
                load(path to imageInfo.attributes) {
                    size(Size.ORIGINAL)
                    fadeIn(context.shortAnimTime)
                    listener(
                        onSuccess = { _, _ ->
                            binding.progress.fadeOutUnsafe()
                        },
                        onError = { _, result ->
                            Log.e(TAG, "Coil load error: $path", result.throwable)
                            showError(binding, result.throwable)
                        }
                    )
                }
            }
        } else {
            binding.largeImage.apply {
                setDoubleTapZoomDuration(300)
                orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF
                // Otherwise OnImageEventListener.onReady() is never called.
                isVisible = true
                alpha = 0f
                setOnImageEventListener(object : DefaultOnImageEventListener() {
                    override fun onReady() {
                        setDoubleTapZoomScale(binding.largeImage.cropScale)
                        binding.progress.fadeOutUnsafe()
                        binding.largeImage.fadeInUnsafe(true)
                    }

                    override fun onImageLoadError(e: Exception) {
                        e.printStackTrace()
                        showError(binding, e)
                    }
                })
                setImageRestoringSavedState(ImageSource.uri(path.fileProviderUri))
            }
        }
    }

    private val ImageInfo.shouldUseLargeImageView: Boolean
        get() {
            // See BitmapFactory.cpp encodedFormatToString()
            if (mimeType == MimeType.IMAGE_GIF) {
                return false
            }
            if (width <= 0 || height <= 0) {
                return false
            }
            // 4 bytes per pixel for ARGB_8888.
            if (width * height * 4 > MAX_BITMAP_SIZE) {
                return true
            }
            if (width > 2048 || height > 2048) {
                val ratio = width.toFloat() / height
                if (ratio < 0.5 || ratio > 2) {
                    return true
                }
            }
            return false
        }

    private val SubsamplingScaleImageView.cropScale: Float
        get() {
            val viewWidth = (width - paddingLeft - paddingRight)
            val viewHeight = (height - paddingTop - paddingBottom)
            val orientation = appliedOrientation
            val rotated90Or270 = orientation == SubsamplingScaleImageView.ORIENTATION_90
                || orientation == SubsamplingScaleImageView.ORIENTATION_270
            val imageWidth = if (rotated90Or270) sHeight else sWidth
            val imageHeight = if (rotated90Or270) sWidth else sHeight
            return max(viewWidth.toFloat() / imageWidth, viewHeight.toFloat() / imageHeight)
        }

    private fun showError(binding: ImageViewerItemBinding, throwable: Throwable) {
        Log.e(TAG, "Image error", throwable)
        binding.progress.fadeOutUnsafe()
        binding.errorText.text = throwable.toString()
        binding.errorText.fadeInUnsafe(true)
        binding.image.isVisible = false
        binding.largeImage.isVisible = false
    }

    companion object {
        private const val TAG = "ImageViewerAdapter"
        // @see android.graphics.RecordingCanvas#MAX_BITMAP_SIZE
        private const val MAX_BITMAP_SIZE = 100 * 1024 * 1024
    }

    class ViewHolder(val binding: ImageViewerItemBinding) : RecyclerView.ViewHolder(binding.root)

    private class ImageInfo(
        val attributes: BasicFileAttributes,
        val width: Int,
        val height: Int,
        val mimeType: MimeType
    )
}
