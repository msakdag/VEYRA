package com.veyra.auto

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import com.veyra.auto.media.MusicRepository
import com.veyra.auto.media.PlayerConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val audioPermission =
    if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

private fun formatTime(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}

@Composable
fun MediaScreen(connection: PlayerConnection, onBack: () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var songs by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        granted = ContextCompat.checkSelfPermission(context, audioPermission) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun askPermission() {
        val list = mutableListOf(audioPermission)
        if (Build.VERSION.SDK_INT >= 33) list.add(Manifest.permission.POST_NOTIFICATIONS)
        launcher.launch(list.toTypedArray())
    }

    LaunchedEffect(Unit) { if (!granted) askPermission() }
    LaunchedEffect(granted) {
        if (granted) {
            songs = withContext(Dispatchers.IO) { MusicRepository.loadSongs(context) }
            loaded = true
        }
    }
    LaunchedEffect(connection.controller) {
        while (true) {
            connection.refreshPosition()
            delay(500)
        }
    }

    Column(
        Modifier.fillMaxSize().background(Navy).statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Geri", color = Mint) }
            Spacer(Modifier.width(8.dp))
            Text("Medya", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        NowPlaying(connection)
        Spacer(Modifier.height(10.dp))
        when {
            !granted -> Column {
                Text("Müzikleri listelemek için ses dosyalarına erişim izni gerekli.", color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(10.dp))
                Button(onClick = { askPermission() }, colors = ButtonDefaults.buttonColors(containerColor = Mint)) {
                    Text("İzin ver", color = Navy)
                }
                Spacer(Modifier.height(8.dp))
                Text("Düğme çalışmazsa: Ayarlar > Uygulamalar > VEYRA > İzinler.", color = Muted, fontSize = 12.sp)
            }
            !loaded -> Text("Müzikler yükleniyor…", color = Muted)
            songs.isEmpty() -> Text("Telefonda müzik bulunamadı.", color = Muted)
            else -> LazyColumn(Modifier.weight(1f)) {
                itemsIndexed(songs, key = { _, s -> s.mediaId }) { index, song ->
                    SongRow(song, song.mediaId == connection.currentMediaId) {
                        connection.playFromList(songs, index)
                    }
                }
            }
        }
    }
}

@Composable
private fun NowPlaying(c: PlayerConnection) {
    var drag by remember { mutableStateOf<Float?>(null) }
    val duration = c.durationMs.coerceAtLeast(1L).toFloat()
    val ready = c.controller != null
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (c.title.isEmpty()) "Parça seçilmedi" else c.title,
                color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(c.artist, color = Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Slider(
                value = (drag ?: c.positionMs.toFloat()).coerceIn(0f, duration),
                onValueChange = { drag = it },
                onValueChangeFinished = {
                    drag?.let { c.seekTo(it.toLong()) }
                    drag = null
                },
                valueRange = 0f..duration,
                enabled = ready,
                colors = SliderDefaults.colors(
                    thumbColor = Mint, activeTrackColor = Mint, inactiveTrackColor = Navy
                )
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(drag?.toLong() ?: c.positionMs), color = Muted, fontSize = 12.sp)
                Text(formatTime(c.durationMs), color = Muted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { c.previous() }, enabled = ready, colors = ButtonDefaults.buttonColors(containerColor = Mint)) {
                    Text("Önceki", color = Navy)
                }
                Button(onClick = { c.togglePlay() }, enabled = ready, colors = ButtonDefaults.buttonColors(containerColor = Mint)) {
                    Text(if (c.isPlaying) "Duraklat" else "Oynat", color = Navy)
                }
                Button(onClick = { c.next() }, enabled = ready, colors = ButtonDefaults.buttonColors(containerColor = Mint)) {
                    Text("Sonraki", color = Navy)
                }
            }
        }
    }
}

@Composable
private fun SongRow(song: MediaItem, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Text(
            song.mediaMetadata.title?.toString().orEmpty(),
            color = if (selected) Mint else Color.White,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            song.mediaMetadata.artist?.toString().orEmpty(),
            color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}
