/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.provider.document

import android.net.Uri
import android.os.Parcelable
import java.time.Instant
import java8.nio.file.attribute.FileTime
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import naipingzai.materialfile.provider.common.AbstractContentProviderFileAttributes
import naipingzai.materialfile.provider.common.FileTimeParceler

@Parcelize
internal class DocumentFileAttributes(
    override val lastModifiedTime: @WriteWith<FileTimeParceler> FileTime,
    override val mimeType: String?,
    override val size: Long,
    override val fileKey: Parcelable,
    private val flags: Int
) : AbstractContentProviderFileAttributes() {
    fun flags(): Int = flags

    companion object {
        fun from(
            lastModifiedTimeMillis: Long,
            mimeType: String?,
            size: Long,
            flags: Int,
            uri: Uri
        ): DocumentFileAttributes {
            val lastModifiedTime = FileTime.from(Instant.ofEpochMilli(lastModifiedTimeMillis))
            val fileKey = uri
            return DocumentFileAttributes(lastModifiedTime, mimeType, size, fileKey, flags)
        }
    }
}
