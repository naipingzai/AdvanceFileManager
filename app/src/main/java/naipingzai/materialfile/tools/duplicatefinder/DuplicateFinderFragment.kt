/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.duplicatefinder

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
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java8.nio.file.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.DuplicateFinderFragmentBinding
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.file.fileProviderUri
import naipingzai.materialfile.file.guessFromPath
import naipingzai.materialfile.filelist.FileListActivity
import naipingzai.materialfile.provider.common.delete
import naipingzai.materialfile.provider.common.newDirectoryStream
import naipingzai.materialfile.provider.common.newInputStream
import naipingzai.materialfile.provider.common.readAttributes
import naipingzai.materialfile.provider.linux.media.MediaScanner
import naipingzai.materialfile.tools.FileTypeUtils
import naipingzai.materialfile.tools.FileTypeUtils.FileTypeFilter
import naipingzai.materialfile.tools.trash.TrashHelper
import naipingzai.materialfile.util.createViewIntent
import naipingzai.materialfile.util.startActivitySafe
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.zip.CRC32
import kotlinx.coroutines.CancellationException

class DuplicateFinderFragment : Fragment() {
    private lateinit var binding: DuplicateFinderFragmentBinding
    private val duplicateGroups = mutableListOf<DuplicateGroup>()
    private lateinit var adapter: DuplicateAdapter
    private var scanJob: Job? = null
    private var currentSortMode = SortMode.PATH_LONGEST
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
    data class DuplicateFile(
        val path: String,
        val name: String,
        val size: Long,
        val lastModified: Long,
        var isChecked: Boolean = false
    ) : Parcelable

    @Parcelize
    data class DuplicateGroup(
        val hash: String,
        val size: Long,
        val files: MutableList<DuplicateFile>
    ) : Parcelable

    private data class ScannedFile(val path: Path, val size: Long, val lastModified: Long)

    enum class SortMode {
        PATH_LONGEST, PATH_SHORTEST, DATE_NEWEST, DATE_OLDEST
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        DuplicateFinderFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.duplicate_finder, menu)
                optionsMenu = menu
                updateMenuVisibility()
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.action_keep_rule -> { showKeepRuleDialog(); true }
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

        adapter = DuplicateAdapter(
            duplicateGroups,
            onFileCheck = { groupIndex, fileIndex ->
                val group = duplicateGroups.getOrNull(groupIndex) ?: return@DuplicateAdapter
                val file = group.files.getOrNull(fileIndex) ?: return@DuplicateAdapter
                file.isChecked = !file.isChecked
                // Find the flat list position for this specific file item
                // (skip headers and items in preceding groups)
                val flatPosition = adapter.findFlatPosition(groupIndex, fileIndex)
                if (flatPosition >= 0) {
                    adapter.notifyItemChanged(flatPosition)
                }
                updateMenuVisibility()
            },
            onFileOpen = { groupIndex, fileIndex ->
                openDuplicateFile(groupIndex, fileIndex)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.scanButton.setOnClickListener { startScan() }
        binding.addPathButton.setOnClickListener { directoryPickerLauncher.launch(null) }

        // Accept initial path from caller
        val extraPath = arguments?.getString(EXTRA_PATH) ?: activity?.intent?.getStringExtra(EXTRA_PATH)
        if (!extraPath.isNullOrEmpty() && java.io.File(extraPath).isDirectory) {
            selectedPaths.clear()
            selectedPaths.add(extraPath)
            binding.addPathButton.isVisible = false
        }

        // Restore saved state
        if (savedInstanceState != null) {
            val saved = savedInstanceState.getParcelableArrayList<DuplicateGroup>(KEY_RESULTS)
            val wasShowingResults = savedInstanceState.getBoolean(KEY_SHOWING_RESULTS, false)
            val savedPaths = savedInstanceState.getStringArrayList(KEY_SELECTED_PATHS)
            val savedSortOrdinal = savedInstanceState.getInt(KEY_SORT_MODE, 0)
            currentSortMode = SortMode.entries.getOrElse(savedSortOrdinal) { SortMode.PATH_LONGEST }
            if (savedPaths != null && savedPaths.isNotEmpty()) {
                selectedPaths.clear()
                selectedPaths.addAll(savedPaths)
            }
            if (wasShowingResults && saved != null) {
                duplicateGroups.clear()
                duplicateGroups.addAll(saved)
                adapter.refreshFlatList()
                adapter.notifyDataSetChanged()
                binding.configLayout.isVisible = false
                binding.resultsLayout.isVisible = true
                binding.scanProgressBar.isVisible = false
                showingResults = true
                updateMenuVisibility()
                updateBackCallback()
                val totalDuplicates = duplicateGroups.sumOf { it.files.size - 1 }
                val totalWastedSize = duplicateGroups.sumOf { it.size * (it.files.size - 1) }
                binding.resultSummaryText.text = getString(
                    R.string.duplicate_result_summary,
                    duplicateGroups.size,
                    totalDuplicates,
                    TrashHelper.formatFileSize(totalWastedSize)
                )
            }
        }

        refreshPathChips()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val isShowingResults = ::binding.isInitialized &&
            binding.resultsLayout.isVisible
        outState.putBoolean(KEY_SHOWING_RESULTS, isShowingResults)
        outState.putStringArrayList(KEY_SELECTED_PATHS, ArrayList(selectedPaths))
        outState.putInt(KEY_SORT_MODE, currentSortMode.ordinal)
        if (isShowingResults) {
            outState.putParcelableArrayList(KEY_RESULTS, ArrayList(duplicateGroups.take(MAX_SAVED_GROUPS)))
        }
    }

    companion object {
        const val EXTRA_PATH = "extra_path"

        private const val KEY_RESULTS = "duplicate_results"
        private const val KEY_SHOWING_RESULTS = "showing_results"
        private const val KEY_SELECTED_PATHS = "selected_paths"
        private const val KEY_SORT_MODE = "sort_mode"
        private const val MAX_SAVED_GROUPS = 500
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

    private fun updateMenuVisibility() {
        val menu = optionsMenu ?: return
        menu.findItem(R.id.action_keep_rule)?.isVisible = showingResults
        val hasChecked = showingResults && duplicateGroups.any { group ->
            group.files.any { it.isChecked }
        }
        menu.findItem(R.id.action_delete_selected)?.isVisible = hasChecked
    }

    private fun updateBackCallback() {
        backPressCallback?.isEnabled = showingResults
    }

    private fun getSelectedAlgorithms(): List<String> {
        val algorithms = mutableListOf<String>()
        if (binding.chipCrc32.isChecked) algorithms.add("CRC32")
        if (binding.chipMd5.isChecked) algorithms.add("MD5")
        if (binding.chipSha1.isChecked) algorithms.add("SHA-1")
        if (binding.chipSha256.isChecked) algorithms.add("SHA-256")
        if (binding.chipByteCompare.isChecked) algorithms.add("BYTE")
        return algorithms
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

    private fun startScan() {
        val minSizeStr = binding.minSizeInput.text?.toString()?.trim() ?: "1024"
        val minSize = minSizeStr.toLongOrNull() ?: 1024L
        val maxSizeStr = binding.maxSizeInput.text?.toString()?.trim() ?: ""
        val maxSize = maxSizeStr.toLongOrNull() ?: Long.MAX_VALUE
        val algorithms = getSelectedAlgorithms()
        val fileTypeFilter = getFileTypeFilter()
        val includeHiddenDirs = binding.includeHiddenDirsSwitch.isChecked
        val includeHiddenFiles = binding.includeHiddenFilesSwitch.isChecked
        val followSymlinks = binding.followSymlinksSwitch.isChecked

        if (algorithms.isEmpty()) {
            Snackbar.make(binding.root, R.string.duplicate_no_algorithm, Snackbar.LENGTH_SHORT)
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
                Snackbar.make(binding.root, getString(R.string.duplicate_invalid_path) + ": $p", Snackbar.LENGTH_SHORT)
                    .show()
                return
            }
            if (!attrs.isDirectory) {
                Snackbar.make(binding.root, getString(R.string.duplicate_invalid_path) + ": $p", Snackbar.LENGTH_SHORT)
                    .show()
                return
            }
            rootPaths.add(rp)
        }

        duplicateGroups.clear()
        adapter.refreshFlatList()
        adapter.notifyDataSetChanged()
        binding.configLayout.isVisible = false
        binding.resultsLayout.isVisible = true
        binding.scanProgressBar.isVisible = true
        binding.resultSummaryText.text = getString(R.string.duplicate_scan_phase_collecting)
        showingResults = true
        updateMenuVisibility()
        updateBackCallback()

        scanJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val groups = withContext(Dispatchers.IO) {
                    findDuplicates(
                        rootPaths, minSize, maxSize, algorithms, fileTypeFilter,
                        includeHiddenDirs, includeHiddenFiles, followSymlinks
                    )
                }
                duplicateGroups.clear()
                duplicateGroups.addAll(groups)
                applySortAndAutoSelect()
                adapter.refreshFlatList()
                adapter.notifyDataSetChanged()
                binding.scanProgressBar.isVisible = false

                val totalDuplicates = duplicateGroups.sumOf { it.files.size - 1 }
                val totalWastedSize = duplicateGroups.sumOf { it.size * (it.files.size - 1) }
                binding.resultSummaryText.text = getString(
                    R.string.duplicate_result_summary,
                    duplicateGroups.size,
                    totalDuplicates,
                    TrashHelper.formatFileSize(totalWastedSize)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.scanProgressBar.isVisible = false
                binding.resultSummaryText.text = getString(
                    R.string.duplicate_scan_error, e.message ?: e.toString()
                )
            }
        }
    }

    private suspend fun updateProgress(resId: Int, vararg args: Any) {
        withContext(Dispatchers.Main) {
            if (isAdded) {
                binding.resultSummaryText.text = getString(resId, *args)
            }
        }
    }

    private suspend fun collectFiles(
        dirPath: Path,
        includeHiddenDirs: Boolean,
        includeHiddenFiles: Boolean,
        followSymlinks: Boolean,
        minSize: Long,
        maxSize: Long,
        fileTypeFilter: FileTypeFilter,
        result: MutableList<ScannedFile>
    ) {
        val directoryStream = try {
            dirPath.newDirectoryStream()
        } catch (e: IOException) {
            return
        }
        directoryStream.use {
            for (path in it) {
                currentCoroutineContext().ensureActive()
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
                    collectFiles(
                        path, includeHiddenDirs, includeHiddenFiles, followSymlinks,
                        minSize, maxSize, fileTypeFilter, result
                    )
                } else if (attributes.isRegularFile) {
                    if (!includeHiddenFiles && isHidden) continue
                    val size = attributes.size()
                    if (size in minSize..maxSize && FileTypeUtils.matchesFileType(name, fileTypeFilter)) {
                        result.add(
                            ScannedFile(path, size, attributes.lastModifiedTime().toMillis())
                        )
                    }
                }
            }
        }
    }

    private suspend fun findDuplicates(
        rootPaths: List<Path>,
        minSize: Long,
        maxSize: Long,
        algorithms: List<String>,
        fileTypeFilter: FileTypeFilter,
        includeHiddenDirs: Boolean,
        includeHiddenFiles: Boolean,
        followSymlinks: Boolean
    ): List<DuplicateGroup> {
        // Phase 1: Collect and filter files from all root paths
        val allFiles = mutableListOf<ScannedFile>()
        for (rootPath in rootPaths) {
            collectFiles(
                rootPath, includeHiddenDirs, includeHiddenFiles, followSymlinks,
                minSize, maxSize, fileTypeFilter, allFiles
            )
        }
        updateProgress(R.string.duplicate_scan_phase_size)

        // Phase 2: Group by size
        val sizeMap = mutableMapOf<Long, MutableList<ScannedFile>>()
        allFiles.forEach { sf ->
            sizeMap.getOrPut(sf.size) { mutableListOf() }.add(sf)
        }

        // Only keep groups with size > 1
        val candidateFiles = sizeMap.values.filter { it.size > 1 }.flatten()
        val totalCandidates = candidateFiles.size

        // Phase 3: For groups with same size, compute partial hash (first 8KB)
        var processedCount = 0
        val partialHashMap = mutableMapOf<String, MutableList<ScannedFile>>()
        sizeMap.values.filter { it.size > 1 }.forEach { files ->
            files.forEach { sf ->
                currentCoroutineContext().ensureActive()
                val partialHash = computeHash(sf.path, algorithms, partial = true)
                if (partialHash != null) {
                    val key = "${sf.size}_$partialHash"
                    partialHashMap.getOrPut(key) { mutableListOf() }.add(sf)
                }
                processedCount++
                if (processedCount % 50 == 0) {
                    updateProgress(
                        R.string.duplicate_scan_phase_hash, processedCount, totalCandidates
                    )
                }
            }
        }

        // Phase 4: For groups with same partial hash, compute full hash
        val fullCandidates = partialHashMap.values.filter { it.size > 1 }.flatten()
        val totalFullCandidates = fullCandidates.size
        processedCount = 0

        val fullHashMap = mutableMapOf<String, MutableList<ScannedFile>>()
        partialHashMap.values.filter { it.size > 1 }.forEach { files ->
            files.forEach { sf ->
                currentCoroutineContext().ensureActive()
                val fullHash = computeHash(sf.path, algorithms, partial = false)
                if (fullHash != null) {
                    fullHashMap.getOrPut(fullHash) { mutableListOf() }.add(sf)
                }
                processedCount++
                if (processedCount % 20 == 0) {
                    updateProgress(
                        R.string.duplicate_scan_phase_verify, processedCount, totalFullCandidates
                    )
                }
            }
        }

        // Phase 5: If byte-by-byte comparison is selected, verify byte-for-byte
        val useByteCmp = algorithms.contains("BYTE")
        val resultMap = if (useByteCmp) {
            val verified = mutableMapOf<String, MutableList<ScannedFile>>()
            fullHashMap.entries.filter { it.value.size > 1 }.forEach { (hash, files) ->
                val subGroups = mutableListOf<MutableList<ScannedFile>>()
                for (sf in files) {
                    var matched = false
                    for (group in subGroups) {
                        if (filesAreIdentical(group[0].path, sf.path)) {
                            group.add(sf)
                            matched = true
                            break
                        }
                    }
                    if (!matched) {
                        subGroups.add(mutableListOf(sf))
                    }
                }
                subGroups.filter { it.size > 1 }.forEachIndexed { idx, group ->
                    verified["${hash}_v$idx"] = group
                }
            }
            verified
        } else {
            fullHashMap
        }

        // Phase 6: Build result groups
        return resultMap.entries
            .filter { it.value.size > 1 }
            .map { (hash, files) ->
                DuplicateGroup(
                    hash = hash,
                    size = files[0].size,
                    files = files.map { sf ->
                        DuplicateFile(
                            path = sf.path.toString(),
                            name = sf.path.fileName.toString(),
                            size = sf.size,
                            lastModified = sf.lastModified
                        )
                    }.toMutableList()
                )
            }
            .sortedByDescending { it.size * it.files.size }
    }

    private suspend fun computeHash(filePath: Path, algorithms: List<String>, partial: Boolean): String? {
        return try {
            val digests = mutableListOf<MessageDigest>()
            var crc: CRC32? = null
            val hashAlgorithms = algorithms.filter { it != "BYTE" }

            for (algo in hashAlgorithms) {
                if (algo == "CRC32") {
                    crc = CRC32()
                } else {
                    digests.add(MessageDigest.getInstance(algo))
                }
            }

            if (digests.isEmpty() && crc == null) {
                // Only BYTE selected, use MD5 as internal grouping
                digests.add(MessageDigest.getInstance("MD5"))
            }

            filePath.newInputStream().use { inputStream ->
                val buffer = ByteArray(if (partial) 8192 else 65536)
                if (partial) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        digests.forEach { it.update(buffer, 0, bytesRead) }
                        crc?.update(buffer, 0, bytesRead)
                    }
                } else {
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        currentCoroutineContext().ensureActive()
                        digests.forEach { it.update(buffer, 0, bytesRead) }
                        crc?.update(buffer, 0, bytesRead)
                    }
                }
            }

            val parts = mutableListOf<String>()
            digests.forEach { digest ->
                parts.add(digest.digest().joinToString("") { "%02x".format(it) })
            }
            crc?.let { parts.add(it.value.toString(16)) }
            parts.joinToString("_")
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun filesAreIdentical(path1: Path, path2: Path): Boolean {
        return try {
            path1.newInputStream().use { is1 ->
                path2.newInputStream().use { is2 ->
                    val buf1 = ByteArray(65536)
                    val buf2 = ByteArray(65536)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read1 = is1.read(buf1)
                        val read2 = is2.read(buf2)
                        if (read1 != read2) return false
                        if (read1 == -1) return true
                        for (i in 0 until read1) {
                            if (buf1[i] != buf2[i]) return false
                        }
                    }
                    @Suppress("UNREACHABLE_CODE")
                    true
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun applySortAndAutoSelect() {
        duplicateGroups.forEach { group ->
            val keepIndex = when (currentSortMode) {
                SortMode.PATH_LONGEST -> group.files.indices.maxByOrNull {
                    group.files[it].path.length
                } ?: 0
                SortMode.PATH_SHORTEST -> group.files.indices.minByOrNull {
                    group.files[it].path.length
                } ?: 0
                SortMode.DATE_NEWEST -> group.files.indices.maxByOrNull {
                    group.files[it].lastModified
                } ?: 0
                SortMode.DATE_OLDEST -> group.files.indices.minByOrNull {
                    group.files[it].lastModified
                } ?: 0
            }
            group.files.forEachIndexed { index, file ->
                file.isChecked = index != keepIndex
            }
        }
    }

    private fun showKeepRuleDialog() {
        val ruleOptions = arrayOf(
            getString(R.string.duplicate_sort_path_longest),
            getString(R.string.duplicate_sort_path_shortest),
            getString(R.string.duplicate_sort_date_newest),
            getString(R.string.duplicate_sort_date_oldest)
        )
        val checked = currentSortMode.ordinal

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.duplicate_keep_rule_title)
            .setSingleChoiceItems(ruleOptions, checked) { dialog, which ->
                currentSortMode = SortMode.entries[which]
                applySortAndAutoSelect()
                adapter.refreshFlatList()
                adapter.notifyDataSetChanged()
                updateMenuVisibility()
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openDuplicateFile(groupIndex: Int, fileIndex: Int) {
        val file = duplicateGroups.getOrNull(groupIndex)?.files?.getOrNull(fileIndex) ?: return
        try {
            val path = Paths.get(file.path)
            val uri = path.fileProviderUri
            val mimeType = MimeType.guessFromPath(file.name)
            val intent = uri.createViewIntent(mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivitySafe(intent)
        } catch (e: Exception) {
            Snackbar.make(binding.root, R.string.open_file_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun deleteSelected() {
        val selectedFiles = duplicateGroups.flatMap { it.files.filter { f -> f.isChecked } }
        if (selectedFiles.isEmpty()) {
            Snackbar.make(binding.root, R.string.duplicate_none_selected, Snackbar.LENGTH_SHORT)
                .show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.duplicate_delete_title)
            .setMessage(getString(R.string.duplicate_delete_message, selectedFiles.size,
                TrashHelper.formatFileSize(selectedFiles.sumOf { it.size })))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    var deleted = 0
                    // Track only successfully deleted paths so we don't remove
                    // files from the UI that still exist on disk
                    val deletedPaths = mutableSetOf<String>()
                    withContext(Dispatchers.IO) {
                        selectedFiles.forEach { item ->
                            try {
                                val filePath = Paths.get(item.path)
                                if (!TrashHelper.moveToTrash(filePath)) {
                                    filePath.delete()
                                }
                                MediaScanner.scan(filePath.toFile(), true)
                                deletedPaths.add(item.path)
                                deleted++
                            } catch (_: Exception) {}
                        }
                    }
                    // Remove successfully deleted files from their groups
                    duplicateGroups.forEach { group ->
                        group.files.removeAll { it.path in deletedPaths }
                    }
                    // Remove groups that are no longer duplicate groups (0 or 1 file remaining)
                    duplicateGroups.removeAll { it.files.size <= 1 }
                    adapter.refreshFlatList()
                    adapter.notifyDataSetChanged()
                    updateMenuVisibility()

                    val totalDuplicates = duplicateGroups.sumOf { it.files.size - 1 }
                    val totalWastedSize = duplicateGroups.sumOf { it.size * (it.files.size - 1) }
                    binding.resultSummaryText.text = getString(
                        R.string.duplicate_result_summary,
                        duplicateGroups.size,
                        totalDuplicates,
                        TrashHelper.formatFileSize(totalWastedSize)
                    )

                    Snackbar.make(
                        binding.root,
                        getString(R.string.duplicate_deleted_count, deleted),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showConfig() {
        scanJob?.cancel()
        binding.configLayout.isVisible = true
        binding.resultsLayout.isVisible = false
        showingResults = false
        updateMenuVisibility()
        updateBackCallback()
    }
}
