package com.zilagent.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.zilagent.app.R
import com.zilagent.app.ui.components.AppLanguage
import com.zilagent.app.ui.components.GlassCard
import com.zilagent.app.ui.components.ZilAgentBackground
import com.zilagent.app.ui.components.premiumTouchEffect
import com.zilagent.app.widget.WidgetStore

data class OnboardingStep(
    val titleTr: String,
    val titleEn: String,
    val descriptionTr: String,
    val descriptionEn: String,
    val lottieRes: Int? = null,
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(0) }
    var language by remember { mutableStateOf(AppLanguage.fromCode(WidgetStore.getAppLanguage(context))) }
    fun trEn(tr: String, en: String): String = if (language == AppLanguage.EN) en else tr

    val steps = listOf(
        OnboardingStep(
            titleTr = "Hoş Geldiniz!",
            titleEn = "Welcome!",
            descriptionTr = "ZilAgent ile okul programınızı dijitalleştirin. Saniye bazında ders takibi yapın.",
            descriptionEn = "Digitize your school day with ZilAgent. Track every lesson down to the second.",
            lottieRes = R.raw.empty_animation,
        ),
        OnboardingStep(
            titleTr = "Akıllı Profiller",
            titleEn = "Smart Profiles",
            descriptionTr = "Farklı programlar arasında tek dokunuşla geçiş yapın.",
            descriptionEn = "Switch between multiple schedules with a single tap.",
        ),
        OnboardingStep(
            titleTr = "Modern Widget'lar",
            titleEn = "Modern Widgets",
            descriptionTr = "Ana ekrandan doğrudan geri sayım ve ders akışını takip edin.",
            descriptionEn = "Follow countdown and daily flow directly from your home screen.",
        ),
        OnboardingStep(
            titleTr = "Sınav Modu",
            titleEn = "Exam Mode",
            descriptionTr = "Büyük ekran geri sayım ile sınıfın her yerinden görünür süre takibi.",
            descriptionEn = "Large full-screen countdown visible from anywhere in the classroom.",
        ),
    )

    ZilAgentBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = language == AppLanguage.TR,
                    onClick = {
                        language = AppLanguage.TR
                        WidgetStore.setAppLanguage(context, "tr")
                    },
                    label = { Text("🇹🇷 Türkçe") },
                )
                FilterChip(
                    selected = language == AppLanguage.EN,
                    onClick = {
                        language = AppLanguage.EN
                        WidgetStore.setAppLanguage(context, "en")
                    },
                    label = { Text("🇬🇧 English") },
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val step = steps[currentStep]
            Box(modifier = Modifier.height(250.dp), contentAlignment = Alignment.Center) {
                if (step.lottieRes != null) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(step.lottieRes))
                    LottieAnimation(composition, iterations = LottieConstants.IterateForever, modifier = Modifier.size(200.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (currentStep + 1).toString(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = trEn(step.titleTr, step.titleEn),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = trEn(step.descriptionTr, step.descriptionEn),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentStep) 12.dp else 8.dp)
                            .background(
                                color = if (index == currentStep) Color.White else Color.White.copy(alpha = 0.4f),
                                shape = CircleShape,
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (currentStep < steps.size - 1) {
                        currentStep++
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .premiumTouchEffect(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.3f)),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = if (currentStep == steps.size - 1) trEn("BAŞLA", "START") else trEn("SONRAKİ", "NEXT"),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                if (currentStep < steps.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}
