/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.app

import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.core.content.edit
import naipingzai.materialfile.R
import naipingzai.materialfile.compat.PreferenceManagerCompat
import naipingzai.materialfile.compat.getDescriptionCompat
import naipingzai.materialfile.compat.readBooleanCompat
import naipingzai.materialfile.compat.writeBooleanCompat
import naipingzai.materialfile.compat.writeParcelableListCompat
import naipingzai.materialfile.file.DocumentTreeUri
import naipingzai.materialfile.file.asExternalStorageUriOrNull
import naipingzai.materialfile.file.displayName
import naipingzai.materialfile.file.storageVolume
import naipingzai.materialfile.filelist.FileSortOptions
import naipingzai.materialfile.navigation.BookmarkDirectory
import naipingzai.materialfile.navigation.StandardDirectorySettings
import naipingzai.materialfile.provider.archive.ArchiveFileSystem
import naipingzai.materialfile.provider.common.ByteString
import naipingzai.materialfile.provider.common.moveToByteString
import naipingzai.materialfile.provider.content.ContentFileSystem
import naipingzai.materialfile.provider.document.DocumentFileSystem
import naipingzai.materialfile.provider.document.resolver.ExternalStorageProviderHacks
import naipingzai.materialfile.provider.linux.LinuxFileSystem
import naipingzai.materialfile.provider.root.RootStrategy
import naipingzai.materialfile.storage.DocumentTree
import naipingzai.materialfile.storage.FileSystemRoot
import naipingzai.materialfile.storage.PrimaryStorageVolume
import naipingzai.materialfile.util.StableUriParceler
import naipingzai.materialfile.util.asBase64
import naipingzai.materialfile.util.readParcelable
import naipingzai.materialfile.util.readParcelableListCompat
import naipingzai.materialfile.util.toBase64
import naipingzai.materialfile.util.toByteArray
import naipingzai.materialfile.util.use

internal fun upgradeAppTo1_1_0() {
    // Migrate settings.
    migratePathSetting1_1_0(R.string.pref_key_file_list_default_directory)
    migrateFileSortOptionsSetting1_1_0()
    migrateCreateArchiveTypeSetting1_1_0()
    migrateStandardDirectorySettingsSetting1_1_0()
    migrateBookmarkDirectoriesSetting1_1_0()
    for (key in pathSharedPreferences.all.keys) {
        migrateFileSortOptionsSetting1_1_0(pathSharedPreferences, key)
    }
}

private const val PARCEL_VAL_PARCELABLE = 4
private const val PARCEL_VAL_LIST = 11

private fun migratePathSetting1_1_0(@StringRes keyRes: Int) {
    val key = application.getString(keyRes)
    val oldBytes = defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray()
        ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            newParcel.writeInt(PARCEL_VAL_PARCELABLE)
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                migratePath1_1_0(oldParcel, newParcel)
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

private fun migrateFileSortOptionsSetting1_1_0() {
    migrateFileSortOptionsSetting1_1_0(
        defaultSharedPreferences, application.getString(R.string.pref_key_file_list_sort_options)
    )
}

private fun migrateFileSortOptionsSetting1_1_0(sharedPreferences: SharedPreferences, key: String) {
    val oldBytes = sharedPreferences.getString(key, null)?.asBase64()?.toByteArray() ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            newParcel.writeInt(PARCEL_VAL_PARCELABLE)
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                newParcel.writeString(oldParcel.readString())
                newParcel.writeString(FileSortOptions.By.entries[oldParcel.readInt()].name)
                newParcel.writeString(FileSortOptions.Order.entries[oldParcel.readInt()].name)
                newParcel.writeInt(oldParcel.readByte().toInt())
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    sharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

fun migrateCreateArchiveTypeSetting1_1_0() {
    val key = application.getString(R.string.pref_key_create_archive_type)
    val oldValue = defaultSharedPreferences.getString(key, null) ?: return
    val newValue = oldValue.replace(Regex("type_.+$")) {
        when (it.value) {
            "type_zip" -> "zipRadio"
            "type_tar_xz" -> "tarXzRadio"
            "type_seven_z" -> "sevenZRadio"
            else -> "zipRadio"
        }
    }
    defaultSharedPreferences.edit { putString(key, newValue) }
}

private fun migrateStandardDirectorySettingsSetting1_1_0() {
    val key = application.getString(R.string.pref_key_standard_directory_settings)
    val oldBytes = defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray()
        ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            newParcel.writeInt(PARCEL_VAL_LIST)
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                val size = oldParcel.readInt()
                newParcel.writeInt(size)
                repeat(size) {
                    oldParcel.readInt()
                    newParcel.writeInt(PARCEL_VAL_PARCELABLE)
                    newParcel.writeString(StandardDirectorySettings::class.java.name)
                    newParcel.writeString(oldParcel.readString())
                    newParcel.writeString(oldParcel.readString())
                    newParcel.writeInt(oldParcel.readByte().toInt())
                }
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

private fun migrateBookmarkDirectoriesSetting1_1_0() {
    val key = application.getString(R.string.pref_key_bookmark_directories)
    val oldBytes = defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray()
        ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            newParcel.writeInt(PARCEL_VAL_LIST)
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                val size = oldParcel.readInt()
                newParcel.writeInt(size)
                repeat(size) {
                    oldParcel.readInt()
                    newParcel.writeInt(PARCEL_VAL_PARCELABLE)
                    newParcel.writeString(BookmarkDirectory::class.java.name)
                    newParcel.writeLong(oldParcel.readLong())
                    newParcel.writeString(oldParcel.readString())
                    migratePath1_1_0(oldParcel, newParcel)
                }
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

private val oldByteStringCreator = object : Parcelable.Creator<ByteString> {
    override fun createFromParcel(source: Parcel): ByteString =
        source.createByteArray()!!.moveToByteString()

    override fun newArray(size: Int): Array<ByteString?> = arrayOfNulls(size)
}

private fun migratePath1_1_0(oldParcel: Parcel, newParcel: Parcel) {
    val className = oldParcel.readString()
    newParcel.writeString(className)
    newParcel.writeByte(oldParcel.readByte())
    newParcel.writeBooleanCompat(oldParcel.readByte() != 0.toByte())
    newParcel.writeParcelableListCompat(oldParcel.createTypedArrayList(oldByteStringCreator), 0)
    when (className) {
        "naipingzai.materialfile.provider.archive.ArchivePath" -> {
            oldParcel.readString()
            newParcel.writeString(ArchiveFileSystem::class.java.name)
            migratePath1_1_0(oldParcel, newParcel)
        }
        "naipingzai.materialfile.provider.content.ContentPath" -> {
            oldParcel.readString()
            newParcel.writeString(ContentFileSystem::class.java.name)
            newParcel.writeParcelable(oldParcel.readParcelable<Uri>(), 0)
        }
        "naipingzai.materialfile.provider.document.DocumentPath" -> {
            oldParcel.readString()
            newParcel.writeString(DocumentFileSystem::class.java.name)
            newParcel.writeParcelable(oldParcel.readParcelable<Uri>(), 0)
        }
        "naipingzai.materialfile.provider.linux.LinuxPath" -> {
            oldParcel.readString()
            newParcel.writeString(LinuxFileSystem::class.java.name)
            newParcel.writeBooleanCompat(oldParcel.readByte() != 0.toByte())
        }
        else -> throw IllegalStateException(className)
    }
}

private val pathSharedPreferences: SharedPreferences
    get() {
        val name = "${PreferenceManagerCompat.getDefaultSharedPreferencesName(application)}_path"
        val mode = PreferenceManagerCompat.defaultSharedPreferencesMode
        return application.getSharedPreferences(name, mode)
    }

internal fun upgradeAppTo1_2_0() {
    migrateStoragesSetting1_2_0()
}

private fun migrateStoragesSetting1_2_0() {
    val key = application.getString(R.string.pref_key_storages)
    val storages = (listOf(FileSystemRoot(null, true), PrimaryStorageVolume(null, true))
        + DocumentTreeUri.persistedUris.map {
            DocumentTree(
                null, it.storageVolume?.getDescriptionCompat(application) ?: it.displayName
                    ?: it.value.toString(), it
            )
        })
    val bytes = Parcel.obtain().use { parcel ->
        parcel.writeValue(storages)
        parcel.marshall()
    }
    defaultSharedPreferences.edit { putString(key, bytes.toBase64().value) }
}

internal fun upgradeAppTo1_3_0() {
    migrateSmbServersSetting1_3_0()
}

private fun migrateSmbServersSetting1_3_0() {
    val key = application.getString(R.string.pref_key_storages)
    val oldBytes = defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray()
        ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            newParcel.writeInt(PARCEL_VAL_LIST)
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                val size = oldParcel.readInt()
                newParcel.writeInt(size)
                repeat(size) {
                    val oldPosition = oldParcel.dataPosition()
                    oldParcel.readInt()
                    val className = oldParcel.readString()
                    if (className == "naipingzai.materialfile.storage.SmbServer") {
                        newParcel.writeInt(PARCEL_VAL_PARCELABLE)
                        newParcel.writeString("naipingzai.materialfile.storage.SmbServer")
                        val id = oldParcel.readLong()
                        newParcel.writeLong(id)
                        val customName = oldParcel.readString()
                        newParcel.writeString(customName)
                        oldParcel.readString()
                        newParcel.writeString(
                            "naipingzai.materialfile.provider.smb.client.Authority"
                        )
                        val authorityHost = oldParcel.readString()
                        newParcel.writeString(authorityHost)
                        val authorityPort = oldParcel.readInt()
                        newParcel.writeInt(authorityPort)
                        oldParcel.readString()
                        newParcel.writeString(
                            "naipingzai.materialfile.provider.smb.client.Authentication"
                        )
                        val authenticationUsername = oldParcel.readString()
                        newParcel.writeString(authenticationUsername)
                        val authenticationDomain = oldParcel.readString()
                        newParcel.writeString(authenticationDomain)
                        val authenticationPassword = oldParcel.readString()
                        newParcel.writeString(authenticationPassword)
                        val relativePath = ""
                        newParcel.writeString(relativePath)
                    } else {
                        oldParcel.setDataPosition(oldPosition)
                        val storage = oldParcel.readValue(appClassLoader)
                        newParcel.writeValue(storage)
                    }
                }
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

internal fun upgradeAppTo1_4_0() {
    migratePathSetting1_4_0(R.string.pref_key_file_list_default_directory)
    migrateBookmarkDirectoriesSetting1_4_0()
    migrateRootStrategySetting1_4_0()
}

private fun migratePathSetting1_4_0(@StringRes keyRes: Int) {
    val key = application.getString(keyRes)
    val oldBytes = defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray()
        ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                newParcel.writeInt(oldParcel.readInt())
                migratePath1_4_0(oldParcel, newParcel)
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

private fun migrateBookmarkDirectoriesSetting1_4_0() {
    val key = application.getString(R.string.pref_key_bookmark_directories)
    val oldBytes = defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray()
        ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                newParcel.writeInt(oldParcel.readInt())
                val size = oldParcel.readInt()
                newParcel.writeInt(size)
                repeat(size) {
                    newParcel.writeInt(oldParcel.readInt())
                    newParcel.writeString(oldParcel.readString())
                    newParcel.writeLong(oldParcel.readLong())
                    newParcel.writeString(oldParcel.readString())
                    migratePath1_4_0(oldParcel, newParcel)
                }
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

private fun migratePath1_4_0(oldParcel: Parcel, newParcel: Parcel) {
    val className = oldParcel.readString()
    newParcel.writeString(className)
    newParcel.writeByte(oldParcel.readByte())
    newParcel.writeBooleanCompat(oldParcel.readBooleanCompat())
    newParcel.writeParcelableListCompat(oldParcel.readParcelableListCompat<ByteString>(), 0)
    when (className) {
        "naipingzai.materialfile.provider.archive.ArchivePath" -> {
            newParcel.writeString(oldParcel.readString())
            migratePath1_4_0(oldParcel, newParcel)
        }
        "naipingzai.materialfile.provider.content.ContentPath" -> {
            newParcel.writeParcelable(oldParcel.readParcelable<ContentFileSystem>(), 0)
            newParcel.writeParcelable(oldParcel.readParcelable<Uri>(), 0)
        }
        "naipingzai.materialfile.provider.document.DocumentPath" ->
            newParcel.writeParcelable(oldParcel.readParcelable<DocumentFileSystem>(), 0)
        "naipingzai.materialfile.provider.linux.LinuxPath" -> {
            newParcel.writeParcelable(oldParcel.readParcelable<LinuxFileSystem>(), 0)
            oldParcel.readBooleanCompat()
        }
        "naipingzai.materialfile.provider.sftp.SftpPath",
        "naipingzai.materialfile.provider.smb.SmbPath" -> {
            // Network file systems are no longer supported, skip the data
            throw IllegalStateException("Unsupported network path type: $className")
        }
        else -> throw IllegalStateException(className)
    }
}

private fun migrateRootStrategySetting1_4_0() {
    val key = application.getString(R.string.pref_key_root_strategy)
    val oldValue = defaultSharedPreferences.getString(key, null)?.toInt() ?: return
    val newValue = when (oldValue) {
        0 -> RootStrategy.NEVER
        3 -> RootStrategy.ALWAYS
        else -> RootStrategy.AUTOMATIC
    }.ordinal.toString()
    defaultSharedPreferences.edit { putString(key, newValue) }
}

internal fun upgradeAppTo1_5_0() {
    // Network storage migration removed - no longer supported
}

internal fun upgradeAppTo1_6_0() {
    addViewTypePathSetting1_6_0()
}

private fun addViewTypePathSetting1_6_0() {
    val keys = pathSharedPreferences.all.keys.toSet()
    val sortOptionsKey = application.getString(R.string.pref_key_file_list_sort_options)
    val viewTypeKey = application.getString(R.string.pref_key_file_list_view_type)
    val defaultViewType = application.getString(R.string.pref_default_value_file_list_view_type)
    for (key in keys) {
        if (!key.startsWith(sortOptionsKey)) {
            continue
        }
        val newKey = key.replaceFirst(sortOptionsKey, viewTypeKey)
        if (newKey in keys) {
            continue
        }
        pathSharedPreferences.edit { putString(newKey, defaultViewType) }
    }
}

internal fun upgradeAppTo1_7_2() {
    migrateDocumentManagerShortcutSetting1_7_2()
}

private fun migrateDocumentManagerShortcutSetting1_7_2() {
    val key = application.getString(R.string.pref_key_storages)
    val oldBytes =
        defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray() ?: return
    val newBytes =
        try {
            Parcel.obtain().use { newParcel ->
                Parcel.obtain().use { oldParcel ->
                    oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                    oldParcel.setDataPosition(0)
                    newParcel.writeInt(oldParcel.readInt())
                    readWriteLengthPrefixedValue(oldParcel, newParcel) {
                        val size = oldParcel.readInt()
                        newParcel.writeInt(size)
                        repeat(size) {
                            val oldPosition = oldParcel.dataPosition()
                            oldParcel.readInt()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Skip prefix length.
                                oldParcel.readInt()
                            }
                            val className = oldParcel.readString()
                            oldParcel.setDataPosition(oldPosition)
                            when (className) {
                                "naipingzai.materialfile.storage.DocumentManagerShortcut" -> {
                                    newParcel.writeInt(oldParcel.readInt())
                                    readWriteLengthPrefixedValue(oldParcel, newParcel) {
                                        oldParcel.readString()
                                        newParcel.writeString(
                                            "naipingzai.materialfile.storage" +
                                                ".ExternalStorageShortcut"
                                        )
                                        val id = oldParcel.readLong()
                                        newParcel.writeLong(id)
                                        val customName = oldParcel.readString()
                                        newParcel.writeString(customName)
                                        var uri = StableUriParceler.create(oldParcel)!!
                                        if (uri.asExternalStorageUriOrNull() == null) {
                                            // Reset to a valid external storage URI.
                                            uri =
                                                ExternalStorageProviderHacks
                                                    .DOCUMENT_URI_ANDROID_DATA
                                        }
                                        with(StableUriParceler) { uri.write(newParcel, 0) }
                                    }
                                }
                                else -> {
                                    val storage = oldParcel.readValue(appClassLoader)
                                    newParcel.writeValue(storage)
                                }
                            }
                        }
                    }
                }
                newParcel.marshall()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

private fun readWriteLengthPrefixedValue(oldParcel: Parcel, newParcel: Parcel, block: () -> Unit) {
    var lengthPosition = 0
    var startPosition = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        oldParcel.readInt()
        lengthPosition = newParcel.dataPosition()
        newParcel.writeInt(-1)
        startPosition = newParcel.dataPosition()
    }
    block()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val endPosition = newParcel.dataPosition()
        newParcel.setDataPosition(lengthPosition)
        newParcel.writeInt(endPosition - startPosition)
        newParcel.setDataPosition(endPosition)
    }
}
