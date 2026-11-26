package com.veyra.auto

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("veyra_settings", Context.MODE_PRIVATE) }
    var preferOfflineVoice by remember { mutableStateOf(prefs.getBoolean("offline_voice", true)) }
    var showVoiceHelp by remember { mutableStateOf(prefs.getBoolean("voice_help", true)) }
    var confirmClearStations by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Navy).padding(18.dp)) {
        TextButton(onClick = onBack) { Text("← Geri", color = Mint) }
        Text("Ayarlar", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Text("VEYRA uygulama tercihleri", color = Muted)
        Spacer(Modifier.height(18.dp))
        SettingSwitch("Çevrimdışı ses tanımayı tercih et", "Cihaz destekliyorsa konuşma tanıma isteğinde offline tercih gönderilir.", preferOfflineVoice) {
            preferOfflineVoice = it; prefs.edit().putBoolean("offline_voice", it).apply()
        }
        SettingSwitch("Sesli komut ipuçlarını göster", "Sesli komut ekranındaki örnek komutlar için tercih.", showVoiceHelp) {
            showVoiceHelp = it; prefs.edit().putBoolean("voice_help", it).apply()
        }
        Spacer(Modifier.height(20.dp))
        Text("Veri ve depolama", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { confirmClearStations = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Kayıtlı radyo istasyonlarını sil")
        }
        Text("İstasyon kayıtları uygulamanın yerel verilerinde tutulur.", color = Muted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(20.dp))
        Text("VEYRA 0.8.0 • Tercihler", color = Muted, style = MaterialTheme.typography.bodySmall)
    }
    if (confirmClearStations) {
        AlertDialog(
            onDismissRequest = { confirmClearStations = false },
            title = { Text("Radyo kayıtlarını sil?") },
            text = { Text("Eklediğin istasyonların kayıtları silinecek. Bu işlem geri alınamaz.") },
            confirmButton = { TextButton(onClick = {
                context.getSharedPreferences("veyra_radio", Context.MODE_PRIVATE).edit().clear().apply()
                confirmClearStations = false
            }) { Text("Sil") } },
            dismissButton = { TextButton(onClick = { confirmClearStations = false }) { Text("İptal") } }
        )
    }
}

@Composable
private fun SettingSwitch(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = Color.White)
            Text(description, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
