/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.emptysearch

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
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlinx.parcelize.Parcelize
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.EmptySearchFragmentBinding
import java8.nio.file.DirectoryStream
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.Paths
import java8.nio.file.attribute.BasicFileAttributes
import naipingzai.materialfile.filelist.FileListActivity
import naipingzai.materialfile.provider.common.delete
import naipingzai.materialfile.provider.common.newDirectoryStream
import naipingzai.materialfile.provider.common.readAttributes
import naipingzai.materialfile.provider.linux.media.MediaScanner
import naipingzai.materialfile.tools.trash.TrashHelper
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException

class EmptySearchFragment : Fragment() {
    private lateinit var binding: EmptySearchFragmentBinding
    private val searchResults = mutableListOf<EmptyItem>()
    private lateinit var adapter: EmptyItemAdapter
    private var searchJob: Job? = null
    private val selectedPaths = mutableListOf(Environment.getExternalStorageDirectory().absolutePath)
    private var optionsMenu: Menu? = null
    private var backPressCallback: OnBackPressedCallback? = null
    private var showingResults = false

    private val directoryPickerLauncher = registerForActivityResult(
        FileListActivity.OpenDirectoryContract()
    ) { path ->
        path ?: return@registerForActivityResult
        addPath(path.toFile().absolutePath)
    }

    @Parcelize
    data class EmptyItem(
        val path: String,
        val name: String,
        val isDirectory: Boolean,
        val size: Long = 0L,
        var isChecked: Boolean = false
    ) : Parcelable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        EmptySearchFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.empty_search, menu)
                optionsMenu = menu
                updateMenuVisibility()
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.action_delete_selected -> { deleteSelected(); true }
                    R.id.action_select_all -> { toggleSelectAll(); true }
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

        adapter = EmptyItemAdapter(
            searchResults,
            onItemClick = { position -> toggleSelection(position) },
            onItemLongClick = { position -> toggleSelection(position) },
            onIconClick = { position -> toggleSelection(position) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.searchButton.setOnClickListener { startSearch() }
        binding.addPathButton.setOnClickListener { directoryPickerLauncher.launch(null) }

        // Accept initial path from caller
        val extraPath = arguments?.getString(EXTRA_PATH) ?: activity?.intent?.getStringExtra(EXTRA_PATH)
        if (!extraPath.isNullOrEmpty() && java.io.File(extraPath).isDirectory) {
            selectedPaths.clear()
            selectedPaths.add(extraPath)
            binding.addPathButton.isVisible = false
        }
        refreshPathChips()

        // Restore saved state
        if (savedInstanceState != null) {
            val saved = savedInstanceState.getParcelableArrayList<EmptyItem>(KEY_RESULTS)
            val wasShowingResults = savedInstanceState.getBoolean(KEY_SHOWING_RESULTS, false)
            val savedPathList = savedInstanceState.getStringArrayList(KEY_SELECTED_PATH)
            selectedPaths.clear()
            if (savedPathList != null) {
                selectedPaths.addAll(savedPathList.filter { it.isNotEmpty() })
            }
            if (selectedPaths.isEmpty()) {
                selectedPaths.add(Environment.getExternalStorageDirectory().absolutePath)
            }
            refreshPathChips()
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
                    R.string.empty_search_result_count, searchResults.size
                )
            }
        }

    }



    private fun updateMenuVisibility() {
        val menu = optionsMenu ?: return
        val hasChecked = showingResults && searchResults.any { it.isChecked }
        menu.findItem(R.id.action_delete_selected)?.isVisible = hasChecked
        val selectAllItem = menu.findItem(R.id.action_select_all)
        selectAllItem?.isVisible = showingResults && searchResults.isNotEmpty()
        val allSelected = showingResults && searchResults.isNotEmpty() && searchResults.all { it.isChecked }
        selectAllItem?.title = if (allSelected) {
            getString(R.string.empty_search_deselect_all)
        } else {
            getString(R.string.empty_search_select_all)
        }
    }

    private fun updateBackCallback() {
        backPressCallback?.isEnabled = showingResults
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val isShowingResults = ::binding.isInitialized &&
            binding.resultsLayout.isVisible
        outState.putBoolean(KEY_SHOWING_RESULTS, isShowingResults)
        outState.putStringArrayList(KEY_SELECTED_PATH, ArrayList(selectedPaths))
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

    private fun addPath(path: String) {
        if (path !in selectedPaths) {
            selectedPaths.add(path)
            refreshPathChips()
        }
    }

    private fun removePath(path: String) {
        if (selectedPaths.size > 1) {
            selectedPaths.remove(path)
            refreshPathChips()
        }
    }

    private fun refreshPathChips() {
        binding.pathChipGroup.removeAllViews()
        selectedPaths.forEach { path ->
            val chip = layoutInflater.inflate(
                R.layout.chip_input_closeable, binding.pathChipGroup, false
            ) as Chip
            chip.text = path
            chip.isCloseIconVisible = selectedPaths.size > 1
            chip.setOnCloseIconClickListener { removePath(path) }
            binding.pathChipGroup.addView(chip)
        }
    }

    private fun startSearch() {
        val searchFiles = binding.chipEmptyFiles.isChecked
        val searchFolders = binding.chipEmptyFolders.isChecked
        val includeHiddenDirs = binding.includeHiddenDirsSwitch.isChecked
        val includeHiddenFiles = binding.includeHiddenFilesSwitch.isChecked
        val followSymlinks = binding.followSymlinksSwitch.isChecked

        if (!searchFiles && !searchFolders) {
            Snackbar.make(binding.root, R.string.empty_search_select_type, Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        // Validate all paths
        val rootPaths = mutableListOf<Path>()
        for (p in selectedPaths) {
            val rp = Paths.get(p)
            val attrs = try {
                rp.readAttributes(BasicFileAttributes::class.java)
            } catch (e: IOException) {
                Snackbar.make(binding.root, getString(R.string.empty_search_invalid_path) + ": $p", Snackbar.LENGTH_SHORT)
                    .show()
                return
            }
            if (!attrs.isDirectory) {
                Snackbar.make(binding.root, getString(R.string.empty_search_invalid_path) + ": $p", Snackbar.LENGTH_SHORT)
                    .show()
                return
            }
            rootPaths.add(rp)
        }

        searchResults.clear()
        adapter.notifyDataSetChanged()
        binding.configLayout.isVisible = false
        binding.resultsLayout.isVisible = true
        binding.searchProgressBar.isVisible = true
        binding.resultCountText.text = getString(R.string.empty_search_searching)
        showingResults = true
        updateMenuVisibility()
        updateBackCallback()

        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    val found = mutableListOf<EmptyItem>()
                    for (rootPath in rootPaths) {
                        searchEmpty(
                            rootPath, searchFiles, searchFolders,
                            includeHiddenDirs, includeHiddenFiles, followSymlinks,
                            found
                        )
                    }
                    found
                }
                searchResults.addAll(results)
                adapter.notifyDataSetChanged()
                binding.searchProgressBar.isVisible = false
                binding.resultCountText.text = getString(
                    R.string.empty_search_result_count, searchResults.size
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.searchProgressBar.isVisible = false
                binding.resultCountText.text = getString(
                    R.string.empty_search_error, e.message ?: e.toString()
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

    private suspend fun searchEmpty(
        dirPath: Path,
        searchFiles: Boolean,
        searchFolders: Boolean,
        includeHiddenDirs: Boolean,
        includeHiddenFiles: Boolean,
        followSymlinks: Boolean,
        results: MutableList<EmptyItem>
    ) {
        searchEmptyRecursive(
            dirPath, null, searchFiles, searchFolders,
            includeHiddenDirs, includeHiddenFiles, followSymlinks,
            results
        ) { }
    }

    private suspend fun searchEmptyRecursive(
        dirPath: Path,
        existingStream: DirectoryStream<Path>?,
        searchFiles: Boolean,
        searchFolders: Boolean,
        includeHiddenDirs: Boolean,
        includeHiddenFiles: Boolean,
        followSymlinks: Boolean,
        results: MutableList<EmptyItem>,
        onScan: () -> Unit
    ) {
        val directoryStream = existingStream ?: try {
            dirPath.newDirectoryStream()
        } catch (e: IOException) {
            return
        }
        try {
            for (path in directoryStream) {
                coroutineContext.ensureActive()
                onScan()
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
                    if (searchFolders) {
                        // Open child directory stream once to check emptiness,
                        // and reuse it for recursion if non-empty, avoiding a
                        // redundant second DirectoryStream open.
                        val childStream = try {
                            path.newDirectoryStream()
                        } catch (e: IOException) {
                            null
                        }
                        if (childStream != null) {
                            val childIterator = childStream.iterator()
                            if (!childIterator.hasNext()) {
                                childStream.close()
                                results.add(
                                    EmptyItem(
                                        path = path.toString(),
                                        name = name,
                                        isDirectory = true,
                                        size = 0L
                                    )
                                )
                                if (results.size % 50 == 0) {
                                    updateProgress(
                                        R.string.empty_search_scanning_progress, results.size
                                    )
                                }
                            } else {
                                // Non-empty directory, recurse using the already-opened stream
                                searchEmptyRecursive(
                                    path, childStream, searchFiles, searchFolders,
                                    includeHiddenDirs, includeHiddenFiles, followSymlinks,
                                    results, onScan
                                )
                            }
                        }
                    } else {
                        searchEmptyRecursive(
                            path, null, searchFiles, searchFolders,
                            includeHiddenDirs, includeHiddenFiles, followSymlinks,
                            results, onScan
                        )
                    }
                } else if (attributes.isRegularFile) {
                    if (!includeHiddenFiles && isHidden) continue
                    if (searchFiles && attributes.size() == 0L) {
                        results.add(
                            EmptyItem(
                                path = path.toString(),
                                name = name,
                                isDirectory = false,
                                size = attributes.size()
                            )
                        )
                        if (results.size % 50 == 0) {
                            updateProgress(
                                R.string.empty_search_scanning_progress, results.size
                            )
                        }
                    }
                }
            }
        } finally {
            directoryStream.close()
        }
    }

    private fun deleteSelected() {
        val selected = searchResults.filter { it.isChecked }
        if (selected.isEmpty()) {
            Snackbar.make(binding.root, R.string.empty_search_none_selected, Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.empty_search_delete_title)
            .setMessage(getString(R.string.empty_search_delete_message, selected.size))
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
                    binding.resultCountText.text = getString(
                        R.string.empty_search_result_count, searchResults.size
                    )
                    Snackbar.make(
                        binding.root,
                        getString(R.string.empty_search_deleted_count, deleted),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
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

    private fun toggleSelectAll() {
        val allSelected = searchResults.isNotEmpty() && searchResults.all { it.isChecked }
        val newState = !allSelected
        searchResults.forEach { it.isChecked = newState }
        adapter.notifyDataSetChanged()
        updateMenuVisibility()
    }

}
