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
            "file_search" -> "文件搜索"
            "duplicate_finder" -> "重复文件查找"
            "empty_search" -> "空文件夹搜索"
            "recent_files" -> "最近文件"
            "hex_viewer" -> "十六进制查看"
            "encryption" -> "文件加密"
            "file_compare" -> "文件对比"
            else -> "文件工具"
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
                binding.selectedFilesPreview.selectedFilesTitle.text = "已选择 ${files.size} 个文件"
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
