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
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.R
import com.advancefilemanager.databinding.FragmentFileSearchBinding
import kotlinx.coroutines.*
import java.io.File

class FileSearchToolFragment : Fragment() {

    private var _binding: FragmentFileSearchBinding? = null
    private val binding get() = _binding!!

    private val results = mutableListOf<File>()
    private val adapter = FileListAdapter(results)
    private var searchJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFileSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    results.clear()
                    adapter.notifyDataSetChanged()
                    binding.emptyText.visibility = View.VISIBLE
                }
                return true
            }
        })

        binding.emptyText.visibility = View.VISIBLE
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        val rootPath = arguments?.getString("filePath") ?: "/sdcard"
        val rootDir = File(rootPath)

        binding.progressBar.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE
        results.clear()
        adapter.notifyDataSetChanged()

        searchJob = scope.launch {
            val found = withContext(Dispatchers.IO) {
                searchFiles(rootDir, query)
            }
            results.clear()
            results.addAll(found)
            adapter.notifyDataSetChanged()
            binding.progressBar.visibility = View.GONE
            binding.emptyText.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
            if (results.isEmpty()) {
                binding.emptyText.text = "未找到匹配的文件"
            }
        }
    }

    private fun searchFiles(dir: File, query: String): List<File> {
        val result = mutableListOf<File>()
        val lowerQuery = query.lowercase()
        try {
            dir.walkTopDown().forEach { file ->
                if (file.name.lowercase().contains(lowerQuery)) {
                    result.add(file)
                    if (result.size >= 500) return result
                }
            }
        } catch (_: Exception) {}
        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        scope.cancel()
        _binding = null
    }

    private class FileListAdapter(private val files: List<File>) :
        RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

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
            holder.subtitle.text = "${file.parent} · ${TrashUtil.formatFileSize(file.length())} · ${TrashUtil.formatDate(file.lastModified())}"
        }

        override fun getItemCount() = files.size
    }
}
