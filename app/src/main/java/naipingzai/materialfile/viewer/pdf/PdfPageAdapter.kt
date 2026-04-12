/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.viewer.pdf

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.util.LruCache
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import naipingzai.materialfile.R

class PdfPageAdapter(
    private val renderer: PdfRenderer,
    private val screenWidth: Int,
    private val scope: CoroutineScope
) : RecyclerView.Adapter<PdfPageAdapter.ViewHolder>() {

    // Memory-based bitmap cache: use 1/8 of available heap
    private val bitmapCache: LruCache<Int, Bitmap>

    // Mutex to serialize PdfRenderer access (PdfRenderer is not thread-safe)
    private val renderMutex = Mutex()

    // Default placeholder height estimated from first page aspect ratio
    private val defaultPageHeight: Int

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        bitmapCache = object : LruCache<Int, Bitmap>(cacheSize) {
            override fun sizeOf(key: Int, bitmap: Bitmap): Int = bitmap.byteCount / 1024
        }

        // Get first page dimensions for placeholder sizing
        defaultPageHeight = try {
            val page = renderer.openPage(0)
            val scale = screenWidth.toFloat() / page.width
            val height = (page.height * scale).toInt()
            page.close()
            height
        } catch (e: Exception) {
            (screenWidth * 1.414).toInt() // A4 ratio fallback
        }
    }

    class ViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
        var renderJob: Job? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.pdf_page_item, parent, false) as ImageView
        return ViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.renderJob?.cancel()

        val cached = bitmapCache.get(position)
        if (cached != null) {
            holder.imageView.minimumHeight = 0
            holder.imageView.setImageBitmap(cached)
            return
        }

        // Show placeholder with estimated height to prevent layout jumps
        holder.imageView.setImageBitmap(null)
        holder.imageView.minimumHeight = defaultPageHeight

        // Render page asynchronously to avoid blocking the UI thread
        holder.renderJob = scope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                renderMutex.withLock { renderPage(position) }
            }
            if (bitmap != null) {
                bitmapCache.put(position, bitmap)
                if (holder.bindingAdapterPosition == position) {
                    holder.imageView.setImageBitmap(bitmap)
                    holder.imageView.minimumHeight = 0
                }
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        // Don't cancel renderJob here — let it finish to populate cache
        holder.imageView.setImageBitmap(null)
    }

    private fun renderPage(index: Int): Bitmap? {
        return try {
            val page = renderer.openPage(index)
            val scale = screenWidth.toFloat() / page.width
            val bitmapWidth = screenWidth
            val bitmapHeight = (page.height * scale).toInt()
            val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    override fun getItemCount(): Int = renderer.pageCount

    fun clearCache() {
        bitmapCache.evictAll()
    }
}
