package com.zilagent.app.util

import java.time.LocalDate

data class AnnualSpecialTemplate(
    val id: String,
    val nameTr: String,
    val nameEn: String,
    val startMonth: Int,
    val startDay: Int,
    val endMonth: Int,
    val endDay: Int,
) {
    fun name(isEn: Boolean): String = if (isEn) nameEn else nameTr

    fun rangeForYear(year: Int): Pair<LocalDate, LocalDate> {
        val start = LocalDate.of(year, startMonth, startDay)
        var end = LocalDate.of(year, endMonth, endDay)
        if (end.isBefore(start)) {
            end = end.plusYears(1)
        }
        return start to end
    }
}

object SpecialDaysCatalog {
    val templates: List<AnnualSpecialTemplate> = listOf(
        AnnualSpecialTemplate("teachers_day", "Öğretmenler Günü", "Teachers' Day", 11, 24, 11, 24),
        AnnualSpecialTemplate("atatrk_week", "Atatürk Haftası", "Ataturk Week", 11, 10, 11, 16),
        AnnualSpecialTemplate("human_rights_week", "İnsan Hakları ve Demokrasi Haftası", "Human Rights and Democracy Week", 12, 10, 12, 16),
        AnnualSpecialTemplate("local_goods_week", "Tutum, Yatırım ve Türk Malları Haftası", "Turkish Goods and Investment Week", 12, 12, 12, 18),
        AnnualSpecialTemplate("energy_saving_week", "Enerji Tasarrufu Haftası", "Energy Saving Week", 1, 11, 1, 17),
        AnnualSpecialTemplate("green_crescent_week", "Yeşilay Haftası", "Green Crescent Week", 3, 1, 3, 7),
        AnnualSpecialTemplate("independence_march_day", "İstiklal Marşı'nın Kabulü", "Adoption of Independence March", 3, 12, 3, 12),
        AnnualSpecialTemplate("canakkale_day", "Çanakkale Zaferi ve Şehitleri Anma Günü", "Canakkale Victory and Martyrs' Day", 3, 18, 3, 18),
        AnnualSpecialTemplate("forest_week", "Orman Haftası", "Forest Week", 3, 21, 3, 26),
        AnnualSpecialTemplate("libraries_week", "Kütüphaneler Haftası", "Libraries Week", 3, 25, 3, 31),
        AnnualSpecialTemplate("tourism_week", "Turizm Haftası", "Tourism Week", 4, 15, 4, 22),
        AnnualSpecialTemplate("traffic_week", "Trafik ve İlk Yardım Haftası", "Traffic and First Aid Week", 5, 1, 5, 7),
        AnnualSpecialTemplate("disabled_week", "Engelliler Haftası", "Disability Awareness Week", 5, 10, 5, 16),
        AnnualSpecialTemplate("museums_week", "Müzeler Haftası", "Museums Week", 5, 18, 5, 24),
        AnnualSpecialTemplate("environment_week", "Çevre Koruma Haftası", "Environment Protection Week", 6, 5, 6, 11),
        AnnualSpecialTemplate("primary_education_week", "İlköğretim Haftası", "Primary Education Week", 9, 9, 9, 15),
        AnnualSpecialTemplate("animal_protection_day", "Hayvanları Koruma Günü", "Animal Protection Day", 10, 4, 10, 4),
        AnnualSpecialTemplate("republic_week", "Cumhuriyet Bayramı Haftası", "Republic Day Week", 10, 28, 11, 3),
    )

    fun byId(id: String): AnnualSpecialTemplate? = templates.firstOrNull { it.id == id }
}
