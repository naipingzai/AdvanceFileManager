/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.filetools

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
import com.advancefilemanager.R
import com.advancefilemanager.databinding.FragmentEmptySearchBinding
import kotlinx.coroutines.*
import java.io.File

class EmptySearchToolFragment : Fragment() {

    private var _binding: FragmentEmptySearchBinding? = null
    private val binding get() = _binding!!

    private val results = mutableListOf<File>()
    private val adapter = EmptyItemAdapter(results)
    private var scanJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEmptySearchBinding.inflate(inflater, container, false)
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
        results.clear()
        adapter.notifyDataSetChanged()

        scanJob = scope.launch {
            val found = withContext(Dispatchers.IO) {
                findEmpty(rootDir)
            }
            results.clear()
            results.addAll(found)
            adapter.notifyDataSetChanged()
            binding.progressBar.visibility = View.GONE
            binding.btnScan.isEnabled = true
            if (results.isEmpty()) {
                Toast.makeText(requireContext(), "未找到空文件或空文件夹", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun findEmpty(dir: File): List<File> {
        val result = mutableListOf<File>()
        try {
            dir.walkTopDown().forEach { file ->
                if (file == dir) return@forEach
                if (file.isFile && file.length() == 0L) {
                    result.add(file)
                } else if (file.isDirectory) {
                    val children = file.listFiles()
                    if (children == null || children.isEmpty()) {
                        result.add(file)
                    }
                }
                if (result.size >= 1000) return result
            }
        } catch (_: Exception) {}
        return result
    }

    private fun deleteSelected() {
        val toDelete = adapter.getSelectedFiles()
        if (toDelete.isEmpty()) {
            Toast.makeText(requireContext(), "请选择要删除的项目", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            var count = 0
            withContext(Dispatchers.IO) {
                for (file in toDelete) {
                    if (file.deleteRecursively()) count++
                }
            }
            Toast.makeText(requireContext(), "已删除 $count 项", Toast.LENGTH_SHORT).show()
            startScan()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scanJob?.cancel()
        scope.cancel()
        _binding = null
    }

    private class EmptyItemAdapter(private val files: List<File>) :
        RecyclerView.Adapter<EmptyItemAdapter.ViewHolder>() {

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
            val file = files[position]
            holder.title.text = file.name
            holder.subtitle.text = if (file.isDirectory) "空文件夹 · ${file.absolutePath}" else "空文件 · ${file.absolutePath}"
            holder.checkbox.visibility = View.VISIBLE
            holder.checkbox.isChecked = selectedFiles.contains(file.absolutePath)
            holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedFiles.add(file.absolutePath)
                else selectedFiles.remove(file.absolutePath)
            }
        }

        override fun getItemCount() = files.size

        fun getSelectedFiles(): List<File> {
            return selectedFiles.map { File(it) }.filter { it.exists() }
        }
    }
}
