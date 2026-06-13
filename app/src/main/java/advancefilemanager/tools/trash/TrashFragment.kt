/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools.trash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.advancefilemanager.ui.applyOverlay
import java8.nio.file.Path
import java8.nio.file.Paths
import java8.nio.file.attribute.BasicFileAttributes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.advancefilemanager.R
import com.advancefilemanager.databinding.TrashFragmentBinding
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.file.fileProviderUri
import com.advancefilemanager.file.guessFromPath
import com.advancefilemanager.provider.common.createDirectories
import com.advancefilemanager.provider.common.delete
import com.advancefilemanager.provider.common.exists
import com.advancefilemanager.provider.common.moveTo
import com.advancefilemanager.provider.common.newDirectoryStream
import com.advancefilemanager.provider.common.readAttributes
import com.advancefilemanager.provider.linux.media.MediaScanner
import com.advancefilemanager.util.createViewIntent
import com.advancefilemanager.util.startActivitySafe
import java.io.IOException

class TrashFragment : Fragment() {
    private lateinit var binding: TrashFragmentBinding
    private val trashItems = mutableListOf<TrashItem>()
    private lateinit var adapter: TrashAdapter

    data class TrashItem(
        val originalPath: String,
        val trashPath: String,
        val name: String,
        val size: Long,
        val deleteTime: Long,
        val isDirectory: Boolean,
        var isChecked: Boolean = false
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        TrashFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.trash, menu)
                optionsMenu = menu
                updateMenuState()
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.action_select_all -> { toggleSelectAll(); true }
                    R.id.action_restore_selected -> { restoreSelected(); true }
                    R.id.action_delete_selected -> { deleteSelected(); true }
                    else -> false
                }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        activity.onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    if (trashItems.any { it.isChecked }) {
                        clearSelection()
                    } else {
                        isEnabled = false
                        activity.onBackPressedDispatcher.onBackPressed()
                    }
                }
            }.also { backPressCallback = it }
        )

        adapter = TrashAdapter(
            trashItems,
            onItemClick = { position ->
                if (position !in trashItems.indices) return@TrashAdapter
                if (trashItems.any { it.isChecked }) {
                    // In selection mode: click = toggle selection
                    trashItems[position].isChecked = !trashItems[position].isChecked
                    adapter.notifyItemChanged(position)
                    updateMenuState()
                } else {
                    // Normal mode: click = open
                    openTrashFile(position)
                }
            },
            onItemLongClick = { position ->
                if (position !in trashItems.indices) return@TrashAdapter
                if (trashItems.any { it.isChecked }) {
                    // In selection mode: long-click = open
                    openTrashFile(position)
                } else {
                    // Normal mode: long-click = select
                    trashItems[position].isChecked = !trashItems[position].isChecked
                    adapter.notifyItemChanged(position)
                    updateMenuState()
                }
            },
            onIconClick = { position ->
                if (position !in trashItems.indices) return@TrashAdapter
                // Icon click always toggles selection
                trashItems[position].isChecked = !trashItems[position].isChecked
                adapter.notifyItemChanged(position)
                updateMenuState()
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.emptyTrashFab.setOnClickListener {
            if (trashItems.isEmpty()) {
                Snackbar.make(binding.root, R.string.trash_empty, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.trash_empty_action)
                .setMessage(R.string.trash_empty_confirm)
                .setPositiveButton(android.R.string.ok) { _, _ -> emptyTrash() }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .applyOverlay(requireContext())
                .show()
        }

        loadTrashItems()
    }

    private var optionsMenu: Menu? = null
    private var backPressCallback: OnBackPressedCallback? = null

    private fun clearSelection() {
        trashItems.forEach { it.isChecked = false }
        adapter.notifyDataSetChanged()
        updateMenuState()
    }

    private fun toggleSelectAll() {
        val allSelected = trashItems.all { it.isChecked }
        trashItems.forEach { it.isChecked = !allSelected }
        adapter.notifyDataSetChanged()
        updateMenuState()
    }

    private fun updateMenuState() {
        val menu = optionsMenu ?: return
        val hasItems = trashItems.isNotEmpty()
        val hasSelected = trashItems.any { it.isChecked }
        val allSelected = trashItems.all { it.isChecked }

        backPressCallback?.isEnabled = hasSelected

        menu.findItem(R.id.action_select_all)?.apply {
            isVisible = hasItems
            setTitle(if (allSelected) R.string.trash_deselect_all else R.string.trash_select_all)
        }
        menu.findItem(R.id.action_restore_selected)?.isVisible = hasSelected
        menu.findItem(R.id.action_delete_selected)?.isVisible = hasSelected
    }

    private fun openTrashFile(position: Int) {
        val item = trashItems[position]
        if (item.isDirectory) {
            Snackbar.make(binding.root, R.string.trash_open_directory_hint, Snackbar.LENGTH_SHORT)
                .show()
            return
        }
        try {
            val path = Paths.get(item.trashPath)
            val uri = path.fileProviderUri
            val mimeType = MimeType.guessFromPath(item.name)
            val intent = uri.createViewIntent(mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivitySafe(intent)
        } catch (e: Exception) {
            Snackbar.make(binding.root, R.string.trash_open_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun loadTrashItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            val unknownOriginStr = getString(R.string.trash_unknown_origin)
            val items = withContext(Dispatchers.IO) {
                val result = mutableListOf<TrashItem>()
                val trashDir = TrashHelper.getTrashDir()
                val metaDir = TrashHelper.getTrashMetaDir()

                try {
                    trashDir.newDirectoryStream().use { stream ->
                        for (entry in stream) {
                            val name = entry.fileName.toString()
                            if (name == ".meta") continue
                            val metaPath = metaDir.resolve("$name.meta")
                            val originalPath = if (metaPath.exists()) {
                                metaPath.toFile().readText().trim()
                            } else {
                                unknownOriginStr
                            }
                            val attrs = try {
                                entry.readAttributes(BasicFileAttributes::class.java)
                            } catch (e: IOException) {
                                continue
                            }
                            result.add(
                                TrashItem(
                                    originalPath = originalPath,
                                    trashPath = entry.toString(),
                                    name = name,
                                    size = if (attrs.isDirectory) getDirSize(entry) else attrs.size(),
                                    deleteTime = attrs.lastModifiedTime().toMillis(),
                                    isDirectory = attrs.isDirectory
                                )
                            )
                        }
                    }
                } catch (_: IOException) {}
                result.sortByDescending { it.deleteTime }
                result
            }
            trashItems.clear()
            trashItems.addAll(items)
            adapter.notifyDataSetChanged()
            updateEmptyView()
        }
    }

    private fun getDirSize(dirPath: Path, depth: Int = 0): Long {
        if (depth > 64) return 0L
        var size = 0L
        try {
            dirPath.newDirectoryStream().use { stream ->
                for (entry in stream) {
                    val attrs = try {
                        entry.readAttributes(BasicFileAttributes::class.java)
                    } catch (e: IOException) {
                        continue
                    }
                    size += if (attrs.isDirectory) getDirSize(entry, depth + 1) else attrs.size()
                }
            }
        } catch (_: IOException) {}
        return size
    }

    private fun updateEmptyView() {
        binding.emptyView.isVisible = trashItems.isEmpty()
        binding.recyclerView.isVisible = trashItems.isNotEmpty()
        updateMenuState()
    }

    private fun emptyTrash() {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val trashDir = TrashHelper.getTrashDir()
                try {
                    trashDir.newDirectoryStream().use { stream ->
                        for (entry in stream) {
                            TrashHelper.deleteRecursivelySafe(entry)
                        }
                    }
                } catch (_: IOException) {}
            }
            trashItems.clear()
            adapter.notifyDataSetChanged()
            updateEmptyView()
            Snackbar.make(binding.root, R.string.trash_emptied, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun restoreSelected() {
        val selected = trashItems.filter { it.isChecked }
        if (selected.isEmpty()) {
            Snackbar.make(binding.root, R.string.trash_none_selected, Snackbar.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            var restored = 0
            var skipped = 0
            withContext(Dispatchers.IO) {
                selected.forEach { item ->
                    try {
                        // Skip items with unknown/invalid original path
                        if (!item.originalPath.startsWith("/")) {
                            skipped++
                            return@forEach
                        }
                        val trashPath = Paths.get(item.trashPath)
                        val originalPath = Paths.get(item.originalPath)
                        if (originalPath.exists()) {
                            skipped++
                            return@forEach
                        }
                        originalPath.parent?.createDirectories()
                        trashPath.moveTo(originalPath)
                        MediaScanner.scan(originalPath.toFile())
                        val metaPath = TrashHelper.getTrashMetaDir()
                            .resolve(trashPath.fileName.toString() + ".meta")
                        try { metaPath.delete() } catch (_: IOException) {}
                        restored++
                    } catch (_: Exception) {}
                }
            }
            Snackbar.make(
                binding.root,
                getString(R.string.trash_restored_count, restored),
                Snackbar.LENGTH_SHORT
            ).show()
            loadTrashItems()
        }
    }

    private fun deleteSelected() {
        val selected = trashItems.filter { it.isChecked }
        if (selected.isEmpty()) {
            Snackbar.make(binding.root, R.string.trash_none_selected, Snackbar.LENGTH_SHORT).show()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.trash_delete_selected)
            .setMessage(getString(R.string.trash_delete_confirm, selected.size))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val count = selected.size
                    withContext(Dispatchers.IO) {
                        selected.forEach { item ->
                            val trashPath = Paths.get(item.trashPath)
                            TrashHelper.deleteRecursivelySafe(trashPath)
                            val metaPath = TrashHelper.getTrashMetaDir()
                                .resolve(trashPath.fileName.toString() + ".meta")
                            try { metaPath.delete() } catch (_: IOException) {}
                        }
                    }
                    Snackbar.make(
                        binding.root,
                        getString(R.string.trash_deleted_count, count),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    loadTrashItems()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .applyOverlay(requireContext())
            .show()
    }
}
