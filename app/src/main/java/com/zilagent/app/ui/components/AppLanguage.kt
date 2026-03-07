package com.zilagent.app.ui.components

import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(val code: String) {
    TR("tr"),
    EN("en");

    companion object {
        fun fromCode(code: String): AppLanguage = if (code.lowercase() == "en") EN else TR
    }
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.TR }
