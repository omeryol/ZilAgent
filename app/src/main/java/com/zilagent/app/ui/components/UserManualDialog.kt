package com.zilagent.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val gradient: List<Color>,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserManualDialog(onDismiss: () -> Unit) {
    val isEn = LocalAppLanguage.current == AppLanguage.EN

    val pages = if (isEn) {
        listOf(
            ManualPage(
                "Welcome to ZilAgent",
                "Your advanced assistant to manage school bells, lesson flow, and exam timers from one place.",
                Icons.Default.Celebration,
                IconGradients.Purple,
            ),
            ManualPage(
                "Multiple Profiles",
                "Save and switch between profiles like normal day, Friday, or support courses with one tap.",
                Icons.Default.Person,
                IconGradients.Blue,
            ),
            ManualPage(
                "Smart Lesson Tracking",
                "Define classes and subjects, then instantly see current lesson and remaining time.",
                Icons.Default.MenuBook,
                IconGradients.Green,
            ),
            ManualPage(
                "Powerful Widgets",
                "Customize widgets with text size, color, opacity, alignment and more.",
                Icons.Default.Widgets,
                IconGradients.Sunset,
            ),
            ManualPage(
                "Exam & Custom Timer",
                "Start one-time countdowns for exams or events. Use exam mode for full-screen visibility.",
                Icons.Default.Timer,
                IconGradients.Lava,
            ),
            ManualPage(
                "Backup Safety",
                "Backup all settings and schedules as JSON and restore anytime.",
                Icons.Default.CloudUpload,
                IconGradients.Blue,
            ),
            ManualPage(
                "Dynamic Design",
                "Enjoy dynamic countdown colors and modern glass UI that looks clean and premium.",
                Icons.Default.ColorLens,
                IconGradients.Purple,
            ),
        )
    } else {
        listOf(
            ManualPage(
                "ZilAgent'e Hoş Geldiniz",
                "Okul zil vakitlerini, ders akışını ve sınav sayaçlarını tek yerden yönetmenizi sağlayan gelişmiş yardımcınız.",
                Icons.Default.Celebration,
                IconGradients.Purple,
            ),
            ManualPage(
                "Çoklu Profiller",
                "Normal gün, cuma günü veya destekleme kursu gibi farklı programları profil olarak kaydedip hızlıca geçiş yapın.",
                Icons.Default.Person,
                IconGradients.Blue,
            ),
            ManualPage(
                "Akıllı Ders Takibi",
                "Sınıfları ve ders isimlerini tanımlayarak o an hangi derste olduğunuzu ve kalan süreyi anlık görün.",
                Icons.Default.MenuBook,
                IconGradients.Green,
            ),
            ManualPage(
                "Güçlü Widgetlar",
                "Widgetları yazı boyutu, renk, şeffaflık, hizalama gibi seçeneklerle tamamen kendinize göre ayarlayın.",
                Icons.Default.Widgets,
                IconGradients.Sunset,
            ),
            ManualPage(
                "Sınav ve Özel Sayaç",
                "Sınavlar veya özel etkinlikler için tek seferlik geri sayım başlatın. Sınav modunda tam ekran takip edin.",
                Icons.Default.Timer,
                IconGradients.Lava,
            ),
            ManualPage(
                "Yedekleme Güvenliği",
                "Tüm ayarları ve programları JSON olarak yedekleyin, istediğiniz zaman geri yükleyin.",
                Icons.Default.CloudUpload,
                IconGradients.Blue,
            ),
            ManualPage(
                "Dinamik Tasarım",
                "Geri sayım renk geçişleri ve modern cam tasarımıyla hem şık hem işlevsel bir deneyim yaşayın.",
                Icons.Default.ColorLens,
                IconGradients.Purple,
            ),
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.7f)
                .clip(RoundedCornerShape(32.dp)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (isEn) "User Guide" else "Kullanım Kılavuzu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.5f))
                    }
                }

                Spacer(Modifier.height(16.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) { index ->
                    val page = pages[index]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            GradientIcon(page.icon, page.gradient, size = 80.dp, iconSize = 48.dp)
                        }

                        Spacer(Modifier.height(32.dp))

                        Text(
                            page.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            page.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(pages.size) { i ->
                            val active = pagerState.currentPage == i
                            Box(
                                modifier = Modifier
                                    .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                                    .clip(CircleShape)
                                    .background(if (active) Color.White else Color.White.copy(alpha = 0.2f)),
                            )
                        }
                    }

                    val isLast = pagerState.currentPage == pages.size - 1
                    Button(
                        onClick = {
                            if (isLast) onDismiss() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(if (isLast) (if (isEn) "Got it" else "Anladım") else (if (isEn) "Next" else "Sıradaki"), color = Color.White, fontWeight = FontWeight.Bold)
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
