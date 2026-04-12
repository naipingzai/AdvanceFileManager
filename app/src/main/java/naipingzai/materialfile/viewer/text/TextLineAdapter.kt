/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.viewer.text

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import naipingzai.materialfile.R

class TextLineAdapter(
    private val lines: List<String>,
    private var lineNumberWidth: Int = 4
) : RecyclerView.Adapter<TextLineAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lineNumber: TextView = view.findViewById(R.id.lineNumber)
        val lineContent: TextView = view.findViewById(R.id.lineContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_line_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.lineNumber.text = String.format("%${lineNumberWidth}d", position + 1)
        holder.lineContent.text = lines[position]
    }

    override fun getItemCount(): Int = lines.size

    fun updateLineNumberWidth(totalLines: Int) {
        val newWidth = totalLines.toString().length.coerceAtLeast(4)
        if (newWidth != lineNumberWidth) {
            lineNumberWidth = newWidth
            notifyItemRangeChanged(0, lines.size)
        }
    }
}
