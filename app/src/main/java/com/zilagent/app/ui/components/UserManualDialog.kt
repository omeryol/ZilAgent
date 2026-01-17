package com.zilagent.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

data class ManualPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradient: List<Color>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserManualDialog(onDismiss: () -> Unit) {
    val pages = listOf(
        ManualPage(
            "ZilAgent'e Hoş Geldiniz",
            "Okul zil vakitlerini, ders programınızı ve sınav sürelerini tek bir yerden yönetmenizi sağlayan en gelişmiş asistanınız.",
            Icons.Default.Celebration,
            IconGradients.Purple
        ),
        ManualPage(
            "Çoklu Profiller",
            "Normal gün, Cuma günü veya destekleme kursu gibi farklı ders saati programlarını profiller olarak kaydedip kolayca geçiş yapabilirsiniz.",
            Icons.Default.Person,
            IconGradients.Blue
        ),
        ManualPage(
            "Akıllı Ders Takibi",
            "Sınıflarınızı ve ders isimlerini tanımlayarak, o an hangi derste olduğunuzu ve bitime ne kadar kaldığını anlık görün.",
            Icons.Default.MenuBook,
            IconGradients.Green
        ),
        ManualPage(
            "Eşsiz Widgetlar",
            "Ana ekranınıza ekleyeceğiniz widgetları; yazı boyutu, renk, şeffaflık ve hizalama gibi onlarca seçenekle tamamen size özel hale getirin.",
            Icons.Default.Widgets,
            IconGradients.Sunset
        ),
        ManualPage(
            "Sınav & Özel Sayaç",
            "Sınavlar veya özel etkinlikler için tek seferlik geri sayımlar başlatın. Sınav modu ile sessiz çalışma ortamı oluşturun.",
            Icons.Default.Timer,
            IconGradients.Lava
        ),
        ManualPage(
            "Veri Güvenliği",
            "Tüm ayarlarınızı ve programınızı JSON dosyası olarak yedekleyin. Telefon değiştirseniz bile verileriniz hep güvende kalsın.",
            Icons.Default.CloudUpload,
            IconGradients.Blue
        ),
        ManualPage(
            "Dinamik Tasarım",
            "Geri sayım biterken renk değiştiren sayaçlar ve modern glassmorphism tasarımı ile ZilAgent hem şık hem de işlevsel.",
            Icons.Default.ColorLens,
            IconGradients.Purple
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.7f)
                .clip(RoundedCornerShape(32.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Kullanım Kılavuzu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.5f))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) { index ->
                    val page = pages[index]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GradientIcon(page.icon, page.gradient, size = 80.dp, iconSize = 48.dp)
                        }

                        Spacer(Modifier.height(32.dp))

                        Text(
                            page.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            page.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }

                // Footer: Dots & Next
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Page Indicator
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(pages.size) { i ->
                            val active = pagerState.currentPage == i
                            Box(
                                modifier = Modifier
                                    .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                                    .clip(CircleShape)
                                    .background(if (active) Color.White else Color.White.copy(alpha = 0.2f))
                            )
                        }
                    }

                    // Button
                    val isLast = pagerState.currentPage == pages.size - 1
                    Button(
                        onClick = {
                            if (isLast) onDismiss()
                            else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (isLast) "Anladım" else "Sıradaki", color = Color.White, fontWeight = FontWeight.Bold)
                        if (!isLast) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
