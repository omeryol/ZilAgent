package com.zilagent.app.ui.theme

import androidx.compose.ui.graphics.Color

object ThemePalette {
    fun getPalette(name: String): Pair<Color, Color> {
        return when (name) {
            // Doğa
            "Okyanus" -> Pair(Color(0xFF4FACFE), Color(0xFF00F2FE))
            "Orman" -> Pair(Color(0xFF43E97B), Color(0xFF38F9D7))
            "Gün Batımı" -> Pair(Color(0xFFFA709A), Color(0xFFFEE140))
            "Çöl" -> Pair(Color(0xFFF7971E), Color(0xFFFFD200))
            "Kutup" -> Pair(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
            
            // Modern & Neon
            "Cyberpunk" -> Pair(Color(0xFFFF00CC), Color(0xFF333399))
            "Gece Yarısı" -> Pair(Color(0xFF0F2027), Color(0xFF2C5364))
            "Neon Asit" -> Pair(Color(0xFFD4FC79), Color(0xFF96E6A1))
            "Derin Uzay" -> Pair(Color(0xFF000428), Color(0xFF004E92))
            "Zehir" -> Pair(Color(0xFFcc2b5e), Color(0xFF753a88))
            
            // Pastel & Yumuşak
            "Şeker" -> Pair(Color(0xFFFF9A9E), Color(0xFFFECFEF))
            "Nane" -> Pair(Color(0xFF84FAB0), Color(0xFF8FD3F4))
            "Lavanta" -> Pair(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
            "Şeftali" -> Pair(Color(0xFFF6D365), Color(0xFFFDA085))
            "Bulut" -> Pair(Color(0xFFcfd9df), Color(0xFFe2ebf0))
            
            // Klasik & Canlı
            "Ateş" -> Pair(Color(0xFFF093FB), Color(0xFFF5576C))
            "Güneş" -> Pair(Color(0xFFF6D365), Color(0xFFFDA085)) // Keep legacy mapping just in case
            "Kiraz" -> Pair(Color(0xFFEB3349), Color(0xFFF45C43))
            "Elektrik" -> Pair(Color(0xFF4776E6), Color(0xFF8E54E9))
            "Asil" -> Pair(Color(0xFF141E30), Color(0xFF243B55))

            else -> Pair(Color(0xFF4FACFE), Color(0xFF00F2FE)) // Default Okyanus
        }
    }

    fun getAllThemeNames(): List<String> {
        return listOf(
            "Okyanus", "Orman", "Gün Batımı", "Çöl", "Kutup",
            "Cyberpunk", "Gece Yarısı", "Neon Asit", "Derin Uzay", "Zehir",
            "Şeker", "Nane", "Lavanta", "Şeftali", "Bulut",
            "Kiraz", "Elektrik", "Asil"
        )
    }
}
