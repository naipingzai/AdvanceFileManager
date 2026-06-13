/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.filetools

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.advancefilemanager.app.AppActivity
import com.advancefilemanager.R
import com.advancefilemanager.databinding.ActivityFileToolsBinding
import com.advancefilemanager.feature.protocol.FeatureContract
import java.io.File

class FileToolsActivity : AppActivity() {

    private lateinit var binding: ActivityFileToolsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileToolsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val actionType = intent.getStringExtra(FeatureContract.EXTRA_ACTION_TYPE) ?: "file_search"
        val filePath = intent.getStringExtra(FeatureContract.EXTRA_FILE_PATH)
        val filePaths = intent.getStringArrayExtra(FeatureContract.EXTRA_FILE_PATHS)

        val title = when (actionType) {
            "file_search" -> getString(R.string.file_search_title)
            "duplicate_finder" -> getString(R.string.duplicate_finder_title)
            "empty_search" -> getString(R.string.empty_search_title)
            "recent_files" -> getString(R.string.recent_files_title)
            "hex_viewer" -> getString(R.string.hex_viewer_title)
            "encryption" -> getString(R.string.encryption_title)
            "file_compare" -> getString(R.string.file_compare_title)
            else -> getString(R.string.file_tools_title)
        }
        supportActionBar?.title = title

        setupSelectedFilesPreview(filePaths, filePath)

        if (savedInstanceState == null) {
            val fragment = createFragment(actionType, filePath, filePaths)
            supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
        }
    }

    private fun setupSelectedFilesPreview(filePaths: Array<String>?, filePath: String?) {
        val paths = filePaths?.toList() ?: filePath?.let { listOf(it) }
        if (!paths.isNullOrEmpty() && paths.size > 1) {
            val files = paths.map { File(it) }.filter { it.exists() }
            if (files.size > 1) {
                binding.selectedFilesPreview.root.visibility = View.VISIBLE
                binding.selectedFilesPreview.selectedFilesTitle.text = getString(R.string.selected_files_count, files.size)
                binding.selectedFilesPreview.selectedFilesRecyclerView.apply {
                    layoutManager = LinearLayoutManager(
                        this@FileToolsActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    adapter = SelectedFilesAdapter(files)
                }
            }
        }
    }

    private fun createFragment(actionType: String, filePath: String?, filePaths: Array<String>?): Fragment {
        val args = Bundle().apply {
            filePath?.let { putString("filePath", it) }
            filePaths?.let { putStringArray("filePaths", it) }
        }
        val fragment: Fragment = when (actionType) {
            "file_search" -> FileSearchToolFragment()
            "duplicate_finder" -> DuplicateFinderToolFragment()
            "empty_search" -> EmptySearchToolFragment()
            "recent_files" -> RecentFilesToolFragment()
            "hex_viewer" -> HexViewerToolFragment()
            "encryption" -> EncryptionToolFragment()
            "file_compare" -> FileCompareToolFragment()
            else -> FileSearchToolFragment()
        }
        fragment.arguments = args
        return fragment
    }
}
