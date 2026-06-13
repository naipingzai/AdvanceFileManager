/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.pdf

import android.content.Intent
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import com.advancefilemanager.R
import com.advancefilemanager.databinding.PdfViewerFragmentBinding
import com.advancefilemanager.util.ParcelableArgs
import com.advancefilemanager.util.args
import com.advancefilemanager.util.extraPath
import com.advancefilemanager.util.finish
import java8.nio.file.Path
import com.advancefilemanager.file.fileProviderUri
import com.advancefilemanager.provider.common.newInputStream
import com.advancefilemanager.provider.common.size
import com.advancefilemanager.util.mediumAnimTime
import java.io.File

class PdfViewerFragment : Fragment() {
    private val args by args<Args>()
    private lateinit var binding: PdfViewerFragmentBinding
    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var adapter: PdfPageAdapter? = null
    private var tempFile: File? = null
    private var loadJob: Job? = null
    private var isUiVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        PdfViewerFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        // RecyclerView: only bottom padding for nav bar; content starts at screen top
        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }

        // Page indicator: apply bottom insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.pageIndicatorCard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val lp = v.layoutParams as FrameLayout.LayoutParams
            lp.bottomMargin = systemBars.bottom + (16 * resources.displayMetrics.density).toInt()
            v.layoutParams = lp
            insets
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                updatePageIndicator()
            }
        })

        // Tap to toggle toolbar / page indicator / system bars
        val gestureDetector = GestureDetector(requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    toggleUi()
                    return true
                }
            })
        binding.recyclerView.addOnItemTouchListener(
            object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(
                    rv: RecyclerView, e: MotionEvent
                ): Boolean {
                    gestureDetector.onTouchEvent(e)
                    return false
                }
            })

        loadPdf(path)
    }

    private fun loadPdf(path: Path) {
        binding.progress.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE

        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Get file size for progress reporting
                val fileSize = withContext(Dispatchers.IO) {
                    try { path.size() } catch (_: Exception) { -1L }
                }

                val renderer = withContext(Dispatchers.IO) {
                    // Copy to temp file for PdfRenderer (needs seekable fd)
                    val tmp = File.createTempFile("pdf_", ".pdf", requireContext().cacheDir)
                    tmp.deleteOnExit()
                    tempFile = tmp

                    path.newInputStream().use { input ->
                        tmp.outputStream().use { output ->
                            val buffer = ByteArray(65536)
                            var totalCopied = 0L
                            while (isActive) {
                                val bytesRead = input.read(buffer)
                                if (bytesRead == -1) break
                                output.write(buffer, 0, bytesRead)
                                totalCopied += bytesRead
                                if (fileSize > 0) {
                                    val progress = (totalCopied * 100 / fileSize).toInt()
                                    withContext(Dispatchers.Main) {
                                        binding.fileSizeText.visibility = View.VISIBLE
                                        binding.fileSizeText.text = getString(
                                            R.string.pdf_viewer_loading_progress,
                                            progress,
                                            formatFileSize(totalCopied),
                                            formatFileSize(fileSize)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val fd = ParcelFileDescriptor.open(
                        tmp, ParcelFileDescriptor.MODE_READ_ONLY
                    )
                    fileDescriptor = fd
                    PdfRenderer(fd)
                }

                pdfRenderer = renderer
                val screenWidth = resources.displayMetrics.widthPixels
                val pdfAdapter = PdfPageAdapter(
                    renderer, screenWidth, viewLifecycleOwner.lifecycleScope
                )
                adapter = pdfAdapter
                binding.recyclerView.adapter = pdfAdapter
                binding.progress.visibility = View.GONE
                binding.fileSizeText.visibility = View.GONE
                binding.pageIndicator.visibility = View.VISIBLE
                updatePageIndicator()
            } catch (e: Exception) {
                e.printStackTrace()
                binding.progress.visibility = View.GONE
                binding.fileSizeText.visibility = View.GONE
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = e.toString()
            }
        }
    }

    private fun updatePageIndicator() {
        val layoutManager = binding.recyclerView.layoutManager as? LinearLayoutManager ?: return
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val pageCount = adapter?.itemCount ?: 0
        if (firstVisible != RecyclerView.NO_POSITION && pageCount > 0) {
            binding.pageIndicator.text = getString(
                R.string.pdf_viewer_page_format, firstVisible + 1, pageCount
            )
        }
    }

    private fun toggleUi() {
        isUiVisible = !isUiVisible
        val activity = requireActivity() as? AppCompatActivity ?: return
        val window = activity.window

        // Animate toolbar
        binding.appBarLayout.animate()
            .alpha(if (isUiVisible) 1f else 0f)
            .translationY(
                if (isUiVisible) 0f else -binding.appBarLayout.bottom.toFloat()
            )
            .setDuration(mediumAnimTime.toLong())
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        if (isUiVisible) {
            binding.pageIndicatorCard.visibility = View.VISIBLE
            WindowInsetsControllerCompat(window, window.decorView).apply {
                show(WindowInsetsCompat.Type.systemBars())
            }
        } else {
            binding.pageIndicatorCard.visibility = View.GONE
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return com.advancefilemanager.util.FormatUtils.formatSize(bytes)
    }



    private fun sharePdf() {
        val path = args.intent.extraPath ?: return
        val uri = path.fileProviderUri
        if (uri == null) {
            android.widget.Toast.makeText(
                requireContext(),
                R.string.open_file_error,
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, null))
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
        adapter?.clearCache()
        pdfRenderer?.close()
        fileDescriptor?.close()
        tempFile?.delete()
    }

    @Parcelize
    class Args(val intent: Intent) : ParcelableArgs
}
