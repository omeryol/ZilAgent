package com.zilagent.app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zilagent.app.ui.dashboard.DashboardScreen
import com.zilagent.app.ui.settings.SettingsScreen
import com.zilagent.app.ui.onboarding.OnboardingScreen
import com.zilagent.app.ui.components.AppLanguage
import com.zilagent.app.ui.components.LocalAppLanguage
import com.zilagent.app.ui.components.LocalPremiumMotionConfig
import com.zilagent.app.ui.components.PremiumMotionConfig
import com.zilagent.app.ui.theme.ZilAgentTheme
import com.zilagent.app.widget.WidgetStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force a full widget state sync when app opens so stale RemoteViews are replaced.
        runCatching {
            com.zilagent.app.manager.BellManager(this).apply {
                refreshWidgetState()
                scheduleMinuteTick()
            }
        }
        setContent {
            // Reactive Theme Mode State
            // Reactive Theme States
            var themeModeState by remember { mutableStateOf(WidgetStore.getThemeMode(this)) }
            var themeColorState by remember { mutableStateOf(WidgetStore.getThemeColorName(this)) }
            var touchAnimationsEnabled by remember { mutableStateOf(WidgetStore.isTouchAnimationsEnabled(this)) }
            var touchAnimationIntensity by remember { mutableStateOf(WidgetStore.getTouchAnimationIntensity(this)) }
            var touchAnimationStyle by remember { mutableStateOf(WidgetStore.getTouchAnimationStyle(this)) }
            var appLanguageState by remember { mutableStateOf(WidgetStore.getAppLanguage(this)) }
            
            val listener = remember {
                SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                    when (key) {
                        "THEME_MODE" -> themeModeState = prefs.getInt(key, 0)
                        "THEME_COLOR" -> themeColorState = prefs.getString(key, "Lavanta") ?: "Lavanta"
                        "TOUCH_ANIMATIONS_ENABLED" -> touchAnimationsEnabled = prefs.getBoolean(key, true)
                        "TOUCH_ANIMATIONS_INTENSITY" -> touchAnimationIntensity = prefs.getInt(key, 60)
                        "TOUCH_ANIMATIONS_STYLE" -> touchAnimationStyle = prefs.getInt(key, 0)
                        "APP_LANGUAGE" -> appLanguageState = prefs.getString(key, "tr") ?: "tr"
                    }
                }
            }
            
            DisposableEffect(Unit) {
                val prefs = getSharedPreferences(WidgetStore.PREFS_NAME, Context.MODE_PRIVATE)
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            val isDark = when(themeModeState) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            ZilAgentTheme(darkTheme = isDark, colorPaletteName = themeColorState, dynamicColor = false) {
                CompositionLocalProvider(
                    LocalAppLanguage provides AppLanguage.fromCode(appLanguageState),
                    LocalPremiumMotionConfig provides PremiumMotionConfig(
                        enabled = touchAnimationsEnabled,
                        intensity = touchAnimationIntensity,
                        style = touchAnimationStyle,
                    )
                ) {
                    val animatedBg by animateColorAsState(
                        targetValue = MaterialTheme.colorScheme.background,
                        animationSpec = tween(durationMillis = 350),
                        label = "theme_bg_anim",
                    )
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = animatedBg
                    ) {
                        val startExam = intent.getBooleanExtra("START_EXAM_MODE", false)
                        ZilAgentAppNavHost(startExamMode = startExam)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Also refresh when returning to foreground to avoid stale widget cache on launcher.
        runCatching {
            com.zilagent.app.manager.BellManager(this).apply {
                refreshWidgetState()
                scheduleMinuteTick()
            }
        }
        runCatching { com.zilagent.app.widget.PanoramicCountdownWidget.updateAll(this) }
        runCatching { com.zilagent.app.widget.SyllabusWidget.updateAll(this) }
    }
}

@Composable
fun ZilAgentAppNavHost(startExamMode: Boolean = false) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val hasCompletedOnboarding = remember { WidgetStore.hasCompletedOnboarding(context) }
    
    val startDestination = if (!hasCompletedOnboarding && !startExamMode) "onboarding" else "dashboard"

    LaunchedEffect(startExamMode) {
        if (startExamMode) {
            navController.navigate("exam_mode")
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    WidgetStore.setOnboardingCompleted(context)
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                onFinishToSettings = {
                    WidgetStore.setOnboardingCompleted(context)
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                    navController.navigate("settings")
                },
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToCreate = { profileId ->
                    navController.navigate("create_schedule?profileId=$profileId")
                },
                onNavigateToExamMode = { navController.navigate("exam_mode") },
                onNavigateToProfiles = { navController.navigate("profiles") },
            )
        }
        composable("profiles") {
            com.zilagent.app.ui.profiles.ProfilesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { profileId ->
                    navController.navigate("create_schedule?profileId=$profileId")
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfiles = { navController.navigate("profiles") }
            )
        }
        composable(
            route = "create_schedule?profileId={profileId}",
            arguments = listOf(
                androidx.navigation.navArgument("profileId") {
                    type = androidx.navigation.NavType.LongType
                    defaultValue = -1L
                }
            ),
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "zilagent://create_schedule" })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: -1L
            com.zilagent.app.ui.settings.CreateScheduleScreen(
                onSaveComplete = { navController.popBackStack() },
                profileId = profileId
            )
        }
        composable(
            route = "exam_mode",
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "zilagent://exam_mode" })
        ) {
            com.zilagent.app.ui.exam.ExamModeScreen(
                onClose = { navController.popBackStack() }
            )
        }
        composable("classes") {
            com.zilagent.app.ui.syllabus.ClassManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("subjects") {
            com.zilagent.app.ui.syllabus.SubjectManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
