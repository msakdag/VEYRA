package com.veyra.auto.media

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

class PlayerConnection(private val context: Context) {

    var controller by mutableStateOf<MediaController?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var currentMediaId by mutableStateOf<String?>(null)
        private set
    var title by mutableStateOf("")
        private set
    var artist by mutableStateOf("")
        private set
    var positionMs by mutableLongStateOf(0L)
        private set
    var durationMs by mutableLongStateOf(0L)
        private set

    private var future: ListenableFuture<MediaController>? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            sync(player)
        }
    }

    private fun sync(p: Player) {
        isPlaying = p.isPlaying
        currentMediaId = p.currentMediaItem?.mediaId
        title = p.mediaMetadata.title?.toString().orEmpty()
        artist = p.mediaMetadata.artist?.toString().orEmpty()
        durationMs = if (p.duration == C.TIME_UNSET) 0L else p.duration
        positionMs = p.currentPosition
    }

    fun connect() {
        if (future != null) return
        val token = SessionToken(context, ComponentName(context, VeyraMediaService::class.java))
        val f = MediaController.Builder(context, token).buildAsync()
        future = f
        f.addListener({
            try {
                val c = f.get()
                c.addListener(listener)
                controller = c
                sync(c)
            } catch (e: Exception) {
                if (future === f) future = null
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun disconnect() {
        controller?.removeListener(listener)
        future?.let { MediaController.releaseFuture(it) }
        future = null
        controller = null
    }

    fun refreshPosition() {
        controller?.let { sync(it) }
    }

    fun playFromList(items: List<MediaItem>, index: Int) {
        val c = controller ?: return
        val same = c.mediaItemCount == items.size &&
            c.getMediaItemAt(index).mediaId == items[index].mediaId
        if (same) {
            c.seekTo(index, 0L)
        } else {
            c.setMediaItems(items, index, 0L)
        }
        c.prepare()
        c.play()
    }

    fun togglePlay() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun next() { controller?.seekToNextMediaItem() }

    fun previous() { controller?.seekToPrevious() }

    fun seekTo(ms: Long) { controller?.seekTo(ms) }
}
