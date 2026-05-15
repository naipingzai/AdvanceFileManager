/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.settings

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
import naipingzai.materialfile.R
import naipingzai.materialfile.app.ToolHostActivity
import naipingzai.materialfile.compat.ListFormatterCompat
import naipingzai.materialfile.navigation.BookmarkDirectory
import naipingzai.materialfile.util.startActivitySafe

class BookmarkDirectoriesPreference : Preference {
    private var emptySummary = summary
    private var lifecycleOwner: LifecycleOwner? = null

    private val observer = Observer<List<BookmarkDirectory>> { onBookmarkDirectoryListChanged(it) }

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
            Settings.BOOKMARK_DIRECTORIES.observeForever(observer)
        }
    }

    override fun onDetached() {
        super.onDetached()
        lifecycleOwner?.let {
            Settings.BOOKMARK_DIRECTORIES.removeObserver(observer)
            lifecycleOwner = null
        }
    }

    private fun onBookmarkDirectoryListChanged(bookmarkDirectories: List<BookmarkDirectory>) {
        val names = bookmarkDirectories.map { it.name }
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
            ToolHostActivity.createIntent<BookmarkDirectoryListFragment>(
                R.string.settings_bookmark_directory_list_title
            )
        )
    }
}
