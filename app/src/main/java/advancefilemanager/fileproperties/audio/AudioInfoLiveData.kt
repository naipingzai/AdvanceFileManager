/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.fileproperties.audio

import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.time.Duration
import java8.nio.file.Path
import com.advancefilemanager.compat.METADATA_KEY_SAMPLERATE
import com.advancefilemanager.compat.use
import com.advancefilemanager.fileproperties.PathObserverLiveData
import com.advancefilemanager.fileproperties.extractMetadataNotBlank
import com.advancefilemanager.util.Failure
import com.advancefilemanager.util.Loading
import com.advancefilemanager.util.Stateful
import com.advancefilemanager.util.Success
import com.advancefilemanager.util.setDataSource
import com.advancefilemanager.util.valueCompat

class AudioInfoLiveData(path: Path) : PathObserverLiveData<Stateful<AudioInfo>>(path) {
    init {
        loadValue()
        observe()
    }

    override fun loadValue() {
        value = Loading(value?.value)
        Dispatchers.IO.asExecutor().execute {
            val value = try {
                val audioInfo = MediaMetadataRetriever().use { retriever ->
                    retriever.setDataSource(path)
                    val title = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_TITLE
                    )
                    val artist = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_ARTIST
                    )
                    val album = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_ALBUM
                    )
                    val albumArtist = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST
                    )
                    val composer = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_COMPOSER
                    )
                    val discNumber = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER
                    )
                    val trackNumber = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER
                    )
                    val year = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_YEAR
                    )
                    val genre = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_GENRE
                    )
                    val duration = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_DURATION
                    )?.toLongOrNull()?.let { Duration.ofMillis(it) }
                    val bitRate = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_BITRATE
                    )?.toIntOrNull()
                    val sampleRate = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever::class.METADATA_KEY_SAMPLERATE
                    )?.toIntOrNull()
                    AudioInfo(
                        title, artist, album, albumArtist, composer, discNumber, trackNumber, year,
                        genre, duration, bitRate, sampleRate
                    )
                }
                Success(audioInfo)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }
}
