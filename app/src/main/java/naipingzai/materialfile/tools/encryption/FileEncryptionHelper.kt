/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.tools.encryption

import android.view.LayoutInflater
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import naipingzai.materialfile.R
import naipingzai.materialfile.provider.linux.media.MediaScanner
import naipingzai.materialfile.tools.FileTypeUtils
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

private const val BUFFER_SIZE = 8192
private const val PBKDF2_ITERATIONS = 65536
private const val AES_KEY_SIZE = 256
private const val GCM_TAG_LENGTH = 128
private const val SALT_SIZE = 16
private const val IV_SIZE = 12
private const val FILE_HEADER = "MFENC"
private const val MIN_PASSWORD_LENGTH = 4

object FileEncryptionHelper {

    fun encrypt(fragment: Fragment, filePath: String) {
        showPasswordDialog(fragment, isEncrypt = true) { password, deleteOriginal ->
            doEncrypt(fragment, File(filePath), password, deleteOriginal)
        }
    }

    fun decrypt(fragment: Fragment, filePath: String) {
        showPasswordDialog(fragment, isEncrypt = false) { password, deleteOriginal ->
            doDecrypt(fragment, File(filePath), password, deleteOriginal)
        }
    }

    private fun showPasswordDialog(
        fragment: Fragment,
        isEncrypt: Boolean,
        onConfirm: (String, Boolean) -> Unit
    ) {
        val view = LayoutInflater.from(fragment.requireContext())
            .inflate(R.layout.dialog_encryption_password, null)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.passwordInput)
        val deleteOriginalCheckBox = view.findViewById<CheckBox>(R.id.deleteOriginalCheckBox)

        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(if (isEncrypt) R.string.encryption_encrypt else R.string.encryption_decrypt)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val password = passwordInput.text.toString()
                if (password.length < MIN_PASSWORD_LENGTH) {
                    showError(fragment, fragment.getString(R.string.encryption_password_too_short))
                    return@setPositiveButton
                }
                onConfirm(password, deleteOriginalCheckBox.isChecked)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun doEncrypt(fragment: Fragment, file: File, password: String, deleteOriginal: Boolean) {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    encryptFile(file, password, deleteOriginal)
                    null
                } catch (e: Exception) {
                    e.message ?: fragment.getString(R.string.encryption_status_failed)
                }
            }
            if (result == null) {
                showSuccess(fragment, fragment.getString(R.string.encryption_status_success))
            } else {
                showError(fragment, result)
            }
        }
    }

    private fun doDecrypt(fragment: Fragment, file: File, password: String, deleteOriginal: Boolean) {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    decryptFile(file, password, deleteOriginal)
                    null
                } catch (e: Exception) {
                    e.message ?: fragment.getString(R.string.encryption_status_failed)
                }
            }
            if (result == null) {
                showSuccess(fragment, fragment.getString(R.string.encryption_status_success))
            } else {
                showError(fragment, result)
            }
        }
    }

    internal suspend fun encryptFile(file: File, password: String, deleteOriginal: Boolean) {
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val encDir = OutputPaths.resolve(OutputPaths.ENCRYPTED)
        if (!encDir.mkdirs() && !encDir.isDirectory) {
            throw IOException("Failed to create output directory")
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
        if (deleteOriginal) {
            file.delete()
            MediaScanner.scan(file, true)
        }
    }

    internal suspend fun decryptFile(file: File, password: String, deleteOriginal: Boolean) {
        val decDir = OutputPaths.resolve(OutputPaths.DECRYPTED)
        if (!decDir.mkdirs() && !decDir.isDirectory) {
            throw IOException("Failed to create output directory")
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

    private fun showSuccess(fragment: Fragment, message: String) {
        try {
            val view = fragment.view ?: return
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
        } catch (_: Exception) {}
    }

    private fun showError(fragment: Fragment, message: String) {
        try {
            MaterialAlertDialogBuilder(fragment.requireContext())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        } catch (_: Exception) {}
    }
}
