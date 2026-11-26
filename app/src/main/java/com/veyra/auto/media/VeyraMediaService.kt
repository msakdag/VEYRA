package com.veyra.auto.media

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class VeyraMediaService : MediaLibraryService() {

    private var player: ExoPlayer? = null
    private var librarySession: MediaLibrarySession? = null

    override fun onCreate() {
        super.onCreate()
        val exo = ExoPlayer.Builder(this).build()
        player = exo
        librarySession = MediaLibrarySession.Builder(this, exo, LibraryCallback()).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        librarySession

    override fun onDestroy() {
        librarySession?.release()
        librarySession = null
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun item(id: String, title: String, browsable: Boolean): MediaItem =
        MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setIsBrowsable(browsable)
                    .setIsPlayable(!browsable)
                    .build()
            )
            .build()

    private inner class LibraryCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> =
            Futures.immediateFuture(
                LibraryResult.ofItem(item("root", "VEYRA", true), params)
            )

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val entries: List<MediaItem> = when (parentId) {
                "root" -> listOf(
                    item("media", "Medya", true),
                    item("radio", "Radyo", true),
                    item("voice", "Sesli Komut", true)
                )
                "media" -> listOf(item("local", "Yerel medya", false))
                "radio" -> listOf(item("stations", "Radyo istasyonları", false))
                "voice" -> listOf(item("voice-info", "Türkçe sesli komut", false))
                else -> emptyList()
            }
            val from = (page.toLong() * pageSize).coerceIn(0L, entries.size.toLong()).toInt()
            val to = ((page.toLong() + 1) * pageSize).coerceIn(0L, entries.size.toLong()).toInt()
            return Futures.immediateFuture(
                LibraryResult.ofItemList(ImmutableList.copyOf(entries.subList(from, to)), params)
            )
        }
    }
}
