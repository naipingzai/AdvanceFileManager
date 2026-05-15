/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools.recentfiles

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import android.webkit.MimeTypeMap
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import com.advancefilemanager.R
import com.advancefilemanager.databinding.RecentFilesFragmentBinding
import com.advancefilemanager.filelist.FileListActivity
import java8.nio.file.Paths
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException

class RecentFilesFragment : Fragment() {

    private lateinit var binding: RecentFilesFragmentBinding
    private val recentResults = mutableListOf<RecentFileItem>()
    private lateinit var adapter: RecentFileAdapter
    private var scanJob: Job? = null
    private val selectedPaths = mutableListOf<String>()
    private var backPressCallback: OnBackPressedCallback? = null
    private var showingResults = false

    private val directoryPickerLauncher = registerForActivityResult(
        FileListActivity.OpenDirectoryContract()
    ) { path ->
        path ?: return@registerForActivityResult
        addPath(path.toFile().absolutePath)
    }

    @Parcelize
    data class RecentFileItem(
        val path: String,
        val name: String,
        val size: Long,
        val lastModified: Long,
        val isDirectory: Boolean
    ) : Parcelable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        RecentFilesFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (selectedPaths.isEmpty()) {
            val extraPath = arguments?.getString(EXTRA_PATH) ?: activity?.intent?.getStringExtra(EXTRA_PATH)
            if (!extraPath.isNullOrEmpty() && File(extraPath).isDirectory) {
                selectedPaths.add(extraPath)
            } else {
                selectedPaths.add(Environment.getExternalStorageDirectory().absolutePath)
            }
        }

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

        adapter = RecentFileAdapter(recentResults) { position ->
            val item = recentResults.getOrNull(position) ?: return@RecentFileAdapter
            try {
                val path = Paths.get(item.path)
                // Try to open with system default app first
                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    val file = path.toFile()
                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider",
                        file
                    )
                    setDataAndType(uri, getMimeType(file.name))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    startActivity(openIntent)
                } catch (e: Exception) {
                    // Fallback to file manager if no app can open the file
                    val intent = FileListActivity.createViewIntent(path)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, R.string.recent_files_open_error, Snackbar.LENGTH_SHORT).show()
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.searchButton.setOnClickListener { startSearch() }
        binding.addPathButton.setOnClickListener { directoryPickerLauncher.launch(null) }
        refreshPathChips()

        if (savedInstanceState != null) {
            val saved = savedInstanceState.getParcelableArrayList<RecentFileItem>(KEY_RESULTS)
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
                recentResults.clear()
                recentResults.addAll(saved)
                adapter.notifyDataSetChanged()
                binding.configLayout.isVisible = false
                binding.resultsLayout.isVisible = true
                binding.scanProgressBar.isVisible = false
                showingResults = true
                updateBackCallback()
                binding.resultCountText.text = getString(
                    R.string.recent_files_result_count, recentResults.size
                )
            }
        }
    }

    private fun updateBackCallback() {
        backPressCallback?.isEnabled = showingResults
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val isShowingResults = ::binding.isInitialized && binding.resultsLayout.isVisible
        outState.putBoolean(KEY_SHOWING_RESULTS, isShowingResults)
        outState.putStringArrayList(KEY_SELECTED_PATH, ArrayList(selectedPaths))
        if (isShowingResults) {
            outState.putParcelableArrayList(KEY_RESULTS, ArrayList(recentResults.take(MAX_SAVED_RESULTS)))
        }
    }

    companion object {
        const val EXTRA_PATH = "extra_path"
        private const val KEY_RESULTS = "recent_results"
        private const val KEY_SHOWING_RESULTS = "showing_results"
        private const val KEY_SELECTED_PATH = "selected_path"
        private const val MAX_SAVED_RESULTS = 500
    }

    override fun onDestroyView() {
        scanJob?.cancel()
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

    private fun getTimeRangeMillis(): Long {
        val chipId = binding.timeRangeChipGroup.checkedChipId
        return when (chipId) {
            R.id.chip_1hour -> 1L * 60 * 60 * 1000
            R.id.chip_24hours -> 24L * 60 * 60 * 1000
            R.id.chip_7days -> 7L * 24 * 60 * 60 * 1000
            R.id.chip_30days -> 30L * 24 * 60 * 60 * 1000
            else -> 24L * 60 * 60 * 1000
        }
    }

    private fun startSearch() {
        for (p in selectedPaths) {
            val f = File(p)
            if (!f.exists() || !f.isDirectory) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.recent_files_invalid_path) + ": $p",
                    Snackbar.LENGTH_SHORT
                ).show()
                return
            }
        }

        recentResults.clear()
        adapter.notifyDataSetChanged()
        binding.configLayout.isVisible = false
        binding.resultsLayout.isVisible = true
        binding.scanProgressBar.isVisible = true
        binding.resultCountText.text = getString(R.string.recent_files_scanning)
        showingResults = true
        updateBackCallback()

        val timeRange = getTimeRangeMillis()
        val cutoffTime = System.currentTimeMillis() - timeRange
        val includeHidden = binding.includeHiddenSwitch.isChecked
        val maxResults = 500

        scanJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    val found = mutableListOf<RecentFileItem>()
                    for (rootPath in selectedPaths) {
                        scanRecent(File(rootPath), cutoffTime, includeHidden, found, maxResults)
                    }
                    found.sortedByDescending { it.lastModified }
                }
                recentResults.clear()
                recentResults.addAll(results.take(maxResults))
                adapter.notifyDataSetChanged()
                binding.scanProgressBar.isVisible = false
                binding.resultCountText.text = getString(
                    R.string.recent_files_result_count, recentResults.size
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.scanProgressBar.isVisible = false
                binding.resultCountText.text = getString(
                    R.string.recent_files_error, e.message ?: e.toString()
                )
            }
        }
    }

    private suspend fun scanRecent(
        dir: File,
        cutoffTime: Long,
        includeHidden: Boolean,
        results: MutableList<RecentFileItem>,
        maxResults: Int
    ) {
        if (results.size >= maxResults) return
        val children = dir.listFiles() ?: return
        for (child in children) {
            kotlin.coroutines.coroutineContext.ensureActive()
            if (results.size >= maxResults) return
            if (!includeHidden && child.isHidden) continue
            if (child.isDirectory) {
                scanRecent(child, cutoffTime, includeHidden, results, maxResults)
            } else {
                if (child.lastModified() >= cutoffTime) {
                    results.add(
                        RecentFileItem(
                            path = child.absolutePath,
                            name = child.name,
                            size = child.length(),
                            lastModified = child.lastModified(),
                            isDirectory = false
                        )
                    )
                }
            }
        }
    }

    private fun showConfig() {
        scanJob?.cancel()
        binding.configLayout.isVisible = true
        binding.resultsLayout.isVisible = false
        showingResults = false
        updateBackCallback()
    }

    private fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            ?: "application/octet-stream"
    }
}
