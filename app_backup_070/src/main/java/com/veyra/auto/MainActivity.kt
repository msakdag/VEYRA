package com.veyra.auto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veyra.auto.media.PlayerConnection

internal val Navy = Color(0xFF101827)
internal val Panel = Color(0xFF18283B)
internal val Mint = Color(0xFF69E0D0)
internal val Muted = Color(0xFF9CAEC2)

class MainActivity : ComponentActivity() {
    private lateinit var connection: PlayerConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connection = PlayerConnection(applicationContext)
        setContent { VeyraApp(connection) }
    }

    override fun onStart() {
        super.onStart()
        connection.connect()
    }

    override fun onStop() {
        connection.disconnect()
        super.onStop()
    }
}

@Composable
fun VeyraApp(connection: PlayerConnection) {
    var selected by remember { mutableStateOf<String?>(null) }
    BackHandler(enabled = selected != null) { selected = null }
    MaterialTheme(colorScheme = darkColorScheme(primary = Mint, background = Navy, surface = Panel)) {
        if (selected == "Medya") {
            MediaScreen(connection) { selected = null }
        } else {
            VeyraHome(selected) { selected = it }
        }
    }
}

@Composable
fun VeyraHome(selected: String?, onSelect: (String?) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Navy).statusBarsPadding().navigationBarsPadding().padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(18.dp))
        Box(Modifier.size(88.dp).background(Panel, RoundedCornerShape(26.dp)), contentAlignment = Alignment.Center) {
            Text("V", color = Mint, fontSize = 46.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        Text("VEYRA", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
        Text("DRIVE • LISTEN • DISCOVER", color = Mint, fontSize = 11.sp, letterSpacing = 1.3.sp)
        Spacer(Modifier.height(26.dp))
        if (selected == null) {
            Text("Hoş geldin", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text("Araç içi multimedya deneyimin", color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            HomeTile("Medya", "Telefondaki müzikler • Faz 2") { onSelect("Medya") }
            HomeTile("Android Auto", "Araç ekranı • Hazır") { onSelect("Android Auto") }
            HomeTile("Radyo", "İnternet radyosu • Faz 4") { onSelect("Radyo") }
            HomeTile("Sesli komut", "Türkçe ses tanıma • Faz 5") { onSelect("Sesli komut") }
            HomeTile("Ayarlar", "Proje bilgileri") { onSelect("Ayarlar") }
        } else {
            Spacer(Modifier.height(16.dp))
            Text(selected, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(
                when (selected) {
                    "Radyo" -> "İnternet radyoları, favoriler ve istasyon listesi FAZ 4 kapsamında geliştirilecek."
                    "Sesli komut" -> "Türkçe komutlarla müzik ve radyo kontrolü FAZ 5 kapsamında eklenecek."
                    "Android Auto" -> "Araç ekranında VEYRA medya uygulaması olarak görünür. Önce Medya bölümünde müzik iznini ver."
                    else -> "VEYRA, Android Auto odaklı multimedya uygulaması. Sürüm 0.3.0."
                },
                color = Muted, fontSize = 15.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = { onSelect(null) }, colors = ButtonDefaults.buttonColors(containerColor = Mint)) {
                Text("Ana ekrana dön", color = Navy)
            }
        }
        Spacer(Modifier.weight(1f))
        Text("VEYRA • 0.3.0 • Prototip", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun HomeTile(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(horizontal = 17.dp, vertical = 15.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = Muted, fontSize = 13.sp)
        }
    }
}
