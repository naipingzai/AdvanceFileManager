/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.viewer.image

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager2.widget.ViewPager2
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.ImageViewerFragmentBinding
import naipingzai.materialfile.file.fileProviderUri
import naipingzai.materialfile.provider.common.delete
import naipingzai.materialfile.provider.linux.isLinuxPath
import naipingzai.materialfile.tools.trash.TrashHelper
import naipingzai.materialfile.ui.DepthPageTransformer
import naipingzai.materialfile.util.ParcelableArgs
import naipingzai.materialfile.util.ParcelableListParceler
import naipingzai.materialfile.util.ParcelableState
import naipingzai.materialfile.util.args
import naipingzai.materialfile.util.createSendImageIntent
import naipingzai.materialfile.util.extraPath
import naipingzai.materialfile.util.extraPathList
import naipingzai.materialfile.util.finish
import naipingzai.materialfile.util.getState
import naipingzai.materialfile.util.mediumAnimTime
import naipingzai.materialfile.util.putState
import naipingzai.materialfile.util.showToast
import naipingzai.materialfile.util.startActivitySafe
import naipingzai.materialfile.util.withChooser
import naipingzai.materialfile.lib.systemuihelper.SystemUiHelper
import java.io.IOException

class ImageViewerFragment : Fragment(), ConfirmDeleteDialogFragment.Listener {
    private val args by args<Args>()
    private val argsPaths by lazy { args.intent.extraPathList }

    private lateinit var paths: MutableList<Path>

    private lateinit var binding: ImageViewerFragmentBinding

    private lateinit var systemUiHelper: SystemUiHelper

    private lateinit var adapter: ImageViewerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paths = (savedInstanceState?.getState<State>()?.paths ?: argsPaths).toMutableList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ImageViewerFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (paths.isEmpty()) {
            showToast(R.string.open_file_error)
            finish()
            return
        }

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Register menu provider for delete and share actions
        activity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.image_viewer, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        activity.onBackPressed()
                        true
                    }
                    R.id.action_delete -> {
                        confirmDelete()
                        true
                    }
                    R.id.action_share -> {
                        share()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
        // Our app bar will draw the status bar background.
        activity.window.statusBarColor = Color.TRANSPARENT
        binding.appBarLayout.applySystemWindowInsetsToPadding(left = true, top = true, right = true)
        systemUiHelper = SystemUiHelper(
            activity, SystemUiHelper.LEVEL_IMMERSIVE, SystemUiHelper.FLAG_IMMERSIVE_STICKY
        ) { visible: Boolean ->
            binding.appBarLayout.animate()
                .alpha(if (visible) 1f else 0f)
                .translationY(if (visible) 0f else -binding.appBarLayout.bottom.toFloat())
                .setDuration(mediumAnimTime.toLong())
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
        // This will set up window flags.
        systemUiHelper.show()
        adapter = ImageViewerAdapter(viewLifecycleOwner) { systemUiHelper.toggle() }.apply {
            replace(paths)
        }
        binding.viewPager.apply {
            // 1 is the default for the old androidx.viewpager.widget.ViewPager.
            offscreenPageLimit = 1
            adapter = this@ImageViewerFragment.adapter
            // ViewPager saves its position and will restore it later.
            setCurrentItem(args.position, false)
            setPageTransformer(DepthPageTransformer)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateTitle()
                }
            })
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (paths.isEmpty()) {
            // We did finish the activity in onActivityCreated(), however we will still be called
            // here before the activity is actually finished.
            return
        }

        updateTitle()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putState(State(paths))
    }



    private fun confirmDelete() {
        ConfirmDeleteDialogFragment.show(currentPath, this)
    }

    override fun delete(path: Path) {
        try {
            if (path.isLinuxPath) {
                val file = path.toFile()
                if (TrashHelper.moveToTrash(file)) {
                    // Successfully moved to trash
                } else {
                    path.delete()
                }
            } else {
                path.delete()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            showToast(e.toString())
            return
        }
        paths.removeAll(listOf(path))
        if (paths.isEmpty()) {
            finish()
            return
        }
        adapter.replace(paths)
        // ViewPager only asynchronously sets current item to 0, which isn't a desirable behavior
        // for us and will make updateTitle() crash for index out of bounds.
        if (binding.viewPager.currentItem > paths.lastIndex) {
            binding.viewPager.currentItem = paths.lastIndex
        }
        updateTitle()
        // Work around blank screen due to ViewPager2.PageTransformer not being called (and thus the
        // next item keeps its 0 alpha) when we have offscreenPageLimit = 1.
        binding.viewPager.doOnPreDraw { binding.viewPager.requestTransform() }
    }

    private fun updateTitle() {
        val path = currentPath
        requireActivity().title = path.fileName.toString()
        val size = paths.size
        binding.toolbar.subtitle = if (size > 1) {
            getString(
                R.string.image_viewer_subtitle_format, binding.viewPager.currentItem + 1, size
            )
        } else {
            null
        }
    }

    private fun share() {
        val path = currentPath
        val intent = path.fileProviderUri.createSendImageIntent()
            .apply { extraPath = path }
            .withChooser()
        startActivitySafe(intent)
    }

    private val currentPath: Path
        get() = paths[binding.viewPager.currentItem]

    @Parcelize
    class Args(val intent: Intent, val position: Int) : ParcelableArgs

    @Parcelize
    private class State(val paths: @WriteWith<ParcelableListParceler> List<Path>) : ParcelableState
}
