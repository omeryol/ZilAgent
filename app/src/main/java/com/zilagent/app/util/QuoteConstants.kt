package com.zilagent.app.util

object QuoteConstants {

    data class WidgetQuote(
        val content: String,
        val source: String,
    )

    private const val STORED_QUOTE_SEPARATOR = " ||| "

    private val systemQuoteEntries: List<SystemQuoteSeed> = SYSTEM_QUOTE_CATALOG

    val HOLIDAY_QUOTES_TR: List<String> = systemQuoteEntries.map { it.contentTr }.distinct()
    val HOLIDAY_QUOTES_EN: List<String> = systemQuoteEntries.map { it.contentEn }.distinct()

    val HOLIDAY_QUOTES: List<String>
        get() = HOLIDAY_QUOTES_TR

    fun systemQuotes(languageCode: String): List<String> {
        return systemQuoteEntries
            .map { encodeStoredQuote(it.localizedContent(languageCode), it.source) }
            .distinct()
    }

    fun getRandomQuote(languageCode: String = "tr"): String {
        return systemQuoteEntries.random().localizedContent(languageCode)
    }

    fun getRandomQuoteDisplayText(languageCode: String = "tr"): String {
        return formatQuoteDisplay(getWidgetQuote(languageCode = languageCode, seed = System.nanoTime()))
    }

    fun getWidgetQuote(
        languageCode: String = "tr",
        seed: Long,
    ): WidgetQuote {
        val entry = entryForSeed(seed)
        return WidgetQuote(
            content = entry.localizedContent(languageCode),
            source = entry.source,
        )
    }

    fun encodeStoredQuote(
        content: String,
        source: String = "",
    ): String {
        val safeContent = content.trim()
        val safeSource = source.trim()
        return if (safeSource.isBlank()) safeContent else "$safeContent$STORED_QUOTE_SEPARATOR$safeSource"
    }

    fun parseStoredQuote(raw: String): WidgetQuote {
        val parts = raw.split(STORED_QUOTE_SEPARATOR, limit = 2)
        val content = parts.firstOrNull().orEmpty().trim()
        val source = parts.getOrNull(1).orEmpty().trim()
        return WidgetQuote(content = content, source = source)
    }

    fun formatQuoteDisplay(raw: String): String {
        return formatQuoteDisplay(parseStoredQuote(raw))
    }

    fun formatQuoteDisplay(quote: WidgetQuote): String {
        val safeContent = quote.content.trim()
        val safeSource = quote.source.trim()
        return if (safeSource.isBlank()) safeContent else "$safeContent - $safeSource"
    }

    suspend fun getRandomQuoteFromDb(
        quoteDao: com.zilagent.app.data.dao.QuoteDao,
        languageCode: String = "tr",
    ): String {
        val quotes = quoteDao.getAllQuotesSync()
        return if (quotes.isNotEmpty()) {
            formatQuoteDisplay(quotes.random().content)
        } else {
            getRandomQuoteDisplayText(languageCode)
        }
    }

    private fun entryForSeed(seed: Long): SystemQuoteSeed {
        val safeIndex = floorMod(seed, systemQuoteEntries.size.toLong()).toInt()
        return systemQuoteEntries[safeIndex]
    }

    private fun SystemQuoteSeed.localizedContent(languageCode: String): String {
        return if (languageCode.lowercase() == "en") contentEn else contentTr
    }

    private fun floorMod(value: Long, divisor: Long): Long {
        val mod = value % divisor
        return if (mod >= 0) mod else mod + divisor
    }
}
