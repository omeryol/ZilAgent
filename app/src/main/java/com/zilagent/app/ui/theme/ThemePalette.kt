package com.zilagent.app.ui.theme

import androidx.compose.ui.graphics.Color

object ThemePalette {
    private val aliases = mapOf(
        // Turkish -> canonical
        "Okyanus" to "Ocean",
        "Orman" to "Forest",
        "Gün Batımı" to "Sunset",
        "Çöl" to "Desert",
        "Kutup" to "Polar",
        "Gece Yarısı" to "Midnight",
        "Şeker" to "Candy",
        "Nane" to "Mint",
        "Lavanta" to "Lavender",
        "Şeftali" to "Peach",
        "Bulut" to "Cloud",
        "Ateş" to "Fire",
        "Güneş" to "Sun",
        "Kiraz" to "Cherry",
        "Elektrik" to "Electric",
        "Asil" to "Royal",
        // keep canonical stable
        "Ocean" to "Ocean",
        "Forest" to "Forest",
        "Sunset" to "Sunset",
        "Desert" to "Desert",
        "Polar" to "Polar",
        "Cyberpunk" to "Cyberpunk",
        "Midnight" to "Midnight",
        "Neon Acid" to "Neon Acid",
        "Deep Space" to "Deep Space",
        "Venom" to "Venom",
        "Candy" to "Candy",
        "Mint" to "Mint",
        "Lavender" to "Lavender",
        "Peach" to "Peach",
        "Cloud" to "Cloud",
        "Fire" to "Fire",
        "Sun" to "Sun",
        "Cherry" to "Cherry",
        "Electric" to "Electric",
        "Royal" to "Royal",
    )

    private fun key(name: String): String = aliases[name] ?: "Ocean"

    fun getPalette(name: String): Pair<Color, Color> {
        return when (key(name)) {
            "Ocean" -> Pair(Color(0xFF4FACFE), Color(0xFF00F2FE))
            "Forest" -> Pair(Color(0xFF43E97B), Color(0xFF38F9D7))
            "Sunset" -> Pair(Color(0xFFFA709A), Color(0xFFFEE140))
            "Desert" -> Pair(Color(0xFFF7971E), Color(0xFFFFD200))
            "Polar" -> Pair(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
            "Cyberpunk" -> Pair(Color(0xFFFF00CC), Color(0xFF333399))
            "Midnight" -> Pair(Color(0xFF0F2027), Color(0xFF2C5364))
            "Neon Acid" -> Pair(Color(0xFFD4FC79), Color(0xFF96E6A1))
            "Deep Space" -> Pair(Color(0xFF000428), Color(0xFF004E92))
            "Venom" -> Pair(Color(0xFFCC2B5E), Color(0xFF753A88))
            "Candy" -> Pair(Color(0xFFFF9A9E), Color(0xFFFECFEF))
            "Mint" -> Pair(Color(0xFF84FAB0), Color(0xFF8FD3F4))
            "Lavender" -> Pair(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
            "Peach" -> Pair(Color(0xFFF6D365), Color(0xFFFDA085))
            "Cloud" -> Pair(Color(0xFFCFD9DF), Color(0xFFE2EBF0))
            "Fire" -> Pair(Color(0xFFF093FB), Color(0xFFF5576C))
            "Sun" -> Pair(Color(0xFFF6D365), Color(0xFFFDA085))
            "Cherry" -> Pair(Color(0xFFEB3349), Color(0xFFF45C43))
            "Electric" -> Pair(Color(0xFF4776E6), Color(0xFF8E54E9))
            "Royal" -> Pair(Color(0xFF141E30), Color(0xFF243B55))
            else -> Pair(Color(0xFF4FACFE), Color(0xFF00F2FE))
        }
    }

    fun getAllThemeNames(): List<String> {
        return listOf(
            "Ocean", "Forest", "Sunset", "Desert", "Polar",
            "Cyberpunk", "Midnight", "Neon Acid", "Deep Space", "Venom",
            "Candy", "Mint", "Lavender", "Peach", "Cloud",
            "Fire", "Sun", "Cherry", "Electric", "Royal",
        )
    }
}
