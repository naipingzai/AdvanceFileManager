/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.graphics.drawable.NinePatchDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import java8.nio.file.Path
import com.advancefilemanager.app.DialogHostActivity
import com.advancefilemanager.databinding.BookmarkDirectoryListFragmentBinding
import com.advancefilemanager.filelist.FileListActivity
import com.advancefilemanager.navigation.BookmarkDirectories
import com.advancefilemanager.navigation.BookmarkDirectory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.advancefilemanager.navigation.EditBookmarkDirectoryDialogFragment
import com.advancefilemanager.ui.ScrollingViewOnApplyWindowInsetsListener
import com.advancefilemanager.util.fadeToVisibilityUnsafe
import com.advancefilemanager.util.getDrawable
import com.advancefilemanager.R
import com.advancefilemanager.util.launchSafe
import com.advancefilemanager.util.putArgs
import com.advancefilemanager.util.startActivitySafe
import com.advancefilemanager.ui.applyOverlay

class BookmarkDirectoryListFragment : Fragment(), BookmarkDirectoryListAdapter.Listener {
    private val openPathLauncher =
        registerForActivityResult(FileListActivity.OpenDirectoryContract(), ::onOpenPathResult)

    private lateinit var binding: BookmarkDirectoryListFragmentBinding

    private lateinit var adapter: BookmarkDirectoryListAdapter
    private lateinit var dragDropManager: RecyclerViewDragDropManager
    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        BookmarkDirectoryListFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(
            activity, RecyclerView.VERTICAL, false
        )
        adapter = BookmarkDirectoryListAdapter(this)
        dragDropManager = RecyclerViewDragDropManager().apply {
            setDraggingItemShadowDrawable(
                getDrawable(
                    com.h6ah4i.android.materialshadowninepatch.R.drawable.ms9_composite_shadow_z2
                ) as NinePatchDrawable
            )
        }
        wrappedAdapter = dragDropManager.createWrappedAdapter(adapter)
        binding.recyclerView.adapter = wrappedAdapter
        binding.recyclerView.itemAnimator = DraggableItemAnimator()
        dragDropManager.attachRecyclerView(binding.recyclerView)
        binding.recyclerView.setOnApplyWindowInsetsListener(
            ScrollingViewOnApplyWindowInsetsListener(binding.recyclerView)
        )
        binding.fab.setOnClickListener { onAddBookmarkDirectory() }

        Settings.BOOKMARK_DIRECTORIES.observe(viewLifecycleOwner) {
            onBookmarkDirectoryListChanged(it)
        }
    }

    override fun onPause() {
        super.onPause()

        dragDropManager.cancelDrag()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        dragDropManager.release()
        WrapperAdapterUtils.releaseAll(wrappedAdapter)
    }

    private fun onBookmarkDirectoryListChanged(bookmarkDirectories: List<BookmarkDirectory>) {
        binding.emptyView.fadeToVisibilityUnsafe(bookmarkDirectories.isEmpty())
        adapter.replace(bookmarkDirectories)
    }

    private fun onAddBookmarkDirectory() {
        openPathLauncher.launchSafe(null, this)
    }

    private fun onOpenPathResult(result: Path?) {
        result ?: return
        BookmarkDirectories.add(BookmarkDirectory(null, result))
    }

    override fun editBookmarkDirectory(bookmarkDirectory: BookmarkDirectory) {
        startActivitySafe(
            DialogHostActivity.createIntent<EditBookmarkDirectoryDialogFragment>()
                .putArgs(EditBookmarkDirectoryDialogFragment.Args(bookmarkDirectory))
        )
    }

    override fun moveBookmarkDirectory(fromPosition: Int, toPosition: Int) {
        BookmarkDirectories.move(fromPosition, toPosition)
    }

    override fun deleteBookmarkDirectory(bookmarkDirectory: BookmarkDirectory) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(
                getString(
                    R.string.file_delete_message_directory_format,
                    bookmarkDirectory.name ?: bookmarkDirectory.path.toString()
                )
            )
            .setPositiveButton(R.string.delete) { _, _ ->
                BookmarkDirectories.remove(bookmarkDirectory)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .applyOverlay(requireContext())
            .show()
    }
}
