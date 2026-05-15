/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.csv

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import com.advancefilemanager.R
import com.advancefilemanager.databinding.CsvViewerFragmentBinding
import com.advancefilemanager.provider.common.newInputStream
import com.advancefilemanager.util.ParcelableArgs
import com.advancefilemanager.util.args
import com.advancefilemanager.util.extraPath
import com.advancefilemanager.util.finish
import com.advancefilemanager.util.mediumAnimTime
import java.io.BufferedReader
import java.io.InputStreamReader

class CsvViewerFragment : Fragment() {
    private val args by args<Args>()
    private lateinit var binding: CsvViewerFragmentBinding
    private var loadJob: Job? = null
    private var isUiVisible = true
    private var topPaddingDp = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        CsvViewerFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val path = args.intent.extraPath
        if (path == null) {
            finish()
            return
        }

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.title = path.fileName.toString()

        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        activity.window.statusBarColor = Color.TRANSPARENT
        binding.appBarLayout.applySystemWindowInsetsToPadding(
            left = true, top = true, right = true
        )

        // Bottom nav bar padding for WebView
        ViewCompat.setOnApplyWindowInsetsListener(binding.webView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }

        setupWebView()
        setupTapToToggle()

        // Defer loading until appBarLayout is measured so we know its height
        binding.appBarLayout.post {
            topPaddingDp = binding.appBarLayout.height / resources.displayMetrics.density
            loadCsv(path)
        }
    }

    private fun setupTapToToggle() {
        val gestureDetector = GestureDetector(requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    toggleUi()
                    return true
                }
            })
        binding.webView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun toggleUi() {
        isUiVisible = !isUiVisible
        val window = requireActivity().window

        binding.appBarLayout.animate()
            .alpha(if (isUiVisible) 1f else 0f)
            .translationY(
                if (isUiVisible) 0f else -binding.appBarLayout.bottom.toFloat()
            )
            .setDuration(mediumAnimTime.toLong())
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        if (isUiVisible) {
            WindowInsetsControllerCompat(window, window.decorView).apply {
                show(WindowInsetsCompat.Type.systemBars())
            }
        } else {
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = false
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        }
        binding.webView.setBackgroundColor(0)
    }

    private fun loadCsv(path: java8.nio.file.Path) {
        binding.progress.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE
        binding.webView.visibility = View.GONE

        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val html = withContext(Dispatchers.IO) {
                    path.newInputStream().use { input ->
                        val reader = BufferedReader(InputStreamReader(input, "UTF-8"))
                        buildHtmlTable(reader, topPaddingDp)
                    }
                }
                binding.progress.visibility = View.GONE
                binding.webView.visibility = View.VISIBLE
                binding.webView.loadDataWithBaseURL(
                    null, html, "text/html", "UTF-8", null
                )
            } catch (e: Exception) {
                binding.progress.visibility = View.GONE
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = e.message ?: getString(R.string.csv_viewer_error)
            }
        }
    }

    private fun buildHtmlTable(reader: BufferedReader, topPadDp: Float): String {
        val sb = StringBuilder(8192)
        val topPad = topPadDp.toInt()
        sb.append(
            """<!DOCTYPE html>
<html><head><meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
@media (prefers-color-scheme: dark) {
  body { background: #1e1e1e; color: #e0e0e0; }
  table { border-color: #444; }
  th { background: #333; color: #fff; }
  td { border-color: #444; }
  tr:nth-child(even) { background: #2a2a2a; }
  tr:hover { background: #383838; }
}
@media (prefers-color-scheme: light) {
  body { background: #fff; color: #333; }
  th { background: #f0f0f0; color: #333; }
  tr:nth-child(even) { background: #f9f9f9; }
  tr:hover { background: #e8e8e8; }
}
body { margin: 0; padding: ${topPad}px 8px 8px 8px; font-family: sans-serif; font-size: 13px; }
table { border-collapse: collapse; width: 100%; }
th, td { border: 1px solid #ddd; padding: 6px 10px; text-align: left; white-space: nowrap; }
th { position: sticky; top: 0; z-index: 1; font-weight: 600; }
.row-num { color: #999; font-size: 11px; text-align: right; min-width: 30px; }
</style></head><body><table>
"""
        )

        var lineNum = 0
        var isFirstLine = true
        val maxRows = 10000 // Limit for performance

        reader.forEachLine { line ->
            if (lineNum >= maxRows) return@forEachLine
            val fields = parseCsvLine(line)
            if (isFirstLine) {
                sb.append("<thead><tr><th class=\"row-num\">#</th>")
                for (field in fields) {
                    sb.append("<th>").append(escapeHtml(field)).append("</th>")
                }
                sb.append("</tr></thead><tbody>\n")
                isFirstLine = false
            } else {
                sb.append("<tr><td class=\"row-num\">").append(lineNum).append("</td>")
                for (field in fields) {
                    sb.append("<td>").append(escapeHtml(field)).append("</td>")
                }
                sb.append("</tr>\n")
            }
            lineNum++
        }

        if (isFirstLine) {
            sb.append("<tr><td>Empty file</td></tr>")
        }
        sb.append("</tbody></table>")
        if (lineNum >= maxRows) {
            sb.append("<p style='color:#999;text-align:center;padding:16px;'>")
            sb.append("⚠ Showing first $maxRows rows</p>")
        }
        sb.append("</body></html>")
        return sb.toString()
    }

    /**
     * Parse a CSV line handling quoted fields with commas and escaped quotes.
     */
    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes -> {
                    if (c == '"') {
                        if (i + 1 < line.length && line[i + 1] == '"') {
                            sb.append('"')
                            i++ // skip escaped quote
                        } else {
                            inQuotes = false
                        }
                    } else {
                        sb.append(c)
                    }
                }
                c == '"' -> inQuotes = true
                c == ',' -> {
                    fields.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        fields.add(sb.toString())
        return fields
    }

    private fun escapeHtml(text: String): String =
        text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()
        binding.webView.destroy()
    }

    @Parcelize
    class Args(val intent: Intent) : ParcelableArgs
}
