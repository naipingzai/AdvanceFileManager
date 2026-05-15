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
import com.advancefilemanager.navigation.StandardDirectoriesLiveData
import com.advancefilemanager.navigation.StandardDirectory

class StandardDirectoriesPreference : Preference {
    private val observer = Observer<List<StandardDirectory>> { onStandardDirectoriesChanged(it) }
    private var emptySummary = summary
    private var lifecycleOwner: LifecycleOwner? = null

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
            StandardDirectoriesLiveData.observeForever(observer)
        }
    }

    override fun onDetached() {
        super.onDetached()
        lifecycleOwner?.let {
            StandardDirectoriesLiveData.removeObserver(observer)
            lifecycleOwner = null
        }
    }

    private fun onStandardDirectoriesChanged(standardDirectories: List<StandardDirectory>) {
        val context = context
        val titles = standardDirectories.filter { it.isEnabled }.map { it.getTitle(context) }
        val summary = if (titles.isNotEmpty()) ListFormatterCompat.format(titles) else emptySummary
        setSummary(summary)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val summaryText = holder.findViewById(android.R.id.summary) as TextView
        summaryText.ellipsize = TextUtils.TruncateAt.END
        summaryText.isSingleLine = true
    }

    override fun onClick() {
        context.startActivity(
            ToolHostActivity.createIntent<StandardDirectoryListFragment>(
                R.string.settings_standard_directory_list_title
            )
        )
    }
}
