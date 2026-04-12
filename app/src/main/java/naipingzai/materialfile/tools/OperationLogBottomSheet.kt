/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.annotation.MainThread
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.OperationLogBottomSheetBinding
import java.util.Locale

/**
 * M3 风格的操作日志底部面板。
 *
 * 用法示例：
 * ```kotlin
 * val logSheet = OperationLogBottomSheet.newInstance("视频合并")
 * logSheet.show(childFragmentManager, "op_log")
 *
 * logSheet.setStep("规范化", 1, totalFiles)
 * logSheet.appendLog("⏳ 正在转码 file1.mp4 …")
 * logSheet.setProgress(1, totalFiles)
 * logSheet.appendLog("✅ file1.mp4 完成 (3.2s)")
 *
 * logSheet.finish(success = true)   // 或 finish(success = false, "错误原因")
 * ```
 */
class OperationLogBottomSheet : DialogFragment() {

    private var _binding: OperationLogBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val logBuilder = StringBuilder()
    private var startTime = 0L
    private var onCancelClick: (() -> Unit)? = null
    private var isFinished = false

    companion object {
        private const val ARG_TITLE = "title"
        private const val MAX_LOG_CHARS = 50_000
        private const val DIALOG_WIDTH_RATIO = 0.85f
        private const val DIALOG_HEIGHT_RATIO = 0.65f

        fun newInstance(title: String): OperationLogBottomSheet =
            OperationLogBottomSheet().apply {
                arguments = Bundle().apply { putString(ARG_TITLE, title) }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = OperationLogBottomSheetBinding.inflate(inflater, container, false)
        .also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString(ARG_TITLE)
            ?: getString(R.string.operation_log_title)
        binding.titleText.text = title
        startTime = SystemClock.elapsedRealtime()

        binding.cancelButton.setOnClickListener {
            onCancelClick?.invoke()
            dismiss()
        }
        binding.closeButton.setOnClickListener { dismiss() }

        // Don't allow dismiss by tapping outside during operation
        isCancelable = false
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val dm = resources.displayMetrics
            setLayout(
                (dm.widthPixels * DIALOG_WIDTH_RATIO).toInt(),
                (dm.heightPixels * DIALOG_HEIGHT_RATIO).toInt()
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ========== Public API ==========

    /** 设置取消回调 */
    @MainThread
    fun setOnCancelListener(listener: () -> Unit) {
        onCancelClick = listener
    }

    /** 添加一行日志 */
    @MainThread
    fun appendLog(message: String) {
        if (_binding == null) return
        if (logBuilder.isNotEmpty()) logBuilder.append('\n')
        logBuilder.append(message)

        // Trim if too long
        if (logBuilder.length > MAX_LOG_CHARS) {
            val trimmed = logBuilder.substring(logBuilder.length - MAX_LOG_CHARS)
            logBuilder.clear()
            logBuilder.append("… (earlier logs trimmed)\n")
            logBuilder.append(trimmed)
        }

        binding.logText.text = logBuilder.toString()
        // Auto-scroll to bottom
        binding.logScrollView.post {
            binding.logScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
        updateElapsed()
    }

    /** 设置当前步骤描述 + 进度 (current/total) */
    @MainThread
    fun setStep(stepName: String, current: Int = 0, total: Int = 0) {
        if (_binding == null) return
        binding.stepSummaryRow.isVisible = true
        binding.stepText.text = stepName
        if (total > 0) {
            binding.progressText.text = getString(R.string.operation_log_progress, current, total)
            binding.progressText.isVisible = true
        } else {
            binding.progressText.isVisible = false
        }
    }

    /** 更新进度 (current / total)，同时切换到确定进度模式 */
    @MainThread
    fun setProgress(current: Int, total: Int) {
        if (_binding == null) return
        if (total > 0) {
            binding.progressIndicator.isIndeterminate = false
            binding.progressIndicator.max = total
            binding.progressIndicator.setProgressCompat(current, true)
            binding.progressText.text = getString(R.string.operation_log_progress, current, total)
            binding.progressText.isVisible = true
        }
        updateElapsed()
    }

    /** 标记操作完成 */
    @MainThread
    fun finish(success: Boolean, errorMessage: String? = null) {
        if (_binding == null || isFinished) return
        isFinished = true
        isCancelable = true

        binding.progressIndicator.isVisible = false
        binding.cancelButton.isVisible = false
        binding.closeButton.isVisible = true
        binding.statusSpinner.isVisible = false
        binding.statusIcon.isVisible = true

        val elapsed = formatElapsed()
        binding.elapsedText.text = elapsed
        binding.elapsedText.isVisible = true

        if (success) {
            binding.statusIcon.setImageResource(R.drawable.check_icon_white_24dp)
            binding.statusIcon.imageTintList =
                android.content.res.ColorStateList.valueOf(
                    com.google.android.material.color.MaterialColors.getColor(
                        binding.root, com.google.android.material.R.attr.colorPrimary
                    )
                )
            binding.statusIcon.contentDescription = getString(R.string.operation_log_completed)
            appendLog("\n✅ ${getString(R.string.operation_log_completed)} ($elapsed)")
        } else {
            binding.statusIcon.setImageResource(R.drawable.error_icon_white_24dp)
            binding.statusIcon.imageTintList =
                android.content.res.ColorStateList.valueOf(
                    com.google.android.material.color.MaterialColors.getColor(
                        binding.root, com.google.android.material.R.attr.colorError
                    )
                )
            binding.statusIcon.contentDescription = getString(R.string.operation_log_failed)
            appendLog("\n❌ ${getString(R.string.operation_log_failed)}")
            if (!errorMessage.isNullOrBlank()) {
                appendLog("   $errorMessage")
            }
        }

        binding.stepSummaryRow.isVisible = false
    }

    // ========== Internal ==========

    private fun updateElapsed() {
        if (_binding == null) return
        binding.elapsedText.text = formatElapsed()
        binding.elapsedText.isVisible = true
    }

    private fun formatElapsed(): String {
        val ms = SystemClock.elapsedRealtime() - startTime
        val secs = ms / 1000
        return if (secs < 60) {
            String.format(Locale.US, "%ds", secs)
        } else {
            String.format(Locale.US, "%dm %02ds", secs / 60, secs % 60)
        }
    }
}
