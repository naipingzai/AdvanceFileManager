/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.plugin.filetools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.plugin.filetools.databinding.FragmentDuplicateFinderBinding
import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest

class DuplicateFinderToolFragment : Fragment() {

    private var _binding: FragmentDuplicateFinderBinding? = null
    private val binding get() = _binding!!

    private val duplicateGroups = mutableListOf<DuplicateGroup>()
    private val adapter = DuplicateAdapter(duplicateGroups)
    private var scanJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    data class DuplicateGroup(val hash: String, val size: Long, val files: List<File>)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDuplicateFinderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnScan.setOnClickListener { startScan() }
        binding.btnDelete.setOnClickListener { deleteSelected() }
    }

    private fun startScan() {
        scanJob?.cancel()
        val rootPath = arguments?.getString("filePath") ?: "/sdcard"
        val rootDir = File(rootPath)

        binding.progressBar.visibility = View.VISIBLE
        binding.btnScan.isEnabled = false
        duplicateGroups.clear()
        adapter.notifyDataSetChanged()

        scanJob = scope.launch {
            val groups = withContext(Dispatchers.IO) {
                findDuplicates(rootDir)
            }
            duplicateGroups.clear()
            duplicateGroups.addAll(groups)
            adapter.notifyDataSetChanged()
            binding.progressBar.visibility = View.GONE
            binding.btnScan.isEnabled = true
            if (duplicateGroups.isEmpty()) {
                Toast.makeText(requireContext(), "未找到重复文件", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun findDuplicates(dir: File): List<DuplicateGroup> {
        val sizeMap = mutableMapOf<Long, MutableList<File>>()
        try {
            dir.walkTopDown()
                .filter { it.isFile && it.length() > 0 }
                .forEach { file ->
                    sizeMap.getOrPut(file.length()) { mutableListOf() }.add(file)
                }
        } catch (_: Exception) {}

        val result = mutableListOf<DuplicateGroup>()
        for ((size, files) in sizeMap) {
            if (files.size < 2) continue
            val hashMap = mutableMapOf<String, MutableList<File>>()
            for (file in files) {
                val hash = computeMD5(file) ?: continue
                hashMap.getOrPut(hash) { mutableListOf() }.add(file)
            }
            for ((hash, dupes) in hashMap) {
                if (dupes.size >= 2) {
                    result.add(DuplicateGroup(hash, size, dupes))
                }
            }
        }
        return result.sortedByDescending { it.size * it.files.size }
    }

    private fun computeMD5(file: File): String? {
        return try {
            val md = MessageDigest.getInstance("MD5")
            file.inputStream().use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            null
        }
    }

    private fun deleteSelected() {
        val toDelete = adapter.getSelectedFiles()
        if (toDelete.isEmpty()) {
            Toast.makeText(requireContext(), "请选择要删除的文件", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            var count = 0
            withContext(Dispatchers.IO) {
                for (file in toDelete) {
                    if (TrashUtil.moveToTrash(file)) count++
                }
            }
            Toast.makeText(requireContext(), "已移到回收站: $count 个文件", Toast.LENGTH_SHORT).show()
            startScan()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scanJob?.cancel()
        scope.cancel()
        _binding = null
    }

    private class DuplicateAdapter(private val groups: List<DuplicateGroup>) :
        RecyclerView.Adapter<DuplicateAdapter.ViewHolder>() {

        private val selectedFiles = mutableSetOf<String>()

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.itemTitle)
            val subtitle: TextView = view.findViewById(R.id.itemSubtitle)
            val checkbox: CheckBox = view.findViewById(R.id.itemCheckbox)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_file_tool, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (groupIndex, fileIndex) = getGroupAndFileIndex(position)
            val group = groups[groupIndex]

            if (fileIndex == 0) {
                holder.title.text = "重复组 (${group.files.size} 个文件, 每个 ${TrashUtil.formatFileSize(group.size)})"
                holder.subtitle.text = "MD5: ${group.hash.take(16)}..."
                holder.checkbox.visibility = View.GONE
            } else {
                val file = group.files[fileIndex - 1]
                holder.title.text = file.name
                holder.subtitle.text = file.absolutePath
                holder.checkbox.visibility = View.VISIBLE
                holder.checkbox.isChecked = selectedFiles.contains(file.absolutePath)
                holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedFiles.add(file.absolutePath)
                    else selectedFiles.remove(file.absolutePath)
                }
            }
        }

        override fun getItemCount(): Int {
            return groups.sumOf { it.files.size + 1 }
        }

        private fun getGroupAndFileIndex(position: Int): Pair<Int, Int> {
            var offset = 0
            for (i in groups.indices) {
                val groupSize = groups[i].files.size + 1
                if (position < offset + groupSize) {
                    return i to (position - offset)
                }
                offset += groupSize
            }
            return 0 to 0
        }

        fun getSelectedFiles(): List<File> {
            return selectedFiles.map { File(it) }.filter { it.exists() }
        }
    }
}
