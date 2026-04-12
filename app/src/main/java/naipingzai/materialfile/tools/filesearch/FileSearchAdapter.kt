/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.filesearch

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import java8.nio.file.Path
import java8.nio.file.Paths
import java8.nio.file.attribute.BasicFileAttributes
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.ToolFileItemBinding
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.file.guessFromPath
import naipingzai.materialfile.file.iconRes
import naipingzai.materialfile.file.isImage
import naipingzai.materialfile.file.isMedia
import naipingzai.materialfile.provider.common.readAttributes
import naipingzai.materialfile.ui.CheckableItemBackground

class FileSearchAdapter(
    private val items: List<FileSearchFragment.FileSearchItem>,
    private val onItemClick: (Int) -> Unit,
    private val onItemLongClick: (Int) -> Unit,
    private val onIconClick: (Int) -> Unit
) : RecyclerView.Adapter<FileSearchAdapter.ViewHolder>() {

    class ViewHolder(val binding: ToolFileItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ToolFileItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.itemLayout.background =
            CheckableItemBackground.create(0f, 0f, parent.context)
        val holder = ViewHolder(binding)
        binding.itemLayout.setOnClickListener { onItemClick(holder.bindingAdapterPosition) }
        binding.itemLayout.setOnLongClickListener {
            onItemLongClick(holder.bindingAdapterPosition)
            true
        }
        binding.iconLayout.setOnClickListener { onIconClick(holder.bindingAdapterPosition) }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.getOrNull(position) ?: return
        val context = holder.binding.root.context
        holder.binding.apply {
            itemLayout.isChecked = item.isChecked

            val mimeType = if (item.isDirectory) MimeType.DIRECTORY
                else MimeType.guessFromPath(item.name)
            val iconResId = mimeType.iconRes
            iconImage.apply {
                isVisible = true
                setImageResource(iconResId)
            }

            // Load thumbnail for images and media files
            thumbnailImage.apply {
                dispose()
                setImageDrawable(null)
                val supportsThumbnail = !item.isDirectory &&
                    (mimeType.isImage || mimeType.isMedia)
                isVisible = supportsThumbnail
                if (supportsThumbnail) {
                    try {
                        val nioPath: Path = Paths.get(item.path)
                        val attrs = nioPath.readAttributes(BasicFileAttributes::class.java)
                        load(nioPath to attrs) {
                            listener { _, _ ->
                                iconImage.isVisible = false
                            }
                        }
                    } catch (_: Exception) {
                        isVisible = false
                    }
                }
            }

            nameText.text = item.name
            val lastModified = DateUtils.getRelativeTimeSpanString(
                item.lastModified, System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE
            )
            val size = android.text.format.Formatter.formatFileSize(context, item.size)
            val separator = context.getString(R.string.file_item_description_separator)
            descriptionText.text = listOf(lastModified, size).joinToString(separator)
            pathText.text = item.path
        }
    }

    override fun getItemCount(): Int = items.size
}
