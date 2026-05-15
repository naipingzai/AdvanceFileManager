/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.viewer.ebook

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.EbookViewerFragmentBinding
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.file.asMimeTypeOrNull
import naipingzai.materialfile.file.fileProviderUri
import naipingzai.materialfile.file.isEpub
import naipingzai.materialfile.file.isMobi
import naipingzai.materialfile.provider.common.newInputStream
import naipingzai.materialfile.util.ParcelableArgs
import naipingzai.materialfile.util.args
import naipingzai.materialfile.util.createSendStreamIntent
import naipingzai.materialfile.util.extraPath
import naipingzai.materialfile.util.finish
import naipingzai.materialfile.util.mediumAnimTime
import naipingzai.materialfile.util.startActivitySafe
import naipingzai.materialfile.util.withChooser
import java.io.File
import java.io.InputStream
import java8.nio.file.Path

class EbookViewerFragment : Fragment() {
    private val args by args<Args>()
    private lateinit var binding: EbookViewerFragmentBinding
    private var loadJob: Job? = null
    private var imageDir: File? = null
    private var isUiVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        EbookViewerFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.ebook_viewer, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.action_share -> { shareEbook(); true }
                    else -> false
                }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val path = args.intent.extraPath
        if (path == null) {
            finish()
            return
        }

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.title = path.fileName.toString()

        // Overlay toolbar: draw content under status bar
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        activity.window.statusBarColor = Color.TRANSPARENT
        binding.appBarLayout.applySystemWindowInsetsToPadding(
            left = true, top = true, right = true
        )

        setupWebView()
        setupTapToToggle()
        loadEbook(path)
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
            allowFileAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            textZoom = 120
            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        }
        // Dark mode support for WebView content
        binding.webView.setBackgroundColor(0)
    }

    private fun loadEbook(path: Path) {
        binding.progress.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE
        binding.webView.visibility = View.GONE

        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Create temp directory for images
                val imgDir = File(requireContext().cacheDir, "ebook_cache")
                imgDir.deleteRecursively()
                imgDir.mkdirs()
                imageDir = imgDir

                // Detect format by MIME type or file extension
                val fileName = path.fileName?.toString()?.lowercase() ?: ""
                val mimeType = args.intent.type?.asMimeTypeOrNull()
                val isEpubFile = mimeType?.isEpub == true ||
                    fileName.endsWith(".epub")

                val book: Pair<String, String> = withContext(Dispatchers.IO) {
                    if (isEpubFile) {
                        val epub = path.newInputStream().use { input: InputStream ->
                            EpubParser().parse(input, imgDir)
                        }
                        Pair(epub.title, epub.html)
                    } else {
                        val mobi = path.newInputStream().use { input: InputStream ->
                            MobiParser().parse(input, imgDir)
                        }
                        Pair(mobi.title, mobi.html)
                    }
                }

                val activity = requireActivity() as AppCompatActivity
                activity.title = book.first

                // Inject CSS for better readability
                val styledHtml = injectReadingStyles(book.second)
                // Use file:// base URL so WebView can load local images
                val baseUrl = "file://" + imgDir.absolutePath + "/"
                binding.webView.loadDataWithBaseURL(
                    baseUrl, styledHtml, "text/html", "UTF-8", null
                )
                binding.progress.visibility = View.GONE
                binding.webView.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e("EbookViewer", "Error loading ebook", e)
                binding.progress.visibility = View.GONE
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = getString(
                    R.string.ebook_viewer_error, e.message ?: e.toString()
                )
            }
        }
    }

    private fun injectReadingStyles(html: String): String {
        val isDarkMode = (resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

        val bgColor = if (isDarkMode) "#1C1B1F" else "#FFFBFE"
        val textColor = if (isDarkMode) "#E6E1E5" else "#1C1B1F"
        val linkColor = if (isDarkMode) "#D0BCFF" else "#6750A4"

        val css = """
            <style>
                body {
                    background-color: $bgColor;
                    color: $textColor;
                    font-family: serif;
                    line-height: 1.8;
                    padding: 16px;
                    word-wrap: break-word;
                    max-width: 100%;
                }
                a { color: $linkColor; }
                img {
                    max-width: 100%;
                    height: auto;
                }
                h1, h2, h3, h4, h5, h6 {
                    line-height: 1.4;
                    margin-top: 1.2em;
                    margin-bottom: 0.4em;
                }
                p { margin: 0.8em 0; }
                table { max-width: 100%; overflow-x: auto; }
            </style>
        """.trimIndent()

        // Insert CSS into the HTML
        val headEnd = html.indexOf("</head>", ignoreCase = true)
        if (headEnd != -1) {
            return html.substring(0, headEnd) + css + html.substring(headEnd)
        }
        val htmlStart = html.indexOf("<html", ignoreCase = true)
        if (htmlStart != -1) {
            val htmlTagEnd = html.indexOf(">", htmlStart)
            if (htmlTagEnd != -1) {
                return html.substring(0, htmlTagEnd + 1) + "<head>$css</head>" +
                    html.substring(htmlTagEnd + 1)
            }
        }
        return "<html><head>$css</head><body>$html</body></html>"
    }



    private fun shareEbook() {
        val path = args.intent.extraPath ?: return
        val intent = path.fileProviderUri.createSendStreamIntent(MimeType.ANY)
            .apply { extraPath = path }
            .withChooser()
        startActivitySafe(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Restore system bars and decor fits
        val window = requireActivity().window
        if (!isUiVisible) {
            WindowInsetsControllerCompat(window, window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())
        }
        WindowCompat.setDecorFitsSystemWindows(window, true)
        loadJob?.cancel()
        binding.webView.destroy()
        // Clean up temp image files
        imageDir?.deleteRecursively()
        imageDir = null
    }

    @Parcelize
    class Args(val intent: Intent) : ParcelableArgs
}
