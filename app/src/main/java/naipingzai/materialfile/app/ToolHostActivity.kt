/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

/**
 * 通用工具宿主 Activity —— 替代所有"薄壳 Activity + Fragment"的样板代码。
 *
 * 使用方法：
 * ```
 * startActivity(ToolHostActivity.createIntent<MyFragment>(R.string.my_title))
 * ```
 */
class ToolHostActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 触发 ensureSubDecor()
        findViewById<View>(android.R.id.content)

        // 从 Intent extra 恢复标题
        val titleRes = intent.getIntExtra(EXTRA_TITLE_RES, 0)
        if (titleRes != 0) {
            setTitle(titleRes)
        }

        if (savedInstanceState == null) {
            val fragmentClassName = intent.getStringExtra(EXTRA_FRAGMENT_CLASS)
                ?: throw IllegalArgumentException("ToolHostActivity requires EXTRA_FRAGMENT_CLASS")
            val fragment = supportFragmentManager.fragmentFactory
                .instantiate(classLoader, fragmentClassName)
            // 将 Intent extras 转发给 Fragment arguments，使调用方可以传递任意参数
            intent.extras?.let { extras ->
                fragment.arguments = (fragment.arguments ?: Bundle()).apply {
                    putAll(extras)
                }
            }
            supportFragmentManager.commit {
                add(android.R.id.content, fragment)
            }
        }
    }

    companion object {
        private const val EXTRA_FRAGMENT_CLASS =
            "naipingzai.materialfile.extra.FRAGMENT_CLASS"
        private const val EXTRA_TITLE_RES =
            "naipingzai.materialfile.extra.TITLE_RES"

        /**
         * 创建启动指定 Fragment 的 Intent。
         *
         * @param T         要加载的 Fragment 类型
         * @param titleRes  在 ActionBar / 最近任务中显示的标题字符串资源 ID
         */
        inline fun <reified T : Fragment> createIntent(
            @StringRes titleRes: Int = 0
        ): Intent = createIntent(T::class.java.name, titleRes)

        /**
         * 使用 Fragment 类的全限定名创建 Intent（供非内联场景使用）。
         */
        fun createIntent(
            fragmentClassName: String,
            @StringRes titleRes: Int = 0
        ): Intent = Intent(application, ToolHostActivity::class.java).apply {
            putExtra(EXTRA_FRAGMENT_CLASS, fragmentClassName)
            if (titleRes != 0) {
                putExtra(EXTRA_TITLE_RES, titleRes)
            }
        }
    }
}
