/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.about

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.advancefilemanager.R
import com.advancefilemanager.databinding.AboutFragmentBinding
import com.advancefilemanager.tools.formatconvert.FFmpegJni
import com.advancefilemanager.ui.LicensesDialogFragment
import com.advancefilemanager.util.createViewIntent
import com.advancefilemanager.util.startActivitySafe

class AboutFragment : Fragment() {
    private lateinit var binding: AboutFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        AboutFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.gitHubLayout.setOnClickListener { /* GitHub link removed */ }
        binding.licensesLayout.setOnClickListener { LicensesDialogFragment.show(this) }
//#ifdef NONFREE
        binding.privacyPolicyLayout.isVisible = true
        binding.privacyPolicyLayout.setOnClickListener {
            startActivitySafe(PRIVACY_POLICY_URI.createViewIntent())
        }
//#endif
        binding.authorNameLayout.setOnClickListener { /* Link removed */ }
        binding.authorGitHubLayout.setOnClickListener { /* Link removed */ }

        // FFmpeg info
        try {
            binding.ffmpegVersionText.text = FFmpegJni.getVersion()
        } catch (e: Exception) {
            binding.ffmpegVersionText.text = "N/A"
        }
        binding.ffmpegCapabilitiesText.text = getString(R.string.about_ffmpeg_capabilities)
    }

    companion object {
        private val PRIVACY_POLICY_URI =
            Uri.parse("")
    }
}
