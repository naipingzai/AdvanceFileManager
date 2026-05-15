/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.res.TypedArrayUtils
import androidx.core.content.res.use
import androidx.preference.Preference
import com.takisoft.preferencex.PreferenceActivityResultListener
import com.takisoft.preferencex.PreferenceFragmentCompat
import java8.nio.file.Path
import naipingzai.materialfile.filelist.FileListActivity
import naipingzai.materialfile.filelist.toUserFriendlyString
import naipingzai.materialfile.navigation.NavigationRootMapLiveData
import naipingzai.materialfile.util.startActivityForResultSafe
import naipingzai.materialfile.util.valueCompat
import java.util.concurrent.atomic.AtomicInteger

abstract class PathPreference : Preference, PreferenceActivityResultListener {
    private val openPathContract = FileListActivity.OpenDirectoryContract()

    var path: Path = persistedPath
        set(value) {
            if (field == value) {
                return
            }
            field = value
            persistedPath = value
            notifyChanged()
        }

    constructor(context: Context) : super(context) {
        init(null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(attrs, defStyleAttr, 0)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs, defStyleAttr, defStyleRes)
    }

    @SuppressLint("PrivateResource", "RestrictedApi")
    private fun init(attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) {
        isPersistent = false
        context.obtainStyledAttributes(
            attrs, androidx.preference.R.styleable.EditTextPreference, defStyleAttr, defStyleRes
        ).use {
            if (TypedArrayUtils.getBoolean(
                it, androidx.preference.R.styleable.EditTextPreference_useSimpleSummaryProvider,
                androidx.preference.R.styleable.EditTextPreference_useSimpleSummaryProvider, false
            )) {
                summaryProvider = SimpleSummaryProvider
            }
        }
    }

    override fun onPreferenceClick(fragment: PreferenceFragmentCompat, preference: Preference) {
        fragment.startActivityForResultSafe(
            openPathContract.createIntent(fragment.requireContext(), path), requestCode
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == this.requestCode) {
            val result = openPathContract.parseResult(resultCode, data)
            if (result != null) {
                path = result
            }
        }
    }

    private val requestCode: Int = nextRequestCode.getAndIncrement()

    protected abstract var persistedPath: Path

    object SimpleSummaryProvider : SummaryProvider<PathPreference> {
        override fun provideSummary(preference: PathPreference): CharSequence? {
            val path = preference.path
            val navigationRoot = NavigationRootMapLiveData.valueCompat[path]
            return navigationRoot?.getName(preference.context) ?: path.toUserFriendlyString()
        }
    }

    companion object {
        private val nextRequestCode = AtomicInteger(1)
    }
}
