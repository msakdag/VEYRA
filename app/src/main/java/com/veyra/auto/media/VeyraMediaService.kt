package com.veyra.auto.media

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class VeyraMediaService : MediaLibraryService() {

    private var player: ExoPlayer? = null
    private var librarySession: MediaLibrarySession? = null
    private val executor: ListeningExecutorService =
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor())

    @Volatile
    private var songCache: List<MediaItem> = emptyList()

    override fun onCreate() {
        super.onCreate()
        val exo = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
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
        executor.shutdown()
        super.onDestroy()
    }

    private fun hasAudioPermission(): Boolean {
        val perm = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
    }

    private fun refreshSongs(): List<MediaItem> {
        if (!hasAudioPermission()) return emptyList()
        val list = MusicRepository.loadSongs(this)
        songCache = list
        return list
    }

    private fun cachedOrLoad(): List<MediaItem> {
        val c = songCache
        return if (c.isNotEmpty()) c else refreshSongs()
    }

    private fun folder(id: String, title: String): MediaItem =
        MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                    .build()
            )
            .build()

    private inner class LibraryCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> =
            executor.submit(Callable<LibraryResult<MediaItem>> {
                LibraryResult.ofItem(folder("root", "VEYRA"), params)
            })

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> =
            executor.submit(Callable<LibraryResult<MediaItem>> {
                val found: MediaItem? = when (mediaId) {
                    "root" -> folder("root", "VEYRA")
                    "songs" -> folder("songs", "Tüm parçalar")
                    else -> cachedOrLoad().firstOrNull { it.mediaId == mediaId }
                }
                if (found != null) LibraryResult.ofItem(found, null)
                else LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
            })

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> =
            executor.submit(Callable<LibraryResult<ImmutableList<MediaItem>>> {
                val entries: List<MediaItem> = when (parentId) {
                    "root" -> listOf(folder("songs", "Tüm parçalar"))
                    "songs" -> refreshSongs()
                    else -> emptyList()
                }
                val from = (page.toLong() * pageSize).coerceIn(0L, entries.size.toLong()).toInt()
                val to = ((page.toLong() + 1) * pageSize).coerceIn(0L, entries.size.toLong()).toInt()
                LibraryResult.ofItemList(ImmutableList.copyOf(entries.subList(from, to)), params)
            })

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> =
            executor.submit(Callable<MutableList<MediaItem>> {
                val songs = cachedOrLoad()
                mediaItems.mapNotNull { item ->
                    if (item.localConfiguration != null) item
                    else songs.firstOrNull { it.mediaId == item.mediaId }
                }.toMutableList()
            })

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> =
            executor.submit(Callable<MediaSession.MediaItemsWithStartPosition> {
                val songs = cachedOrLoad()
                if (mediaItems.size == 1) {
                    val idx = songs.indexOfFirst { it.mediaId == mediaItems[0].mediaId }
                    if (idx >= 0) {
                        return@Callable MediaSession.MediaItemsWithStartPosition(songs, idx, startPositionMs)
                    }
                }
                val resolved = mediaItems.mapNotNull { item ->
                    if (item.localConfiguration != null) item
                    else songs.firstOrNull { it.mediaId == item.mediaId }
                }
                MediaSession.MediaItemsWithStartPosition(resolved, startIndex, startPositionMs)
            })
    }
}
