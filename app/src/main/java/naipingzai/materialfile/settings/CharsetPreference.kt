/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.settings

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.preference.ListPreference
import java.nio.charset.Charset

class CharsetPreference : ListPreference {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private val allDisplayNames: List<String>
    private val allCharsetNames: List<String>

    init {
        val charsets = Charset.availableCharsets()
        allCharsetNames = charsets.keys.toList()
        allDisplayNames = charsets.values.map { it.displayName() }
        entries = allDisplayNames.toTypedArray()
        entryValues = allCharsetNames.toTypedArray()
    }

    /**
     * Filter the charset list by a query string. Matches against both the charset
     * name (e.g. "UTF-8") and its display name (e.g. "UTF-8"). Passing an empty
     * or blank query restores the full list.
     */
    fun filterCharsets(query: String?) {
        val trimmed = query?.trim() ?: ""
        if (trimmed.isEmpty()) {
            entries = allDisplayNames.toTypedArray()
            entryValues = allCharsetNames.toTypedArray()
            return
        }
        val lowerQuery = trimmed.lowercase()
        val filtered = allCharsetNames.indices.filter { i ->
            allCharsetNames[i].lowercase().contains(lowerQuery) ||
                    allDisplayNames[i].lowercase().contains(lowerQuery)
        }
        entries = filtered.map { allDisplayNames[it] }.toTypedArray()
        entryValues = filtered.map { allCharsetNames[it] }.toTypedArray()
    }
}
