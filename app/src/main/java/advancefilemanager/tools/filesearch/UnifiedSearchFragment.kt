/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.tools.filesearch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.advancefilemanager.R
import com.advancefilemanager.databinding.UnifiedSearchFragmentBinding
import com.advancefilemanager.tools.duplicatefinder.DuplicateFinderFragment
import com.advancefilemanager.tools.emptysearch.EmptySearchFragment
import com.advancefilemanager.tools.filecompare.FileCompareFragment
import com.advancefilemanager.tools.recentfiles.RecentFilesFragment

class UnifiedSearchFragment : Fragment() {

    private lateinit var binding: UnifiedSearchFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        UnifiedSearchFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val extraPath = arguments?.getString(EXTRA_PATH)
        val initialTab = arguments?.getInt(EXTRA_TAB_INDEX, 0) ?: 0

        binding.viewPager.adapter = SearchPagerAdapter(this, extraPath)
        binding.viewPager.offscreenPageLimit = 1

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.unified_search_tab_file_search)
                1 -> getString(R.string.unified_search_tab_duplicates)
                2 -> getString(R.string.unified_search_tab_empty)
                3 -> getString(R.string.unified_search_tab_recent)
                4 -> getString(R.string.unified_search_tab_compare)
                else -> ""
            }
        }.attach()

        if (savedInstanceState == null && initialTab in 0..4) {
            binding.viewPager.setCurrentItem(initialTab, false)
        }
    }

    private class SearchPagerAdapter(
        fragment: Fragment,
        private val extraPath: String?
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment {
            val args = Bundle().apply {
                putBoolean(ARG_EMBEDDED, true)
                if (extraPath != null) {
                    putString(EXTRA_PATH, extraPath)
                }
            }
            return when (position) {
                0 -> FileSearchFragment().apply { arguments = args }
                1 -> DuplicateFinderFragment().apply { arguments = args }
                2 -> EmptySearchFragment().apply { arguments = args }
                3 -> RecentFilesFragment().apply { arguments = args }
                4 -> FileCompareFragment().apply { arguments = args }
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }

    companion object {
        const val EXTRA_PATH = "extra_path"
        const val EXTRA_TAB_INDEX = "extra_tab_index"
        const val ARG_EMBEDDED = "embedded"
    }
}
