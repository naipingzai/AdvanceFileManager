/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.encryption

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import naipingzai.materialfile.R
import naipingzai.materialfile.databinding.EncryptionFragmentBinding
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.filelist.FileListActivity
import naipingzai.materialfile.provider.linux.media.MediaScanner
import naipingzai.materialfile.tools.FileTypeUtils
import naipingzai.materialfile.tools.OperationLogBottomSheet
import naipingzai.materialfile.tools.OutputPaths
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptionFragment : Fragment() {
    private lateinit var binding: EncryptionFragmentBinding
    private val fileList = mutableListOf<EncryptionFileItem>()
    private lateinit var adapter: EncryptionFileAdapter
    private var currentJob: Job? = null

    private val filePickerLauncher = registerForActivityResult(
        FileListActivity.OpenMultipleFilesContract()
    ) { paths: List<java8.nio.file.Path> ->
        if (paths.isEmpty()) return@registerForActivityResult
        paths.forEach { path ->
            val file = path.toFile()
            if (file.exists() && file.isFile) {
                addFile(file)
            }
        }
    }

    @Parcelize
    data class EncryptionFileItem(
        val path: String,
        val name: String,
        val size: Long,
        var status: String = ""
    ) : Parcelable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        EncryptionFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        adapter = EncryptionFileAdapter(fileList) { position ->
            if (position in fileList.indices) {
                fileList.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateCount()
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.addFileButton.setOnClickListener { filePickerLauncher.launch(listOf(MimeType.ANY)) }
        binding.encryptButton.setOnClickListener { processFiles(encrypt = true) }
        binding.decryptButton.setOnClickListener { processFiles(encrypt = false) }

        // Restore saved state
        if (savedInstanceState != null) {
            val saved = savedInstanceState.getParcelableArrayList<EncryptionFileItem>(KEY_FILE_LIST)
            if (saved != null) {
                fileList.clear()
                fileList.addAll(saved)
                adapter.notifyDataSetChanged()
            }
        }

        updateCount()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val saveList = if (fileList.size > MAX_SAVED_FILES) ArrayList(fileList.take(MAX_SAVED_FILES))
            else ArrayList(fileList)
        outState.putParcelableArrayList(KEY_FILE_LIST, saveList)
    }

    companion object {
        private const val KEY_FILE_LIST = "file_list"
        private const val MAX_SAVED_FILES = 500
        private const val BUFFER_SIZE = 8192
        private const val PBKDF2_ITERATIONS = 65536
        private const val AES_KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
        private const val SALT_SIZE = 16
        private const val IV_SIZE = 12
        private const val FILE_HEADER = "MFENC"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentJob?.cancel()
    }

    private fun addFile(file: File) {
        if (fileList.any { it.path == file.absolutePath }) return
        fileList.add(
            EncryptionFileItem(
                path = file.absolutePath,
                name = file.name,
                size = file.length()
            )
        )
        adapter.notifyItemInserted(fileList.size - 1)
        updateCount()
    }

    private fun updateCount() {
        binding.fileCountText.text = getString(R.string.encryption_file_count, fileList.size)
    }

    private fun processFiles(encrypt: Boolean) {
        if (fileList.isEmpty()) {
            Snackbar.make(binding.root, R.string.encryption_no_files, Snackbar.LENGTH_SHORT).show()
            return
        }

        val password = binding.passwordInput.text.toString()
        if (password.isEmpty()) {
            Snackbar.make(binding.root, R.string.encryption_no_password, Snackbar.LENGTH_SHORT).show()
            return
        }

        if (password.length < 4) {
            Snackbar.make(binding.root, R.string.encryption_password_too_short, Snackbar.LENGTH_SHORT).show()
            return
        }

        val actionTitle = if (encrypt) {
            getString(R.string.encryption_confirm_encrypt, fileList.size)
        } else {
            getString(R.string.encryption_confirm_decrypt, fileList.size)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (encrypt) R.string.encryption_encrypt else R.string.encryption_decrypt)
            .setMessage(actionTitle)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                executeProcess(password, encrypt)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun executeProcess(password: String, encrypt: Boolean) {
        binding.progressBar.isVisible = true
        binding.encryptButton.isEnabled = false
        binding.decryptButton.isEnabled = false
        binding.addFileButton.isEnabled = false

        val deleteOriginal = binding.deleteOriginalSwitch.isChecked
        val logSheet = OperationLogBottomSheet.newInstance(
            getString(if (encrypt) R.string.operation_log_encrypting else R.string.operation_log_decrypting)
        )

        currentJob = viewLifecycleOwner.lifecycleScope.launch {
            logSheet.setOnCancelListener {
                currentJob?.cancel()
            }
            logSheet.show(childFragmentManager, "enc_log")

            val totalFiles = fileList.size
            logSheet.appendLog(
                if (encrypt) "🔒 ${getString(R.string.encryption_encrypt)} $totalFiles ${getString(R.string.encryption_file_count, totalFiles)}"
                else "🔓 ${getString(R.string.encryption_decrypt)} $totalFiles ${getString(R.string.encryption_file_count, totalFiles)}"
            )

            var successCount = 0
            var failCount = 0

            for (i in fileList.indices) {
                if (!isActive) break
                val item = fileList[i]
                logSheet.setProgress(i, totalFiles)
                logSheet.appendLog(getString(
                    if (encrypt) R.string.operation_log_file_encrypting else R.string.operation_log_file_decrypting,
                    item.name
                ))

                val t0 = System.currentTimeMillis()
                var errorDetail: String? = null
                val result = withContext(Dispatchers.IO) {
                    try {
                        if (encrypt) {
                            encryptFile(File(item.path), password, deleteOriginal)
                        } else {
                            decryptFile(File(item.path), password, deleteOriginal)
                        }
                        true
                    } catch (e: Exception) {
                        errorDetail = e.localizedMessage ?: e.javaClass.simpleName
                        false
                    }
                }
                val elapsed = System.currentTimeMillis() - t0
                val elapsedStr = String.format(java.util.Locale.US, "%.1fs", elapsed / 1000.0)

                if (result) {
                    successCount++
                    fileList[i] = item.copy(
                        status = getString(R.string.encryption_status_success)
                    )
                    logSheet.appendLog(getString(R.string.operation_log_file_done, item.name, elapsedStr))
                } else {
                    failCount++
                    fileList[i] = item.copy(
                        status = getString(R.string.encryption_status_failed)
                    )
                    logSheet.appendLog(getString(R.string.operation_log_file_fail, item.name,
                        errorDetail ?: getString(R.string.media_tool_error_unknown)))
                }
                adapter.notifyItemChanged(i)
            }

            logSheet.setProgress(totalFiles, totalFiles)

            binding.progressBar.isVisible = false
            binding.encryptButton.isEnabled = true
            binding.decryptButton.isEnabled = true
            binding.addFileButton.isEnabled = true

            logSheet.appendLog(getString(R.string.encryption_result, successCount, failCount))
            logSheet.finish(success = failCount == 0)
        }
    }

    private suspend fun encryptFile(file: File, password: String, deleteOriginal: Boolean) {
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val encDir = OutputPaths.resolve(OutputPaths.ENCRYPTED)
        if (!encDir.mkdirs() && !encDir.isDirectory) {
            throw IOException("Failed to create output directory: $encDir")
        }
        val outputFile = FileTypeUtils.getUniqueFile(encDir, file.name, "enc")
        try {
            FileOutputStream(outputFile).use { fos ->
                fos.write(FILE_HEADER.toByteArray())
                fos.write(salt)
                fos.write(iv)

                FileInputStream(file).use { fis ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var read: Int
                    while (fis.read(buffer).also { read = it } != -1) {
                        currentCoroutineContext().ensureActive()
                        val encrypted = cipher.update(buffer, 0, read)
                        if (encrypted != null) fos.write(encrypted)
                    }
                    val finalBlock = cipher.doFinal()
                    if (finalBlock != null) fos.write(finalBlock)
                }
            }
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        }

        MediaScanner.scan(outputFile)

        // Delete original if encryption succeeded
        if (deleteOriginal) {
            file.delete()
            MediaScanner.scan(file, true)
        }
    }

    private suspend fun decryptFile(file: File, password: String, deleteOriginal: Boolean) {
        val decDir = OutputPaths.resolve(OutputPaths.DECRYPTED)
        if (!decDir.mkdirs() && !decDir.isDirectory) {
            throw IOException("Failed to create output directory: $decDir")
        }
        val outputName = if (file.name.endsWith(".enc")) {
            file.name.removeSuffix(".enc")
        } else {
            file.name + ".dec"
        }
        val nameOnly = outputName.substringBeforeLast('.')
        val extOnly = outputName.substringAfterLast('.', "")
        val outputFile = FileTypeUtils.getUniqueFile(decDir, nameOnly, extOnly)

        try {
            FileInputStream(file).use { fis ->
                val dis = DataInputStream(fis)
                val header = ByteArray(FILE_HEADER.length)
                dis.readFully(header)
                if (String(header) != FILE_HEADER) {
                    throw IllegalArgumentException("Not an encrypted file")
                }

                val salt = ByteArray(SALT_SIZE)
                dis.readFully(salt)
                val iv = ByteArray(IV_SIZE)
                dis.readFully(iv)

                val key = deriveKey(password, salt)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

                FileOutputStream(outputFile).use { fos ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var read: Int
                    while (fis.read(buffer).also { read = it } != -1) {
                        currentCoroutineContext().ensureActive()
                        val decrypted = cipher.update(buffer, 0, read)
                        if (decrypted != null) fos.write(decrypted)
                    }
                    val finalBlock = cipher.doFinal()
                    if (finalBlock != null) fos.write(finalBlock)
                }
            }
        } catch (e: Exception) {
            outputFile.delete()
            throw e
        }

        MediaScanner.scan(outputFile)

        // Delete encrypted file if option is set
        if (deleteOriginal) {
            file.delete()
            MediaScanner.scan(file, true)
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_SIZE)
        try {
            val secretKey = factory.generateSecret(spec)
            return SecretKeySpec(secretKey.encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}
