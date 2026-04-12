/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.about

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.AboutFragmentBinding
import naipingzai.materialfile.tools.formatconvert.FFmpegJni
import naipingzai.materialfile.ui.LicensesDialogFragment
import naipingzai.materialfile.util.createViewIntent
import naipingzai.materialfile.util.startActivitySafe

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
