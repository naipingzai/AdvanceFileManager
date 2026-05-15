/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

/**
 * 通用 Dialog 宿主 Activity（Translucent 主题）——
 * 替代所有仅做 DialogFragment 容器的薄壳 Activity。
 *
 * Intent extras 会自动转发为 Fragment arguments，使 args 委托在 Fragment 中正常工作。
 *
 * 使用方法：
 * ```
 * startActivity(
 *     DialogHostActivity.createIntent<MyDialogFragment>()
 *         .putArgs(MyDialogFragment.Args(…))
 * )
 * ```
 */
class DialogHostActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 触发 ensureSubDecor()
        findViewById<View>(android.R.id.content)

        if (savedInstanceState == null) {
            val fragmentClassName = intent.getStringExtra(EXTRA_FRAGMENT_CLASS)
                ?: throw IllegalArgumentException("DialogHostActivity requires EXTRA_FRAGMENT_CLASS")
            val fragment = supportFragmentManager.fragmentFactory
                .instantiate(classLoader, fragmentClassName)
            // 将 Intent extras 转发给 Fragment arguments（使 putArgs/args 委托正常工作）
            intent.extras?.let { extras ->
                fragment.arguments = (fragment.arguments ?: Bundle()).apply {
                    putAll(extras)
                }
            }
            supportFragmentManager.commit {
                add(fragment, fragmentClassName)
            }
        }
    }

    companion object {
        private const val EXTRA_FRAGMENT_CLASS =
            "com.advancefilemanager.extra.DIALOG_FRAGMENT_CLASS"

        /**
         * 创建启动指定 DialogFragment 的 Intent。
         */
        inline fun <reified T : Fragment> createIntent(): Intent =
            createIntent(T::class.java.name)

        /**
         * 使用 Fragment 类的全限定名创建 Intent。
         */
        fun createIntent(fragmentClassName: String): Intent =
            Intent(application, DialogHostActivity::class.java).apply {
                putExtra(EXTRA_FRAGMENT_CLASS, fragmentClassName)
            }
    }
}
