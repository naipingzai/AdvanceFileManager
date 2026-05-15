/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import java8.nio.file.Path
import com.advancefilemanager.databinding.NavigationFragmentBinding
import com.advancefilemanager.util.startActivitySafe

class NavigationFragment : Fragment(), NavigationItem.Listener {
    private lateinit var binding: NavigationFragmentBinding

    private lateinit var adapter: NavigationListAdapter

    var listener: Listener? = null
        set(value) {
            field = value
            if (value != null && view != null) {
                bindListener()
            }
        }

    private var listenerBound = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        NavigationFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.setHasFixedSize(true)
        val context = requireContext()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = NavigationListAdapter(this, context)
        binding.recyclerView.adapter = adapter

        val viewLifecycleOwner = viewLifecycleOwner
        NavigationItemListLiveData.observe(viewLifecycleOwner) { onNavigationItemsChanged(it) }
        if (listener != null) {
            bindListener()
        }
    }

    private fun bindListener() {
        if (listenerBound) return
        val l = listener ?: return
        listenerBound = true
        l.observeCurrentPath(viewLifecycleOwner) { onCurrentPathChanged(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerBound = false
    }

    private fun onNavigationItemsChanged(navigationItems: List<NavigationItem?>) {
        adapter.replace(navigationItems)
    }

    private fun onCurrentPathChanged(path: Path) {
        adapter.notifyCheckedChanged()
    }

    override val currentPath: Path
        get() = listener!!.currentPath

    override fun navigateTo(path: Path) {
        listener?.navigateTo(path)
    }

    override fun navigateToRoot(path: Path) {
        listener?.navigateToRoot(path)
    }

    override fun launchIntent(intent: Intent) {
        listener?.onDrawerIntentLaunching()
        startActivitySafe(intent)
    }

    override fun closeNavigationDrawer() {
        listener?.closeNavigationDrawer()
    }

    interface Listener {
        val currentPath: Path
        fun navigateTo(path: Path)
        fun navigateToRoot(path: Path)
        fun navigateToDefaultRoot()
        fun observeCurrentPath(owner: LifecycleOwner, observer: (Path) -> Unit)
        fun closeNavigationDrawer()
        /** 通知宿主即将从抽屉启动一个 Intent（用于返回时恢复抽屉状态） */
        fun onDrawerIntentLaunching()
    }
}
