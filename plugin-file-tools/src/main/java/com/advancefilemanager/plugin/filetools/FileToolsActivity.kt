/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.filetools

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.advancefilemanager.plugin.filetools.databinding.ActivityFileToolsBinding
import com.advancefilemanager.plugin.protocol.PluginContract

class FileToolsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileToolsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileToolsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val actionType = intent.getStringExtra(PluginContract.EXTRA_ACTION_TYPE) ?: "file_search"
        val filePath = intent.getStringExtra(PluginContract.EXTRA_FILE_PATH)

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

        if (savedInstanceState == null) {
            val fragment = createFragment(actionType, filePath)
            supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
        }
    }

    private fun createFragment(actionType: String, filePath: String?): Fragment {
        val args = Bundle().apply {
            filePath?.let { putString("filePath", it) }
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
