package com.zilagent.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zilagent.app.ui.components.GlassCard
import com.airbnb.lottie.compose.*
import com.zilagent.app.R

data class OnboardingStep(
    val title: String,
    val description: String,
    val lottieRes: Int? = null
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentStep by remember { mutableStateOf(0) }
    
    val steps = listOf(
        OnboardingStep(
            "Hoş Geldiniz!",
            "ZilAgent ile okul programınızı dijitalleştirin. Saniyeler bazında hassasiyetle derslerinizi takip edin.",
            R.raw.empty_animation // Using existing placeholder
        ),
        OnboardingStep(
            "Akıllı Profiller",
            "Sabah, Öğle veya Gece programlarınız arasında tek tıkla geçiş yapın. Her profil için ayrı zil vakitleri tanımlayın.",
        ),
        OnboardingStep(
            "Modern Widget'lar",
            "Uygulamayı açmadan her şeyi ana ekranınızdan görün. 3 farklı widget tasarımı ile stilinizi yansıtın.",
        ),
        OnboardingStep(
            "Sınav Modu & Bildirimler",
            "Sınav esnasında kalan süreyi görün, zil çalmada 1 dakika kala bildirim alın.",
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val step = steps[currentStep]
            
            // Lottie or Icon placeholder
            Box(modifier = Modifier.height(250.dp), contentAlignment = Alignment.Center) {
                if (step.lottieRes != null) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(step.lottieRes))
                    LottieAnimation(composition, iterations = LottieConstants.IterateForever, modifier = Modifier.size(200.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentStep + 1).toString(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Progress Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentStep) 12.dp else 8.dp)
                            .background(
                                color = if (index == currentStep) Color.White else Color.White.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
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
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.3f)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = if (currentStep == steps.size - 1) "BAŞLA" else "SONRAKİ",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (currentStep < steps.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}
