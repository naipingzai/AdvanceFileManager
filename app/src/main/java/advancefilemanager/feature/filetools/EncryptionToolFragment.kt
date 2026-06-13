/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package com.advancefilemanager.feature.filetools

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.advancefilemanager.R
import com.advancefilemanager.databinding.FragmentEncryptionBinding
import kotlinx.coroutines.*
import java.io.File
import com.advancefilemanager.ui.applyOverlay

class EncryptionToolFragment : Fragment() {

    private var _binding: FragmentEncryptionBinding? = null
    private val binding get() = _binding!!

    private val files = mutableListOf<File>()
    private val adapter = EncryptionAdapter(files)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEncryptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnEncrypt.setOnClickListener { showPasswordDialog(encrypt = true) }
        binding.btnDecrypt.setOnClickListener { showPasswordDialog(encrypt = false) }

        loadFiles()
    }

    private fun loadFiles() {
        val passedPaths = arguments?.getStringArray("filePaths")
        if (!passedPaths.isNullOrEmpty()) {
            files.clear()
            files.addAll(passedPaths.map { File(it) }.filter { it.isFile }.sortedBy { it.name })
            adapter.notifyDataSetChanged()
            return
        }
        val rootPath = arguments?.getString("filePath") ?: "/sdcard"
        val rootDir = File(rootPath)
        scope.launch {
            val found = withContext(Dispatchers.IO) {
                rootDir.listFiles()?.filter { it.isFile }?.sortedBy { it.name } ?: emptyList()
            }
            files.clear()
            files.addAll(found)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showPasswordDialog(encrypt: Boolean) {
        val selected = adapter.getSelectedFiles()
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "请选择文件", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(requireContext()).apply {
            hint = "输入密码"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (encrypt) "加密文件" else "解密文件")
            .setMessage("请输入${if (encrypt) "加密" else "解密"}密码")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val password = input.text.toString()
                if (password.isEmpty()) {
                    Toast.makeText(requireContext(), "密码不能为空", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                processFiles(selected, password, encrypt)
            }
            .setNegativeButton("取消", null)
            .create()
            .applyOverlay(requireContext())
            .show()
    }

    private fun processFiles(selectedFiles: List<File>, password: String, encrypt: Boolean) {
        binding.progressBar.visibility = View.VISIBLE
        scope.launch {
            var successCount = 0
            var failCount = 0
            withContext(Dispatchers.IO) {
                for (file in selectedFiles) {
                    val result = if (encrypt) {
                        FileEncryptionUtil.encrypt(file, password)
                    } else {
                        FileEncryptionUtil.decrypt(file, password)
                    }
                    if (result != null) successCount++ else failCount++
                }
            }
            binding.progressBar.visibility = View.GONE
            val action = if (encrypt) "加密" else "解密"
            Toast.makeText(
                requireContext(),
                "${action}完成: 成功 $successCount, 失败 $failCount",
                Toast.LENGTH_SHORT
            ).show()
            loadFiles()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        _binding = null
    }

    private class EncryptionAdapter(private val files: List<File>) :
        RecyclerView.Adapter<EncryptionAdapter.ViewHolder>() {

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
            val isEncrypted = FileEncryptionUtil.isEncryptedFile(file)
            holder.title.text = file.name
            holder.subtitle.text = buildString {
                append(TrashUtil.formatFileSize(file.length()))
                if (isEncrypted) append(" · 已加密")
            }
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
