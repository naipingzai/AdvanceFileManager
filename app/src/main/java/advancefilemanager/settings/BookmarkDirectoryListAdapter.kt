/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.view.ViewGroup
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder
import com.advancefilemanager.compat.foregroundCompat
import com.advancefilemanager.compat.isTransformedTouchPointInViewCompat
import com.advancefilemanager.databinding.BookmarkDirectoryItemBinding
import com.advancefilemanager.filelist.toUserFriendlyString
import com.advancefilemanager.navigation.BookmarkDirectory
import com.advancefilemanager.ui.SimpleAdapter
import com.advancefilemanager.util.layoutInflater

class BookmarkDirectoryListAdapter(
    private val listener: Listener
) : SimpleAdapter<BookmarkDirectory, BookmarkDirectoryListAdapter.ViewHolder>(),
    DraggableItemAdapter<BookmarkDirectoryListAdapter.ViewHolder> {
    override val hasStableIds: Boolean
        get() = true

    override fun getItemId(position: Int): Long = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            BookmarkDirectoryItemBinding.inflate(parent.context.layoutInflater, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmarkDirectory = getItem(position)
        val binding = holder.binding
        // Need to remove the ripple before it's drawn onto the bitmap for dragging.
        binding.root.foregroundCompat!!.mutate().setVisible(!holder.dragState.isActive, false)
        binding.root.setOnClickListener { listener.editBookmarkDirectory(bookmarkDirectory) }
        binding.root.setOnLongClickListener {
            listener.deleteBookmarkDirectory(bookmarkDirectory)
            true
        }
        binding.nameText.text = bookmarkDirectory.name
        binding.pathText.text = bookmarkDirectory.path.toUserFriendlyString()
    }

    override fun onCheckCanStartDrag(holder: ViewHolder, position: Int, x: Int, y: Int): Boolean =
        (holder.binding.root as ViewGroup).isTransformedTouchPointInViewCompat(
            x.toFloat(), y.toFloat(), holder.binding.dragHandleView, null
        )

    override fun onGetItemDraggableRange(holder: ViewHolder, position: Int): ItemDraggableRange? =
        null

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean = true

    override fun onItemDragStarted(position: Int) {
        notifyItemChanged(position)
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        val start = minOf(fromPosition, toPosition)
        val end = maxOf(fromPosition, toPosition)
        notifyItemRangeChanged(start, end - start + 1)
    }

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) {
            return
        }
        listener.moveBookmarkDirectory(fromPosition, toPosition)
    }

    class ViewHolder(val binding: BookmarkDirectoryItemBinding) : AbstractDraggableItemViewHolder(
        binding.root
    )

    interface Listener {
        fun editBookmarkDirectory(bookmarkDirectory: BookmarkDirectory)
        fun moveBookmarkDirectory(fromPosition: Int, toPosition: Int)
        fun deleteBookmarkDirectory(bookmarkDirectory: BookmarkDirectory)
    }
}
