/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.audio

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.material.slider.Slider
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import java8.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import com.advancefilemanager.R
import com.advancefilemanager.databinding.AudioPlayerFragmentBinding
import com.advancefilemanager.file.MimeType
import com.advancefilemanager.file.fileProviderUri
import com.advancefilemanager.lib.systemuihelper.SystemUiHelper
import com.advancefilemanager.provider.common.delete
import com.advancefilemanager.provider.linux.isLinuxPath
import com.advancefilemanager.tools.trash.TrashHelper
import com.advancefilemanager.util.ParcelableArgs
import com.advancefilemanager.util.ParcelableListParceler
import com.advancefilemanager.util.ParcelableState
import com.advancefilemanager.util.args
import com.advancefilemanager.util.createSendStreamIntent
import com.advancefilemanager.util.extraPath
import com.advancefilemanager.util.extraPathList
import com.advancefilemanager.util.finish
import com.advancefilemanager.util.getState
import com.advancefilemanager.util.mediumAnimTime
import com.advancefilemanager.util.putState
import com.advancefilemanager.util.showToast
import com.advancefilemanager.util.startActivitySafe
import com.advancefilemanager.util.withChooser
import java.io.IOException
import kotlin.math.abs

class AudioPlayerFragment : Fragment(), ConfirmDeleteAudioDialogFragment.Listener {
    companion object {
        private const val SEEK_BAR_MAX = 1000f
    }

    private val args by args<Args>()
    private val argsPaths by lazy { args.intent.extraPathList }

    private lateinit var paths: MutableList<Path>
    private var currentPosition: Int = 0

    private lateinit var binding: AudioPlayerFragmentBinding

    private lateinit var systemUiHelper: SystemUiHelper

    private var player: ExoPlayer? = null
    private var playWhenReady: Boolean = true

    private var isLongPressing = false
    private var originalSpeed = 1.0f

    // For swipe gesture
    private var swipeStartX = 0f
    private var swipeStartPosition = 0L
    private var isSwiping = false
    private val SWIPE_THRESHOLD = 50f
    private val SEEK_PER_PIXEL = 100L

    private lateinit var gestureDetector: GestureDetectorCompat
    private var controlsVisible = false
    private var showMilliseconds = false
    private var isSeeking = false
    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (controlsVisible && !isSeeking) {
                updateSeekBar()
            }
            if (player?.isPlaying == true) {
                updateHandler.postDelayed(this, if (showMilliseconds) 50L else 500L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paths = (savedInstanceState?.getState<State>()?.paths ?: argsPaths).toMutableList()
        currentPosition = savedInstanceState?.getState<State>()?.position ?: args.position
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        AudioPlayerFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (paths.isEmpty()) {
            finish()
            return
        }

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Register menu provider for delete and share actions
        activity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.audio_player, menu)
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
        activity.window.statusBarColor = Color.TRANSPARENT
        binding.appBarLayout.applySystemWindowInsetsToPadding(
            left = true, top = true, right = true
        )

        systemUiHelper = SystemUiHelper(
            activity, SystemUiHelper.LEVEL_IMMERSIVE, SystemUiHelper.FLAG_IMMERSIVE_STICKY
        ) { _: Boolean -> }
        // Initially hide toolbar and seek bar
        binding.appBarLayout.alpha = 0f
        binding.appBarLayout.translationY = -binding.appBarLayout.bottom.toFloat()
        systemUiHelper.hide()

        setupSlider()
        setupGestureDetector()
        updateTitle()
        loadAlbumArt()
    }

    private fun loadAlbumArt() {
        if (paths.isEmpty()) return
        val path = currentPath
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val context = requireContext()
                val uri = path.fileProviderUri
                val art = withContext(Dispatchers.IO) {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, uri)
                        retriever.embeddedPicture?.let {
                            BitmapFactory.decodeByteArray(it, 0, it.size)
                        }
                    } finally {
                        retriever.release()
                    }
                }
                if (art != null) {
                    binding.albumArt.setImageBitmap(art)
                    binding.albumArt.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    binding.albumArt.imageTintList = null
                    binding.albumArt.setPadding(0, 0, 0, 0)
                }
            } catch (_: Exception) { }
        }
    }

    private fun setupSlider() {
        binding.seekBar.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                isSeeking = true
            }
            override fun onStopTrackingTouch(slider: Slider) {
                isSeeking = false
                val duration = player?.duration ?: 0L
                if (duration > 0) {
                    val newPosition =
                        (slider.value / SEEK_BAR_MAX * duration.toFloat()).toLong()
                    player?.seekTo(newPosition)
                }
                startProgressUpdates()
            }
        })
        binding.seekBar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val duration = player?.duration ?: 0L
                if (duration > 0) {
                    val newPosition = (value / SEEK_BAR_MAX * duration.toFloat()).toLong()
                    binding.seekCurrentTimeText.text = formatTime(newPosition)
                }
            }
        }
        binding.timeLayout.setOnClickListener {
            showMilliseconds = !showMilliseconds
            updateSeekBar()
        }
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetectorCompat(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    toggleControls()
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    player?.let {
                        if (it.isPlaying) it.pause() else it.play()
                        if (!controlsVisible) toggleControls()
                        else updateSeekBar()
                    }
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    startFastForward()
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    if (e1 != null && abs(e2.x - e1.x) > abs(e2.y - e1.y)) {
                        if (!isSwiping && abs(e2.x - e1.x) > SWIPE_THRESHOLD) {
                            isSwiping = true
                            swipeStartX = e1.x
                            swipeStartPosition = player?.currentPosition ?: 0L
                        }
                        if (isSwiping) {
                            val deltaX = e2.x - swipeStartX
                            val seekDelta = (deltaX * SEEK_PER_PIXEL).toLong()
                            val duration = player?.duration ?: 0L
                            val newPosition =
                                (swipeStartPosition + seekDelta).coerceIn(0L, duration)
                            player?.seekTo(newPosition)

                            val seekSeconds = seekDelta / 1000
                            val sign = if (seekSeconds >= 0) "+" else ""
                            binding.speedIndicator.text = "${sign}${seekSeconds}s"
                            binding.speedIndicator.visibility = View.VISIBLE

                            showSeekBar(newPosition, duration)
                        }
                        return true
                    }
                    return false
                }
            }
        )

        binding.albumArt.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isLongPressing) stopFastForward()
                    if (isSwiping) {
                        isSwiping = false
                        binding.speedIndicator.visibility = View.GONE
                    }
                }
            }
            true
        }
    }

    private fun startFastForward() {
        isLongPressing = true
        player?.let {
            originalSpeed = it.playbackParameters.speed
            it.setPlaybackSpeed(2.0f)
        }
        binding.speedIndicator.visibility = View.VISIBLE
        binding.speedIndicator.text = "2.0x"
    }

    private fun stopFastForward() {
        isLongPressing = false
        player?.setPlaybackSpeed(originalSpeed)
        binding.speedIndicator.visibility = View.GONE
    }

    private fun toggleControls() {
        if (controlsVisible) hideControls() else showControls()
    }

    private fun showControls() {
        controlsVisible = true
        binding.appBarLayout.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(200)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
        binding.seekBarLayout.let { layout ->
            layout.alpha = 0f
            layout.visibility = View.VISIBLE
            layout.animate().alpha(1f).setDuration(200).start()
        }
        updateSeekBar()
        if (player?.isPlaying == true) startProgressUpdates()
        systemUiHelper.show()
    }

    private fun hideControls() {
        controlsVisible = false
        stopProgressUpdates()
        binding.appBarLayout.animate()
            .alpha(0f)
            .translationY(-binding.appBarLayout.bottom.toFloat())
            .setDuration(300)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
        binding.seekBarLayout.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction { binding.seekBarLayout.visibility = View.GONE }
            .start()
        systemUiHelper.hide()
    }

    private fun startProgressUpdates() {
        updateHandler.removeCallbacks(updateRunnable)
        updateHandler.post(updateRunnable)
    }

    private fun stopProgressUpdates() {
        updateHandler.removeCallbacks(updateRunnable)
    }

    private fun updateSeekBar() {
        val currentPos = player?.currentPosition ?: 0L
        val duration = player?.duration ?: 0L
        if (duration > 0 && !isSeeking) {
            binding.seekBar.value =
                (currentPos.toFloat() / duration.toFloat() * SEEK_BAR_MAX)
                    .coerceIn(0f, SEEK_BAR_MAX)
        }
        binding.seekCurrentTimeText.text = formatTime(currentPos)
        binding.seekTotalTimeText.text = formatTime(duration)
    }

    private fun showSeekBar(currentPositionMs: Long, durationMs: Long) {
        if (!controlsVisible) {
            controlsVisible = true
            binding.appBarLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
            systemUiHelper.show()
        }
        binding.seekBarLayout.let { layout ->
            if (layout.visibility != View.VISIBLE) {
                layout.alpha = 0f
                layout.visibility = View.VISIBLE
                layout.animate().alpha(1f).setDuration(200).start()
            }
        }
        if (durationMs > 0) {
            binding.seekBar.value =
                (currentPositionMs.toFloat() / durationMs.toFloat() * SEEK_BAR_MAX)
                    .coerceIn(0f, SEEK_BAR_MAX)
        }
        binding.seekCurrentTimeText.text = formatTime(currentPositionMs)
        binding.seekTotalTimeText.text = formatTime(durationMs)
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (showMilliseconds) {
            val millis = ms % 1000
            if (hours > 0) String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, millis)
            else String.format("%02d:%02d.%03d", minutes, seconds, millis)
        } else {
            if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
            else String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer() {
        if (paths.isEmpty()) return

        player = ExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                binding.progressBar.visibility = View.GONE
                            }
                            Player.STATE_BUFFERING -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            else -> {}
                        }
                    }
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            startProgressUpdates()
                        } else {
                            stopProgressUpdates()
                            if (controlsVisible) updateSeekBar()
                        }
                    }
                })

                val path = paths[currentPosition]
                val uri = path.fileProviderUri
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            }
    }

    private fun releasePlayer() {
        stopProgressUpdates()
        player?.let { exoPlayer ->
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putState(State(paths, currentPosition))
    }

    private fun confirmDelete() {
        ConfirmDeleteAudioDialogFragment.show(currentPath, this)
    }

    override fun delete(path: Path) {
        releasePlayer()
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
            initializePlayer()
            return
        }
        paths.removeAll(listOf(path))
        if (paths.isEmpty()) {
            finish()
            return
        }
        if (currentPosition >= paths.size) {
            currentPosition = paths.size - 1
        }
        initializePlayer()
        loadAlbumArt()
        updateTitle()
    }

    private fun updateTitle() {
        val path = currentPath
        requireActivity().title = path.fileName.toString()
        val size = paths.size
        binding.toolbar.subtitle = if (size > 1) {
            getString(R.string.audio_player_subtitle_format, currentPosition + 1, size)
        } else {
            null
        }
    }

    private fun share() {
        val path = currentPath
        val intent = path.fileProviderUri.createSendStreamIntent(MimeType.ANY)
            .apply { extraPath = path }
            .withChooser()
        startActivitySafe(intent)
    }

    private val currentPath: Path
        get() = paths[currentPosition]

    @Parcelize
    class Args(val intent: Intent, val position: Int) : ParcelableArgs

    @Parcelize
    private class State(
        val paths: @WriteWith<ParcelableListParceler> List<Path>,
        val position: Int
    ) : ParcelableState
}
