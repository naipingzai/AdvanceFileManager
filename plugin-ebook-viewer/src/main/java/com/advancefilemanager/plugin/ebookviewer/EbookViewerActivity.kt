/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.ebookviewer

import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.advancefilemanager.plugin.protocol.PluginContract
import com.advancefilemanager.plugin.ebookviewer.databinding.ActivityEbookViewerBinding
import java.io.File

class EbookViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEbookViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEbookViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val filePath = intent.getStringExtra(PluginContract.EXTRA_FILE_PATH)
            ?: intent.data?.path

        if (filePath != null) {
            supportActionBar?.title = File(filePath).name
            loadEbook(filePath)
        } else {
            finish()
        }
    }

    private fun loadEbook(filePath: String) {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = WebViewClient()
        // TODO: Implement ebook parsing (EPUB/MOBI) and render via WebView
        // This is a placeholder - the actual implementation should be moved
        // from the main app's EbookViewerActivity
        binding.webView.loadData(
            "<html><body><p>Loading: $filePath</p></body></html>",
            "text/html",
            "UTF-8"
        )
    }
}
