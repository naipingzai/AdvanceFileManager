/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.viewer.hex

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import naipingzai.materialfile.R

class HexAdapter(
    private val rows: List<HexRow>
) : RecyclerView.Adapter<HexAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val offsetText: TextView = view.findViewById(R.id.offsetText)
        val hexText: TextView = view.findViewById(R.id.hexText)
        val asciiText: TextView = view.findViewById(R.id.asciiText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hex_row_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = rows[position]

        // Offset column
        holder.offsetText.text = String.format("%08X", row.offset)

        // Hex column
        val hexBuilder = StringBuilder()
        for (i in row.bytes.indices) {
            if (i > 0) hexBuilder.append(' ')
            if (i == 8) hexBuilder.append(' ')
            hexBuilder.append(String.format("%02X", row.bytes[i].toInt() and 0xFF))
        }
        // Pad if less than 16 bytes
        for (i in row.bytes.size until 16) {
            if (i > 0) hexBuilder.append(' ')
            if (i == 8) hexBuilder.append(' ')
            hexBuilder.append("  ")
        }
        holder.hexText.text = hexBuilder.toString()

        // ASCII column
        val asciiBuilder = StringBuilder()
        for (b in row.bytes) {
            val c = b.toInt() and 0xFF
            asciiBuilder.append(if (c in 0x20..0x7E) c.toChar() else '.')
        }
        holder.asciiText.text = asciiBuilder.toString()
    }

    override fun getItemCount(): Int = rows.size
}
