/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.fileproperties.image

import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import android.util.Size
import androidx.exifinterface.media.ExifInterface
import com.caverock.androidsvg.SVG
import java8.nio.file.Path
import naipingzai.materialfile.file.MimeType
import naipingzai.materialfile.fileproperties.PathObserverLiveData
import naipingzai.materialfile.provider.common.getLastModifiedTime
import naipingzai.materialfile.provider.common.newInputStream
import naipingzai.materialfile.util.Failure
import naipingzai.materialfile.util.Loading
import naipingzai.materialfile.util.Stateful
import naipingzai.materialfile.util.Success
import naipingzai.materialfile.util.valueCompat
import okio.buffer
import okio.source
import kotlin.math.roundToInt

class ImageInfoLiveData(
    path: Path,
    private val mimeType: MimeType
) : PathObserverLiveData<Stateful<ImageInfo>>(path) {
    init {
        loadValue()
        observe()
    }

    override fun loadValue() {
        value = Loading(value?.value)
        Dispatchers.IO.asExecutor().execute {
            val value = try {
                val imageInfo = when (mimeType) {
                    MimeType.IMAGE_SVG_XML -> {
                        val svg = path.newInputStream()
                            .source()
                            .buffer()
                            .use { SVG.getFromInputStream(it.inputStream()) }
                        val width = svg.documentWidth
                        val height = svg.documentHeight
                        val dimensions = if (width != -1f && height != -1f) {
                            Size(width.roundToInt(), height.roundToInt())
                        } else {
                            val viewBox = svg.documentViewBox
                            if (viewBox != null) {
                                Size(viewBox.width().roundToInt(), viewBox.height().roundToInt())
                            } else {
                                null
                            }
                        }
                        ImageInfo(dimensions, null)
                    }
                    else -> {
                        val bitmapOptions = BitmapFactory.Options()
                            .apply { inJustDecodeBounds = true }
                        path.newInputStream()
                            .buffered()
                            .use { BitmapFactory.decodeStream(it, null, bitmapOptions) }
                        val width = bitmapOptions.outWidth
                        val height = bitmapOptions.outHeight
                        val dimensions = if (width != -1 && height != -1) {
                            Size(width, height)
                        } else {
                            null
                        }
                        val exifInfo = try {
                            val lastModifiedTime = path.getLastModifiedTime().toInstant()
                            path.newInputStream().buffered().use {
                                val exifInterface = ExifInterface(it)
                                val dateTimeOriginal =
                                    exifInterface.inferDateTimeOriginal(lastModifiedTime)
                                val gpsCoordinates = exifInterface.latLong?.let { it[0] to it[1] }
                                val gpsAltitude = exifInterface.gpsAltitude
                                val make =
                                    exifInterface.getAttributeNotBlank(ExifInterface.TAG_MAKE)
                                val model =
                                    exifInterface.getAttributeNotBlank(ExifInterface.TAG_MODEL)
                                val fNumber = exifInterface.getAttributeDoubleOrNull(
                                    ExifInterface.TAG_F_NUMBER
                                )
                                val shutterSpeedValue = exifInterface.getAttributeDoubleOrNull(
                                    ExifInterface.TAG_SHUTTER_SPEED_VALUE
                                )
                                val focalLength = exifInterface.getAttributeDoubleOrNull(
                                    ExifInterface.TAG_FOCAL_LENGTH
                                )
                                val photographicSensitivity = exifInterface.getAttributeIntOrNull(
                                    ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY
                                )
                                val software =
                                    exifInterface.getAttributeNotBlank(ExifInterface.TAG_SOFTWARE)
                                val description = exifInterface.getAttributeNotBlank(
                                    ExifInterface.TAG_IMAGE_DESCRIPTION
                                ) ?: exifInterface.getAttributeNotBlank(
                                    ExifInterface.TAG_USER_COMMENT
                                )
                                val artist =
                                    exifInterface.getAttributeNotBlank(ExifInterface.TAG_ARTIST)
                                val copyright =
                                    exifInterface.getAttributeNotBlank(ExifInterface.TAG_COPYRIGHT)
                                ExifInfo(
                                    dateTimeOriginal, gpsCoordinates, gpsAltitude, make,
                                    model, fNumber, shutterSpeedValue, focalLength,
                                    photographicSensitivity, software, description, artist,
                                    copyright
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                        ImageInfo(dimensions, exifInfo)
                    }
                }
                Success(imageInfo)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }
}
