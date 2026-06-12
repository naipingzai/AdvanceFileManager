/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.filetools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.R
import com.advancefilemanager.databinding.FragmentRecentFilesBinding
import kotlinx.coroutines.*
import java.io.File

class RecentFilesToolFragment : Fragment() {

    private var _binding: FragmentRecentFilesBinding? = null
    private val binding get() = _binding!!

    private val results = mutableListOf<File>()
    private val adapter = RecentFilesAdapter(results)
    private var scanJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecentFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        loadRecentFiles()
    }

    private fun loadRecentFiles() {
        scanJob?.cancel()
        val rootPath = arguments?.getString("filePath") ?: "/sdcard"
        val rootDir = File(rootPath)

        binding.progressBar.visibility = View.VISIBLE

        scanJob = scope.launch {
            val found = withContext(Dispatchers.IO) {
                findRecentFiles(rootDir)
            }
            results.clear()
            results.addAll(found)
            adapter.notifyDataSetChanged()
            binding.progressBar.visibility = View.GONE
            binding.emptyText.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun findRecentFiles(dir: File): List<File> {
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val result = mutableListOf<File>()
        try {
            dir.walkTopDown().forEach { file ->
                if (file.isFile && file.lastModified() >= sevenDaysAgo) {
                    result.add(file)
                    if (result.size >= 500) return@forEach
                }
            }
        } catch (_: Exception) {}
        return result.sortedByDescending { it.lastModified() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scanJob?.cancel()
        scope.cancel()
        _binding = null
    }

    private class RecentFilesAdapter(private val files: List<File>) :
        RecyclerView.Adapter<RecentFilesAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.itemTitle)
            val subtitle: TextView = view.findViewById(R.id.itemSubtitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_file_tool, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = files[position]
            holder.title.text = file.name
            holder.subtitle.text = "${TrashUtil.formatDate(file.lastModified())} · ${TrashUtil.formatFileSize(file.length())} · ${file.parent}"
        }

        override fun getItemCount() = files.size
    }
}
