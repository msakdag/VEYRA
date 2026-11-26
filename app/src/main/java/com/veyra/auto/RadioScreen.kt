package com.veyra.auto

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.veyra.auto.media.PlayerConnection

private data class Station(val name: String, val url: String)

@Composable
fun RadioScreen(connection: PlayerConnection, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("veyra_radio", Context.MODE_PRIVATE) }
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var stations by remember { mutableStateOf(loadStations(prefs)) }
    Column(Modifier.fillMaxSize().background(Navy).padding(18.dp)) {
        TextButton(onClick = onBack) { Text("← Geri", color = Mint) }
        Text("İnternet Radyosu", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Text("Kendi yayın bağlantılarını ekleyebilir, listeden başlatabilirsin.", color = Muted)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(name, { name = it }, label = { Text("İstasyon adı") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(url, { url = it }, label = { Text("Yayın URL (HTTP/HTTPS)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val u = url.trim()
            if (name.isNotBlank() && (u.startsWith("https://") || u.startsWith("http://"))) {
                stations = stations + Station(name.trim(), u)
                saveStations(prefs, stations)
                name = ""; url = ""
            }
        }, colors = ButtonDefaults.buttonColors(containerColor = Mint)) { Text("İstasyon ekle", color = Navy) }
        Spacer(Modifier.height(12.dp))
        LazyColumn { items(stations) { station ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text(station.name, color = Color.White); Text(station.url, color = Muted, style = MaterialTheme.typography.bodySmall) }
                    Button(onClick = { connection.playStream(station.name, station.url) }, colors = ButtonDefaults.buttonColors(containerColor = Mint)) { Text("Dinle", color = Navy) }
                    TextButton(onClick = { stations = stations.filterNot { it == station }; saveStations(prefs, stations) }) { Text("Sil") }
                }
            }
        } }
    }
}

private fun loadStations(p: android.content.SharedPreferences): List<Station> =
    (p.getString("stations", "") ?: "").split("\n").filter { it.contains("|") }.mapNotNull {
        val a = it.split("|", limit = 2); if (a.size == 2) Station(a[0], a[1]) else null
    }
private fun saveStations(p: android.content.SharedPreferences, stations: List<Station>) {
    p.edit().putString("stations", stations.joinToString("\n") { it.name.replace("|", "") + "|" + it.url }).apply()
}
