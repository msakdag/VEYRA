package com.veyra.auto.media

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.LibraryResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class VeyraMediaService : MediaLibraryService() {

    private lateinit var player: ExoPlayer
    private lateinit var librarySession: MediaLibrarySession

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        librarySession = MediaLibrarySession.Builder(
            this, player, LibraryCallback()
        ).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        librarySession

    override fun onDestroy() {
        if (::librarySession.isInitialized) librarySession.release()
        if (::player.isInitialized) player.release()
        super.onDestroy()
    }

    private fun item(id: String, title: String, browsable: Boolean): MediaItem =
        MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setBrowsable(browsable)
                    .setPlayable(!browsable)
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
        ): ListenableFuture<LibraryResult<List<MediaItem>>> {

            val entries = when (parentId) {
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

            val from = (page * pageSize).coerceAtMost(entries.size)
            val to = ((page + 1) * pageSize).coerceAtMost(entries.size)

            return Futures.immediateFuture(
                LibraryResult.ofItemList(entries.subList(from, to), params)
            )
        }
    }
}
