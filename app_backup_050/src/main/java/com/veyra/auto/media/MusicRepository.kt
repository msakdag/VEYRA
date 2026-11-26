package com.veyra.auto.media

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

object MusicRepository {

    fun loadSongs(context: Context): List<MediaItem> {
        val collection = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sort = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        val result = ArrayList<MediaItem>()

        context.contentResolver.query(collection, projection, selection, null, sort)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val title = c.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "Bilinmeyen parça"
                val artist = c.getString(artistCol)
                    ?.takeIf { it.isNotBlank() && it != "<unknown>" } ?: "Bilinmeyen sanatçı"
                val album = c.getString(albumCol) ?: ""
                result.add(
                    MediaItem.Builder()
                        .setMediaId(id.toString())
                        .setUri(ContentUris.withAppendedId(collection, id))
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(title)
                                .setArtist(artist)
                                .setAlbumTitle(album)
                                .setIsBrowsable(false)
                                .setIsPlayable(true)
                                .build()
                        )
                        .build()
                )
            }
        }
        return result
    }
}
