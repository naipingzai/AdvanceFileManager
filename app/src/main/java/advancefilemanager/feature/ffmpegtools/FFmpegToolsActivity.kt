/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.ffmpegtools

import com.advancefilemanager.R

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.advancefilemanager.app.AppActivity
import com.advancefilemanager.feature.protocol.FeatureContract
import com.advancefilemanager.databinding.ActivityFfmpegToolsBinding
import com.advancefilemanager.feature.filetools.SelectedFilesAdapter
import java.io.File

class FFmpegToolsActivity : AppActivity() {

    private lateinit var binding: ActivityFfmpegToolsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFfmpegToolsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val filePath = intent.getStringExtra(FeatureContract.EXTRA_FILE_PATH)
        val filePaths = intent.getStringArrayExtra(FeatureContract.EXTRA_FILE_PATHS)
        val actionType = intent.getStringExtra(FeatureContract.EXTRA_ACTION_TYPE)

        val feature = actionType?.let { type ->
            MediaToolFeature.entries.find { it.actionType == type }
        }

        if (feature == null || filePath == null) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        supportActionBar?.title = getString(feature.titleRes)

        setupSelectedFilesPreview(filePaths, filePath)

        val file = File(filePath)
        val fragment = FFmpegFeatureFragment.newInstance(feature, file.absolutePath, filePaths)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, fragment)
            .commit()
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
                        this@FFmpegToolsActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    adapter = SelectedFilesAdapter(files)
                }
            }
        }
    }
}
