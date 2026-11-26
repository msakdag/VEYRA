package com.veyra.auto

import android.content.Intent
import android.net.Uri
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

@Composable
fun YouTubeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val suggestions = listOf("Türkçe müzik", "Lo-fi", "Podcast", "Yol müziği", "Klasik müzik", "Jazz")
    Column(Modifier.fillMaxSize().background(Navy).padding(18.dp)) {
        TextButton(onClick = onBack) { Text("← Geri", color = Mint) }
        Text("YouTube keşif", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Text("Arama YouTube uygulamasında veya tarayıcıda açılır. VEYRA video oynatmayı kendi içinde barındırmaz.", color = Muted)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Ara") })
        Button(onClick = { openYouTube(context, query) }, enabled = query.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Mint)) { Text("YouTube'da ara", color = Navy) }
        Spacer(Modifier.height(16.dp))
        Text("Önerilen aramalar", color = Color.White, style = MaterialTheme.typography.titleMedium)
        LazyColumn { items(suggestions) { item ->
            TextButton(onClick = { query = item; openYouTube(context, item) }, modifier = Modifier.fillMaxWidth()) { Text(item, color = Mint) }
        } }
    }
}

private fun openYouTube(context: android.content.Context, query: String) {
    val uri = Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query))
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try { context.startActivity(intent) } catch (_: Exception) { }
}
