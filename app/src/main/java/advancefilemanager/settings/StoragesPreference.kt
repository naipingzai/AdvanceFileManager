/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.settings

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import com.advancefilemanager.R
import com.advancefilemanager.app.ToolHostActivity
import com.advancefilemanager.compat.ListFormatterCompat
import com.advancefilemanager.storage.Storage
import com.advancefilemanager.storage.StorageListFragment
import com.advancefilemanager.util.startActivitySafe

class StoragesPreference : Preference {
    private var emptySummary = summary
    private var lifecycleOwner: LifecycleOwner? = null

    private val observer = Observer<List<Storage>> { onStorageListChanged(it) }

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

    init {
        isPersistent = false
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager) {
        super.onAttachedToHierarchy(preferenceManager)

        val owner = context as? LifecycleOwner
        if (owner != null) {
            lifecycleOwner = owner
            Settings.STORAGES.observeForever(observer)
        }
    }

    override fun onDetached() {
        super.onDetached()
        lifecycleOwner?.let {
            Settings.STORAGES.removeObserver(observer)
            lifecycleOwner = null
        }
    }

    private fun onStorageListChanged(storages: List<Storage>) {
        val context = context
        val names = storages.filter { it.isVisible }.map { it.getName(context) }
        val summary = if (names.isNotEmpty()) ListFormatterCompat.format(names) else emptySummary
        setSummary(summary)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val summaryText = holder.findViewById(android.R.id.summary) as TextView
        summaryText.ellipsize = TextUtils.TruncateAt.END
        summaryText.isSingleLine = true
    }

    override fun onClick() {
        context.startActivitySafe(
            ToolHostActivity.createIntent<StorageListFragment>(
                R.string.storage_list_title
            )
        )
    }
}
