package com.veyra.auto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
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

private val Navy = Color(0xFF101827)
private val Panel = Color(0xFF18283B)
private val Mint = Color(0xFF69E0D0)
private val Muted = Color(0xFF9CAEC2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VeyraHome() }
    }
}

@Composable
fun VeyraHome() {
    var selected by remember { mutableStateOf<String?>(null) }
    BackHandler(enabled = selected != null) { selected = null }
    MaterialTheme(colorScheme = darkColorScheme(primary = Mint, background = Navy, surface = Panel)) {
        Column(
            modifier = Modifier.fillMaxSize().background(Navy).padding(22.dp),
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
                HomeTile("Medya", "Yerel medya modülü • Faz 2", "Bu modül FAZ 2'de etkinleştirilecek.") { selected = "Medya" }
                HomeTile("Radyo", "İnternet radyosu • Faz 5", "Radyo modülü geliştirme planında.") { selected = "Radyo" }
                HomeTile("Sesli komut", "Türkçe ses tanıma • Faz 4", "Sesli komut altyapısı FAZ 4'te eklenecek.") { selected = "Sesli komut" }
                HomeTile("Android Auto", "Araç ekranı • Faz 1", "Android Auto temel entegrasyonu prototip aşamasında.") { selected = "Android Auto" }
                HomeTile("Ayarlar", "Proje bilgileri • Faz 1", "VEYRA 0.1.9 • Android Auto temel prototipi") { selected = "Ayarlar" }
            } else {
                Spacer(Modifier.height(16.dp))
                Text(selected!!, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(
                    when (selected) {
                        "Medya" -> "Yerel medya oynatma ve medya kontrol arayüzü FAZ 2 kapsamında geliştirilecek."
                        "Radyo" -> "Favori istasyonlar ve internet radyosu FAZ 5 kapsamında geliştirilecek."
                        "Sesli komut" -> "Türkçe offline komut tanıma FAZ 4 kapsamında geliştirilecek."
                        "Android Auto" -> "Araç ekranı bağlantısı ve şablon uyumluluğu test aşamalarında tamamlanacak."
                        else -> "VEYRA, Android Auto odaklı multimedya uygulaması. Sürüm 0.1.7."
                    },
                    color = Muted, fontSize = 15.sp
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { selected = null }, colors = ButtonDefaults.buttonColors(containerColor = Mint)) {
                    Text("Ana ekrana dön", color = Navy)
                }
            }
            Spacer(Modifier.weight(1f))
            Text("VEYRA • 0.1.9 • Prototip", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun HomeTile(title: String, subtitle: String, detail: String, onClick: () -> Unit) {
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
