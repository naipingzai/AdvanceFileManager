/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.ffmpegtools

import com.advancefilemanager.R

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.advancefilemanager.feature.protocol.FeatureContract
import com.advancefilemanager.databinding.ActivityFfmpegToolsBinding
import java.io.File

class FFmpegToolsActivity : AppCompatActivity() {

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

        val file = File(filePath)
        val fragment = FFmpegFeatureFragment.newInstance(feature, file.absolutePath)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, fragment)
            .commit()
    }
}
