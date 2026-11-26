package com.veyra.auto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.veyra.auto.media.PlayerConnection

@Composable
fun VoiceScreen(connection: PlayerConnection, onBack: () -> Unit) {
    val context = LocalContext.current
    var recognized by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("Komut dinlemeye hazır.") }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        recognized = matches?.firstOrNull().orEmpty()
        if (recognized.isNotBlank()) feedback = executeVoiceCommand(recognized, connection)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchRecognition(speechLauncher)
        else feedback = "Mikrofon izni verilmedi."
    }

    Column(Modifier.fillMaxSize().background(Navy).padding(18.dp)) {
        TextButton(onClick = onBack) { Text("← Geri", color = Mint) }
        Text("Türkçe Sesli Komut", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Text("Telefonun desteklediği konuşma tanıma servisi kullanılır. Çevrimdışı tanıma desteği cihaz/servise bağlıdır.", color = Muted)
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Algılanan komut", color = Muted)
                Text(recognized.ifBlank { "—" }, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(feedback, color = Mint)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                launchRecognition(speechLauncher)
            } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Mint)) {
            Text("Konuşmayı dinle", color = Navy)
        }
        Spacer(Modifier.height(16.dp))
        Text("Desteklenen örnekler", color = Color.White, style = MaterialTheme.typography.titleMedium)
        listOf("Müziği duraklat", "Müziği devam ettir", "Sonraki şarkı", "Önceki şarkı").forEach {
            Text("• $it", color = Muted, modifier = Modifier.padding(vertical = 5.dp))
        }
    }
}

private fun launchRecognition(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "VEYRA komutunu söyle")
    }
    try { launcher.launch(intent) } catch (_: Exception) { }
}

private fun executeVoiceCommand(text: String, connection: PlayerConnection): String {
    val command = text.lowercase().replace('ı', 'i')
    return when {
        command.contains("duraklat") || command.contains("durdur") || command.contains("pause") -> {
            connection.controller?.pause(); "Oynatma duraklatıldı."
        }
        command.contains("devam") || command.contains("cal") || command.contains("çal") || command.contains("play") -> {
            connection.controller?.play(); "Oynatma devam ettirildi."
        }
        command.contains("sonraki") || command.contains("ileri") -> {
            connection.next(); "Sonraki parçaya geçildi."
        }
        command.contains("onceki") || command.contains("önceki") || command.contains("geri") -> {
            connection.previous(); "Önceki parçaya geçildi."
        }
        else -> "Komut tanınmadı. Örnek: sonraki şarkı."
    }
}
