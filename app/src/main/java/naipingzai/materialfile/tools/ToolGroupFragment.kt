/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import naipingzai.materialfile.databinding.ToolGroupFragmentBinding

/**
 * 通用工具分组 Fragment —— 替代 FileToolsFragment、StorageToolsFragment、SystemToolsFragment。
 *
 * 通过 [ToolGroupPage.ARG_PAGE] 参数确定显示哪个工具分组。
 */
class ToolGroupFragment : Fragment() {
    private lateinit var binding: ToolGroupFragmentBinding

    private val page: ToolGroupPage by lazy {
        ToolGroupPage.valueOf(requireArguments().getString(ToolGroupPage.ARG_PAGE)!!)
    }

    private val toolItems by lazy { page.createToolItems() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = ToolGroupFragmentBinding.inflate(inflater, container, false)
        .also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = ToolCardAdapter(toolItems) { item ->
            startActivity(item.intent)
        }
    }
}
