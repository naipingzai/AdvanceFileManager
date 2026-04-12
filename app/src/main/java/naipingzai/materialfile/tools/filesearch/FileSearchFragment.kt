/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.filesearch

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
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
import java8.nio.file.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlinx.parcelize.Parcelize
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.FileSearchFragmentBinding
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.file.fileProviderUri
import naipingzai.materialfile.file.guessFromPath
import naipingzai.materialfile.filelist.FileListActivity
import naipingzai.materialfile.provider.common.delete
import naipingzai.materialfile.provider.common.newDirectoryStream
import naipingzai.materialfile.provider.common.readAttributes
import naipingzai.materialfile.provider.linux.media.MediaScanner
import naipingzai.materialfile.tools.trash.TrashHelper
import naipingzai.materialfile.tools.FileTypeUtils
import naipingzai.materialfile.tools.FileTypeUtils.FileTypeFilter
import naipingzai.materialfile.util.createViewIntent
import naipingzai.materialfile.util.startActivitySafe
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException

class FileSearchFragment : Fragment() {
    private lateinit var binding: FileSearchFragmentBinding
    private val searchResults = mutableListOf<FileSearchItem>()
    private lateinit var adapter: FileSearchAdapter
    private var searchJob: Job? = null
    private var selectedPath: String = Environment.getExternalStorageDirectory().absolutePath
    private var optionsMenu: Menu? = null
    private var backPressCallback: OnBackPressedCallback? = null
    private var showingResults = false

    private val directoryPickerLauncher = registerForActivityResult(
        FileListActivity.OpenDirectoryContract()
    ) { path ->
        path ?: return@registerForActivityResult
        selectedPath = path.toFile().absolutePath
        binding.pathText.text = selectedPath
    }

    @Parcelize
    data class FileSearchItem(
        val path: String,
        val name: String,
        val size: Long,
        val lastModified: Long,
        val isDirectory: Boolean,
        var isChecked: Boolean = false
    ) : Parcelable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FileSearchFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.file_search, menu)
                optionsMenu = menu
                updateMenuVisibility()
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.action_delete_selected -> { deleteSelected(); true }
                    else -> false
                }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val activity = requireActivity() as AppCompatActivity
        val embedded = arguments?.getBoolean("embedded") == true
        if (embedded) {
            (binding.toolbar.parent as? android.view.View)?.visibility = android.view.View.GONE
        } else {
            activity.setSupportActionBar(binding.toolbar)
            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        activity.onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    if (showingResults) {
                        showConfig()
                    } else {
                        isEnabled = false
                        activity.onBackPressedDispatcher.onBackPressed()
                    }
                }
            }.also { backPressCallback = it }
        )

        adapter = FileSearchAdapter(
            searchResults,
            onItemClick = { position ->
                if (searchResults.any { it.isChecked }) {
                    toggleSelection(position)
                } else {
                    openItem(position)
                }
            },
            onItemLongClick = { position ->
                if (searchResults.any { it.isChecked }) {
                    openItem(position)
                } else {
                    toggleSelection(position)
                }
            },
            onIconClick = { position ->
                toggleSelection(position)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.searchButton.setOnClickListener { startSearch() }
        binding.browseButton.setOnClickListener { directoryPickerLauncher.launch(null) }

        // Accept initial path from caller
        val extraPath = arguments?.getString(EXTRA_PATH) ?: activity?.intent?.getStringExtra(EXTRA_PATH)
        if (!extraPath.isNullOrEmpty() && java.io.File(extraPath).isDirectory) {
            selectedPath = extraPath
            binding.browseButton.isVisible = false
        }
        binding.pathText.text = selectedPath

        // Restore saved state
        if (savedInstanceState != null) {
            val saved = savedInstanceState.getParcelableArrayList<FileSearchItem>(KEY_RESULTS)
            val wasShowingResults = savedInstanceState.getBoolean(KEY_SHOWING_RESULTS, false)
            selectedPath = savedInstanceState.getString(KEY_SELECTED_PATH, selectedPath)
            binding.pathText.text = selectedPath
            if (wasShowingResults && saved != null) {
                searchResults.clear()
                searchResults.addAll(saved)
                adapter.notifyDataSetChanged()
                binding.configLayout.isVisible = false
                binding.resultsLayout.isVisible = true
                binding.searchProgressBar.isVisible = false
                showingResults = true
                updateMenuVisibility()
                updateBackCallback()
                binding.resultCountText.text = getString(
                    R.string.file_search_result_count, searchResults.size
                )
            }
        }
    }



    private fun updateMenuVisibility() {
        val menu = optionsMenu ?: return
        val hasChecked = showingResults && searchResults.any { it.isChecked }
        menu.findItem(R.id.action_delete_selected)?.isVisible = hasChecked
    }

    private fun updateBackCallback() {
        backPressCallback?.isEnabled = showingResults
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val isShowingResults = ::binding.isInitialized &&
            binding.resultsLayout.isVisible
        outState.putBoolean(KEY_SHOWING_RESULTS, isShowingResults)
        outState.putString(KEY_SELECTED_PATH, selectedPath)
        if (isShowingResults) {
            outState.putParcelableArrayList(KEY_RESULTS, ArrayList(searchResults.take(MAX_SAVED_RESULTS)))
        }
    }

    companion object {
        const val EXTRA_PATH = "extra_path"
        private const val KEY_RESULTS = "search_results"
        private const val KEY_SHOWING_RESULTS = "showing_results"
        private const val KEY_SELECTED_PATH = "selected_path"
        private const val MAX_SAVED_RESULTS = 500
    }

    override fun onDestroyView() {
        searchJob?.cancel()
        super.onDestroyView()
    }

    private fun getFileTypeFilter(): FileTypeFilter {
        return when {
            binding.chipTypeImage.isChecked -> FileTypeFilter.IMAGE
            binding.chipTypeVideo.isChecked -> FileTypeFilter.VIDEO
            binding.chipTypeAudio.isChecked -> FileTypeFilter.AUDIO
            binding.chipTypeDocument.isChecked -> FileTypeFilter.DOCUMENT
            else -> FileTypeFilter.ALL
        }
    }

    private fun startSearch() {
        val path = selectedPath
        val namePattern = binding.nameInput.text?.toString()?.trim() ?: ""
        val extension = binding.extensionInput.text?.toString()?.trim() ?: ""
        val minSizeStr = binding.minSizeInput.text?.toString()?.trim() ?: ""
        val maxSizeStr = binding.maxSizeInput.text?.toString()?.trim() ?: ""
        val fileTypeFilter = getFileTypeFilter()
        val includeHiddenDirs = binding.includeHiddenDirsSwitch.isChecked
        val includeHiddenFiles = binding.includeHiddenFilesSwitch.isChecked
        val followSymlinks = binding.followSymlinksSwitch.isChecked

        val rootPath = Paths.get(path)
        val rootAttrs = try {
            rootPath.readAttributes(BasicFileAttributes::class.java)
        } catch (e: IOException) {
            Snackbar.make(binding.root, R.string.file_search_invalid_path, Snackbar.LENGTH_SHORT)
                .show()
            return
        }
        if (!rootAttrs.isDirectory) {
            Snackbar.make(binding.root, R.string.file_search_invalid_path, Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        val minSize = minSizeStr.toLongOrNull()?.times(1024) ?: 0L
        val maxSize = maxSizeStr.toLongOrNull()?.times(1024) ?: Long.MAX_VALUE

        searchResults.clear()
        adapter.notifyDataSetChanged()
        binding.configLayout.isVisible = false
        binding.resultsLayout.isVisible = true
        binding.searchProgressBar.isVisible = true
        binding.resultCountText.text = getString(R.string.file_search_searching)
        showingResults = true
        updateMenuVisibility()
        updateBackCallback()

        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    val found = mutableListOf<FileSearchItem>()
                    searchFiles(
                        rootPath, namePattern, extension, minSize, maxSize,
                        fileTypeFilter, includeHiddenDirs, includeHiddenFiles,
                        followSymlinks, found
                    )
                    found
                }
                searchResults.addAll(results)
                adapter.notifyDataSetChanged()
                binding.searchProgressBar.isVisible = false
                val totalSize = searchResults.sumOf { it.size }
                binding.resultCountText.text = getString(
                    R.string.file_search_result_summary,
                    searchResults.size,
                    TrashHelper.formatFileSize(totalSize)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.searchProgressBar.isVisible = false
                binding.resultCountText.text = getString(
                    R.string.file_search_error, e.message ?: e.toString()
                )
            }
        }
    }

    private suspend fun updateProgress(resId: Int, vararg args: Any) {
        withContext(Dispatchers.Main) {
            if (isAdded) {
                binding.resultCountText.text = getString(resId, *args)
            }
        }
    }

    private suspend fun searchFiles(
        dirPath: Path,
        namePattern: String,
        extension: String,
        minSize: Long,
        maxSize: Long,
        fileTypeFilter: FileTypeFilter,
        includeHiddenDirs: Boolean,
        includeHiddenFiles: Boolean,
        followSymlinks: Boolean,
        results: MutableList<FileSearchItem>
    ) {
        val directoryStream = try {
            dirPath.newDirectoryStream()
        } catch (e: IOException) {
            return
        }
        directoryStream.use {
            for (path in it) {
                coroutineContext.ensureActive()
                val attributes = try {
                    path.readAttributes(
                        BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS
                    )
                } catch (e: IOException) {
                    continue
                }
                val name = path.fileName.toString()
                val isHidden = name.startsWith(".")
                if (!followSymlinks && attributes.isSymbolicLink) continue
                if (attributes.isDirectory) {
                    if (!includeHiddenDirs && isHidden) continue
                    searchFiles(
                        path, namePattern, extension, minSize, maxSize,
                        fileTypeFilter, includeHiddenDirs, includeHiddenFiles,
                        followSymlinks, results
                    )
                } else if (attributes.isRegularFile) {
                    if (!includeHiddenFiles && isHidden) continue
                    var matches = true
                    if (namePattern.isNotEmpty()) {
                        if (!name.contains(namePattern, ignoreCase = true)) matches = false
                    }
                    if (matches && extension.isNotEmpty()) {
                        val extensions = extension.split(",").map { it.trim().lowercase() }
                        val ext = name.substringAfterLast('.', "").lowercase()
                        if (ext !in extensions) matches = false
                    }
                    val size = attributes.size()
                    if (matches && (size < minSize || size > maxSize)) matches = false
                    if (matches && !FileTypeUtils.matchesFileType(name, fileTypeFilter)) matches = false
                    if (matches) {
                        results.add(
                            FileSearchItem(
                                path = path.toString(),
                                name = name,
                                size = size,
                                lastModified = attributes.lastModifiedTime().toMillis(),
                                isDirectory = false
                            )
                        )
                        if (results.size % 100 == 0) {
                            updateProgress(
                                R.string.file_search_scanning_progress, results.size
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showConfig() {
        searchJob?.cancel()
        binding.configLayout.isVisible = true
        binding.resultsLayout.isVisible = false
        showingResults = false
        updateMenuVisibility()
        updateBackCallback()
    }

    private fun toggleSelection(position: Int) {
        if (position in searchResults.indices) {
            searchResults[position].isChecked = !searchResults[position].isChecked
            adapter.notifyItemChanged(position)
            updateMenuVisibility()
        }
    }

    private fun deleteSelected() {
        val selected = searchResults.filter { it.isChecked }
        if (selected.isEmpty()) {
            Snackbar.make(binding.root, R.string.file_search_none_selected, Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.file_search_delete_title)
            .setMessage(getString(
                R.string.file_search_delete_message,
                selected.size,
                TrashHelper.formatFileSize(selected.sumOf { it.size })
            ))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    var deleted = 0
                    withContext(Dispatchers.IO) {
                        selected.forEach { item ->
                            try {
                                val filePath = Paths.get(item.path)
                                if (!TrashHelper.moveToTrash(filePath)) {
                                    filePath.delete()
                                }
                                MediaScanner.scan(filePath.toFile(), true)
                                deleted++
                            } catch (_: Exception) {}
                        }
                    }
                    searchResults.removeAll { it.isChecked }
                    adapter.notifyDataSetChanged()
                    updateMenuVisibility()
                    val totalSize = searchResults.sumOf { it.size }
                    binding.resultCountText.text = getString(
                        R.string.file_search_result_summary,
                        searchResults.size,
                        TrashHelper.formatFileSize(totalSize)
                    )
                    Snackbar.make(
                        binding.root,
                        getString(R.string.file_search_deleted_count, deleted),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openItem(position: Int) {
        if (position !in searchResults.indices) return
        val item = searchResults[position]
        try {
            if (item.isDirectory) {
                val intent = FileListActivity.createViewIntent(Paths.get(item.path))
                startActivitySafe(intent)
            } else {
                val path = Paths.get(item.path)
                val uri = path.fileProviderUri
                val mimeType = MimeType.guessFromPath(item.name)
                val intent = uri.createViewIntent(mimeType)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivitySafe(intent)
            }
        } catch (e: Exception) {
            Snackbar.make(binding.root, R.string.open_file_error, Snackbar.LENGTH_SHORT).show()
        }
    }
}
