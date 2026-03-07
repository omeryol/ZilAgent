package com.zilagent.app.util

object QuoteConstants {

    private data class QuoteEntry(val tr: String, val en: String)

    private val QUOTES = listOf(
        QuoteEntry(
            "Eğitim, dünyayı değiştirmek için kullanabileceğiniz en güçlü silahtır. - Nelson Mandela",
            "Education is the most powerful weapon which you can use to change the world. - Nelson Mandela",
        ),
        QuoteEntry(
            "Başarı, hazırlık ile fırsatın buluştuğu yerdir. - Bobby Unser",
            "Success is where preparation and opportunity meet. - Bobby Unser",
        ),
        QuoteEntry(
            "Disiplin, hedeflerle başarı arasındaki köprüdür. - Jim Rohn",
            "Discipline is the bridge between goals and accomplishment. - Jim Rohn",
        ),
        QuoteEntry(
            "Bilmediğini bilmek, bilgeliğin başlangıcıdır. - Sokrates",
            "Knowing what you do not know is the beginning of wisdom. - Socrates",
        ),
        QuoteEntry(
            "Hayatta en hakiki mürşit ilimdir. - Mustafa Kemal Atatürk",
            "Science is the truest guide in life. - Mustafa Kemal Atatürk",
        ),
        QuoteEntry(
            "Vakit nakittir. - Benjamin Franklin",
            "Time is money. - Benjamin Franklin",
        ),
        QuoteEntry(
            "Yaratıcılık, zekânın eğlenmesidir. - Albert Einstein",
            "Creativity is intelligence having fun. - Albert Einstein",
        ),
        QuoteEntry(
            "Düşüncelerine dikkat et; kaderine dönüşür. - Mahatma Gandhi",
            "Watch your thoughts; they become your destiny. - Mahatma Gandhi",
        ),
        QuoteEntry(
            "Sadelik, en yüksek gelişmişlik düzeyidir. - Leonardo da Vinci",
            "Simplicity is the ultimate sophistication. - Leonardo da Vinci",
        ),
        QuoteEntry(
            "En büyük risk, hiç risk almamaktır. - Mark Zuckerberg",
            "The biggest risk is not taking any risk. - Mark Zuckerberg",
        ),
        QuoteEntry(
            "Mutluluk bir varış noktası değil, bir yolculuk biçimidir. - Ralph Waldo Emerson",
            "Happiness is not a destination, it is a way of travel. - Ralph Waldo Emerson",
        ),
        QuoteEntry(
            "Önce kendini fethet, sonra dünyayı. - Platon",
            "Conquer yourself first, then the world. - Plato",
        ),
        QuoteEntry(
            "Cesaret, korkusuzluk değil; korkuya rağmen ilerlemektir. - Mark Twain",
            "Courage is not absence of fear; it is moving forward despite fear. - Mark Twain",
        ),
        QuoteEntry(
            "Rüzgârın yönünü değiştiremiyorsan, yelkenlerini ayarla. - Aristoteles'e atfedilir",
            "If you cannot change the wind, adjust your sails. - Attributed to Aristotle",
        ),
        QuoteEntry(
            "Kendine inanmak, zaferin yarısıdır. - Theodore Roosevelt",
            "Believe you can and you're halfway there. - Theodore Roosevelt",
        ),
        QuoteEntry(
            "Öğrenmeyi bıraktığın gün yaşlanmaya başlarsın. - Henry Ford",
            "Anyone who stops learning is old, whether at twenty or eighty. - Henry Ford",
        ),
        QuoteEntry(
            "Barış, her şeyin temelidir. - Mustafa Kemal Atatürk",
            "Peace is the foundation of everything. - Mustafa Kemal Atatürk",
        ),
        QuoteEntry(
            "Gülümse; dünya seninle aydınlansın. - Anonim (evrensel halk deyişi)",
            "Smile and let your world brighten. - Anonymous (global folk saying)",
        ),
        QuoteEntry(
            "Küçük adımlar büyük hedefleri getirir. - Anonim (modern kişisel gelişim sözü)",
            "Small steps lead to big goals. - Anonymous (modern self-help saying)",
        ),
        QuoteEntry(
            "Her yeni gün yeni bir başlangıçtır. - Anonim (evrensel halk deyişi)",
            "Every new day is a new beginning. - Anonymous (global folk saying)",
        ),
        QuoteEntry(
            "Planlı çalışmak, başarının anahtarıdır. - Anonim (Türk eğitim kültürü)",
            "Working with a plan is the key to success. - Anonymous (Turkish education culture)",
        ),
        QuoteEntry(
            "Dağ ne kadar yüce olsa da bir yolu vardır. - Türk Atasözü (Türk halk kültürü)",
            "No matter how high the mountain is, there is a path. - Turkish Proverb (Turkish folk culture)",
        ),
        QuoteEntry(
            "Akıl akıldan üstündür. - Türk Atasözü (Türk halk kültürü)",
            "One mind can surpass another. - Turkish Proverb (Turkish folk culture)",
        ),
        QuoteEntry(
            "Söz gümüşse, sükût altındır. - Türk Atasözü (Türk halk kültürü)",
            "Speech is silver, silence is golden. - Turkish Proverb (Turkish folk culture)",
        ),
        QuoteEntry(
            "Azla yetinmeyen çoğu bulamaz. - Türk Atasözü (Türk halk kültürü)",
            "One who is not content with little may find nothing greater. - Turkish Proverb (Turkish folk culture)",
        ),
        QuoteEntry(
            "Zahmetsiz rahmet olmaz. - Türk Atasözü (Türk halk kültürü)",
            "No gain comes without effort. - Turkish Proverb (Turkish folk culture)",
        ),
        QuoteEntry(
            "Birlikten kuvvet doğar. - Türk Atasözü (Türk halk kültürü)",
            "Strength comes from unity. - Turkish Proverb (Turkish folk culture)",
        ),
        QuoteEntry(
            "Tebessüm sadakadır. - Hz. Muhammed'e atfedilir (İslam kültürü)",
            "A smile is charity. - Attributed to Prophet Muhammad (Islamic tradition)",
        ),
        QuoteEntry(
            "İlim Çin'de de olsa gidip alınız. - Hz. Muhammed'e atfedilir (İslam kültürü)",
            "Seek knowledge even if it is in China. - Attributed to Prophet Muhammad (Islamic tradition)",
        ),
        QuoteEntry(
            "Hayat kısa, kuşlar uçuyor. - Cemal Süreya",
            "Life is short; birds are flying. - Cemal Süreya",
        ),
        QuoteEntry(
            "Bir mum diğer mumu yakmakla ışığından kaybetmez. - Mevlana'ya atfedilir",
            "A candle loses nothing by lighting another candle. - Attributed to Rumi",
        ),
        QuoteEntry(
            "Ulaşamadığın her şey, senin için bir derstir. - Mevlana'ya atfedilir",
            "Everything you cannot reach teaches you a lesson. - Attributed to Rumi",
        ),
        QuoteEntry(
            "Önce sen değiş, sonra dünya değişsin. - Lev Tolstoy'a atfedilir",
            "Change yourself first, then your world changes. - Attributed to Leo Tolstoy",
        ),
        QuoteEntry(
            "Dostluk, iki bedende bir ruhtur. - Aristoteles",
            "Friendship is a single soul dwelling in two bodies. - Aristotle",
        ),
        QuoteEntry(
            "Bilgi bir hazinedir; uygulama anahtarıdır. - Thomas Fuller",
            "Knowledge is a treasure, but practice is the key to it. - Thomas Fuller",
        ),
        QuoteEntry(
            "İyi bir plan, başarının yarısıdır. - Anonim (evrensel yönetim kültürü)",
            "A good plan is half of success. - Anonymous (global management culture)",
        ),
        QuoteEntry(
            "Zorluklar, karakteri güçlendiren fırsatlardır. - Anonim (evrensel motivasyon kültürü)",
            "Challenges are opportunities that strengthen character. - Anonymous (global motivational culture)",
        ),
        QuoteEntry(
            "Bugün yaptığın yatırım, yarının sonucudur. - Anonim (modern eğitim kültürü)",
            "Today's investment shapes tomorrow's outcome. - Anonymous (modern education culture)",
        ),
        QuoteEntry(
            "Hayallerine giden yol, dinlenmiş bir zihinden geçer. - Anonim (modern yaşam kültürü)",
            "The road to your goals goes through a rested mind. - Anonymous (modern life culture)",
        ),
        QuoteEntry(
            "Düzen, özgürlüğün temelidir. - Anonim (modern verimlilik kültürü)",
            "Order is the foundation of freedom. - Anonymous (modern productivity culture)",
        ),
        QuoteEntry(
            "Kendini geliştir, dünya seni takip eder. - Anonim (kişisel gelişim kültürü)",
            "Improve yourself and the world follows. - Anonymous (self-development culture)",
        )
    )

    val HOLIDAY_QUOTES_TR: List<String> = QUOTES.map { it.tr }
    val HOLIDAY_QUOTES_EN: List<String> = QUOTES.map { it.en }

    // Backward compatibility for existing code paths.
    val HOLIDAY_QUOTES: List<String>
        get() = HOLIDAY_QUOTES_TR

    fun systemQuotes(languageCode: String): List<String> =
        if (languageCode.lowercase() == "en") HOLIDAY_QUOTES_EN else HOLIDAY_QUOTES_TR

    fun getRandomQuote(languageCode: String = "tr"): String {
        return systemQuotes(languageCode).random()
    }

    suspend fun getRandomQuoteFromDb(
        quoteDao: com.zilagent.app.data.dao.QuoteDao,
        languageCode: String = "tr",
    ): String {
        val quotes = quoteDao.getAllQuotesSync()
        return if (quotes.isNotEmpty()) {
            quotes.random().content
        } else {
            getRandomQuote(languageCode)
        }
    }
}
